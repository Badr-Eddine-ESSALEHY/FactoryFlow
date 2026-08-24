package com.factoryflow.schedule.domain;

import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReport;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "schedule_runs")
public class ScheduleRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "schedule_id", nullable = false)
    private ReportSchedule schedule;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private GeneratedReportFormat format;
    @Column(name = "period_start", nullable = false) private LocalDate periodStart;
    @Column(name = "period_end", nullable = false) private LocalDate periodEnd;
    @Column(name = "scheduled_for", nullable = false) private Instant scheduledFor;
    @Column(name = "started_at", nullable = false) private Instant startedAt;
    @Column(name = "finished_at") private Instant finishedAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ScheduleRunStatus status;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "generated_report_id") private GeneratedReport generatedReport;
    @Enumerated(EnumType.STRING) @Column(name = "email_delivery_status", nullable = false, length = 40)
    private EmailDeliveryStatus emailDeliveryStatus;
    @Column(name = "error_code", length = 80) private String errorCode;
    @Column(name = "error_message", length = 500) private String errorMessage;

    protected ScheduleRun() { }
    public static ScheduleRun started(ReportSchedule schedule, GeneratedReportFormat format, ReportPeriod period,
                                      Instant scheduledFor, Instant startedAt) {
        ScheduleRun run = new ScheduleRun(); run.schedule = schedule; run.format = format;
        run.periodStart = period.start(); run.periodEnd = period.end(); run.scheduledFor = scheduledFor;
        run.startedAt = startedAt; run.status = ScheduleRunStatus.STARTED;
        run.emailDeliveryStatus = schedule.isEmailEnabled() ? EmailDeliveryStatus.PENDING : EmailDeliveryStatus.NOT_REQUESTED;
        return run;
    }
    public void succeed(GeneratedReport report, EmailDeliveryStatus emailStatus, Instant finishedAt) {
        this.generatedReport = report; this.emailDeliveryStatus = emailStatus; this.finishedAt = finishedAt;
        status = emailStatus == EmailDeliveryStatus.FAILED ? ScheduleRunStatus.PARTIAL_SUCCESS : ScheduleRunStatus.SUCCEEDED;
    }
    public void partialSuccess(GeneratedReport report, EmailDeliveryStatus emailStatus, String code, String message,
                               Instant finishedAt) {
        this.generatedReport = report;
        this.emailDeliveryStatus = emailStatus;
        this.status = ScheduleRunStatus.PARTIAL_SUCCESS;
        this.errorCode = code;
        this.errorMessage = message == null ? null : message.substring(0, Math.min(500, message.length()));
        this.finishedAt = finishedAt;
    }
    public void fail(String code, String message, Instant finishedAt) {
        status = ScheduleRunStatus.FAILED; errorCode = code;
        errorMessage = message == null ? null : message.substring(0, Math.min(500, message.length()));
        this.finishedAt = finishedAt;
    }
    public Long getId() { return id; }
    public ReportSchedule getSchedule() { return schedule; }
    public GeneratedReportFormat getFormat() { return format; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public Instant getScheduledFor() { return scheduledFor; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public ScheduleRunStatus getStatus() { return status; }
    public GeneratedReport getGeneratedReport() { return generatedReport; }
    public EmailDeliveryStatus getEmailDeliveryStatus() { return emailDeliveryStatus; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
}
