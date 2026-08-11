package com.factoryflow.schedule.application;

import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.domain.ScheduleRun;
import com.factoryflow.schedule.persistence.ScheduleRunRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleRunStateService {
    private final ScheduleRunRepository runs; private final GeneratedReportRepository reports;
    public ScheduleRunStateService(ScheduleRunRepository runs, GeneratedReportRepository reports) {
        this.runs = runs; this.reports = reports;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<Long> start(ReportSchedule schedule, GeneratedReportFormat format, ReportPeriod period,
                                Instant scheduledFor, Instant startedAt) {
        if (runs.findByScheduleIdAndPeriodStartAndPeriodEndAndFormat(
                schedule.getId(), period.start(), period.end(), format).isPresent()) return Optional.empty();
        return Optional.of(runs.saveAndFlush(ScheduleRun.started(schedule, format, period, scheduledFor, startedAt)).getId());
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void succeed(Long runId, Long reportId, EmailDeliveryStatus emailStatus, Instant finishedAt) {
        ScheduleRun run = runs.findById(runId).orElseThrow();
        GeneratedReport report = reports.findById(reportId).orElseThrow();
        run.succeed(report, emailStatus, finishedAt);
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long runId, String code, String message, Instant finishedAt) {
        runs.findById(runId).orElseThrow().fail(code, message, finishedAt);
    }
}
