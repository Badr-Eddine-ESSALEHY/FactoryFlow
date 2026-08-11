package com.factoryflow.generatedreport.api;

import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record GenerateReportRequest(
        @NotNull GeneratedReportType type,
        @NotNull GeneratedReportFormat format,
        @NotNull LocalDate periodStart,
        @NotNull LocalDate periodEnd
) { }
