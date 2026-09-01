package com.factoryflow.intelligence.application;
import com.factoryflow.report.application.ReportConfirmedEvent;
import java.time.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
@Component
public class ConfirmedReportIntelligenceListener {
    private final MaintenanceIntelligenceRefreshCoordinator coordinator;
    private final Clock clock;
    public ConfirmedReportIntelligenceListener(MaintenanceIntelligenceRefreshCoordinator coordinator, Clock clock) { this.coordinator = coordinator; this.clock = clock; }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterConfirmed(ReportConfirmedEvent event) {
        LocalDate today = LocalDate.now(clock);
        LocalDate windowEnd = event.effectiveDate().isAfter(today) ? event.effectiveDate() : today;
        event.kpiDefinitionIds().forEach(kpiId -> coordinator.submit(kpiId, windowEnd));
    }
}
