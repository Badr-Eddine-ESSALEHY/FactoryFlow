package com.factoryflow.intelligence.application;

import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult.AnomalyAnalysis;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult.ForecastAnalysis;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceResult.LatestObservationExpectation;

public record AnalyticalRuntimeResponse(
        String analysisId,
        Long kpiDefinitionId,
        AnomalyAnalysis anomaly,
        ForecastAnalysis forecast,
        LatestObservationExpectation latestObservationExpectation
) {
}
