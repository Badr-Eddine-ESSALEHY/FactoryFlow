package com.factoryflow.schedule.api;

import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.schedule.domain.ScheduleRun;
import com.factoryflow.schedule.domain.ScheduleRunStatus;
import java.time.Instant;
import java.time.LocalDate;

public record ScheduleRunResponse(Long id, GeneratedReportFormat format, LocalDate periodStart, LocalDate periodEnd,
                                  Instant scheduledFor, Instant startedAt, Instant finishedAt, ScheduleRunStatus status,
                                  Long generatedReportId, EmailDeliveryStatus emailDeliveryStatus,
                                  String errorCode, String errorMessage) {
    public static ScheduleRunResponse from(ScheduleRun run) {
        return new ScheduleRunResponse(run.getId(), run.getFormat(), run.getPeriodStart(), run.getPeriodEnd(),
                run.getScheduledFor(), run.getStartedAt(), run.getFinishedAt(), run.getStatus(),
                run.getGeneratedReport() == null ? null : run.getGeneratedReport().getId(),
                run.getEmailDeliveryStatus(), run.getErrorCode(), run.getErrorMessage());
    }
}
