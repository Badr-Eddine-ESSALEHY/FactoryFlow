package com.factoryflow.report.application;
import java.time.LocalDate;
import java.util.Set;
public record ReportConfirmedEvent(Long reportId, LocalDate effectiveDate, Set<Long> kpiDefinitionIds) {
    public ReportConfirmedEvent { kpiDefinitionIds = Set.copyOf(kpiDefinitionIds); }
}
