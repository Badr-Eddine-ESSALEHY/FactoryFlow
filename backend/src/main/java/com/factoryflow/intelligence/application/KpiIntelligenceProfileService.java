package com.factoryflow.intelligence.application;
import com.factoryflow.intelligence.api.*;
import com.factoryflow.intelligence.domain.KpiIntelligenceProfile;
import com.factoryflow.intelligence.persistence.KpiIntelligenceProfileRepository;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.shared.error.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KpiIntelligenceProfileService {
    private final KpiIntelligenceProfileRepository profiles; private final KpiDefinitionRepository kpis;
    public KpiIntelligenceProfileService(KpiIntelligenceProfileRepository profiles, KpiDefinitionRepository kpis) { this.profiles = profiles; this.kpis = kpis; }
    @Transactional
    public KpiIntelligenceProfile requireOrCreate(Long kpiId) {
        return profiles.findByKpiId(kpiId).orElseGet(() -> {
            var kpi = kpis.findById(kpiId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.KPI_DEFINITION_NOT_FOUND, "KPI definition not found."));
            try { return profiles.saveAndFlush(KpiIntelligenceProfile.defaults(kpi)); }
            catch (DataIntegrityViolationException duplicate) { return profiles.findByKpiId(kpiId).orElseThrow(() -> duplicate); }
        });
    }
    @Transactional public IntelligenceProfileResponse get(Long kpiId) { return IntelligenceProfileResponse.from(requireOrCreate(kpiId)); }
    @Transactional
    public IntelligenceProfileResponse update(Long kpiId, IntelligenceProfileRequest request) {
        KpiIntelligenceProfile profile = requireOrCreate(kpiId);
        if (profile.getVersion() != request.version()) throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "The intelligence profile was modified by another request.");
        try { profile.update(request.enabled(), request.expectedCadenceDays(), request.forecastHorizon(), request.seasonalPeriod(), request.historyWindowDays()); }
        catch (IllegalArgumentException e) { throw new ApiException(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, e.getMessage()); }
        return IntelligenceProfileResponse.from(profiles.saveAndFlush(profile));
    }
}
