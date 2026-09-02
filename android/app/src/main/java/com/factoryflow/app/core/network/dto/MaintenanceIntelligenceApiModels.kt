package com.factoryflow.app.core.network.dto

import com.squareup.moshi.JsonClass
import java.math.BigDecimal

@JsonClass(generateAdapter = false)
data class IntelligenceOverviewDto(
    val kpis: List<IntelligenceOverviewItemDto> = emptyList(),
    val recentAlerts: List<IntelligenceAlertDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class IntelligenceOverviewItemDto(
    val profile: IntelligenceProfileDto,
    val latestSuccessfulAnalysis: IntelligenceAnalysisSummaryDto?,
    val latestRefreshAttempt: IntelligenceAnalysisSummaryDto?,
    val alertCount: Long,
)

@JsonClass(generateAdapter = false)
data class IntelligenceProfileDto(
    val id: Long,
    val kpiDefinitionId: Long,
    val kpiCode: String,
    val kpiDisplayName: String,
    val enabled: Boolean,
    val expectedCadenceDays: Int?,
    val forecastHorizon: Int,
    val seasonalPeriod: Int?,
    val historyWindowDays: Int,
    val createdAt: String,
    val updatedAt: String,
    val version: Long,
)

@JsonClass(generateAdapter = false)
data class IntelligenceAnalysisSummaryDto(
    val id: Long,
    val analysisId: String,
    val kpiDefinitionId: Long,
    val kpiCode: String,
    val kpiDisplayName: String,
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
    val forecastMae: BigDecimal?,
    val forecastRmse: BigDecimal?,
    val forecastSmape: BigDecimal?,
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

@JsonClass(generateAdapter = false)
data class IntelligenceAnalysisDetailDto(
    val summary: IntelligenceAnalysisSummaryDto,
    val result: MaintenanceIntelligenceResultDto?,
)

@JsonClass(generateAdapter = false)
data class MaintenanceIntelligenceResultDto(
    val kpi: IntelligenceKpiDto,
    val windowStart: String,
    val windowEnd: String,
    val generatedAt: String,
    val preparation: IntelligencePreparationDto,
    val historicalObservations: List<IntelligenceObservationDto> = emptyList(),
    val trend: IntelligenceTrendDto,
    val anomaly: IntelligenceAnomalyDto,
    val forecast: IntelligenceForecastDto,
    val latestObservationExpectation: LatestObservationExpectationDto,
)

@JsonClass(generateAdapter = false)
data class IntelligenceKpiDto(
    val definitionId: Long,
    val code: String,
    val displayName: String,
    val unit: String?,
)

@JsonClass(generateAdapter = false)
data class IntelligencePreparationDto(
    val sourceRecordCount: Int,
    val usableObservationCount: Int,
    val missingValueCount: Int,
    val cadence: IntelligenceCadenceDto,
)

@JsonClass(generateAdapter = false)
data class IntelligenceCadenceDto(
    val state: String,
    val observedCadenceDays: Int?,
    val expectedCadenceDays: Int?,
    val cadenceBasis: String,
    val ambiguity: String,
    val distinctDateCount: Int,
    val duplicateDateCount: Int,
    val missingValueCount: Int,
    val resamplingApplied: Boolean,
    val resamplingPolicy: String,
)

@JsonClass(generateAdapter = false)
data class IntelligenceObservationDto(
    val entryId: Long,
    val reportId: Long,
    val effectiveDate: String,
    val confirmedAt: String?,
    val value: BigDecimal,
)

@JsonClass(generateAdapter = false)
data class IntelligenceTrendDto(
    val direction: String,
    val slopePerObservation: BigDecimal?,
    val absoluteChange: BigDecimal?,
    val percentageChange: BigDecimal?,
    val observationCount: Int,
)

@JsonClass(generateAdapter = false)
data class IntelligenceAnomalyDto(
    val state: String,
    val insufficientReason: String?,
    val algorithm: String,
    val featureNames: List<String> = emptyList(),
    val trainingObservationCount: Int,
    val anomalyThreshold: BigDecimal?,
    val scoreSemantics: AnomalyScoreSemanticsDto,
    val points: List<IntelligenceAnomalyPointDto> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class IntelligenceAnomalyPointDto(
    val entryId: Long,
    val reportId: Long,
    val effectiveDate: String,
    val confirmedAt: String?,
    val value: BigDecimal,
    val anomalyScore: BigDecimal?,
    val decisionFunction: BigDecimal?,
    val anomalous: Boolean?,
)

@JsonClass(generateAdapter = false)
data class AnomalyScoreSemanticsDto(
    val kind: String,
    val orientation: String,
    val scope: String,
    val probability: Boolean,
    val severity: Boolean,
    val crossModelComparable: Boolean,
)

@JsonClass(generateAdapter = false)
data class IntelligenceForecastDto(
    val state: String,
    val insufficientReason: String?,
    val selectedModelFamily: String?,
    val selectedModelConfiguration: Map<String, Any?> = emptyMap(),
    val trainingObservationCount: Int,
    val requestedHorizon: Int,
    val effectiveEvaluatedHorizons: List<Int> = emptyList(),
    val rollingOriginCount: Int,
    val generatedAt: String,
    val points: List<IntelligenceForecastPointDto> = emptyList(),
    val selectedMetrics: IntelligenceForecastMetricsDto?,
    val candidates: List<IntelligenceCandidateDto> = emptyList(),
    val modelSelection: IntelligenceModelSelectionDto?,
    val selectedModelDiagnostics: IntelligenceModelDiagnosticsDto?,
    val selectionReason: String?,
    val forecastDirection: String,
    val intervalConfidence: BigDecimal?,
)

@JsonClass(generateAdapter = false)
data class IntelligenceForecastPointDto(
    val effectiveDate: String,
    val value: BigDecimal,
    val lowerBound: BigDecimal?,
    val upperBound: BigDecimal?,
    val intervalAvailable: Boolean,
)

@JsonClass(generateAdapter = false)
data class IntelligenceForecastMetricsDto(
    val mae: BigDecimal,
    val rmse: BigDecimal,
    val smape: BigDecimal,
    val nonSeasonalMase: BigDecimal?,
    val seasonalMase: BigDecimal?,
)

@JsonClass(generateAdapter = false)
data class IntelligenceHorizonEvaluationDto(
    val horizonStep: Int,
    val observationCount: Int,
    val metrics: IntelligenceForecastMetricsDto,
)

@JsonClass(generateAdapter = false)
data class IntelligenceCandidateDto(
    val family: String,
    val configuration: Map<String, Any?> = emptyMap(),
    val state: String,
    val reason: String?,
    val rollingOriginCount: Int,
    val effectiveEvaluatedHorizons: List<Int> = emptyList(),
    val metrics: IntelligenceForecastMetricsDto?,
    val perHorizonMetrics: List<IntelligenceHorizonEvaluationDto> = emptyList(),
    val primaryMetricStandardError: BigDecimal?,
    val diagnostics: IntelligenceModelDiagnosticsDto?,
)

@JsonClass(generateAdapter = false)
data class IntelligenceModelReferenceDto(
    val family: String,
    val configuration: Map<String, Any?> = emptyMap(),
    val metrics: IntelligenceForecastMetricsDto,
)

@JsonClass(generateAdapter = false)
data class IntelligenceModelSelectionDto(
    val rawBest: IntelligenceModelReferenceDto,
    val parsimoniousChoice: IntelligenceModelReferenceDto,
    val selected: IntelligenceModelReferenceDto,
    val primaryMetric: String,
    val rawBestStandardError: BigDecimal,
    val competitiveThreshold: BigDecimal,
    val parsimonyChangedSelection: Boolean,
    val finalFallbackApplied: Boolean,
    val rule: String,
)

@JsonClass(generateAdapter = false)
data class IntelligenceModelDiagnosticsDto(
    val applicable: Boolean,
    val converged: Boolean?,
    val finiteParameters: Boolean?,
    val arRootsStable: Boolean?,
    val maRootsInvertible: Boolean?,
    val ljungBoxPValue: BigDecimal?,
    val residualAutocorrelationWarning: Boolean?,
    val warnings: List<String> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class LatestObservationExpectationDto(
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
    val selectedModelConfiguration: Map<String, Any?> = emptyMap(),
    val selectedMetrics: IntelligenceForecastMetricsDto?,
    val modelSelection: IntelligenceModelSelectionDto?,
    val selectedModelDiagnostics: IntelligenceModelDiagnosticsDto?,
)

@JsonClass(generateAdapter = false)
data class IntelligenceAlertDto(
    val id: Long,
    val kpiDefinitionId: Long,
    val kpiCode: String,
    val kpiDisplayName: String,
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
