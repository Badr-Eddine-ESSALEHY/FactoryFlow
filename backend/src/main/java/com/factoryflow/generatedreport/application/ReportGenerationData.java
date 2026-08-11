package com.factoryflow.generatedreport.application;

import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.report.domain.AcquisitionSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReportGenerationData(
        GeneratedReportType type,
        ReportPeriod period,
        Instant generatedAt,
        List<Row> rows
) {
    public record Row(
            LocalDate effectiveDate,
            Long sourceReportId,
            AcquisitionSource source,
            String kpiName,
            String unit,
            BigDecimal confirmedValue,
            String submittedBy,
            Instant confirmedAt
    ) { }
}
