from datetime import date, datetime, timedelta, timezone
from decimal import Decimal

from intelligence.contracts import (
    AnalysisRequest,
    CadenceMetadata,
    KpiIdentity,
    Observation,
    RuntimeConfiguration,
)


def request_for(
    values: list[float | int],
    *,
    dates: list[date] | None = None,
    anomaly_minimum: int = 12,
    forecast_minimum: int = 8,
    horizon: int = 4,
    seasonal_period: int = 7,
    missing_count: int = 0,
    expected_cadence: int | None = None,
) -> AnalysisRequest:
    start = date(2026, 1, 1)
    observation_dates = dates or [start + timedelta(days=index) for index in range(len(values))]
    distinct = list(dict.fromkeys(observation_dates))
    duplicates = len(observation_dates) - len(distinct)
    intervals = [(distinct[index] - distinct[index - 1]).days for index in range(1, len(distinct))]
    regular = len(distinct) > 1 and duplicates == 0 and len(set(intervals)) == 1
    if not distinct:
        state, interval = "EMPTY", None
    elif duplicates:
        state, interval = "IRREGULAR", None
    elif len(distinct) == 1:
        state, interval = "SINGLE_DATE", None
    elif regular:
        state, interval = "REGULAR", intervals[0]
    else:
        state, interval = "IRREGULAR", None
    if duplicates:
        ambiguity = "DUPLICATE_EFFECTIVE_DATES"
    elif missing_count:
        ambiguity = "MISSING_OBSERVATIONS"
    elif state in {"EMPTY", "SINGLE_DATE"}:
        ambiguity = "INSUFFICIENT_OBSERVATIONS"
    elif state == "IRREGULAR":
        ambiguity = "IRREGULAR_OBSERVED_SPACING"
    elif expected_cadence is not None and expected_cadence != interval:
        ambiguity = "OBSERVED_SPACING_DIFFERS_FROM_EXPECTED"
    else:
        ambiguity = "NONE"
    basis = "CONFIGURED_EXPECTED" if expected_cadence is not None else (
        "INFERRED_OBSERVED" if state == "REGULAR" and not missing_count else "UNKNOWN")
    observations = [
        Observation(
            entryId=index + 1,
            reportId=index + 101,
            effectiveDate=observation_dates[index],
            confirmedAt=datetime(2026, 1, 1, tzinfo=timezone.utc) + timedelta(days=index),
            value=Decimal(str(value)),
        )
        for index, value in enumerate(values)
    ]
    return AnalysisRequest(
        analysisId="analysis-1",
        kpi=KpiIdentity(definitionId=9, code="TEMP", displayName="Température", unit="°C"),
        windowStart=start,
        windowEnd=observation_dates[-1] if observation_dates else start,
        generatedAt=datetime(2026, 2, 1, tzinfo=timezone.utc),
        cadence=CadenceMetadata(
            state=state,
            observedCadenceDays=interval,
            expectedCadenceDays=expected_cadence,
            cadenceBasis=basis,
            ambiguity=ambiguity,
            distinctDateCount=len(distinct),
            duplicateDateCount=duplicates,
            missingValueCount=missing_count,
            resamplingApplied=False,
            resamplingPolicy="NONE",
        ),
        observations=observations,
        configuration=RuntimeConfiguration(
            anomalyMinimumHistory=anomaly_minimum,
            anomalyEstimators=200,
            anomalyRandomState=42,
            anomalyRollingWindow=5,
            forecastMinimumHistory=forecast_minimum,
            forecastHorizon=horizon,
            backtestMinimumTraining=6,
            backtestMinimumFolds=3,
            backtestMaximumFolds=8,
            seasonalPeriod=seasonal_period,
            seasonalMinimumCycles=3,
            intervalConfidence=0.95,
        ),
    )
