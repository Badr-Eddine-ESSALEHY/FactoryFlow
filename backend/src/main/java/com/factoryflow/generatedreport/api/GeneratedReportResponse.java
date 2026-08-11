package com.factoryflow.generatedreport.api;

import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationOrigin;
import com.factoryflow.generatedreport.domain.GenerationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record GeneratedReportResponse(
        Long id,
        GeneratedReportType type,
        GeneratedReportFormat format,
        LocalDate periodStart,
        LocalDate periodEnd,
        GenerationOrigin origin,
        GenerationStatus generationStatus,
        EmailDeliveryStatus emailDeliveryStatus,
        int version,
        Instant generatedAt,
        String fileName,
        Long generatedBy,
        Long regeneratedFromId,
        List<Long> sourceReportIds
) {
    public static GeneratedReportResponse from(GeneratedReport report) {
        return new GeneratedReportResponse(
                report.getId(), report.getType(), report.getFormat(), report.getPeriodStart(), report.getPeriodEnd(),
                report.getOrigin(), report.getGenerationStatus(), report.getEmailDeliveryStatus(), report.getVersion(),
                report.getGeneratedAt(), report.getFileName(),
                report.getGeneratedBy() == null ? null : report.getGeneratedBy().getId(),
                report.getRegeneratedFrom() == null ? null : report.getRegeneratedFrom().getId(),
                report.getSourceReports().stream().map(source -> source.getId()).sorted().toList()
        );
    }
}
