package com.factoryflow.report.api;

import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.domain.ReportStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReportResponse(
        Long id,
        ReportStatus status,
        LocalDate effectiveDate,
        AcquisitionSource source,
        String rawText,
        Instant submittedAt,
        Instant updatedAt,
        Instant confirmedAt,
        long version,
        SubmittedByResponse submittedBy,
        List<DraftEntryResponse> entries,
        List<UnknownLineResponse> unrecognizedLines
) {
    public static ReportResponse from(MaintenanceReport report) {
        return new ReportResponse(
                report.getId(), report.getStatus(), report.getEffectiveDate(), report.getSource(), report.getRawText(),
                report.getSubmittedAt(), report.getUpdatedAt(), report.getConfirmedAt(), report.getVersion(),
                new SubmittedByResponse(report.getSubmittedBy().getId(), report.getSubmittedBy().getName()),
                report.getEntries().stream().map(DraftEntryResponse::from).toList(),
                report.getUnrecognizedLines().stream().map(UnknownLineResponse::from).toList()
        );
    }

    public record SubmittedByResponse(Long id, String name) { }
}
