package com.factoryflow.report.api;

import com.factoryflow.report.domain.AcquisitionSource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record DraftReportRequest(
        @NotNull LocalDate effectiveDate,
        @NotNull AcquisitionSource source,
        String rawText,
        List<@Valid DraftEntryRequest> entries,
        List<@Valid DraftUnknownLineRequest> unrecognizedLines
) {
    public DraftReportRequest {
        entries = entries == null ? List.of() : List.copyOf(entries);
        unrecognizedLines = unrecognizedLines == null ? List.of() : List.copyOf(unrecognizedLines);
    }
}
