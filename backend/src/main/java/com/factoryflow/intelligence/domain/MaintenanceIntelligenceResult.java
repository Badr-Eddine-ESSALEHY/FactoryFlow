package com.factoryflow.intelligence.domain;

import com.factoryflow.analytics.domain.TrendDirection;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record MaintenanceIntelligenceResult(
        KpiIdentity kpi,
        LocalDate windowStart,
        LocalDate windowEnd,
        Instant generatedAt,
        PreparationSummary preparation,
        List<PreparedKpiSeries.Observation> historicalObservations,
        TrendAnalysis trend,
        AnomalyAnalysis anomaly,
        ForecastAnalysis forecast,
        LatestObservationExpectation latestObservationExpectation
) {
    public MaintenanceIntelligenceResult {
        historicalObservations = List.copyOf(historicalObservations);
    }

    public record PreparationSummary(
            int sourceRecordCount,
            int usableObservationCount,
            int missingValueCount,
            PreparedKpiSeries.CadenceMetadata cadence
    ) {
    }

    public record TrendAnalysis(
            TrendDirection direction,
            BigDecimal slopePerObservation,
            BigDecimal absoluteChange,
            BigDecimal percentageChange,
            int observationCount
    ) {
    }

    public record AnomalyAnalysis(
            AnalysisState state,
            String insufficientReason,
            String algorithm,
            List<String> featureNames,
            int trainingObservationCount,
            BigDecimal anomalyThreshold,
            AnomalyScoreSemantics scoreSemantics,
            List<AnomalyPoint> points
    ) {
        public AnomalyAnalysis {
            featureNames = List.copyOf(featureNames);
            points = List.copyOf(points);
        }
    }

    public record AnomalyPoint(
            Long entryId,
            Long reportId,
            LocalDate effectiveDate,
            Instant confirmedAt,
            BigDecimal value,
            BigDecimal anomalyScore,
            BigDecimal decisionFunction,
            Boolean anomalous
    ) {
    }

    public record AnomalyScoreSemantics(
            String kind,
            String orientation,
            String scope,
            boolean probability,
            boolean severity,
            boolean crossModelComparable
    ) {
    }

    public record ForecastAnalysis(
            AnalysisState state,
            String insufficientReason,
            String selectedModelFamily,
            Map<String, Object> selectedModelConfiguration,
            int trainingObservationCount,
            int requestedHorizon,
            List<Integer> effectiveEvaluatedHorizons,
            int rollingOriginCount,
            Instant generatedAt,
            List<ForecastPoint> points,
            ForecastMetrics selectedMetrics,
            List<CandidateEvaluation> candidates,
            ModelSelectionDecision modelSelection,
            ModelFitDiagnostics selectedModelDiagnostics,
            String selectionReason,
            String forecastDirection,
            BigDecimal intervalConfidence
    ) {
        public ForecastAnalysis {
            selectedModelConfiguration = selectedModelConfiguration == null
                    ? Map.of() : Map.copyOf(selectedModelConfiguration);
            effectiveEvaluatedHorizons = List.copyOf(effectiveEvaluatedHorizons);
            points = List.copyOf(points);
            candidates = List.copyOf(candidates);
        }
    }

    public record ForecastPoint(
            LocalDate effectiveDate,
            BigDecimal value,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            boolean intervalAvailable
    ) {
    }

    public record ForecastMetrics(
            BigDecimal mae,
            BigDecimal rmse,
            BigDecimal smape,
            BigDecimal nonSeasonalMase,
            BigDecimal seasonalMase
    ) {
    }

    public record HorizonEvaluation(
            int horizonStep,
            int observationCount,
            ForecastMetrics metrics
    ) {
    }

    public record CandidateEvaluation(
            String family,
            Map<String, Object> configuration,
            CandidateState state,
            String reason,
            int rollingOriginCount,
            List<Integer> effectiveEvaluatedHorizons,
            ForecastMetrics metrics,
            List<HorizonEvaluation> perHorizonMetrics,
            BigDecimal primaryMetricStandardError,
            ModelFitDiagnostics diagnostics
    ) {
        public CandidateEvaluation {
            configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
            effectiveEvaluatedHorizons = List.copyOf(effectiveEvaluatedHorizons);
            perHorizonMetrics = List.copyOf(perHorizonMetrics);
        }
    }

    public record ModelReference(
            String family,
            Map<String, Object> configuration,
            ForecastMetrics metrics
    ) {
        public ModelReference {
            configuration = configuration == null ? Map.of() : Map.copyOf(configuration);
        }
    }

    public record ModelSelectionDecision(
            ModelReference rawBest,
            ModelReference parsimoniousChoice,
            ModelReference selected,
            String primaryMetric,
            BigDecimal rawBestStandardError,
            BigDecimal competitiveThreshold,
            boolean parsimonyChangedSelection,
            boolean finalFallbackApplied,
            String rule
    ) {
    }

    public record ModelFitDiagnostics(
            boolean applicable,
            Boolean converged,
            Boolean finiteParameters,
            Boolean arRootsStable,
            Boolean maRootsInvertible,
            BigDecimal ljungBoxPValue,
            Boolean residualAutocorrelationWarning,
            List<String> warnings
    ) {
        public ModelFitDiagnostics {
            warnings = List.copyOf(warnings);
        }
    }

    public record LatestObservationExpectation(
            AnalysisState state,
            String insufficientReason,
            Long entryId,
            Long reportId,
            LocalDate effectiveDate,
            BigDecimal actualValue,
            int trainingObservationCount,
            BigDecimal expectedValue,
            BigDecimal lowerBound,
            BigDecimal upperBound,
            boolean intervalAvailable,
            Boolean outsideInterval,
            String selectedModelFamily,
            Map<String, Object> selectedModelConfiguration,
            ForecastMetrics selectedMetrics,
            ModelSelectionDecision modelSelection,
            ModelFitDiagnostics selectedModelDiagnostics
    ) {
        public LatestObservationExpectation {
            selectedModelConfiguration = selectedModelConfiguration == null
                    ? Map.of() : Map.copyOf(selectedModelConfiguration);
        }
    }

    public enum AnalysisState {
        COMPLETED,
        INSUFFICIENT_DATA
    }

    public enum CandidateState {
        EVALUATED,
        INELIGIBLE,
        FAILED
    }
}
