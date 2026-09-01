package com.factoryflow.intelligence.application;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("factoryflow.intelligence")
public record MaintenanceIntelligenceProperties(
        @NotNull String runtimeUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration requestTimeout,
        @Min(3) int anomalyMinimumHistory,
        @Min(10) int anomalyEstimators,
        int anomalyRandomState,
        @Min(2) int anomalyRollingWindow,
        @Min(3) int forecastMinimumHistory,
        @Min(1) int forecastHorizon,
        @Min(2) int backtestMinimumTraining,
        @Min(1) int backtestMinimumFolds,
        @Min(1) int backtestMaximumFolds,
        @Min(2) int seasonalPeriod,
        @Min(2) int seasonalMinimumCycles,
        @DecimalMin("0.5") @DecimalMax("0.999") double intervalConfidence
) {
}
