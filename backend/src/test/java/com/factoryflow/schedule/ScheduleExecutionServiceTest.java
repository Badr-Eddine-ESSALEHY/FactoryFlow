package com.factoryflow.schedule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.factoryflow.email.application.EmailDeliveryService;
import com.factoryflow.generatedreport.api.GeneratedReportResponse;
import com.factoryflow.generatedreport.application.GeneratedReportService;
import com.factoryflow.generatedreport.domain.EmailDeliveryStatus;
import com.factoryflow.generatedreport.domain.GeneratedReportFormat;
import com.factoryflow.generatedreport.domain.GeneratedReportType;
import com.factoryflow.generatedreport.domain.GenerationOrigin;
import com.factoryflow.generatedreport.domain.GenerationStatus;
import com.factoryflow.notification.application.NotificationService;
import com.factoryflow.notification.domain.NotificationType;
import com.factoryflow.schedule.application.ScheduleExecutionService;
import com.factoryflow.schedule.application.SchedulePeriodCalculator;
import com.factoryflow.schedule.application.ScheduleRunStateService;
import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.domain.ReportScheduleType;
import com.factoryflow.schedule.persistence.ReportScheduleRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ScheduleExecutionServiceTest {

    @Test
    void partialGenerationKeepsValidFileAndSendsNoPartialEmail() {
        ReportScheduleRepository schedules = mock(ReportScheduleRepository.class);
        ScheduleRunStateService runState = mock(ScheduleRunStateService.class);
        GeneratedReportService generation = mock(GeneratedReportService.class);
        EmailDeliveryService email = mock(EmailDeliveryService.class);
        NotificationService notifications = mock(NotificationService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-22T17:00:00Z"), ZoneOffset.UTC);
        var service = new ScheduleExecutionService(
                schedules,
                new SchedulePeriodCalculator(),
                runState,
                generation,
                email,
                clock,
                notifications
        );
        ReportSchedule schedule = ReportSchedule.create(
                null,
                ReportScheduleType.DAILY,
                LocalTime.of(18, 0),
                null,
                true,
                true,
                true,
                Set.of("maintenance@example.com"),
                true
        );
        Instant scheduledFor = Instant.parse("2026-08-22T17:00:00Z");
        when(schedules.findForExecution(7L)).thenReturn(Optional.of(schedule));
        when(runState.start(eq(schedule), eq(GeneratedReportFormat.EXCEL), any(), eq(scheduledFor), any()))
                .thenReturn(Optional.of(10L));
        when(runState.start(eq(schedule), eq(GeneratedReportFormat.PDF), any(), eq(scheduledFor), any()))
                .thenReturn(Optional.of(11L));
        when(generation.generateScheduled(eq(schedule), eq(GeneratedReportFormat.EXCEL), any()))
                .thenReturn(response());
        doThrow(new IllegalStateException("PDF renderer unavailable"))
                .when(generation).generateScheduled(eq(schedule), eq(GeneratedReportFormat.PDF), any());

        service.execute(7L, scheduledFor);

        verify(email, never()).deliver(any(), any());
        verify(email).markFailedWithoutDelivery(List.of(99L));
        verify(runState).partial(
                eq(10L),
                eq(99L),
                eq(EmailDeliveryStatus.FAILED),
                eq("SCHEDULE_BATCH_INCOMPLETE"),
                any(),
                any()
        );
        verify(runState).fail(eq(11L), eq("SCHEDULE_GENERATION_FAILED"), any(), any());
        verify(notifications, times(1)).notify(
                isNull(),
                eq(NotificationType.SCHEDULE_FAILED),
                any(),
                any(),
                isNull(),
                eq(99L)
        );
    }

    private GeneratedReportResponse response() {
        LocalDate date = LocalDate.of(2026, 8, 21);
        return new GeneratedReportResponse(
                99L,
                GeneratedReportType.DAILY,
                GeneratedReportFormat.EXCEL,
                date,
                date,
                GenerationOrigin.SCHEDULED,
                GenerationStatus.READY,
                EmailDeliveryStatus.PENDING,
                1,
                Instant.parse("2026-08-22T17:00:00Z"),
                "FactoryFlow_DAILY.xlsx",
                null,
                null,
                null,
                List.of()
        );
    }
}
