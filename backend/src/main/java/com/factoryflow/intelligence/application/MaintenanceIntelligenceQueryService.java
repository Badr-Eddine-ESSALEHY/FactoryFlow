package com.factoryflow.intelligence.application;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.factoryflow.intelligence.api.*;
import com.factoryflow.intelligence.domain.*;
import com.factoryflow.intelligence.persistence.*;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.shared.api.PageResponse;
import com.factoryflow.shared.error.*;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaintenanceIntelligenceQueryService {
    private static final List<IntelligenceAnalysisStatus> USABLE = List.of(IntelligenceAnalysisStatus.COMPLETED, IntelligenceAnalysisStatus.INSUFFICIENT_DATA);
    private final MaintenanceIntelligenceAnalysisRepository analyses; private final MaintenanceIntelligenceAlertRepository alerts;
    private final KpiDefinitionRepository kpis; private final KpiIntelligenceProfileService profiles; private final ObjectMapper mapper;
    public MaintenanceIntelligenceQueryService(MaintenanceIntelligenceAnalysisRepository analyses,
            MaintenanceIntelligenceAlertRepository alerts, KpiDefinitionRepository kpis,
            KpiIntelligenceProfileService profiles, ObjectMapper mapper) {
        this.analyses = analyses; this.alerts = alerts; this.kpis = kpis; this.profiles = profiles; this.mapper = mapper;
    }
    @Transactional(readOnly = true)
    public IntelligenceAnalysisDetailResponse latest(Long kpiId) {
        var analysis = analyses.findFirstByKpiIdAndStatusInOrderByGeneratedAtDesc(kpiId, USABLE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.INTELLIGENCE_ANALYSIS_NOT_FOUND, "No persisted intelligence analysis exists for this KPI."));
        return detail(analysis);
    }
    @Transactional(readOnly = true) public IntelligenceAnalysisDetailResponse detail(Long id) {
        return detail(analyses.findDetailedById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.INTELLIGENCE_ANALYSIS_NOT_FOUND, "Intelligence analysis not found.")));
    }
    @Transactional(readOnly = true)
    public PageResponse<IntelligenceAnalysisSummaryResponse> history(Long kpiId, Pageable pageable) {
        return PageResponse.from(analyses.findByKpiIdOrderByGeneratedAtDesc(kpiId, pageable), IntelligenceAnalysisSummaryResponse::from);
    }
    @Transactional
    public IntelligenceOverviewResponse overview() {
        var items = kpis.findAllByActiveOrderByDisplayNameAsc(true).stream().map(kpi -> {
            var profile = profiles.get(kpi.getId());
            var successful = analyses.findFirstByKpiIdAndStatusInOrderByGeneratedAtDesc(kpi.getId(), USABLE).map(IntelligenceAnalysisSummaryResponse::from).orElse(null);
            var attempt = analyses.findFirstByKpiIdOrderByGeneratedAtDesc(kpi.getId()).map(IntelligenceAnalysisSummaryResponse::from).orElse(null);
            return new IntelligenceOverviewItemResponse(profile, successful, attempt, alerts.countByKpiId(kpi.getId()));
        }).toList();
        var recent = alerts.findAll(alertSpecification(null, null, null, null, null),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream().map(IntelligenceAlertResponse::from).toList();
        return new IntelligenceOverviewResponse(items, recent);
    }
    @Transactional(readOnly = true)
    public PageResponse<IntelligenceAlertResponse> alerts(Long kpiId, ContextualAlertType type,
            IntelligenceAttentionLevel level, Instant from, Instant to, Pageable pageable) {
        return PageResponse.from(alerts.findAll(alertSpecification(kpiId, type, level, from, to), pageable),
                IntelligenceAlertResponse::from);
    }
    @Transactional(readOnly = true)
    public IntelligenceAlertResponse alert(Long id) {
        return IntelligenceAlertResponse.from(alerts.findDetailedById(id).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.INTELLIGENCE_ALERT_NOT_FOUND, "Intelligence alert not found.")));
    }
    private IntelligenceAnalysisDetailResponse detail(MaintenanceIntelligenceAnalysis analysis) {
        try {
            MaintenanceIntelligenceResult result = analysis.getResultSnapshot() == null ? null
                    : mapper.treeToValue(analysis.getResultSnapshot(), MaintenanceIntelligenceResult.class);
            return new IntelligenceAnalysisDetailResponse(IntelligenceAnalysisSummaryResponse.from(analysis), result);
        } catch (JsonProcessingException failure) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR, "The persisted intelligence snapshot could not be read.");
        }
    }
    private Specification<MaintenanceIntelligenceAlert> alertSpecification(Long kpiId, ContextualAlertType type,
            IntelligenceAttentionLevel level, Instant from, Instant to) {
        Specification<MaintenanceIntelligenceAlert> specification = Specification.unrestricted();
        if (kpiId != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("kpi").get("id"), kpiId));
        if (type != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("type"), type));
        if (level != null) specification = specification.and((root, query, builder) ->
                builder.equal(root.get("attentionLevel"), level));
        if (from != null) specification = specification.and((root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("createdAt"), from));
        if (to != null) specification = specification.and((root, query, builder) ->
                builder.lessThan(root.get("createdAt"), to));
        return specification;
    }
}
