import unittest
from datetime import date, timedelta

import numpy as np

from intelligence.anomaly import FEATURE_NAMES, analyze_anomalies, prepare_features
from tests.support import request_for


class IsolationForestTest(unittest.TestCase):
    def test_insufficient_history_is_explicit(self):
        result = analyze_anomalies(request_for([1, 2, 3], anomaly_minimum=4))
        self.assertEqual("INSUFFICIENT_DATA", result.state)
        self.assertEqual("ANOMALY_MINIMUM_HISTORY_NOT_MET", result.insufficientReason)
        self.assertEqual(3, len(result.points))
        self.assertTrue(all(point.anomalyScore is None for point in result.points))

    def test_feature_vector_uses_value_change_per_day_and_trailing_deviation(self):
        features = prepare_features(request_for([0, 2, 8], anomaly_minimum=3))
        np.testing.assert_allclose([[0, 0, 0], [2, 2, 2], [8, 6, 7]], features)
        self.assertEqual(
            ["confirmed_value", "change_per_day", "deviation_from_trailing_median"],
            FEATURE_NAMES,
        )

    def test_fixed_random_state_is_deterministic_and_preserves_traceability(self):
        request = request_for([10, 10.2, 9.9, 10.1, 10, 9.8, 10.2, 10.1, 9.9, 10, 10.1, 9.8])
        first = analyze_anomalies(request)
        second = analyze_anomalies(request)
        self.assertEqual(
            [point.anomalyScore for point in first.points],
            [point.anomalyScore for point in second.points],
        )
        self.assertEqual(request.observations[4].entryId, first.points[4].entryId)
        self.assertEqual(request.observations[4].reportId, first.points[4].reportId)
        self.assertEqual(request.observations[4].effectiveDate, first.points[4].effectiveDate)

    def test_obvious_outlier_has_stronger_evidence_than_ordinary_points(self):
        values = [100, 101, 99, 100, 100, 101, 99, 100, 100, 101, 99, 100, 250]
        result = analyze_anomalies(request_for(values))
        outlier = result.points[-1]
        ordinary_scores = [float(point.anomalyScore) for point in result.points[:-1]]
        self.assertTrue(outlier.anomalous)
        self.assertGreater(float(outlier.anomalyScore), max(ordinary_scores))
        self.assertAlmostEqual(float(outlier.anomalyScore), -float(outlier.decisionFunction), places=12)
        self.assertEqual("MODEL_RELATIVE_EVIDENCE", result.scoreSemantics.kind)
        self.assertFalse(result.scoreSemantics.probability)
        self.assertFalse(result.scoreSemantics.crossModelComparable)

    def test_stable_series_and_zero_values_are_valid_inputs(self):
        result = analyze_anomalies(request_for([0] * 12))
        self.assertEqual("COMPLETED", result.state)
        self.assertEqual(12, len(result.points))
        self.assertTrue(all(not point.anomalous for point in result.points))
        self.assertTrue(all(point.value == 0 for point in result.points))

    def test_duplicate_dates_abstain_without_fabricating_elapsed_time_and_preserve_sources(self):
        dates = [date(2026, 1, 1) + timedelta(days=index) for index in range(11)]
        dates.append(dates[-1])
        request = request_for(list(range(12)), dates=dates)
        result = analyze_anomalies(request)
        self.assertEqual("INSUFFICIENT_DATA", result.state)
        self.assertEqual("DUPLICATE_EFFECTIVE_DATES", result.insufficientReason)
        self.assertEqual([point.entryId for point in request.observations],
                         [point.entryId for point in result.points])
        self.assertTrue(all(point.anomalyScore is None and point.anomalous is None for point in result.points))
        with self.assertRaisesRegex(ValueError, "strictly increasing"):
            prepare_features(request)


if __name__ == "__main__":
    unittest.main()
