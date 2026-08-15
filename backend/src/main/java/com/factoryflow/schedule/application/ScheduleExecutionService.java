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
import java.util.List;
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
            notifySafely(schedule, NotificationType.SCHEDULED_DOCUMENT_READY,
                    "Document planifié prêt", "Le document " + format + " a été généré pour la période "
                            + period.start() + " – " + period.end() + ".", null, report.id());
            if (emailStatus == EmailDeliveryStatus.FAILED) {
                notifySafely(schedule, NotificationType.EMAIL_FAILED,
                        "Envoi e-mail échoué", "Le document a été conservé mais son envoi par e-mail a échoué.", null, report.id());
            }
        } catch (RuntimeException exception) {
            runState.fail(runId, "SCHEDULE_GENERATION_FAILED", exception.getMessage(), clock.instant());
            notifySafely(schedule, NotificationType.SCHEDULE_FAILED,
                    "Génération planifiée échouée", "Aucun document corrompu n’a été publié. Consultez l’historique d’exécution.", null, null);
        }
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
}
