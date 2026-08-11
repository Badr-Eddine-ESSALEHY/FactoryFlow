package com.factoryflow.parser.api;

import com.factoryflow.report.domain.AcquisitionSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnalyzeReportRequest(
        @NotBlank String rawText,
        @NotNull AcquisitionSource source
) {
}
