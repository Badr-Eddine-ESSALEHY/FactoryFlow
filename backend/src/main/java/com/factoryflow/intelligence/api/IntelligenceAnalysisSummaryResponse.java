package com.factoryflow.intelligence.api;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceAnalysis;
import java.math.BigDecimal;
import java.time.*;
public record IntelligenceAnalysisSummaryResponse(Long id, String analysisId, Long kpiDefinitionId,
        String kpiCode, String kpiDisplayName, String status, LocalDate windowStart, LocalDate windowEnd,
        Instant generatedAt, long durationMillis, int usableObservationCount, int missingObservationCount,
        String cadenceState, String cadenceBasis, String cadenceAmbiguity, String trendDirection,
        String anomalyState, String anomalyReason, Boolean latestAnomalous, BigDecimal latestAnomalyScore,
        String forecastState, String forecastReason, String selectedModelFamily, String forecastDirection,
        Integer forecastHorizon, BigDecimal forecastMae, BigDecimal forecastRmse, BigDecimal forecastSmape,
        String expectationState, String expectationReason, BigDecimal latestActualValue, BigDecimal expectedValue,
        BigDecimal expectedLowerBound, BigDecimal expectedUpperBound, Boolean outsideExpectedInterval,
        String contextualizationStatus, String technicalFailureCode, String technicalFailureMessage) {
    public static IntelligenceAnalysisSummaryResponse from(MaintenanceIntelligenceAnalysis a) {
        return new IntelligenceAnalysisSummaryResponse(a.getId(), a.getAnalysisId(), a.getKpi().getId(), a.getKpi().getCode(),
                a.getKpi().getDisplayName(), a.getStatus().name(), a.getWindowStart(), a.getWindowEnd(), a.getGeneratedAt(),
                a.getDurationMillis(), a.getUsableObservationCount(), a.getMissingObservationCount(), a.getCadenceState(),
                a.getCadenceBasis(), a.getCadenceAmbiguity(), a.getTrendDirection() == null ? null : a.getTrendDirection().name(),
                a.getAnomalyState(), a.getAnomalyReason(), a.getLatestAnomalous(), a.getLatestAnomalyScore(),
                a.getForecastState(), a.getForecastReason(), a.getSelectedModelFamily(), a.getForecastDirection(),
                a.getForecastHorizon(), a.getForecastMae(), a.getForecastRmse(), a.getForecastSmape(),
                a.getExpectationState(), a.getExpectationReason(), a.getLatestActualValue(), a.getExpectedValue(),
                a.getExpectedLowerBound(), a.getExpectedUpperBound(), a.getOutsideExpectedInterval(),
                a.getContextualizationStatus().name(), a.getTechnicalFailureCode(), a.getTechnicalFailureMessage());
    }
}
