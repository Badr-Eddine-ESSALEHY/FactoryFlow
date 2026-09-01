package com.factoryflow.intelligence.application;

public record MaintenanceIntelligenceSettings(
        Integer expectedCadenceDays,
        int forecastHorizon,
        Integer seasonalPeriod
) {
    public MaintenanceIntelligenceSettings {
        if (expectedCadenceDays != null && expectedCadenceDays < 1) {
            throw new IllegalArgumentException("Expected cadence must be positive");
        }
        if (forecastHorizon < 1) {
            throw new IllegalArgumentException("Forecast horizon must be positive");
        }
        if (seasonalPeriod != null && seasonalPeriod < 2) {
            throw new IllegalArgumentException("Seasonal period must be at least two observations");
        }
    }
}
