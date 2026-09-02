package com.factoryflow.app.core.model

import java.math.BigDecimal

data class IntelligenceOverview(
    val kpis: List<IntelligenceKpiSummary>,
    val recentAlerts: List<IntelligenceAlert>,
)

data class IntelligenceKpiSummary(
    val profile: IntelligenceProfile,
    val latestSuccessfulAnalysis: IntelligenceAnalysisSummary?,
    val latestRefreshAttempt: IntelligenceAnalysisSummary?,
    val alertCount: Long,
)

data class IntelligenceProfile(
    val id: Long,
    val kpiDefinitionId: Long,
    val code: String,
    val displayName: String,
    val enabled: Boolean,
    val expectedCadenceDays: Int?,
    val forecastHorizon: Int,
    val seasonalPeriod: Int?,
    val historyWindowDays: Int,
    val updatedAt: String,
)

data class IntelligenceAnalysisSummary(
    val id: Long,
    val publicId: String,
    val kpiDefinitionId: Long,
    val code: String,
    val displayName: String,
    val status: String,
    val windowStart: String,
    val windowEnd: String,
    val generatedAt: String,
    val durationMillis: Long,
    val usableObservationCount: Int,
    val missingObservationCount: Int,
    val cadenceState: String?,
    val cadenceBasis: String?,
    val cadenceAmbiguity: String?,
    val trendDirection: String?,
    val anomalyState: String?,
    val anomalyReason: String?,
    val latestAnomalous: Boolean?,
    val latestAnomalyScore: BigDecimal?,
    val forecastState: String?,
    val forecastReason: String?,
    val selectedModelFamily: String?,
    val forecastDirection: String?,
    val forecastHorizon: Int?,
    val forecastMetrics: ForecastMetrics?,
    val expectationState: String?,
    val expectationReason: String?,
    val latestActualValue: BigDecimal?,
    val expectedValue: BigDecimal?,
    val expectedLowerBound: BigDecimal?,
    val expectedUpperBound: BigDecimal?,
    val outsideExpectedInterval: Boolean?,
    val contextualizationStatus: String?,
    val technicalFailureCode: String?,
    val technicalFailureMessage: String?,
)

data class IntelligenceAnalysis(
    val summary: IntelligenceAnalysisSummary,
    val result: IntelligenceResult?,
)

data class IntelligenceResult(
    val kpi: IntelligenceKpi,
    val windowStart: String,
    val windowEnd: String,
    val generatedAt: String,
    val preparation: IntelligencePreparation,
    val historicalObservations: List<IntelligenceObservation>,
    val trend: TrendAnalysis,
    val anomaly: AnomalyAnalysis,
    val forecast: ForecastAnalysis,
    val latestObservationExpectation: LatestObservationExpectation,
)

data class IntelligenceKpi(
    val id: Long,
    val code: String,
    val displayName: String,
    val unit: String?,
)

data class IntelligencePreparation(
    val sourceRecordCount: Int,
    val usableObservationCount: Int,
    val missingValueCount: Int,
    val cadence: CadenceAnalysis,
)

data class CadenceAnalysis(
    val state: String,
    val observedCadenceDays: Int?,
    val expectedCadenceDays: Int?,
    val basis: String,
    val ambiguity: String,
    val distinctDateCount: Int,
    val duplicateDateCount: Int,
    val missingValueCount: Int,
    val resamplingApplied: Boolean,
    val resamplingPolicy: String,
)

data class IntelligenceObservation(
    val entryId: Long,
    val reportId: Long,
    val effectiveDate: String,
    val confirmedAt: String?,
    val value: BigDecimal,
)

data class TrendAnalysis(
    val direction: String,
    val slopePerObservation: BigDecimal?,
    val absoluteChange: BigDecimal?,
    val percentageChange: BigDecimal?,
    val observationCount: Int,
)

data class AnomalyAnalysis(
    val state: String,
    val insufficientReason: String?,
    val algorithm: String,
    val featureNames: List<String>,
    val trainingObservationCount: Int,
    val threshold: BigDecimal?,
    val scoreSemantics: AnomalyScoreSemantics,
    val points: List<AnomalyPoint>,
)

data class AnomalyPoint(
    val entryId: Long,
    val reportId: Long,
    val effectiveDate: String,
    val confirmedAt: String?,
    val value: BigDecimal,
    val score: BigDecimal?,
    val decisionFunction: BigDecimal?,
    val anomalous: Boolean?,
)

data class AnomalyScoreSemantics(
    val kind: String,
    val orientation: String,
    val scope: String,
    val probability: Boolean,
    val severity: Boolean,
    val crossModelComparable: Boolean,
)

data class ForecastAnalysis(
    val state: String,
    val insufficientReason: String?,
    val selectedModelFamily: String?,
    val selectedModelConfiguration: Map<String, Any?>,
    val trainingObservationCount: Int,
    val requestedHorizon: Int,
    val effectiveEvaluatedHorizons: List<Int>,
    val rollingOriginCount: Int,
    val generatedAt: String,
    val points: List<ForecastPoint>,
    val selectedMetrics: ForecastMetrics?,
    val candidates: List<ForecastCandidate>,
    val modelSelection: ModelSelection?,
    val selectedModelDiagnostics: ModelDiagnostics?,
    val selectionReason: String?,
    val direction: String,
    val intervalConfidence: BigDecimal?,
)

data class ForecastPoint(
    val effectiveDate: String,
    val value: BigDecimal,
    val lowerBound: BigDecimal?,
    val upperBound: BigDecimal?,
    val intervalAvailable: Boolean,
)

data class ForecastMetrics(
    val mae: BigDecimal,
    val rmse: BigDecimal,
    val smape: BigDecimal,
    val nonSeasonalMase: BigDecimal?,
    val seasonalMase: BigDecimal?,
)

data class HorizonEvaluation(
    val horizonStep: Int,
    val observationCount: Int,
    val metrics: ForecastMetrics,
)

data class ForecastCandidate(
    val family: String,
    val configuration: Map<String, Any?>,
    val state: String,
    val reason: String?,
    val rollingOriginCount: Int,
    val effectiveEvaluatedHorizons: List<Int>,
    val metrics: ForecastMetrics?,
    val perHorizonMetrics: List<HorizonEvaluation>,
    val primaryMetricStandardError: BigDecimal?,
    val diagnostics: ModelDiagnostics?,
)

data class ModelReference(
    val family: String,
    val configuration: Map<String, Any?>,
    val metrics: ForecastMetrics,
)

data class ModelSelection(
    val rawBest: ModelReference,
    val parsimoniousChoice: ModelReference,
    val selected: ModelReference,
    val primaryMetric: String,
    val rawBestStandardError: BigDecimal,
    val competitiveThreshold: BigDecimal,
    val parsimonyChangedSelection: Boolean,
    val finalFallbackApplied: Boolean,
    val rule: String,
)

data class ModelDiagnostics(
    val applicable: Boolean,
    val converged: Boolean?,
    val finiteParameters: Boolean?,
    val arRootsStable: Boolean?,
    val maRootsInvertible: Boolean?,
    val ljungBoxPValue: BigDecimal?,
    val residualAutocorrelationWarning: Boolean?,
    val warnings: List<String>,
)

data class LatestObservationExpectation(
    val state: String,
    val insufficientReason: String?,
    val entryId: Long?,
    val reportId: Long?,
    val effectiveDate: String?,
    val actualValue: BigDecimal?,
    val trainingObservationCount: Int,
    val expectedValue: BigDecimal?,
    val lowerBound: BigDecimal?,
    val upperBound: BigDecimal?,
    val intervalAvailable: Boolean,
    val outsideInterval: Boolean?,
    val selectedModelFamily: String?,
    val selectedModelConfiguration: Map<String, Any?>,
    val selectedMetrics: ForecastMetrics?,
    val modelSelection: ModelSelection?,
    val selectedModelDiagnostics: ModelDiagnostics?,
)

data class IntelligenceAlert(
    val id: Long,
    val kpiDefinitionId: Long,
    val code: String,
    val displayName: String,
    val analysisId: Long,
    val analysisPublicId: String,
    val sourceEntryId: Long,
    val sourceReportId: Long,
    val type: String,
    val attentionLevel: String,
    val observationDate: String,
    val actualValue: BigDecimal,
    val anomalous: Boolean,
    val anomalyScore: BigDecimal?,
    val expectedValue: BigDecimal?,
    val expectedLowerBound: BigDecimal?,
    val expectedUpperBound: BigDecimal?,
    val outsideExpectedInterval: Boolean?,
    val trendContext: String,
    val forecastDirectionContext: String?,
    val selectedModelFamily: String?,
    val createdAt: String,
    val updatedAt: String,
    val notificationStatus: String,
    val notificationFailure: String?,
)

data class IntelligencePage<T>(
    val content: List<T>,
    val page: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
)
