package com.factoryflow.intelligence.api;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceAlert;
import java.math.BigDecimal;
import java.time.*;
public record IntelligenceAlertResponse(Long id, Long kpiDefinitionId, String kpiCode, String kpiDisplayName,
        Long analysisId, String analysisPublicId, Long sourceEntryId, Long sourceReportId, String type,
        String attentionLevel, LocalDate observationDate, BigDecimal actualValue, boolean anomalous,
        BigDecimal anomalyScore, BigDecimal expectedValue, BigDecimal expectedLowerBound,
        BigDecimal expectedUpperBound, Boolean outsideExpectedInterval, String trendContext,
        String forecastDirectionContext, String selectedModelFamily, Instant createdAt, Instant updatedAt,
        String notificationStatus, String notificationFailure) {
    public static IntelligenceAlertResponse from(MaintenanceIntelligenceAlert a) {
        return new IntelligenceAlertResponse(a.getId(), a.getKpi().getId(), a.getKpi().getCode(), a.getKpi().getDisplayName(),
                a.getAnalysis().getId(), a.getAnalysis().getAnalysisId(), a.getSourceEntry().getId(), a.getSourceReport().getId(),
                a.getType().name(), a.getAttentionLevel().name(), a.getObservationDate(), a.getActualValue(), a.isAnomalous(),
                a.getAnomalyScore(), a.getExpectedValue(), a.getExpectedLowerBound(), a.getExpectedUpperBound(),
                a.getOutsideExpectedInterval(), a.getTrendContext().name(), a.getForecastDirectionContext(),
                a.getSelectedModelFamily(), a.getCreatedAt(), a.getUpdatedAt(), a.getNotificationStatus().name(), a.getNotificationFailure());
    }
}
