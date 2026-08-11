package com.factoryflow.schedule.application;

import com.factoryflow.email.application.EmailDeliveryService;
import com.factoryflow.generatedreport.api.GeneratedReportResponse;
import com.factoryflow.generatedreport.application.GeneratedReportService;
import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.persistence.ReportScheduleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class ScheduleExecutionService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of(ReportSchedule.BUSINESS_TIMEZONE);
    private final ReportScheduleRepository schedules; private final SchedulePeriodCalculator periods;
    private final ScheduleRunStateService runState; private final GeneratedReportService generation;
    private final EmailDeliveryService email; private final Clock clock;
    public ScheduleExecutionService(ReportScheduleRepository schedules, SchedulePeriodCalculator periods,
                                    ScheduleRunStateService runState, GeneratedReportService generation,
                                    EmailDeliveryService email, Clock clock) {
        this.schedules = schedules; this.periods = periods; this.runState = runState;
        this.generation = generation; this.email = email; this.clock = clock;
    }

    public void execute(Long scheduleId, Instant scheduledFor) {
        ReportSchedule schedule = schedules.findById(scheduleId).orElse(null);
        if (schedule == null || !schedule.isEnabled()) return;
        ReportPeriod period = periods.calculate(schedule.getType(), scheduledFor.atZone(BUSINESS_ZONE).toLocalDate());
        for (GeneratedReportFormat format : formats(schedule)) executeFormat(schedule, period, format, scheduledFor);
    }

    private void executeFormat(ReportSchedule schedule, ReportPeriod period, GeneratedReportFormat format,
                               Instant scheduledFor) {
        Long runId;
        try {
            var started = runState.start(schedule, format, period, scheduledFor, clock.instant());
            if (started.isEmpty()) return;
            runId = started.get();
        } catch (DataIntegrityViolationException duplicate) {
            return;
        }
        try {
            GeneratedReportResponse report = generation.generateScheduled(schedule, format, period);
            EmailDeliveryStatus emailStatus = schedule.isEmailEnabled()
                    ? email.deliver(report.id(), schedule.getRecipients()) : EmailDeliveryStatus.NOT_REQUESTED;
            runState.succeed(runId, report.id(), emailStatus, clock.instant());
        } catch (RuntimeException exception) {
            runState.fail(runId, "SCHEDULE_GENERATION_FAILED", exception.getMessage(), clock.instant());
        }
    }

    private List<GeneratedReportFormat> formats(ReportSchedule schedule) {
        List<GeneratedReportFormat> formats = new ArrayList<>(2);
        if (schedule.isGenerateExcel()) formats.add(GeneratedReportFormat.EXCEL);
        if (schedule.isGeneratePdf()) formats.add(GeneratedReportFormat.PDF);
        return formats;
    }
}
