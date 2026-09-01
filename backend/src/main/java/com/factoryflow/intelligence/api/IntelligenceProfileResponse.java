package com.factoryflow.intelligence.api;
import com.factoryflow.intelligence.domain.KpiIntelligenceProfile;
import java.time.Instant;
public record IntelligenceProfileResponse(Long id, Long kpiDefinitionId, String kpiCode, String kpiDisplayName,
        boolean enabled, Integer expectedCadenceDays, int forecastHorizon, Integer seasonalPeriod,
        int historyWindowDays, Instant createdAt, Instant updatedAt, long version) {
    public static IntelligenceProfileResponse from(KpiIntelligenceProfile p) {
        return new IntelligenceProfileResponse(p.getId(), p.getKpi().getId(), p.getKpi().getCode(), p.getKpi().getDisplayName(),
                p.isEnabled(), p.getExpectedCadenceDays(), p.getForecastHorizon(), p.getSeasonalPeriod(),
                p.getHistoryWindowDays(), p.getCreatedAt(), p.getUpdatedAt(), p.getVersion());
    }
}
