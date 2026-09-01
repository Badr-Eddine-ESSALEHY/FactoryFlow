package com.factoryflow.intelligence.infrastructure.persistence;

import com.factoryflow.intelligence.application.ConfirmedKpiHistorySource;
import com.factoryflow.intelligence.domain.ConfirmedKpiHistoryRecord;
import com.factoryflow.intelligence.domain.KpiIdentity;
import com.factoryflow.report.domain.ReportStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class JpaConfirmedKpiHistorySource implements ConfirmedKpiHistorySource {
    private final MaintenanceIntelligenceHistoryRepository repository;

    public JpaConfirmedKpiHistorySource(MaintenanceIntelligenceHistoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ConfirmedKpiHistoryRecord> load(Long kpiDefinitionId, LocalDate windowStart, LocalDate windowEnd) {
        return repository.findConfirmedHistory(kpiDefinitionId, windowStart, windowEnd).stream()
                .map(point -> new ConfirmedKpiHistoryRecord(
                        new KpiIdentity(point.getKpiDefinitionId(), point.getCode(), point.getDisplayName(), point.getUnit()),
                        point.getEntryId(), point.getReportId(), point.getEffectiveDate(), point.getConfirmedAt(),
                        ReportStatus.valueOf(point.getReportStatus()), point.getFinalValue()))
                .toList();
    }
}
