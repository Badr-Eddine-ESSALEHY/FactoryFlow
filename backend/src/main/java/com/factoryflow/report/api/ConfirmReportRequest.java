package com.factoryflow.report.api;

import jakarta.validation.Valid;
import java.util.List;

public record ConfirmReportRequest(
        List<@Valid ConfirmationEntryRequest> entries,
        List<@Valid UnknownLineResolutionRequest> unrecognizedLineResolutions
) {
    public ConfirmReportRequest {
        entries = entries == null ? List.of() : List.copyOf(entries);
        unrecognizedLineResolutions = unrecognizedLineResolutions == null ? List.of() : List.copyOf(unrecognizedLineResolutions);
    }
}
