package com.factoryflow.schedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.factoryflow.auth.domain.UserAccount;
import com.factoryflow.auth.persistence.UserAccountRepository;
import com.factoryflow.email.application.ScheduledReportEmailSender;
import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GenerationStatus;
import com.factoryflow.generatedreport.persistence.GeneratedReportRepository;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.kpi.persistence.KpiDefinitionRepository;
import com.factoryflow.notification.domain.NotificationType;
import com.factoryflow.notification.persistence.UserNotificationRepository;
import com.factoryflow.report.domain.AcquisitionSource;
import com.factoryflow.report.domain.MaintenanceReport;
import com.factoryflow.report.persistence.MaintenanceReportRepository;
import com.factoryflow.schedule.application.ScheduleExecutionService;
import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.domain.ReportScheduleType;
import com.factoryflow.schedule.domain.ScheduleRunStatus;
import com.factoryflow.schedule.persistence.ReportScheduleRepository;
import com.factoryflow.schedule.persistence.ScheduleRunRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ScheduleExecutionIntegrationTest {
    @Autowired ScheduleExecutionService execution; @Autowired ReportScheduleRepository schedules;
    @Autowired ScheduleRunRepository runs; @Autowired GeneratedReportRepository generated;
    @Autowired UserAccountRepository users; @Autowired PasswordEncoder passwords;
    @Autowired KpiDefinitionRepository definitions; @Autowired MaintenanceReportRepository reports;
    @Autowired UserNotificationRepository notifications;
    @Autowired EntityManager entityManager;
    @MockitoBean ScheduledReportEmailSender emailSender;
    private final List<Long> scheduleIds = new ArrayList<>();
    private MaintenanceReport source; private KpiDefinition kpi; private UserAccount user;

    @Test
    void persistedSchedulesGenerateOnceAndKeepReadyFilesWhenEmailFails() {
        LocalDate executionDate = LocalDate.of(2026, 8, 12);
        setupSource(executionDate.minusDays(1));

        ReportSchedule delivered = saveSchedule(true, true, true, true);
        entityManager.clear();
        assertThat(schedules.findById(delivered.getId())).isPresent().get()
                .extracting(ReportSchedule::getTimezone).isEqualTo("Africa/Casablanca");
        Instant fire = executionDate.atTime(18, 0).atZone(ZoneId.of("Africa/Casablanca")).toInstant();
        execution.execute(delivered.getId(), fire);
        execution.execute(delivered.getId(), fire);
        var deliveredRuns = runs.findAllByScheduleId(delivered.getId());
        assertThat(deliveredRuns).hasSize(2);
        assertThat(deliveredRuns).allSatisfy(run -> {
            assertThat(run.getStatus()).isEqualTo(ScheduleRunStatus.SUCCEEDED);
            assertThat(run.getEmailDeliveryStatus()).isEqualTo(EmailDeliveryStatus.DELIVERED);
        });
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<com.factoryflow.generatedreport.domain.GeneratedReport>> deliveredAttachments =
                ArgumentCaptor.forClass(List.class);
        verify(emailSender).send(deliveredAttachments.capture(), any());
        assertThat(deliveredAttachments.getValue()).hasSize(2)
                .extracting(com.factoryflow.generatedreport.domain.GeneratedReport::getFormat)
                .containsExactlyInAnyOrder(
                        com.factoryflow.generatedreport.domain.GeneratedReportFormat.EXCEL,
                        com.factoryflow.generatedreport.domain.GeneratedReportFormat.PDF);

        doThrow(new IllegalStateException("simulated SMTP failure")).when(emailSender).send(any(), any());
        ReportSchedule failedEmail = saveSchedule(true, true, true, true);
        execution.execute(failedEmail.getId(), fire);
        var failedRuns = runs.findAllByScheduleId(failedEmail.getId());
        assertThat(failedRuns).hasSize(2).allSatisfy(failedRun -> {
            assertThat(failedRun.getStatus()).isEqualTo(ScheduleRunStatus.PARTIAL_SUCCESS);
            assertThat(failedRun.getEmailDeliveryStatus()).isEqualTo(EmailDeliveryStatus.FAILED);
            assertThat(generated.findById(failedRun.getGeneratedReport().getId())).isPresent().get()
                    .satisfies(report -> {
                        assertThat(report.getGenerationStatus()).isEqualTo(GenerationStatus.READY);
                        assertThat(report.getEmailDeliveryStatus()).isEqualTo(EmailDeliveryStatus.FAILED);
                        assertThat(report.getFileName()).contains("schedule-" + failedEmail.getId());
                    });
        });
        verify(emailSender, times(2)).send(any(), any());
        assertThat(notifications.findByUserIdOrderByCreatedAtDesc(
                user.getId(), org.springframework.data.domain.Pageable.unpaged()))
                .filteredOn(notification -> notification.getType() == NotificationType.EMAIL_FAILED)
                .hasSize(1);
        String deliveredFile = generated.findById(deliveredRuns.getFirst().getGeneratedReport().getId())
                .orElseThrow().getFileName();
        String failedFile = generated.findById(failedRuns.getFirst().getGeneratedReport().getId())
                .orElseThrow().getFileName();
        assertThat(deliveredFile).isNotEqualTo(failedFile);

        ReportSchedule disabled = saveSchedule(false, false, true, false);
        execution.execute(disabled.getId(), fire);
        assertThat(runs.findAllByScheduleId(disabled.getId())).isEmpty();
    }

    private ReportSchedule saveSchedule(boolean enabled, boolean email, boolean excel, boolean pdf) {
        ReportSchedule schedule = schedules.saveAndFlush(ReportSchedule.create(user, ReportScheduleType.DAILY,
                LocalTime.of(18, 0), null, excel, pdf, email,
                email ? Set.of("reports@example.com") : Set.of(), enabled));
        scheduleIds.add(schedule.getId()); return schedule;
    }
    private void setupSource(LocalDate date) {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        user = users.saveAndFlush(UserAccount.create("Schedule Engineer", "schedule-" + suffix + "@example.com",
                passwords.encode("unused-password")));
        kpi = definitions.saveAndFlush(KpiDefinition.create("SCHEDULE_" + suffix.substring(0, 10),
                "Schedule test KPI", "Test", "bar", null, null, true, List.of()));
        source = MaintenanceReport.draft(user, date, AcquisitionSource.MANUAL, "Schedule test");
        source.addEntry(kpi, "Schedule KPI", "Schedule KPI: 7.5", new BigDecimal("7.5"), new BigDecimal("7.5"),
                BigDecimal.ONE, false, "bar", Set.of());
        source.getEntries().getFirst().confirm(new BigDecimal("7.5")); source.confirm();
        source = reports.saveAndFlush(source);
    }

    @AfterEach
    void cleanup() {
        for (Long scheduleId : scheduleIds) {
            runs.deleteAllByScheduleId(scheduleId);
            generated.deleteAll(generated.findAllByScheduleId(scheduleId));
            schedules.deleteById(scheduleId);
        }
        if (source != null) reports.deleteById(source.getId());
        if (kpi != null) definitions.deleteById(kpi.getId());
        if (user != null) notifications.deleteAll(notifications.findByUserIdOrderByCreatedAtDesc(
                user.getId(), org.springframework.data.domain.Pageable.unpaged()));
        if (user != null) users.deleteById(user.getId());
    }
}
