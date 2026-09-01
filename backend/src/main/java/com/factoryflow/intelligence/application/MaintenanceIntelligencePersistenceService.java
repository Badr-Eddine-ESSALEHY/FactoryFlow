package com.factoryflow.intelligence.application;

import com.fasterxml.jackson.databind.*;
import com.factoryflow.intelligence.domain.*;
import com.factoryflow.intelligence.persistence.MaintenanceIntelligenceAnalysisRepository;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class MaintenanceIntelligencePersistenceService {
    private final MaintenanceIntelligenceAnalysisRepository analyses;
    private final ObjectMapper mapper;
    public MaintenanceIntelligencePersistenceService(MaintenanceIntelligenceAnalysisRepository analyses,
            ObjectMapper mapper) { this.analyses = analyses; this.mapper = mapper; }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MaintenanceIntelligenceAnalysis persistSuccess(KpiIntelligenceProfile profile, LocalDate start, LocalDate end,
            MaintenanceIntelligenceResult result, Instant completedAt, long durationMillis) {
        JsonNode profileSnapshot = mapper.valueToTree(new ProfileSnapshot(profile.isEnabled(), profile.getExpectedCadenceDays(),
                profile.getForecastHorizon(), profile.getSeasonalPeriod(), profile.getHistoryWindowDays(), profile.getVersion()));
        JsonNode resultSnapshot = mapper.valueToTree(result);
        return analyses.saveAndFlush(MaintenanceIntelligenceAnalysis.successful(profile.getKpi(), profile.getVersion(),
                profileSnapshot, start, end, result, resultSnapshot, completedAt, durationMillis));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public MaintenanceIntelligenceAnalysis persistFailure(KpiIntelligenceProfile profile, LocalDate start, LocalDate end,
            Instant at, long durationMillis, String code, String message) {
        JsonNode snapshot = mapper.valueToTree(new ProfileSnapshot(profile.isEnabled(), profile.getExpectedCadenceDays(),
                profile.getForecastHorizon(), profile.getSeasonalPeriod(), profile.getHistoryWindowDays(), profile.getVersion()));
        return analyses.saveAndFlush(MaintenanceIntelligenceAnalysis.failure(profile.getKpi(), profile.getVersion(), snapshot,
                start, end, at, durationMillis, code, message));
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void contextCompleted(Long id) { analyses.findById(id).orElseThrow().contextualizationCompleted(); }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void contextFailed(Long id, String message) { analyses.findById(id).orElseThrow().contextualizationFailed(message); }
    public record ProfileSnapshot(boolean enabled, Integer expectedCadenceDays, int forecastHorizon,
                                  Integer seasonalPeriod, int historyWindowDays, long version) { }
}
