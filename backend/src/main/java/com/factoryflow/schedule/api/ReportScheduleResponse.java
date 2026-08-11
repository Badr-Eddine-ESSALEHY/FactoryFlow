package com.factoryflow.schedule.api;

import com.factoryflow.schedule.domain.ReportSchedule;
import com.factoryflow.schedule.domain.ReportScheduleType;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public record ReportScheduleResponse(Long id, ReportScheduleType type, LocalTime time, DayOfWeek dayOfWeek,
                                     String timezone, boolean enabled, boolean generateExcel, boolean generatePdf,
                                     boolean emailEnabled, List<String> recipients, ZonedDateTime nextRunAt,
                                     long version) {
    public static ReportScheduleResponse from(ReportSchedule schedule, ZonedDateTime nextRunAt) {
        return new ReportScheduleResponse(schedule.getId(), schedule.getType(), schedule.getTime(),
                schedule.getDayOfWeek(), schedule.getTimezone(), schedule.isEnabled(), schedule.isGenerateExcel(),
                schedule.isGeneratePdf(), schedule.isEmailEnabled(), schedule.getRecipients().stream().sorted().toList(),
                nextRunAt, schedule.getVersion());
    }
}
