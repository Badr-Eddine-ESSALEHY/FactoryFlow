package com.factoryflow.generatedreport.api;

import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import jakarta.validation.constraints.NotNull;

public record IndividualReportExportRequest(
        @NotNull Long reportId,
        @NotNull GeneratedReportFormat format
) { }
