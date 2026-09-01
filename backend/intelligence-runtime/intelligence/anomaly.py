from __future__ import annotations

from statistics import median

import numpy as np
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import RobustScaler

from .contracts import AnalysisRequest, AnomalyAnalysis, AnomalyPoint, AnomalyScoreSemantics


FEATURE_NAMES = ["confirmed_value", "change_per_day", "deviation_from_trailing_median"]
SCORE_SEMANTICS = AnomalyScoreSemantics(
    kind="MODEL_RELATIVE_EVIDENCE",
    orientation="HIGHER_IS_MORE_ANOMALOUS",
    scope="FITTED_KPI_WINDOW",
    probability=False,
    severity=False,
    crossModelComparable=False,
)


def prepare_features(request: AnalysisRequest) -> np.ndarray:
    observations = request.observations
    values = [float(point.value) for point in observations]
    rows: list[list[float]] = []
    window = request.configuration.anomalyRollingWindow
    for index, point in enumerate(observations):
        if index == 0:
            change_per_day = 0.0
            local_deviation = 0.0
        else:
            elapsed_days = (point.effectiveDate - observations[index - 1].effectiveDate).days
            if elapsed_days <= 0:
                raise ValueError("time-aware anomaly features require strictly increasing effective dates")
            change_per_day = (values[index] - values[index - 1]) / elapsed_days
            prior_values = values[max(0, index - window):index]
            local_deviation = values[index] - median(prior_values)
        rows.append([values[index], change_per_day, local_deviation])
    return np.asarray(rows, dtype=float)


def analyze_anomalies(request: AnalysisRequest) -> AnomalyAnalysis:
    count = len(request.observations)
    if request.cadence.duplicateDateCount > 0:
        return _insufficient(request, "DUPLICATE_EFFECTIVE_DATES")
    if count < request.configuration.anomalyMinimumHistory:
        return _insufficient(request, "ANOMALY_MINIMUM_HISTORY_NOT_MET")

    features = RobustScaler().fit_transform(prepare_features(request))
    model = IsolationForest(
        n_estimators=request.configuration.anomalyEstimators,
        contamination="auto",
        random_state=request.configuration.anomalyRandomState,
        n_jobs=1,
    )
    labels = model.fit_predict(features)
    decisions = model.decision_function(features)
    points = [
        AnomalyPoint(
            entryId=observation.entryId,
            reportId=observation.reportId,
            effectiveDate=observation.effectiveDate,
            confirmedAt=observation.confirmedAt,
            value=observation.value,
            anomalyScore=float(-decisions[index]),
            decisionFunction=float(decisions[index]),
            anomalous=bool(labels[index] == -1),
        )
        for index, observation in enumerate(request.observations)
    ]
    return AnomalyAnalysis(
        state="COMPLETED",
        insufficientReason=None,
        algorithm="SKLEARN_ISOLATION_FOREST",
        featureNames=FEATURE_NAMES,
        trainingObservationCount=count,
        anomalyThreshold=0.0,
        scoreSemantics=SCORE_SEMANTICS,
        points=points,
    )


def _insufficient(request: AnalysisRequest, reason: str) -> AnomalyAnalysis:
    return AnomalyAnalysis(
        state="INSUFFICIENT_DATA",
        insufficientReason=reason,
        algorithm="SKLEARN_ISOLATION_FOREST",
        featureNames=FEATURE_NAMES,
        trainingObservationCount=len(request.observations),
        anomalyThreshold=None,
        scoreSemantics=SCORE_SEMANTICS,
        points=[
            AnomalyPoint(
                entryId=observation.entryId,
                reportId=observation.reportId,
                effectiveDate=observation.effectiveDate,
                confirmedAt=observation.confirmedAt,
                value=observation.value,
                anomalyScore=None,
                decisionFunction=None,
                anomalous=None,
            )
            for observation in request.observations
        ],
    )
