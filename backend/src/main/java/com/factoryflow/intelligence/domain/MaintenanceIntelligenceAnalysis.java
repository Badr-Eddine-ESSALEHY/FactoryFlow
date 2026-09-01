package com.factoryflow.intelligence.domain;

import com.fasterxml.jackson.databind.JsonNode;
import com.factoryflow.analytics.domain.TrendDirection;
import com.factoryflow.kpi.domain.KpiDefinition;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "maintenance_intelligence_analyses")
public class MaintenanceIntelligenceAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "analysis_id", nullable = false, unique = true, length = 64) private String analysisId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "kpi_definition_id") private KpiDefinition kpi;
    @Column(name = "profile_version", nullable = false) private long profileVersion;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "profile_snapshot", nullable = false, columnDefinition = "jsonb") private JsonNode profileSnapshot;
    @Column(name = "snapshot_schema_version", nullable = false) private int snapshotSchemaVersion;
    @Column(name = "window_start", nullable = false) private LocalDate windowStart;
    @Column(name = "window_end", nullable = false) private LocalDate windowEnd;
    @Column(name = "generated_at", nullable = false) private Instant generatedAt;
    @Column(name = "completed_at", nullable = false) private Instant completedAt;
    @Column(name = "duration_millis", nullable = false) private long durationMillis;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private IntelligenceAnalysisStatus status;
    @Column(name = "technical_failure_code", length = 80) private String technicalFailureCode;
    @Column(name = "technical_failure_message", length = 500) private String technicalFailureMessage;
    @Column(name = "usable_observation_count", nullable = false) private int usableObservationCount;
    @Column(name = "missing_observation_count", nullable = false) private int missingObservationCount;
    @Column(name = "cadence_state", length = 30) private String cadenceState;
    @Column(name = "cadence_basis", length = 40) private String cadenceBasis;
    @Column(name = "cadence_ambiguity", length = 60) private String cadenceAmbiguity;
    @Enumerated(EnumType.STRING) @Column(name = "trend_direction", length = 30) private TrendDirection trendDirection;
    @Column(name = "anomaly_state", length = 30) private String anomalyState;
    @Column(name = "anomaly_reason", length = 100) private String anomalyReason;
    @Column(name = "latest_anomalous") private Boolean latestAnomalous;
    @Column(name = "latest_anomaly_score", precision = 20, scale = 10) private BigDecimal latestAnomalyScore;
    @Column(name = "forecast_state", length = 30) private String forecastState;
    @Column(name = "forecast_reason", length = 100) private String forecastReason;
    @Column(name = "selected_model_family", length = 40) private String selectedModelFamily;
    @Column(name = "forecast_direction", length = 30) private String forecastDirection;
    @Column(name = "forecast_horizon") private Integer forecastHorizon;
    @Column(name = "forecast_mae", precision = 20, scale = 10) private BigDecimal forecastMae;
    @Column(name = "forecast_rmse", precision = 20, scale = 10) private BigDecimal forecastRmse;
    @Column(name = "forecast_smape", precision = 20, scale = 10) private BigDecimal forecastSmape;
    @Column(name = "latest_entry_id") private Long latestEntryId;
    @Column(name = "latest_report_id") private Long latestReportId;
    @Column(name = "latest_effective_date") private LocalDate latestEffectiveDate;
    @Column(name = "latest_actual_value", precision = 20, scale = 6) private BigDecimal latestActualValue;
    @Column(name = "expectation_state", length = 30) private String expectationState;
    @Column(name = "expectation_reason", length = 100) private String expectationReason;
    @Column(name = "expected_value", precision = 20, scale = 6) private BigDecimal expectedValue;
    @Column(name = "expected_lower_bound", precision = 20, scale = 6) private BigDecimal expectedLowerBound;
    @Column(name = "expected_upper_bound", precision = 20, scale = 6) private BigDecimal expectedUpperBound;
    @Column(name = "outside_expected_interval") private Boolean outsideExpectedInterval;
    @JdbcTypeCode(SqlTypes.JSON) @Column(name = "result_snapshot", columnDefinition = "jsonb") private JsonNode resultSnapshot;
    @Enumerated(EnumType.STRING) @Column(name = "contextualization_status", nullable = false, length = 30) private ContextualizationStatus contextualizationStatus;
    @Column(name = "contextualization_failure", length = 500) private String contextualizationFailure;
    protected MaintenanceIntelligenceAnalysis() { }

    public static MaintenanceIntelligenceAnalysis successful(KpiDefinition kpi, long profileVersion, JsonNode profileSnapshot,
            LocalDate start, LocalDate end, MaintenanceIntelligenceResult result, JsonNode snapshot,
            Instant completedAt, long durationMillis) {
        MaintenanceIntelligenceAnalysis value = base(kpi, profileVersion, profileSnapshot, start, end, completedAt, durationMillis);
        value.analysisId = java.util.UUID.randomUUID().toString(); value.generatedAt = result.generatedAt();
        value.status = result.anomaly().state() == MaintenanceIntelligenceResult.AnalysisState.COMPLETED
                || result.forecast().state() == MaintenanceIntelligenceResult.AnalysisState.COMPLETED
                ? IntelligenceAnalysisStatus.COMPLETED : IntelligenceAnalysisStatus.INSUFFICIENT_DATA;
        value.usableObservationCount = result.preparation().usableObservationCount();
        value.missingObservationCount = result.preparation().missingValueCount();
        value.cadenceState = result.preparation().cadence().state().name(); value.cadenceBasis = result.preparation().cadence().cadenceBasis().name();
        value.cadenceAmbiguity = result.preparation().cadence().ambiguity().name(); value.trendDirection = result.trend().direction();
        value.anomalyState = result.anomaly().state().name(); value.anomalyReason = result.anomaly().insufficientReason();
        value.forecastState = result.forecast().state().name(); value.forecastReason = result.forecast().insufficientReason();
        value.selectedModelFamily = result.forecast().selectedModelFamily(); value.forecastDirection = result.forecast().forecastDirection(); value.forecastHorizon = result.forecast().requestedHorizon();
        if (result.forecast().selectedMetrics() != null) { value.forecastMae = result.forecast().selectedMetrics().mae(); value.forecastRmse = result.forecast().selectedMetrics().rmse(); value.forecastSmape = result.forecast().selectedMetrics().smape(); }
        var expectation = result.latestObservationExpectation();
        value.latestEntryId = expectation.entryId(); value.latestReportId = expectation.reportId(); value.latestEffectiveDate = expectation.effectiveDate(); value.latestActualValue = expectation.actualValue();
        value.expectationState = expectation.state().name(); value.expectationReason = expectation.insufficientReason();
        value.expectedValue = expectation.expectedValue(); value.expectedLowerBound = expectation.lowerBound(); value.expectedUpperBound = expectation.upperBound(); value.outsideExpectedInterval = expectation.outsideInterval();
        if (expectation.entryId() != null) result.anomaly().points().stream().filter(p -> expectation.entryId().equals(p.entryId())).findFirst().ifPresent(p -> { value.latestAnomalous = p.anomalous(); value.latestAnomalyScore = p.anomalyScore(); });
        value.resultSnapshot = snapshot; value.contextualizationStatus = ContextualizationStatus.PENDING;
        return value;
    }

    public static MaintenanceIntelligenceAnalysis failure(KpiDefinition kpi, long profileVersion, JsonNode profileSnapshot,
            LocalDate start, LocalDate end, Instant at, long durationMillis, String code, String message) {
        MaintenanceIntelligenceAnalysis value = base(kpi, profileVersion, profileSnapshot, start, end, at, durationMillis);
        value.analysisId = java.util.UUID.randomUUID().toString(); value.generatedAt = at; value.status = IntelligenceAnalysisStatus.TECHNICAL_FAILURE;
        value.technicalFailureCode = code; value.technicalFailureMessage = trim(message); value.contextualizationStatus = ContextualizationStatus.NOT_APPLICABLE;
        return value;
    }
    private static MaintenanceIntelligenceAnalysis base(KpiDefinition kpi, long version, JsonNode profile, LocalDate start, LocalDate end, Instant at, long duration) {
        MaintenanceIntelligenceAnalysis value = new MaintenanceIntelligenceAnalysis(); value.kpi = kpi; value.profileVersion = version; value.profileSnapshot = profile;
        value.snapshotSchemaVersion = 1; value.windowStart = start; value.windowEnd = end; value.completedAt = at; value.durationMillis = Math.max(0, duration); return value;
    }
    private static String trim(String value) { return value == null ? null : value.substring(0, Math.min(value.length(), 500)); }
    public void contextualizationCompleted() { contextualizationStatus = ContextualizationStatus.COMPLETED; contextualizationFailure = null; }
    public void contextualizationFailed(String message) { contextualizationStatus = ContextualizationStatus.FAILED; contextualizationFailure = trim(message); }
    public Long getId() { return id; } public String getAnalysisId() { return analysisId; } public KpiDefinition getKpi() { return kpi; }
    public long getProfileVersion() { return profileVersion; } public JsonNode getProfileSnapshot() { return profileSnapshot; }
    public LocalDate getWindowStart() { return windowStart; } public LocalDate getWindowEnd() { return windowEnd; }
    public Instant getGeneratedAt() { return generatedAt; } public Instant getCompletedAt() { return completedAt; } public long getDurationMillis() { return durationMillis; }
    public IntelligenceAnalysisStatus getStatus() { return status; } public String getTechnicalFailureCode() { return technicalFailureCode; } public String getTechnicalFailureMessage() { return technicalFailureMessage; }
    public int getUsableObservationCount() { return usableObservationCount; } public int getMissingObservationCount() { return missingObservationCount; }
    public String getCadenceState() { return cadenceState; } public String getCadenceBasis() { return cadenceBasis; } public String getCadenceAmbiguity() { return cadenceAmbiguity; }
    public TrendDirection getTrendDirection() { return trendDirection; } public String getAnomalyState() { return anomalyState; } public String getAnomalyReason() { return anomalyReason; }
    public Boolean getLatestAnomalous() { return latestAnomalous; } public BigDecimal getLatestAnomalyScore() { return latestAnomalyScore; }
    public String getForecastState() { return forecastState; } public String getForecastReason() { return forecastReason; } public String getSelectedModelFamily() { return selectedModelFamily; }
    public String getForecastDirection() { return forecastDirection; }
    public Integer getForecastHorizon() { return forecastHorizon; } public BigDecimal getForecastMae() { return forecastMae; } public BigDecimal getForecastRmse() { return forecastRmse; } public BigDecimal getForecastSmape() { return forecastSmape; }
    public Long getLatestEntryId() { return latestEntryId; } public Long getLatestReportId() { return latestReportId; } public LocalDate getLatestEffectiveDate() { return latestEffectiveDate; }
    public BigDecimal getLatestActualValue() { return latestActualValue; } public String getExpectationState() { return expectationState; } public String getExpectationReason() { return expectationReason; }
    public BigDecimal getExpectedValue() { return expectedValue; } public BigDecimal getExpectedLowerBound() { return expectedLowerBound; } public BigDecimal getExpectedUpperBound() { return expectedUpperBound; }
    public Boolean getOutsideExpectedInterval() { return outsideExpectedInterval; } public JsonNode getResultSnapshot() { return resultSnapshot; }
    public ContextualizationStatus getContextualizationStatus() { return contextualizationStatus; } public String getContextualizationFailure() { return contextualizationFailure; }
}
