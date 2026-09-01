import math
import unittest
from datetime import date, timedelta
from unittest.mock import patch

import numpy as np

from intelligence.contracts import CandidateEvaluation, ForecastMetrics, ModelFitDiagnostics
from intelligence.forecasting import (
    Candidate, ForecastOutput, ModelDiagnosticError, _candidates, _ets, _evaluate,
    _require_acceptable_sarima, _sarima_diagnostics, _select_candidate, analyze_forecast,
    analyze_latest_observation_expectation,
)
from intelligence.metrics import calculate_metrics
from tests.support import request_for


def evaluation(smape: float, mae: float, rmse: float, standard_error: float) -> CandidateEvaluation:
    return CandidateEvaluation(
        family="TEST", configuration={}, state="EVALUATED", reason=None, rollingOriginCount=5,
        effectiveEvaluatedHorizons=[1],
        metrics=ForecastMetrics(mae=mae, rmse=rmse, smape=smape,
                                nonSeasonalMase=1, seasonalMase=None),
        perHorizonMetrics=[], primaryMetricStandardError=standard_error, diagnostics=None)


def candidate(name: str, complexity: int, forecast) -> Candidate:
    return Candidate(name, {"name": name}, complexity, 2, "HISTORY_NOT_MET", False, True, forecast)


class ForecastingTest(unittest.TestCase):
    def test_latest_observation_expectation_holds_latest_actual_out_of_training(self):
        calls = []
        def recorder(training, horizon, _):
            calls.append(list(training))
            return ForecastOutput([training[-1]], None, None, ModelFitDiagnostics(applicable=False, warnings=[]))
        values = list(range(19)) + [999]
        with patch("intelligence.forecasting._candidates", return_value=[candidate("RECORDER", 0, recorder)]):
            result = analyze_latest_observation_expectation(request_for(values, horizon=4, seasonal_period=50))
        self.assertEqual("COMPLETED", result.state)
        self.assertEqual(999, result.actualValue)
        self.assertTrue(calls)
        self.assertTrue(all(999 not in training for training in calls))
        self.assertEqual(19, len(calls[-1]))
        self.assertFalse(result.intervalAvailable)
        self.assertIsNone(result.outsideInterval)

    def test_latest_observation_expectation_compares_only_genuine_interval(self):
        def with_interval(training, horizon, _):
            expected = training[-1]
            return ForecastOutput([expected], [expected - 2], [expected + 2], ModelFitDiagnostics(applicable=False, warnings=[]))
        with patch("intelligence.forecasting._candidates", return_value=[candidate("INTERVAL", 0, with_interval)]):
            result = analyze_latest_observation_expectation(request_for(list(range(19)) + [100], seasonal_period=50))
        self.assertTrue(result.intervalAvailable)
        self.assertTrue(result.outsideInterval)
        self.assertEqual(18, result.expectedValue)

    def test_latest_observation_expectation_reports_insufficient_prior_history(self):
        result = analyze_latest_observation_expectation(request_for([1, 2, 3], forecast_minimum=3))
        self.assertEqual("INSUFFICIENT_DATA", result.state)
        self.assertEqual("LATEST_EXPECTATION_MINIMUM_HISTORY_NOT_MET", result.insufficientReason)
        self.assertEqual(2, result.trainingObservationCount)

    def test_multi_horizon_evaluation_uses_expanding_origins_without_future_leakage(self):
        calls = []

        def recorder(training, horizon, _):
            calls.append((len(training), list(training), horizon))
            return ForecastOutput([training[-1]] * horizon, None, None,
                                  ModelFitDiagnostics(applicable=False, warnings=[]))

        request = request_for(list(range(20)), horizon=7, seasonal_period=50)
        result = _evaluate(candidate("RECORDER", 0, recorder), list(range(20)), [8, 9, 10], request)
        self.assertEqual([(8, 7), (9, 7), (10, 7)], [(length, horizon) for length, _, horizon in calls])
        self.assertEqual(list(range(8)), calls[0][1])
        self.assertEqual(list(range(1, 8)), result.effectiveEvaluatedHorizons)
        self.assertEqual(3, result.rollingOriginCount)
        self.assertEqual([3] * 7, [item.observationCount for item in result.perHorizonMetrics])

    def test_seven_step_model_selection_uses_all_horizons_not_only_first_step(self):
        def first_step_only(training, horizon, _):
            return ForecastOutput(
                [training[-1] + 1] + [training[-1] + 100] * (horizon - 1), None, None,
                ModelFitDiagnostics(applicable=False, warnings=[]))

        def multi_step(training, horizon, _):
            return ForecastOutput(
                [training[-1] + step + 1 for step in range(1, horizon + 1)], None, None,
                ModelFitDiagnostics(applicable=False, warnings=[]))

        candidates = [candidate("FIRST_STEP_ONLY", 0, first_step_only),
                      candidate("MULTI_STEP", 1, multi_step)]
        with patch("intelligence.forecasting._candidates", return_value=candidates):
            result = analyze_forecast(request_for(list(range(30)), horizon=7, seasonal_period=50))
        self.assertEqual("MULTI_STEP", result.selectedModelFamily)
        self.assertEqual(list(range(1, 8)), result.effectiveEvaluatedHorizons)
        first_step_evaluation = next(item for item in result.candidates if item.family == "FIRST_STEP_ONLY")
        self.assertEqual(0.0, float(first_step_evaluation.perHorizonMetrics[0].metrics.mae))
        self.assertGreater(float(first_step_evaluation.metrics.mae), 50.0)

    def test_insufficient_irregular_duplicate_and_ambiguous_cadence_never_forecast(self):
        self.assertEqual("FORECAST_MINIMUM_HISTORY_NOT_MET",
                         analyze_forecast(request_for([1, 2, 3], forecast_minimum=8)).insufficientReason)
        dates = [date(2026, 1, 1) + timedelta(days=value) for value in [0, 1, 3, 6, 7, 9, 10, 12, 13, 15, 16, 18]]
        self.assertEqual("IRREGULAR_SAMPLING",
                         analyze_forecast(request_for(list(range(12)), dates=dates)).insufficientReason)
        duplicate_dates = [date(2026, 1, 1) + timedelta(days=index) for index in range(11)] + [date(2026, 1, 11)]
        self.assertEqual("DUPLICATE_OBSERVATION_DATES",
                         analyze_forecast(request_for(list(range(12)), dates=duplicate_dates)).insufficientReason)
        self.assertEqual("CADENCE_AMBIGUOUS_DUE_TO_MISSING_OBSERVATIONS",
                         analyze_forecast(request_for(list(range(12)), missing_count=1)).insufficientReason)

    def test_observed_and_configured_cadence_are_distinct_contracts(self):
        dates = [date(2026, 1, 1) + timedelta(days=2 * index) for index in range(20)]
        inferred = request_for(list(range(20)), dates=dates, horizon=3, seasonal_period=50)
        self.assertEqual("INFERRED_OBSERVED", inferred.cadence.cadenceBasis)
        self.assertEqual(2, inferred.cadence.observedCadenceDays)
        result = analyze_forecast(inferred)
        self.assertEqual([dates[-1] + timedelta(days=2 * step) for step in [1, 2, 3]],
                         [point.effectiveDate for point in result.points])

        configured = request_for(list(range(20)), dates=dates, horizon=3, seasonal_period=50,
                                 expected_cadence=2)
        self.assertEqual("CONFIGURED_EXPECTED", configured.cadence.cadenceBasis)
        self.assertEqual("COMPLETED", analyze_forecast(configured).state)
        mismatch = request_for(list(range(20)), dates=dates, horizon=3, seasonal_period=50,
                               expected_cadence=1)
        self.assertEqual("OBSERVED_CADENCE_DIFFERS_FROM_EXPECTED",
                         analyze_forecast(mismatch).insufficientReason)

    def test_constant_series_selects_naive_and_mase_denominators_are_undefined(self):
        result = analyze_forecast(request_for([5] * 20, horizon=3, seasonal_period=50))
        self.assertEqual("COMPLETED", result.state)
        self.assertEqual("NAIVE", result.selectedModelFamily)
        self.assertIsNone(result.selectedMetrics.nonSeasonalMase)
        self.assertIsNone(result.selectedMetrics.seasonalMase)

    def test_all_candidates_share_origins_and_report_every_requested_horizon(self):
        result = analyze_forecast(request_for([2 * index + 5 for index in range(32)], horizon=5,
                                              seasonal_period=50))
        evaluated = [item for item in result.candidates if item.state == "EVALUATED"]
        self.assertGreaterEqual(len(evaluated), 2)
        self.assertEqual(1, len({item.rollingOriginCount for item in evaluated}))
        self.assertTrue(all(item.effectiveEvaluatedHorizons == [1, 2, 3, 4, 5] for item in evaluated))
        self.assertEqual([1, 2, 3, 4, 5], result.effectiveEvaluatedHorizons)

    def test_one_standard_error_prefers_simpler_near_equivalent_model(self):
        simple = candidate("ETS", 2, None)
        complex_model = candidate("SARIMA", 7, None)
        raw, selected, standard_error, threshold = _select_candidate([
            (simple, evaluation(5.5, 2, 3, 0.2)),
            (complex_model, evaluation(5.0, 1.9, 2.9, 1.0)),
        ])
        self.assertEqual("SARIMA", raw[0].family)
        self.assertEqual("ETS", selected[0].family)
        self.assertEqual(1.0, standard_error)
        self.assertEqual(6.0, threshold)

    def test_clear_reproducible_improvement_still_selects_complex_model(self):
        simple = candidate("ETS", 2, None)
        complex_model = candidate("SARIMA", 7, None)
        raw, selected, _, _ = _select_candidate([
            (simple, evaluation(10.0, 4, 5, 0.2)),
            (complex_model, evaluation(5.0, 2, 3, 0.5)),
        ])
        self.assertEqual("SARIMA", raw[0].family)
        self.assertEqual("SARIMA", selected[0].family)

    def test_candidate_eligibility_is_horizon_and_complexity_aware(self):
        candidates = _candidates(request_for(list(range(50)), horizon=7, seasonal_period=7))
        minima = {(item.family, item.configuration.get("variant"), item.configuration.get("seasonalOrder", [0])[-1]):
                  item.minimum_training for item in candidates}
        self.assertEqual(21, minima[("SEASONAL_NAIVE", None, 0)])
        self.assertEqual(28, minima[("ETS", "HOLT_WINTERS_ADDITIVE", 0)])
        self.assertEqual(20, minima[("SARIMA", None, 0)])
        self.assertEqual(35, minima[("SARIMA", None, 7)])

        result = analyze_forecast(request_for(list(range(31)), horizon=7, seasonal_period=7))
        reasons = {item.reason for item in result.candidates if item.state == "INELIGIBLE"}
        self.assertIn("HOLT_WINTERS_CYCLES_NOT_MET", reasons)
        self.assertIn("SEASONAL_SARIMA_CYCLES_NOT_MET", reasons)

    def test_horizon_aligned_backtest_requires_enough_complete_origins(self):
        result = analyze_forecast(request_for(list(range(12)), horizon=7, seasonal_period=50))
        self.assertEqual("INSUFFICIENT_DATA", result.state)
        self.assertEqual("HORIZON_ALIGNED_BACKTEST_HISTORY_NOT_MET", result.insufficientReason)

    def test_ets_intervals_are_model_based(self):
        output = _ets("HOLT")([10, 12, 13, 16, 18, 20, 23, 24], 3, 0.95)
        self.assertEqual(3, len(output.values))
        self.assertTrue(all(low <= value <= high
                            for low, value, high in zip(output.lower, output.values, output.upper)))

    def test_mase_exposes_non_seasonal_and_seasonal_scales(self):
        metrics = calculate_metrics([0, 10], [0, 8], [1, 3, 5, 1, 3, 5], seasonal_period=3)
        self.assertAlmostEqual(1.0, float(metrics.mae))
        self.assertAlmostEqual(math.sqrt(2), float(metrics.rmse))
        self.assertAlmostEqual(11.1111111111, float(metrics.smape), places=7)
        self.assertAlmostEqual(1.0 / 2.4, float(metrics.nonSeasonalMase))
        self.assertIsNone(metrics.seasonalMase)
        seasonal = calculate_metrics([11, 13], [10, 12], [1, 2, 3, 2, 3, 4], seasonal_period=3)
        self.assertIsNotNone(seasonal.nonSeasonalMase)
        self.assertAlmostEqual(1.0, float(seasonal.seasonalMase))

    def test_sarima_diagnostics_accept_converged_and_reject_non_converged_fit(self):
        class Result:
            mle_retvals = {"converged": True}
            params = np.array([0.2, 1.0])
            arroots = np.array([2.0])
            maroots = np.array([3.0])
            resid = np.linspace(-1, 1, 20)

        diagnostics = _sarima_diagnostics(Result())
        self.assertTrue(diagnostics.converged)
        self.assertTrue(diagnostics.finiteParameters)
        self.assertTrue(diagnostics.arRootsStable)
        self.assertIn("converged", diagnostics.model_dump())

        Result.mle_retvals = {"converged": False}
        failed = _sarima_diagnostics(Result())
        with self.assertRaisesRegex(ModelDiagnosticError, "SARIMA_NON_CONVERGED"):
            _require_acceptable_sarima(failed)

        def non_converged(*_):
            raise ModelDiagnosticError("SARIMA_NON_CONVERGED", failed)

        evaluation_result = _evaluate(
            candidate("SARIMA", 7, non_converged), list(range(20)), [8, 9, 10],
            request_for(list(range(20)), horizon=3, seasonal_period=50))
        self.assertEqual("FAILED", evaluation_result.state)
        self.assertEqual("SARIMA_NON_CONVERGED", evaluation_result.reason)
        self.assertFalse(evaluation_result.diagnostics.converged)
        self.assertIn("finiteParameters", evaluation_result.diagnostics.model_dump(mode="json"))

    def test_rejected_leading_final_fit_falls_back_to_next_candidate(self):
        full_length = 20

        def rejected_on_final(training, horizon, _):
            if len(training) == full_length:
                raise ValueError("final rejection")
            return ForecastOutput([training[-1]] * horizon, None, None,
                                  ModelFitDiagnostics(applicable=False, warnings=[]))

        def fallback(training, horizon, _):
            return ForecastOutput([training[-1]] * horizon, None, None,
                                  ModelFitDiagnostics(applicable=False, warnings=[]))

        candidates = [candidate("LEADER", 0, rejected_on_final), candidate("FALLBACK", 1, fallback)]
        with patch("intelligence.forecasting._candidates", return_value=candidates):
            result = analyze_forecast(request_for(list(range(full_length)), horizon=3, seasonal_period=50))
        self.assertEqual("FALLBACK", result.selectedModelFamily)
        self.assertEqual("FAILED", result.candidates[0].state)
        self.assertTrue(result.candidates[0].reason.startswith("FINAL_MODEL_FIT_FAILED"))
        self.assertTrue(result.modelSelection.finalFallbackApplied)
        self.assertEqual("FINAL_FIT_FALLBACK_AFTER_ONE_STANDARD_ERROR_SELECTION", result.selectionReason)

    def test_selection_is_deterministic(self):
        request = request_for([10, 11, 13, 12, 15, 16, 18, 17, 20, 22, 21, 24, 25, 26, 28, 27, 30, 31, 33, 32],
                              horizon=3, seasonal_period=50)
        first = analyze_forecast(request)
        second = analyze_forecast(request)
        self.assertEqual(first.selectedModelFamily, second.selectedModelFamily)
        self.assertEqual(first.selectedModelConfiguration, second.selectedModelConfiguration)
        self.assertEqual(first.modelSelection, second.modelSelection)


if __name__ == "__main__":
    unittest.main()
