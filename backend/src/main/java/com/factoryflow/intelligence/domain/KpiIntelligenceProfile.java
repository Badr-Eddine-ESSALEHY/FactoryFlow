package com.factoryflow.intelligence.domain;

import com.factoryflow.kpi.domain.KpiDefinition;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "kpi_intelligence_profiles", uniqueConstraints =
        @UniqueConstraint(name = "uq_mi_profile_kpi", columnNames = "kpi_definition_id"))
public class KpiIntelligenceProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "kpi_definition_id", nullable = false) private KpiDefinition kpi;
    @Column(nullable = false) private boolean enabled;
    @Column(name = "expected_cadence_days") private Integer expectedCadenceDays;
    @Column(name = "forecast_horizon", nullable = false) private int forecastHorizon;
    @Column(name = "seasonal_period") private Integer seasonalPeriod;
    @Column(name = "history_window_days", nullable = false) private int historyWindowDays;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;

    protected KpiIntelligenceProfile() { }

    public static KpiIntelligenceProfile defaults(KpiDefinition kpi) {
        KpiIntelligenceProfile profile = new KpiIntelligenceProfile();
        profile.kpi = java.util.Objects.requireNonNull(kpi);
        profile.enabled = true;
        profile.forecastHorizon = 7;
        profile.historyWindowDays = 365;
        return profile;
    }

    public void update(boolean enabled, Integer expectedCadenceDays, int forecastHorizon,
                       Integer seasonalPeriod, int historyWindowDays) {
        validate(expectedCadenceDays, forecastHorizon, seasonalPeriod, historyWindowDays);
        this.enabled = enabled;
        this.expectedCadenceDays = expectedCadenceDays;
        this.forecastHorizon = forecastHorizon;
        this.seasonalPeriod = seasonalPeriod;
        this.historyWindowDays = historyWindowDays;
    }

    private static void validate(Integer cadence, int horizon, Integer seasonality, int historyDays) {
        if (cadence != null && (cadence < 1 || cadence > 365)) throw new IllegalArgumentException("Expected cadence must be between 1 and 365 days");
        if (horizon < 1 || horizon > 30) throw new IllegalArgumentException("Forecast horizon must be between 1 and 30");
        if (seasonality != null && (seasonality < 2 || seasonality > 365)) throw new IllegalArgumentException("Seasonal period must be between 2 and 365 observations");
        if (historyDays < 30 || historyDays > 3650) throw new IllegalArgumentException("History window must be between 30 and 3650 days");
    }

    @PrePersist void initializeTimestamps() { validate(expectedCadenceDays, forecastHorizon, seasonalPeriod, historyWindowDays); Instant now = Instant.now(); createdAt = now; updatedAt = now; }
    @PreUpdate void updateTimestamp() { validate(expectedCadenceDays, forecastHorizon, seasonalPeriod, historyWindowDays); updatedAt = Instant.now(); }
    public Long getId() { return id; } public KpiDefinition getKpi() { return kpi; }
    public boolean isEnabled() { return enabled; } public Integer getExpectedCadenceDays() { return expectedCadenceDays; }
    public int getForecastHorizon() { return forecastHorizon; } public Integer getSeasonalPeriod() { return seasonalPeriod; }
    public int getHistoryWindowDays() { return historyWindowDays; } public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; } public long getVersion() { return version; }
}
