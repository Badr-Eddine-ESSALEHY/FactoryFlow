package com.factoryflow.intelligence.domain;

import com.factoryflow.analytics.domain.TrendDirection;
import com.factoryflow.kpi.domain.KpiDefinition;
import com.factoryflow.report.domain.KpiEntry;
import com.factoryflow.report.domain.MaintenanceReport;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "maintenance_intelligence_alerts", uniqueConstraints =
        @UniqueConstraint(name = "uq_mi_alert_observation_type", columnNames = {"source_entry_id", "alert_type"}))
public class MaintenanceIntelligenceAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "kpi_definition_id") private KpiDefinition kpi;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "analysis_id") private MaintenanceIntelligenceAnalysis analysis;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "source_entry_id") private KpiEntry sourceEntry;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "source_report_id") private MaintenanceReport sourceReport;
    @Enumerated(EnumType.STRING) @Column(name = "alert_type", nullable = false, length = 50) private ContextualAlertType type;
    @Enumerated(EnumType.STRING) @Column(name = "attention_level", nullable = false, length = 20) private IntelligenceAttentionLevel attentionLevel;
    @Column(name = "observation_date", nullable = false) private LocalDate observationDate;
    @Column(name = "actual_value", nullable = false, precision = 20, scale = 6) private BigDecimal actualValue;
    @Column(nullable = false) private boolean anomalous;
    @Column(name = "anomaly_score", precision = 20, scale = 10) private BigDecimal anomalyScore;
    @Column(name = "expected_value", precision = 20, scale = 6) private BigDecimal expectedValue;
    @Column(name = "expected_lower_bound", precision = 20, scale = 6) private BigDecimal expectedLowerBound;
    @Column(name = "expected_upper_bound", precision = 20, scale = 6) private BigDecimal expectedUpperBound;
    @Column(name = "outside_expected_interval") private Boolean outsideExpectedInterval;
    @Enumerated(EnumType.STRING) @Column(name = "trend_context", nullable = false, length = 30) private TrendDirection trendContext;
    @Column(name = "forecast_direction_context", nullable = false, length = 30) private String forecastDirectionContext;
    @Column(name = "selected_model_family", length = 40) private String selectedModelFamily;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Enumerated(EnumType.STRING) @Column(name = "notification_status", nullable = false, length = 20) private AlertNotificationStatus notificationStatus;
    @Column(name = "notification_failure", length = 500) private String notificationFailure;
    protected MaintenanceIntelligenceAlert() { }

    public static MaintenanceIntelligenceAlert create(KpiDefinition kpi, MaintenanceIntelligenceAnalysis analysis,
            KpiEntry entry, MaintenanceReport report, ContextualAlertType type, IntelligenceAttentionLevel level,
            LocalDate date, BigDecimal actual, boolean anomalous, BigDecimal score, BigDecimal expected,
            BigDecimal lower, BigDecimal upper, Boolean outside, TrendDirection trend, String forecastDirection,
            String model, Instant now) {
        MaintenanceIntelligenceAlert value = new MaintenanceIntelligenceAlert(); value.kpi = kpi; value.analysis = analysis;
        value.sourceEntry = entry; value.sourceReport = report; value.type = type; value.attentionLevel = level;
        value.observationDate = date; value.actualValue = actual; value.anomalous = anomalous; value.anomalyScore = score;
        value.expectedValue = expected; value.expectedLowerBound = lower; value.expectedUpperBound = upper;
        value.outsideExpectedInterval = outside; value.trendContext = trend; value.forecastDirectionContext = forecastDirection;
        value.selectedModelFamily = model; value.createdAt = now; value.updatedAt = now; value.notificationStatus = AlertNotificationStatus.PENDING;
        return value;
    }
    public void updateEvidence(MaintenanceIntelligenceAnalysis analysis, boolean anomalous, BigDecimal score,
            BigDecimal expected, BigDecimal lower, BigDecimal upper, Boolean outside, TrendDirection trend,
            String forecastDirection, String model, Instant now) {
        this.analysis = analysis; this.anomalous = anomalous; this.anomalyScore = score; this.expectedValue = expected;
        this.expectedLowerBound = lower; this.expectedUpperBound = upper; this.outsideExpectedInterval = outside;
        this.trendContext = trend; this.forecastDirectionContext = forecastDirection; this.selectedModelFamily = model; this.updatedAt = now;
    }
    public void notificationSent() { notificationStatus = AlertNotificationStatus.SENT; notificationFailure = null; updatedAt = Instant.now(); }
    public void notificationFailed(String message) { notificationStatus = AlertNotificationStatus.FAILED; notificationFailure = message == null ? null : message.substring(0, Math.min(500, message.length())); updatedAt = Instant.now(); }
    public Long getId() { return id; } public KpiDefinition getKpi() { return kpi; } public MaintenanceIntelligenceAnalysis getAnalysis() { return analysis; }
    public KpiEntry getSourceEntry() { return sourceEntry; } public MaintenanceReport getSourceReport() { return sourceReport; }
    public ContextualAlertType getType() { return type; } public IntelligenceAttentionLevel getAttentionLevel() { return attentionLevel; }
    public LocalDate getObservationDate() { return observationDate; } public BigDecimal getActualValue() { return actualValue; }
    public boolean isAnomalous() { return anomalous; } public BigDecimal getAnomalyScore() { return anomalyScore; }
    public BigDecimal getExpectedValue() { return expectedValue; } public BigDecimal getExpectedLowerBound() { return expectedLowerBound; }
    public BigDecimal getExpectedUpperBound() { return expectedUpperBound; } public Boolean getOutsideExpectedInterval() { return outsideExpectedInterval; }
    public TrendDirection getTrendContext() { return trendContext; } public String getForecastDirectionContext() { return forecastDirectionContext; }
    public String getSelectedModelFamily() { return selectedModelFamily; } public Instant getCreatedAt() { return createdAt; } public Instant getUpdatedAt() { return updatedAt; }
    public AlertNotificationStatus getNotificationStatus() { return notificationStatus; } public String getNotificationFailure() { return notificationFailure; }
}
