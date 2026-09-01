from .anomaly import analyze_anomalies
from .contracts import AnalysisRequest, AnalysisResponse
from .forecasting import analyze_forecast, analyze_latest_observation_expectation


def analyze(request: AnalysisRequest) -> AnalysisResponse:
    return AnalysisResponse(
        analysisId=request.analysisId,
        kpiDefinitionId=request.kpi.definitionId,
        anomaly=analyze_anomalies(request),
        forecast=analyze_forecast(request),
        latestObservationExpectation=analyze_latest_observation_expectation(request),
    )
