package com.factoryflow.intelligence.api;
import com.factoryflow.intelligence.application.*;
import com.factoryflow.intelligence.domain.*;
import com.factoryflow.shared.api.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.time.*;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance-intelligence")
@SecurityRequirement(name = "bearerAuth")
public class MaintenanceIntelligenceController {
    private final MaintenanceIntelligenceQueryService queries; private final KpiIntelligenceProfileService profiles;
    private final MaintenanceIntelligenceRefreshCoordinator refresh;
    public MaintenanceIntelligenceController(MaintenanceIntelligenceQueryService queries,
            KpiIntelligenceProfileService profiles, MaintenanceIntelligenceRefreshCoordinator refresh) {
        this.queries = queries; this.profiles = profiles; this.refresh = refresh;
    }
    @GetMapping("/overview") @Operation(summary = "Get compact Maintenance Intelligence overview")
    public IntelligenceOverviewResponse overview() { return queries.overview(); }
    @GetMapping("/kpis/{kpiId}") @Operation(summary = "Get latest usable rich analysis for one KPI")
    public IntelligenceAnalysisDetailResponse latest(@PathVariable Long kpiId) { return queries.latest(kpiId); }
    @GetMapping("/analyses/{analysisId}") public IntelligenceAnalysisDetailResponse analysis(@PathVariable Long analysisId) { return queries.detail(analysisId); }
    @GetMapping("/kpis/{kpiId}/analyses")
    public PageResponse<IntelligenceAnalysisSummaryResponse> history(@PathVariable Long kpiId,
            @PageableDefault(size = 20, sort = "generatedAt", direction = Sort.Direction.DESC) Pageable pageable) { return queries.history(kpiId, pageable); }
    @PostMapping("/kpis/{kpiId}/refresh") @Operation(summary = "Synchronously refresh one KPI with confirmed history")
    public IntelligenceAnalysisDetailResponse refresh(@PathVariable Long kpiId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate windowEnd) {
        var analysis = refresh.refreshNow(kpiId, windowEnd); return queries.detail(analysis.getId());
    }
    @GetMapping("/kpis/{kpiId}/profile") public IntelligenceProfileResponse profile(@PathVariable Long kpiId) { return profiles.get(kpiId); }
    @PutMapping("/kpis/{kpiId}/profile") public IntelligenceProfileResponse updateProfile(@PathVariable Long kpiId,
            @Valid @RequestBody IntelligenceProfileRequest request) { return profiles.update(kpiId, request); }
    @GetMapping("/alerts")
    public PageResponse<IntelligenceAlertResponse> alerts(@RequestParam(required = false) Long kpiId,
            @RequestParam(required = false) ContextualAlertType type,
            @RequestParam(required = false) IntelligenceAttentionLevel attentionLevel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return queries.alerts(kpiId, type, attentionLevel, from, to, pageable);
    }
    @GetMapping("/alerts/{id}") public IntelligenceAlertResponse alert(@PathVariable Long id) { return queries.alert(id); }
}
