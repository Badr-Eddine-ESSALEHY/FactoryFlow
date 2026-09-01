from __future__ import annotations

import json
import math
import warnings
from dataclasses import dataclass
from datetime import timedelta
from typing import Any, Callable

import numpy as np
import pandas as pd
from statsmodels.stats.diagnostic import acorr_ljungbox
from statsmodels.tsa.exponential_smoothing.ets import ETSModel
from statsmodels.tsa.statespace.sarimax import SARIMAX

from .contracts import (
    AnalysisRequest, CandidateEvaluation, ForecastAnalysis, ForecastPoint,
    CadenceMetadata, HorizonEvaluation, LatestObservationExpectation, ModelFitDiagnostics,
    ModelReference, ModelSelectionDecision,
)
from .metrics import calculate_metrics, smape_errors


@dataclass(frozen=True)
class ForecastOutput:
    values: list[float]
    lower: list[float] | None
    upper: list[float] | None
    diagnostics: ModelFitDiagnostics


@dataclass(frozen=True)
class Candidate:
    family: str
    configuration: dict[str, Any]
    complexity_rank: int
    minimum_training: int
    ineligibility_reason: str
    seasonal: bool
    constant_allowed: bool
    forecast: Callable[[list[float], int, float], ForecastOutput]


class ModelDiagnosticError(ValueError):
    def __init__(self, reason: str, diagnostics: ModelFitDiagnostics):
        super().__init__(reason)
        self.reason = reason
        self.diagnostics = diagnostics


def _not_applicable_diagnostics() -> ModelFitDiagnostics:
    return ModelFitDiagnostics(applicable=False, warnings=[])


def _naive(values: list[float], horizon: int, _: float) -> ForecastOutput:
    return ForecastOutput([values[-1]] * horizon, None, None, _not_applicable_diagnostics())


def _seasonal_naive(period: int):
    def forecast(values: list[float], horizon: int, _: float) -> ForecastOutput:
        predictions = [values[-period + (step % period)] for step in range(horizon)]
        return ForecastOutput(predictions, None, None, _not_applicable_diagnostics())
    return forecast


def _ets(variant: str, period: int | None = None):
    def forecast(values: list[float], horizon: int, confidence: float) -> ForecastOutput:
        trend = "add" if variant in {"HOLT", "HOLT_WINTERS_ADDITIVE"} else None
        seasonal = "add" if variant == "HOLT_WINTERS_ADDITIVE" else None
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            result = ETSModel(
                pd.Series(values, index=pd.RangeIndex(len(values)), dtype=float),
                error="add", trend=trend, seasonal=seasonal,
                seasonal_periods=period if seasonal else None,
                initialization_method="estimated",
            ).fit(disp=False)
        frame = result.get_prediction(start=len(values), end=len(values) + horizon - 1).summary_frame(
            alpha=1.0 - confidence)
        return ForecastOutput(
            frame["mean"].astype(float).tolist(), frame["pi_lower"].astype(float).tolist(),
            frame["pi_upper"].astype(float).tolist(), _not_applicable_diagnostics())
    return forecast


def _sarima(order: tuple[int, int, int], seasonal_order: tuple[int, int, int, int]):
    def forecast(values: list[float], horizon: int, confidence: float) -> ForecastOutput:
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            result = SARIMAX(
                np.asarray(values, dtype=float), order=order, seasonal_order=seasonal_order,
                trend="c" if order[1] == 0 else "n", enforce_stationarity=False,
                enforce_invertibility=False,
            ).fit(disp=False, maxiter=100)
        diagnostics = _sarima_diagnostics(result)
        _require_acceptable_sarima(diagnostics)
        prediction = result.get_forecast(steps=horizon)
        interval = np.asarray(prediction.conf_int(alpha=1.0 - confidence), dtype=float)
        return ForecastOutput(
            np.asarray(prediction.predicted_mean, dtype=float).tolist(),
            interval[:, 0].tolist(), interval[:, 1].tolist(), diagnostics)
    return forecast


def _sarima_diagnostics(result: Any) -> ModelFitDiagnostics:
    converged = bool(getattr(result, "mle_retvals", {}).get("converged", False))
    parameters = np.asarray(getattr(result, "params", []), dtype=float)
    finite_parameters = bool(parameters.size > 0 and np.all(np.isfinite(parameters)))
    ar_roots = np.asarray(getattr(result, "arroots", []), dtype=complex)
    ma_roots = np.asarray(getattr(result, "maroots", []), dtype=complex)
    ar_stable = bool(np.all(np.isfinite(ar_roots)) and np.all(np.abs(ar_roots) > 1.0 + 1e-6))
    ma_invertible = bool(np.all(np.isfinite(ma_roots)) and np.all(np.abs(ma_roots) > 1.0 + 1e-6))
    residuals = np.asarray(getattr(result, "resid", []), dtype=float)
    residuals = residuals[np.isfinite(residuals)]
    ljung_box_p_value = None
    residual_warning = None
    diagnostic_warnings: list[str] = []
    if len(residuals) >= 10:
        lag = min(10, max(1, len(residuals) // 5))
        table = acorr_ljungbox(residuals, lags=[lag], return_df=True)
        ljung_box_p_value = float(table["lb_pvalue"].iloc[-1])
        residual_warning = bool(ljung_box_p_value < 0.05)
        if residual_warning:
            diagnostic_warnings.append("RESIDUAL_AUTOCORRELATION")
    return ModelFitDiagnostics(
        applicable=True, converged=converged, finiteParameters=finite_parameters,
        arRootsStable=ar_stable, maRootsInvertible=ma_invertible,
        ljungBoxPValue=ljung_box_p_value, residualAutocorrelationWarning=residual_warning,
        warnings=diagnostic_warnings)


def _require_acceptable_sarima(diagnostics: ModelFitDiagnostics) -> None:
    if not diagnostics.converged:
        raise ModelDiagnosticError("SARIMA_NON_CONVERGED", diagnostics)
    if not diagnostics.finiteParameters:
        raise ModelDiagnosticError("SARIMA_NON_FINITE_PARAMETERS", diagnostics)
    if not diagnostics.arRootsStable:
        raise ModelDiagnosticError("SARIMA_UNSTABLE_AR_ROOTS", diagnostics)
    if not diagnostics.maRootsInvertible:
        raise ModelDiagnosticError("SARIMA_NON_INVERTIBLE_MA_ROOTS", diagnostics)


def _candidates(request: AnalysisRequest) -> list[Candidate]:
    period = request.configuration.seasonalPeriod
    horizon = request.configuration.forecastHorizon
    minimum_cycles = request.configuration.seasonalMinimumCycles
    seasonal_naive_cycles = max(3, minimum_cycles)
    holt_winters_cycles = max(4, minimum_cycles + 1)
    seasonal_sarima_cycles = max(5, minimum_cycles + 2)
    non_seasonal_sarima_minimum = max(20, 2 * horizon + 6)
    seasonal_sarima_minimum = max(seasonal_sarima_cycles * period, 30, 2 * horizon + 3 * period)
    return [
        Candidate("NAIVE", {"variant": "LAST_OBSERVATION"}, 0, 2,
                  "NAIVE_HISTORY_REQUIREMENT_NOT_MET", False, True, _naive),
        Candidate("SEASONAL_NAIVE", {"seasonalPeriod": period}, 1, seasonal_naive_cycles * period,
                  "SEASONAL_NAIVE_CYCLES_NOT_MET", True, True, _seasonal_naive(period)),
        Candidate("ETS", {"variant": "SIMPLE_EXPONENTIAL_SMOOTHING"}, 2, 4,
                  "ETS_SIMPLE_HISTORY_REQUIREMENT_NOT_MET", False, True, _ets("SIMPLE")),
        Candidate("ETS", {"variant": "HOLT_ADDITIVE_TREND"}, 3, max(8, 2 * horizon),
                  "HOLT_HISTORY_REQUIREMENT_NOT_MET", False, False, _ets("HOLT")),
        Candidate("ETS", {"variant": "HOLT_WINTERS_ADDITIVE", "seasonalPeriod": period}, 4,
                  holt_winters_cycles * period,
                  "HOLT_WINTERS_CYCLES_NOT_MET", True, False, _ets("HOLT_WINTERS_ADDITIVE", period)),
        Candidate("SARIMA", {"order": [1, 0, 0], "seasonalOrder": [0, 0, 0, 0]}, 5,
                  non_seasonal_sarima_minimum, "NON_SEASONAL_SARIMA_HISTORY_REQUIREMENT_NOT_MET",
                  False, False, _sarima((1, 0, 0), (0, 0, 0, 0))),
        Candidate("SARIMA", {"order": [0, 1, 1], "seasonalOrder": [0, 0, 0, 0]}, 6,
                  non_seasonal_sarima_minimum, "NON_SEASONAL_SARIMA_HISTORY_REQUIREMENT_NOT_MET",
                  False, False, _sarima((0, 1, 1), (0, 0, 0, 0))),
        Candidate("SARIMA", {"order": [1, 1, 0], "seasonalOrder": [0, 0, 0, 0]}, 6,
                  non_seasonal_sarima_minimum, "NON_SEASONAL_SARIMA_HISTORY_REQUIREMENT_NOT_MET",
                  False, False, _sarima((1, 1, 0), (0, 0, 0, 0))),
        Candidate("SARIMA", {"order": [1, 0, 0], "seasonalOrder": [1, 0, 0, period]}, 7,
                  seasonal_sarima_minimum, "SEASONAL_SARIMA_CYCLES_NOT_MET",
                  True, False, _sarima((1, 0, 0), (1, 0, 0, period))),
        Candidate("SARIMA", {"order": [0, 1, 1], "seasonalOrder": [0, 1, 1, period]}, 8,
                  seasonal_sarima_minimum, "SEASONAL_SARIMA_CYCLES_NOT_MET",
                  True, False, _sarima((0, 1, 1), (0, 1, 1, period))),
    ]


def _empty_candidate(candidate: Candidate, state: str, reason: str,
                     diagnostics: ModelFitDiagnostics | None = None) -> CandidateEvaluation:
    return CandidateEvaluation(
        family=candidate.family, configuration=candidate.configuration, state=state, reason=reason,
        rollingOriginCount=0, effectiveEvaluatedHorizons=[], metrics=None, perHorizonMetrics=[],
        primaryMetricStandardError=None, diagnostics=diagnostics)


def _aggregate_diagnostics(values: list[ModelFitDiagnostics]) -> ModelFitDiagnostics | None:
    applicable = [value for value in values if value.applicable]
    if not applicable:
        return None
    p_values = [float(value.ljungBoxPValue) for value in applicable if value.ljungBoxPValue is not None]
    return ModelFitDiagnostics(
        applicable=True, converged=all(value.converged is True for value in applicable),
        finiteParameters=all(value.finiteParameters is True for value in applicable),
        arRootsStable=all(value.arRootsStable is True for value in applicable),
        maRootsInvertible=all(value.maRootsInvertible is True for value in applicable),
        ljungBoxPValue=min(p_values) if p_values else None,
        residualAutocorrelationWarning=any(value.residualAutocorrelationWarning is True for value in applicable),
        warnings=sorted({warning for value in applicable for warning in value.warnings}))


def _evaluate(candidate: Candidate, values: list[float], origins: list[int], request: AnalysisRequest) -> CandidateEvaluation:
    horizon = request.configuration.forecastHorizon
    actual_flat: list[float] = []
    predicted_flat: list[float] = []
    actual_by_horizon = [[] for _ in range(horizon)]
    predicted_by_horizon = [[] for _ in range(horizon)]
    fold_primary_errors: list[float] = []
    diagnostics: list[ModelFitDiagnostics] = []
    try:
        for origin in origins:
            output = candidate.forecast(values[:origin], horizon, request.configuration.intervalConfidence)
            predictions = np.asarray(output.values, dtype=float)
            actual = np.asarray(values[origin:origin + horizon], dtype=float)
            if len(predictions) != horizon or len(actual) != horizon or not np.all(np.isfinite(predictions)):
                raise ValueError("invalid horizon-aligned backtest output")
            diagnostics.append(output.diagnostics)
            actual_flat.extend(actual.tolist())
            predicted_flat.extend(predictions.tolist())
            fold_primary_errors.append(float(np.mean(smape_errors(actual, predictions))))
            for step in range(horizon):
                actual_by_horizon[step].append(float(actual[step]))
                predicted_by_horizon[step].append(float(predictions[step]))
        initial_training = values[:origins[0]]
        metrics = calculate_metrics(actual_flat, predicted_flat, initial_training,
                                    request.configuration.seasonalPeriod)
        per_horizon = [HorizonEvaluation(
            horizonStep=step + 1, observationCount=len(actual_by_horizon[step]),
            metrics=calculate_metrics(actual_by_horizon[step], predicted_by_horizon[step], initial_training,
                                      request.configuration.seasonalPeriod)) for step in range(horizon)]
        standard_error = 0.0 if len(fold_primary_errors) < 2 else float(
            np.std(fold_primary_errors, ddof=1) / math.sqrt(len(fold_primary_errors)))
        return CandidateEvaluation(
            family=candidate.family, configuration=candidate.configuration, state="EVALUATED", reason=None,
            rollingOriginCount=len(origins), effectiveEvaluatedHorizons=list(range(1, horizon + 1)),
            metrics=metrics, perHorizonMetrics=per_horizon, primaryMetricStandardError=standard_error,
            diagnostics=_aggregate_diagnostics(diagnostics))
    except ModelDiagnosticError as exception:
        return _empty_candidate(candidate, "FAILED", exception.reason, exception.diagnostics)
    except Exception as exception:
        return _empty_candidate(candidate, "FAILED", f"MODEL_FIT_FAILED:{type(exception).__name__}")


def _numerical_key(pair: tuple[Candidate, CandidateEvaluation]):
    candidate, evaluation = pair
    return (float(evaluation.metrics.smape), float(evaluation.metrics.mae), float(evaluation.metrics.rmse),
            candidate.complexity_rank, candidate.family, json.dumps(candidate.configuration, sort_keys=True))


def _select_candidate(evaluated: list[tuple[Candidate, CandidateEvaluation]]):
    # One-standard-error rule: all candidates no worse than best mean sMAPE + SE(best)
    # are operationally competitive; the least complex competitive candidate wins.
    raw_best = min(evaluated, key=_numerical_key)
    best_error = float(raw_best[1].metrics.smape)
    best_standard_error = float(raw_best[1].primaryMetricStandardError or 0.0)
    competitive_threshold = best_error + best_standard_error
    competitive = [pair for pair in evaluated
                   if float(pair[1].metrics.smape) <= competitive_threshold + 1e-12]
    selected = min(competitive, key=lambda pair: (
        pair[0].complexity_rank, float(pair[1].metrics.mae), float(pair[1].metrics.rmse),
        pair[0].family, json.dumps(pair[0].configuration, sort_keys=True)))
    return raw_best, selected, best_standard_error, competitive_threshold


def _insufficient(request: AnalysisRequest, reason: str,
                  candidates: list[CandidateEvaluation] | None = None) -> ForecastAnalysis:
    return ForecastAnalysis(
        state="INSUFFICIENT_DATA", insufficientReason=reason, selectedModelFamily=None,
        selectedModelConfiguration={}, trainingObservationCount=len(request.observations),
        requestedHorizon=request.configuration.forecastHorizon, effectiveEvaluatedHorizons=[],
        rollingOriginCount=0, generatedAt=request.generatedAt, points=[], selectedMetrics=None,
        candidates=candidates or [], modelSelection=None, selectedModelDiagnostics=None,
        selectionReason=None, forecastDirection="UNAVAILABLE", intervalConfidence=None)


def _forecast_cadence_days(request: AnalysisRequest) -> tuple[int | None, str | None]:
    cadence = request.cadence
    if cadence.duplicateDateCount > 0:
        return None, "DUPLICATE_OBSERVATION_DATES"
    if cadence.missingValueCount > 0:
        if cadence.cadenceBasis == "CONFIGURED_EXPECTED":
            return None, "MISSING_OBSERVATIONS_REQUIRE_RESAMPLING"
        return None, "CADENCE_AMBIGUOUS_DUE_TO_MISSING_OBSERVATIONS"
    if cadence.state != "REGULAR" or cadence.observedCadenceDays is None:
        return None, "IRREGULAR_SAMPLING"
    if cadence.cadenceBasis == "CONFIGURED_EXPECTED":
        if cadence.expectedCadenceDays != cadence.observedCadenceDays:
            return None, "OBSERVED_CADENCE_DIFFERS_FROM_EXPECTED"
        return cadence.expectedCadenceDays, None
    if cadence.cadenceBasis != "INFERRED_OBSERVED":
        return None, "CADENCE_BASIS_UNKNOWN"
    return cadence.observedCadenceDays, None


def analyze_forecast(request: AnalysisRequest) -> ForecastAnalysis:
    count = len(request.observations)
    config = request.configuration
    if count < config.forecastMinimumHistory:
        return _insufficient(request, "FORECAST_MINIMUM_HISTORY_NOT_MET")
    cadence_days, cadence_error = _forecast_cadence_days(request)
    if cadence_error:
        return _insufficient(request, cadence_error)
    if cadence_days is None or cadence_days <= 0:
        return _insufficient(request, "NON_POSITIVE_INTERVAL")
    values = [float(point.value) for point in request.observations]
    constant = bool(np.ptp(np.asarray(values, dtype=float)) == 0.0)
    last_origin = count - config.forecastHorizon
    if last_origin < config.backtestMinimumTraining:
        return _insufficient(request, "HORIZON_ALIGNED_BACKTEST_HISTORY_NOT_MET")
    candidates = _candidates(request)
    potentially_eligible = [candidate for candidate in candidates
                            if last_origin - max(config.backtestMinimumTraining, candidate.minimum_training) + 1
                            >= config.backtestMinimumFolds and (candidate.constant_allowed or not constant)]
    if not potentially_eligible:
        return _insufficient(request, "BACKTEST_MINIMUM_ORIGINS_NOT_MET")
    common_start = max(config.backtestMinimumTraining,
                       max(candidate.minimum_training for candidate in potentially_eligible),
                       last_origin - config.backtestMaximumFolds + 1)
    origins = list(range(common_start, last_origin + 1))
    if len(origins) < config.backtestMinimumFolds:
        return _insufficient(request, "BACKTEST_MINIMUM_ORIGINS_NOT_MET")
    evaluations: list[CandidateEvaluation] = []
    evaluated_pairs: list[tuple[Candidate, CandidateEvaluation]] = []
    for candidate in candidates:
        if common_start < candidate.minimum_training:
            evaluation = _empty_candidate(candidate, "INELIGIBLE", candidate.ineligibility_reason)
        elif constant and not candidate.constant_allowed:
            evaluation = _empty_candidate(candidate, "INELIGIBLE", "CONSTANT_SERIES")
        else:
            evaluation = _evaluate(candidate, values, origins, request)
        evaluations.append(evaluation)
        if evaluation.state == "EVALUATED":
            evaluated_pairs.append((candidate, evaluation))
    if not evaluated_pairs:
        return _insufficient(request, "NO_ELIGIBLE_MODEL", evaluations)
    raw_best, parsimonious, best_se, competitive_threshold = _select_candidate(evaluated_pairs)
    final_order = [parsimonious] + [pair for pair in sorted(evaluated_pairs, key=_numerical_key)
                                   if pair != parsimonious]
    selected_candidate = selected_evaluation = selected_output = None
    for candidate, evaluation in final_order:
        try:
            output = candidate.forecast(values, config.forecastHorizon, config.intervalConfidence)
            _validate_final_output(output, config.forecastHorizon)
            selected_candidate, selected_evaluation, selected_output = candidate, evaluation, output
            break
        except ModelDiagnosticError as exception:
            _replace_failed_evaluation(evaluations, evaluation,
                                       f"FINAL_MODEL_REJECTED:{exception.reason}", exception.diagnostics)
        except Exception as exception:
            _replace_failed_evaluation(evaluations, evaluation,
                                       f"FINAL_MODEL_FIT_FAILED:{type(exception).__name__}", None)
    if selected_candidate is None:
        return _insufficient(request, "ALL_FINAL_MODEL_FITS_FAILED", evaluations)
    intervals_available = selected_output.lower is not None and selected_output.upper is not None
    last_date = request.observations[-1].effectiveDate
    points = [ForecastPoint(
        effectiveDate=last_date + timedelta(days=cadence_days * (index + 1)), value=float(value),
        lowerBound=float(selected_output.lower[index]) if intervals_available else None,
        upperBound=float(selected_output.upper[index]) if intervals_available else None,
        intervalAvailable=intervals_available) for index, value in enumerate(selected_output.values)]
    change = float(selected_output.values[-1]) - values[-1]
    direction_threshold = max(abs(values[-1]) * 0.01, 0.000001)
    forecast_direction = "STABLE" if abs(change) <= direction_threshold else (
        "INCREASING" if change > 0 else "DECREASING")
    raw_reference = _reference(raw_best)
    parsimonious_reference = _reference(parsimonious)
    selected_reference = _reference((selected_candidate, selected_evaluation))
    parsimony_changed = (raw_reference.family != parsimonious_reference.family
                          or raw_reference.configuration != parsimonious_reference.configuration)
    final_fallback_applied = (parsimonious_reference.family != selected_reference.family
                              or parsimonious_reference.configuration != selected_reference.configuration)
    selection = ModelSelectionDecision(
        rawBest=raw_reference, parsimoniousChoice=parsimonious_reference, selected=selected_reference,
        primaryMetric="sMAPE",
        rawBestStandardError=best_se, competitiveThreshold=competitive_threshold,
        parsimonyChangedSelection=parsimony_changed,
        finalFallbackApplied=final_fallback_applied,
        rule="ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY")
    return ForecastAnalysis(
        state="COMPLETED", insufficientReason=None, selectedModelFamily=selected_candidate.family,
        selectedModelConfiguration=selected_candidate.configuration, trainingObservationCount=count,
        requestedHorizon=config.forecastHorizon,
        effectiveEvaluatedHorizons=list(range(1, config.forecastHorizon + 1)),
        rollingOriginCount=len(origins), generatedAt=request.generatedAt, points=points,
        selectedMetrics=selected_evaluation.metrics, candidates=evaluations, modelSelection=selection,
        selectedModelDiagnostics=selected_output.diagnostics,
        selectionReason=("FINAL_FIT_FALLBACK_AFTER_ONE_STANDARD_ERROR_SELECTION"
                         if final_fallback_applied else "ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY"),
        forecastDirection=forecast_direction,
        intervalConfidence=config.intervalConfidence if intervals_available else None)


def analyze_latest_observation_expectation(request: AnalysisRequest) -> LatestObservationExpectation:
    latest = request.observations[-1] if request.observations else None
    if latest is None:
        return LatestObservationExpectation(
            state="INSUFFICIENT_DATA", insufficientReason="NO_LATEST_OBSERVATION",
            entryId=None, reportId=None, effectiveDate=None, actualValue=None,
            trainingObservationCount=0, expectedValue=None, lowerBound=None, upperBound=None,
            intervalAvailable=False, outsideInterval=None, selectedModelFamily=None,
            selectedModelConfiguration={}, selectedMetrics=None, modelSelection=None,
            selectedModelDiagnostics=None)
    prior = request.observations[:-1]
    base = dict(
        entryId=latest.entryId, reportId=latest.reportId, effectiveDate=latest.effectiveDate,
        actualValue=latest.value, trainingObservationCount=len(prior), expectedValue=None,
        lowerBound=None, upperBound=None, intervalAvailable=False, outsideInterval=None,
        selectedModelFamily=None, selectedModelConfiguration={}, selectedMetrics=None,
        modelSelection=None, selectedModelDiagnostics=None)
    if len(prior) < request.configuration.forecastMinimumHistory:
        return LatestObservationExpectation(
            state="INSUFFICIENT_DATA", insufficientReason="LATEST_EXPECTATION_MINIMUM_HISTORY_NOT_MET", **base)
    cadence = _prior_cadence(request, prior)
    holdout_request = request.model_copy(update={
        "windowEnd": prior[-1].effectiveDate,
        "cadence": cadence,
        "observations": prior,
        "configuration": request.configuration.model_copy(update={"forecastHorizon": 1}),
    })
    forecast = analyze_forecast(holdout_request)
    if forecast.state != "COMPLETED":
        return LatestObservationExpectation(
            state="INSUFFICIENT_DATA", insufficientReason=forecast.insufficientReason, **base)
    point = forecast.points[0]
    if point.effectiveDate != latest.effectiveDate:
        return LatestObservationExpectation(
            state="INSUFFICIENT_DATA", insufficientReason="LATEST_OBSERVATION_DATE_NOT_NEXT_CADENCE_STEP", **base)
    outside = None
    if point.intervalAvailable:
        outside = bool(latest.value < point.lowerBound or latest.value > point.upperBound)
    return LatestObservationExpectation(
        state="COMPLETED", insufficientReason=None, **{
            **base,
            "expectedValue": point.value,
            "lowerBound": point.lowerBound,
            "upperBound": point.upperBound,
            "intervalAvailable": point.intervalAvailable,
            "outsideInterval": outside,
            "selectedModelFamily": forecast.selectedModelFamily,
            "selectedModelConfiguration": forecast.selectedModelConfiguration,
            "selectedMetrics": forecast.selectedMetrics,
            "modelSelection": forecast.modelSelection,
            "selectedModelDiagnostics": forecast.selectedModelDiagnostics,
        })


def _prior_cadence(request: AnalysisRequest, prior) -> CadenceMetadata:
    dates = [point.effectiveDate for point in prior]
    distinct = list(dict.fromkeys(dates))
    duplicate_count = len(dates) - len(distinct)
    intervals = [(distinct[index] - distinct[index - 1]).days for index in range(1, len(distinct))]
    regular = len(distinct) > 1 and duplicate_count == 0 and len(set(intervals)) == 1
    state = "EMPTY" if not distinct else ("SINGLE_DATE" if len(distinct) == 1 else ("REGULAR" if regular else "IRREGULAR"))
    observed = intervals[0] if regular else None
    expected = request.cadence.expectedCadenceDays
    if duplicate_count:
        ambiguity = "DUPLICATE_EFFECTIVE_DATES"
    elif request.cadence.missingValueCount:
        ambiguity = "MISSING_OBSERVATIONS"
    elif state in {"EMPTY", "SINGLE_DATE"}:
        ambiguity = "INSUFFICIENT_OBSERVATIONS"
    elif state == "IRREGULAR":
        ambiguity = "IRREGULAR_OBSERVED_SPACING"
    elif expected is not None and expected != observed:
        ambiguity = "OBSERVED_SPACING_DIFFERS_FROM_EXPECTED"
    else:
        ambiguity = "NONE"
    basis = "CONFIGURED_EXPECTED" if expected is not None else (
        "INFERRED_OBSERVED" if regular and request.cadence.missingValueCount == 0 else "UNKNOWN")
    return CadenceMetadata(
        state=state, observedCadenceDays=observed, expectedCadenceDays=expected,
        cadenceBasis=basis, ambiguity=ambiguity, distinctDateCount=len(distinct),
        duplicateDateCount=duplicate_count, missingValueCount=request.cadence.missingValueCount,
        resamplingApplied=False, resamplingPolicy="NONE")


def _reference(pair: tuple[Candidate, CandidateEvaluation]) -> ModelReference:
    return ModelReference(family=pair[0].family, configuration=pair[0].configuration, metrics=pair[1].metrics)


def _replace_failed_evaluation(evaluations: list[CandidateEvaluation], previous: CandidateEvaluation,
                               reason: str, diagnostics: ModelFitDiagnostics | None) -> None:
    evaluations[evaluations.index(previous)] = previous.model_copy(update={
        "state": "FAILED", "reason": reason, "diagnostics": diagnostics})


def _validate_final_output(output: ForecastOutput, horizon: int) -> None:
    if len(output.values) != horizon or not all(np.isfinite(output.values)):
        raise ValueError("invalid final forecast")
    if (output.lower is None) != (output.upper is None):
        raise ValueError("incomplete prediction interval")
    if output.lower is not None:
        if len(output.lower) != horizon or len(output.upper) != horizon:
            raise ValueError("prediction interval horizon mismatch")
        if not all(np.isfinite(output.lower)) or not all(np.isfinite(output.upper)):
            raise ValueError("non-finite prediction interval")
        if any(low > value or high < value
               for low, value, high in zip(output.lower, output.values, output.upper)):
            raise ValueError("prediction interval excludes forecast estimate")
