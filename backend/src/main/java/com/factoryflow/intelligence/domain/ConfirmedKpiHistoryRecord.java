package com.factoryflow.intelligence.domain;

import com.factoryflow.report.domain.ReportStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ConfirmedKpiHistoryRecord(
        KpiIdentity kpi,
        Long entryId,
        Long reportId,
        LocalDate effectiveDate,
        Instant confirmedAt,
        ReportStatus reportStatus,
        BigDecimal finalValue
) {
}
