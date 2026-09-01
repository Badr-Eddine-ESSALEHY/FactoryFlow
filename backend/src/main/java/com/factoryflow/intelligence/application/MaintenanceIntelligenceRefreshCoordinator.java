package com.factoryflow.intelligence.application;
import com.factoryflow.intelligence.domain.MaintenanceIntelligenceAnalysis;
import com.factoryflow.shared.error.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
@Component
public class MaintenanceIntelligenceRefreshCoordinator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MaintenanceIntelligenceRefreshCoordinator.class);
    private final Set<Long> active = ConcurrentHashMap.newKeySet(); private final MaintenanceIntelligenceRefreshOrchestrator orchestrator;
    private final ThreadPoolTaskExecutor executor;
    public MaintenanceIntelligenceRefreshCoordinator(MaintenanceIntelligenceRefreshOrchestrator orchestrator,
            @Qualifier("maintenanceIntelligenceExecutor") ThreadPoolTaskExecutor executor) { this.orchestrator = orchestrator; this.executor = executor; }
    public MaintenanceIntelligenceAnalysis refreshNow(Long kpiId, LocalDate end) {
        if (!active.add(kpiId)) throw new ApiException(HttpStatus.CONFLICT, ApiErrorCode.INTELLIGENCE_REFRESH_IN_PROGRESS, "An intelligence refresh is already running for this KPI.");
        try { return orchestrator.refresh(kpiId, end, false); } finally { active.remove(kpiId); }
    }
    public void submit(Long kpiId, LocalDate end) {
        if (!active.add(kpiId)) return;
        try { executor.execute(() -> { try { orchestrator.refresh(kpiId, end, true); } finally { active.remove(kpiId); } }); }
        catch (RejectedExecutionException rejected) { active.remove(kpiId); LOGGER.warn("MI refresh queue full kpiId={}", kpiId); }
    }
}
