from fastapi import FastAPI

from intelligence.contracts import AnalysisRequest, AnalysisResponse
from intelligence.service import analyze


app = FastAPI(title="FactoryFlow Maintenance Intelligence Runtime", version="1.0.0")


@app.get("/health")
def health() -> dict[str, object]:
    return {
        "ready": True,
        "engine": "scikit-learn IsolationForest + statsmodels ETS/SARIMA",
    }


@app.post("/v1/analyze", response_model=AnalysisResponse)
def analyze_kpi(request: AnalysisRequest) -> AnalysisResponse:
    return analyze(request)
