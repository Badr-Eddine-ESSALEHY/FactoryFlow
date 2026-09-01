from __future__ import annotations

from datetime import date, datetime
from decimal import Decimal
from typing import Any, Literal

from pydantic import BaseModel, ConfigDict, Field, model_validator


class Contract(BaseModel):
    model_config = ConfigDict(extra="forbid", allow_inf_nan=False)


class KpiIdentity(Contract):
    definitionId: int = Field(gt=0)
    code: str | None = None
    displayName: str | None = None
    unit: str | None = None


class CadenceMetadata(Contract):
    state: Literal["EMPTY", "SINGLE_DATE", "REGULAR", "IRREGULAR"]
    observedCadenceDays: int | None = None
    expectedCadenceDays: int | None = None
    cadenceBasis: Literal["INFERRED_OBSERVED", "CONFIGURED_EXPECTED", "UNKNOWN"]
    ambiguity: Literal[
        "NONE",
        "INSUFFICIENT_OBSERVATIONS",
        "MISSING_OBSERVATIONS",
        "IRREGULAR_OBSERVED_SPACING",
        "DUPLICATE_EFFECTIVE_DATES",
        "OBSERVED_SPACING_DIFFERS_FROM_EXPECTED",
    ]
    distinctDateCount: int = Field(ge=0)
    duplicateDateCount: int = Field(ge=0)
    missingValueCount: int = Field(ge=0)
    resamplingApplied: bool
    resamplingPolicy: str


class Observation(Contract):
    entryId: int = Field(gt=0)
    reportId: int = Field(gt=0)
    effectiveDate: date
    confirmedAt: datetime | None = None
    value: Decimal


class RuntimeConfiguration(Contract):
    anomalyMinimumHistory: int = Field(ge=3)
    anomalyEstimators: int = Field(ge=10)
    anomalyRandomState: int
    anomalyRollingWindow: int = Field(ge=2)
    forecastMinimumHistory: int = Field(ge=3)
    forecastHorizon: int = Field(ge=1)
    backtestMinimumTraining: int = Field(ge=2)
    backtestMinimumFolds: int = Field(ge=1)
    backtestMaximumFolds: int = Field(ge=1)
    seasonalPeriod: int = Field(ge=2)
    seasonalMinimumCycles: int = Field(ge=2)
    intervalConfidence: float = Field(ge=0.5, le=0.999)


class AnalysisRequest(Contract):
    analysisId: str = Field(min_length=1)
    kpi: KpiIdentity
    windowStart: date
    windowEnd: date
    generatedAt: datetime
    cadence: CadenceMetadata
    observations: list[Observation]
    configuration: RuntimeConfiguration

    @model_validator(mode="after")
    def validate_series_contract(self):
        if self.configuration.backtestMaximumFolds < self.configuration.backtestMinimumFolds:
            raise ValueError("backtestMaximumFolds must be at least backtestMinimumFolds")
        if self.windowStart > self.windowEnd:
            raise ValueError("windowStart must not be after windowEnd")
        dates = [point.effectiveDate for point in self.observations]
        if dates != sorted(dates):
            raise ValueError("observations must be chronological")
        if any(point.effectiveDate < self.windowStart or point.effectiveDate > self.windowEnd
               for point in self.observations):
            raise ValueError("observation date is outside the historical window")
        if len({point.entryId for point in self.observations}) != len(self.observations):
            raise ValueError("entry identities must be unique")
        duplicate_count = len(dates) - len(set(dates))
        if duplicate_count != self.cadence.duplicateDateCount:
            raise ValueError("cadence duplicate count does not match observations")
        if len(set(dates)) != self.cadence.distinctDateCount:
            raise ValueError("cadence distinct-date count does not match observations")
        if self.cadence.resamplingApplied or self.cadence.resamplingPolicy != "NONE":
            raise ValueError("Phase 1 does not accept resampled observations")
        if self.cadence.state == "REGULAR":
            if duplicate_count or len(dates) < 2 or self.cadence.observedCadenceDays is None:
                raise ValueError("regular cadence metadata is incomplete")
            intervals = [(dates[index] - dates[index - 1]).days for index in range(1, len(dates))]
            if len(set(intervals)) != 1 or intervals[0] != self.cadence.observedCadenceDays:
                raise ValueError("regular cadence interval does not match observations")
        if self.cadence.cadenceBasis == "CONFIGURED_EXPECTED" and self.cadence.expectedCadenceDays is None:
            raise ValueError("configured cadence basis requires expectedCadenceDays")
        if self.cadence.cadenceBasis != "CONFIGURED_EXPECTED" and self.cadence.expectedCadenceDays is not None:
            raise ValueError("expectedCadenceDays requires configured cadence basis")
        return self


class AnomalyPoint(Contract):
    entryId: int
    reportId: int
    effectiveDate: date
    confirmedAt: datetime | None
    value: Decimal
    anomalyScore: Decimal | None = None
    decisionFunction: Decimal | None = None
    anomalous: bool | None = None


class AnomalyScoreSemantics(Contract):
    kind: Literal["MODEL_RELATIVE_EVIDENCE"]
    orientation: Literal["HIGHER_IS_MORE_ANOMALOUS"]
    scope: Literal["FITTED_KPI_WINDOW"]
    probability: Literal[False]
    severity: Literal[False]
    crossModelComparable: Literal[False]


class AnomalyAnalysis(Contract):
    state: Literal["COMPLETED", "INSUFFICIENT_DATA"]
    insufficientReason: str | None = None
    algorithm: str
    featureNames: list[str]
    trainingObservationCount: int
    anomalyThreshold: Decimal | None = None
    scoreSemantics: AnomalyScoreSemantics
    points: list[AnomalyPoint]


class ForecastMetrics(Contract):
    mae: Decimal
    rmse: Decimal
    smape: Decimal
    nonSeasonalMase: Decimal | None = None
    seasonalMase: Decimal | None = None


class HorizonEvaluation(Contract):
    horizonStep: int = Field(ge=1)
    observationCount: int = Field(ge=1)
    metrics: ForecastMetrics


class ModelFitDiagnostics(Contract):
    applicable: bool
    converged: bool | None = None
    finiteParameters: bool | None = None
    arRootsStable: bool | None = None
    maRootsInvertible: bool | None = None
    ljungBoxPValue: Decimal | None = None
    residualAutocorrelationWarning: bool | None = None
    warnings: list[str]


class CandidateEvaluation(Contract):
    family: str
    configuration: dict[str, Any]
    state: Literal["EVALUATED", "INELIGIBLE", "FAILED"]
    reason: str | None = None
    rollingOriginCount: int
    effectiveEvaluatedHorizons: list[int]
    metrics: ForecastMetrics | None = None
    perHorizonMetrics: list[HorizonEvaluation]
    primaryMetricStandardError: Decimal | None = None
    diagnostics: ModelFitDiagnostics | None = None


class ModelReference(Contract):
    family: str
    configuration: dict[str, Any]
    metrics: ForecastMetrics


class ModelSelectionDecision(Contract):
    rawBest: ModelReference
    parsimoniousChoice: ModelReference
    selected: ModelReference
    primaryMetric: Literal["sMAPE"]
    rawBestStandardError: Decimal
    competitiveThreshold: Decimal
    parsimonyChangedSelection: bool
    finalFallbackApplied: bool
    rule: Literal["ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY"]


class ForecastPoint(Contract):
    effectiveDate: date
    value: Decimal
    lowerBound: Decimal | None = None
    upperBound: Decimal | None = None
    intervalAvailable: bool


class ForecastAnalysis(Contract):
    state: Literal["COMPLETED", "INSUFFICIENT_DATA"]
    insufficientReason: str | None = None
    selectedModelFamily: str | None = None
    selectedModelConfiguration: dict[str, Any]
    trainingObservationCount: int
    requestedHorizon: int
    effectiveEvaluatedHorizons: list[int]
    rollingOriginCount: int
    generatedAt: datetime
    points: list[ForecastPoint]
    selectedMetrics: ForecastMetrics | None = None
    candidates: list[CandidateEvaluation]
    modelSelection: ModelSelectionDecision | None = None
    selectedModelDiagnostics: ModelFitDiagnostics | None = None
    selectionReason: str | None = None
    forecastDirection: Literal["INCREASING", "DECREASING", "STABLE", "UNAVAILABLE"]
    intervalConfidence: Decimal | None = None


class LatestObservationExpectation(Contract):
    state: Literal["COMPLETED", "INSUFFICIENT_DATA"]
    insufficientReason: str | None = None
    entryId: int | None = None
    reportId: int | None = None
    effectiveDate: date | None = None
    actualValue: Decimal | None = None
    trainingObservationCount: int
    expectedValue: Decimal | None = None
    lowerBound: Decimal | None = None
    upperBound: Decimal | None = None
    intervalAvailable: bool
    outsideInterval: bool | None = None
    selectedModelFamily: str | None = None
    selectedModelConfiguration: dict[str, Any]
    selectedMetrics: ForecastMetrics | None = None
    modelSelection: ModelSelectionDecision | None = None
    selectedModelDiagnostics: ModelFitDiagnostics | None = None


class AnalysisResponse(Contract):
    analysisId: str
    kpiDefinitionId: int
    anomaly: AnomalyAnalysis
    forecast: ForecastAnalysis
    latestObservationExpectation: LatestObservationExpectation
