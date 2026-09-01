package com.factoryflow.intelligence.api;
import jakarta.validation.constraints.*;
public record IntelligenceProfileRequest(
        @NotNull Boolean enabled,
        @Min(1) @Max(365) Integer expectedCadenceDays,
        @Min(1) @Max(30) int forecastHorizon,
        @Min(2) @Max(365) Integer seasonalPeriod,
        @Min(30) @Max(3650) int historyWindowDays,
        @PositiveOrZero long version
) { }
