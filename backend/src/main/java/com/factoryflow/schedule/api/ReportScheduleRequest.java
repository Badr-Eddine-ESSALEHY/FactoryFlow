package com.factoryflow.schedule.api;

import com.factoryflow.schedule.domain.ReportScheduleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record ReportScheduleRequest(
        @NotNull ReportScheduleType type,
        @NotNull LocalTime time,
        DayOfWeek dayOfWeek,
        @NotBlank @Pattern(regexp = "Africa/Casablanca") String timezone,
        boolean generateExcel,
        boolean generatePdf,
        boolean emailEnabled,
        @NotNull List<@Email String> recipients,
        boolean enabled
) { }
