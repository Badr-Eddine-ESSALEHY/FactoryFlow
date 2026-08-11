package com.factoryflow.report.api;

import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import java.time.Instant;
import java.time.LocalDate;

public record ReportSummaryResponse(
        Long id,
        ReportStatus status,
        AcquisitionSource source,
        LocalDate effectiveDate,
        Instant submittedAt,
        Instant confirmedAt,
        ReportResponse.SubmittedByResponse submittedBy,
        int kpiCount,
        int warningCount
) {
    public static ReportSummaryResponse from(MaintenanceReport report) {
        int warnings = report.getEntries().stream().mapToInt(entry -> entry.getWarningCodes().size()).sum();
        return new ReportSummaryResponse(
                report.getId(), report.getStatus(), report.getSource(), report.getEffectiveDate(),
                report.getSubmittedAt(), report.getConfirmedAt(),
                new ReportResponse.SubmittedByResponse(report.getSubmittedBy().getId(), report.getSubmittedBy().getName()),
                report.getEntries().size(), warnings
        );
    }
}
