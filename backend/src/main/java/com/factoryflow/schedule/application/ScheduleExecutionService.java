package com.factoryflow.schedule.application;

import com.factoryflow.email.application.EmailDeliveryService;
import com.factoryflow.generatedreport.api.GeneratedReportResponse;
import com.factoryflow.generatedreport.application.GeneratedReportService;
import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.ReportPeriod;
import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.persistence.ReportScheduleRepository;
import com.factoryflow.notification.application.NotificationService;
import com.factoryflow.notification.domain.NotificationType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduleExecutionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleExecutionService.class);
    private static final ZoneId BUSINESS_ZONE = ZoneId.of(ReportSchedule.BUSINESS_TIMEZONE);
    private final ReportScheduleRepository schedules; private final SchedulePeriodCalculator periods;
    private final ScheduleRunStateService runState; private final GeneratedReportService generation;
    private final EmailDeliveryService email; private final Clock clock;
    private final NotificationService notifications;
    public ScheduleExecutionService(ReportScheduleRepository schedules, SchedulePeriodCalculator periods,
                                    ScheduleRunStateService runState, GeneratedReportService generation,
                                    EmailDeliveryService email, Clock clock, NotificationService notifications) {
        this.schedules = schedules; this.periods = periods; this.runState = runState;
        this.generation = generation; this.email = email; this.clock = clock;
        this.notifications = notifications;
    }

    public void execute(Long scheduleId, Instant scheduledFor) {
        ReportSchedule schedule = schedules.findForExecution(scheduleId).orElse(null);
        if (schedule == null || !schedule.isEnabled()) return;
        ReportPeriod period = periods.calculate(schedule.getType(), scheduledFor.atZone(BUSINESS_ZONE).toLocalDate());
        Map<GeneratedReportFormat, Long> startedRuns = startRuns(schedule, period, scheduledFor);
        if (startedRuns.isEmpty()) return;

        List<GeneratedDocument> documents = new ArrayList<>(startedRuns.size());
        boolean generationFailed = false;
        for (Map.Entry<GeneratedReportFormat, Long> run : startedRuns.entrySet()) {
            try {
                GeneratedReportResponse report = generation.generateScheduled(schedule, run.getKey(), period);
                documents.add(new GeneratedDocument(run.getValue(), report));
            } catch (RuntimeException exception) {
                generationFailed = true;
                runState.fail(run.getValue(), "SCHEDULE_GENERATION_FAILED", exception.getMessage(), clock.instant());
            }
        }

        if (generationFailed) {
            markIncompleteBatch(schedule, documents);
            notifySafely(schedule, NotificationType.SCHEDULE_FAILED,
                    "Génération planifiée incomplète",
                    "Au moins un format n’a pas pu être généré. Les documents valides ont été conservés et aucun e-mail incomplet n’a été envoyé.",
                    null, documents.isEmpty() ? null : documents.getFirst().report().id());
            return;
        }

        List<Long> reportIds = documents.stream().map(document -> document.report().id()).toList();
        EmailDeliveryStatus emailStatus = schedule.isEmailEnabled()
                ? email.deliver(reportIds, schedule.getRecipients())
                : EmailDeliveryStatus.NOT_REQUESTED;
        Instant finishedAt = clock.instant();
        documents.forEach(document -> runState.succeed(
                document.runId(), document.report().id(), emailStatus, finishedAt));

        Long relatedGeneratedReportId = documents.getFirst().report().id();
        if (emailStatus == EmailDeliveryStatus.FAILED) {
            notifySafely(schedule, NotificationType.EMAIL_FAILED,
                    "Envoi e-mail échoué",
                    "Les documents planifiés sont prêts et téléchargeables, mais leur envoi groupé par e-mail a échoué.",
                    null, relatedGeneratedReportId);
        } else {
            notifySafely(schedule, NotificationType.SCHEDULED_DOCUMENT_READY,
                    documents.size() == 1 ? "Document planifié prêt" : "Documents planifiés prêts",
                    "La génération planifiée est terminée pour la période " + period.start() + " – " + period.end() + ".",
                    null, relatedGeneratedReportId);
        }
    }

    private Map<GeneratedReportFormat, Long> startRuns(ReportSchedule schedule, ReportPeriod period,
                                                       Instant scheduledFor) {
        Map<GeneratedReportFormat, Long> started = new LinkedHashMap<>();
        for (GeneratedReportFormat format : formats(schedule)) {
            try {
                runState.start(schedule, format, period, scheduledFor, clock.instant())
                        .ifPresent(runId -> started.put(format, runId));
            } catch (DataIntegrityViolationException duplicate) {
                // The database uniqueness constraint is the final duplicate-execution guard.
            }
        }
        return started;
    }

    private void markIncompleteBatch(ReportSchedule schedule, List<GeneratedDocument> documents) {
        if (documents.isEmpty()) return;
        EmailDeliveryStatus emailStatus = schedule.isEmailEnabled()
                ? EmailDeliveryStatus.FAILED : EmailDeliveryStatus.NOT_REQUESTED;
        List<Long> reportIds = documents.stream().map(document -> document.report().id()).toList();
        if (schedule.isEmailEnabled()) email.markFailedWithoutDelivery(reportIds);
        Instant finishedAt = clock.instant();
        documents.forEach(document -> runState.partial(
                document.runId(), document.report().id(), emailStatus, "SCHEDULE_BATCH_INCOMPLETE",
                "Another requested format failed; no partial batch email was sent.", finishedAt));
    }

    private void notifySafely(ReportSchedule schedule, NotificationType type, String title, String message,
                              Long reportId, Long generatedId) {
        try {
            notifications.notify(schedule.getCreatedBy(), type, title, message, reportId, generatedId);
        } catch (RuntimeException exception) {
            LOGGER.warn("In-app notification persistence failed scheduleId={} type={}", schedule.getId(), type, exception);
        }
    }

    private List<GeneratedReportFormat> formats(ReportSchedule schedule) {
        List<GeneratedReportFormat> formats = new ArrayList<>(2);
        if (schedule.isGenerateExcel()) formats.add(GeneratedReportFormat.EXCEL);
        if (schedule.isGeneratePdf()) formats.add(GeneratedReportFormat.PDF);
        return formats;
    }

    private record GeneratedDocument(Long runId, GeneratedReportResponse report) { }
}
