package com.factoryflow.intelligence.application;

import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.intelligence.domain.PreparedKpiSeries;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AnalyticalRuntimeRequest(
        String analysisId,
        KpiIdentity kpi,
        LocalDate windowStart,
        LocalDate windowEnd,
        Instant generatedAt,
        PreparedKpiSeries.CadenceMetadata cadence,
        List<PreparedKpiSeries.Observation> observations,
        Configuration configuration
) {
    public AnalyticalRuntimeRequest {
        observations = List.copyOf(observations);
    }

    public record Configuration(
            int anomalyMinimumHistory,
            int anomalyEstimators,
            int anomalyRandomState,
            int anomalyRollingWindow,
            int forecastMinimumHistory,
            int forecastHorizon,
            int backtestMinimumTraining,
            int backtestMinimumFolds,
            int backtestMaximumFolds,
            int seasonalPeriod,
            int seasonalMinimumCycles,
            double intervalConfidence
    ) {
    }
}
