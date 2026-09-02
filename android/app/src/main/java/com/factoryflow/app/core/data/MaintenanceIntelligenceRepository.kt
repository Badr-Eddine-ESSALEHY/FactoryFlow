package com.factoryflow.app.core.data

import com.factoryflow.app.core.model.*
import com.factoryflow.app.core.network.ApiExecutor
import com.factoryflow.app.core.network.FactoryFlowApi
import com.factoryflow.app.core.network.dto.*
import javax.inject.Inject

interface MaintenanceIntelligenceRepository {
    suspend fun overview(): IntelligenceOverview
    suspend fun detail(kpiId: Long): IntelligenceAnalysis
    suspend fun refresh(kpiId: Long): IntelligenceAnalysis
    suspend fun alerts(
        kpiId: Long? = null,
        type: String? = null,
        attentionLevel: String? = null,
        page: Int = 0,
    ): IntelligencePage<IntelligenceAlert>
    suspend fun alert(id: Long): IntelligenceAlert
}

class DefaultMaintenanceIntelligenceRepository @Inject constructor(
    private val api: FactoryFlowApi,
    private val executor: ApiExecutor,
) : MaintenanceIntelligenceRepository {
    override suspend fun overview() = executor.execute { api.intelligenceOverview().toModel() }
    override suspend fun detail(kpiId: Long) = executor.execute { api.intelligenceDetail(kpiId).toModel() }
    override suspend fun refresh(kpiId: Long) = executor.execute { api.refreshIntelligence(kpiId).toModel() }
    override suspend fun alerts(kpiId: Long?, type: String?, attentionLevel: String?, page: Int) = executor.execute {
        api.intelligenceAlerts(kpiId, type, attentionLevel, page).let { response ->
            IntelligencePage(
                content = response.content.map(IntelligenceAlertDto::toModel),
                page = response.page,
                totalElements = response.totalElements,
                totalPages = response.totalPages,
                last = response.last,
            )
        }
    }
    override suspend fun alert(id: Long) = executor.execute { api.intelligenceAlert(id).toModel() }
}

internal fun IntelligenceOverviewDto.toModel() = IntelligenceOverview(
    kpis = kpis.map(IntelligenceOverviewItemDto::toModel),
    recentAlerts = recentAlerts.map(IntelligenceAlertDto::toModel),
)

private fun IntelligenceOverviewItemDto.toModel() = IntelligenceKpiSummary(
    profile = profile.toModel(),
    latestSuccessfulAnalysis = latestSuccessfulAnalysis?.toModel(),
    latestRefreshAttempt = latestRefreshAttempt?.toModel(),
    alertCount = alertCount,
)

private fun IntelligenceProfileDto.toModel() = IntelligenceProfile(
    id = id,
    kpiDefinitionId = kpiDefinitionId,
    code = kpiCode,
    displayName = kpiDisplayName,
    enabled = enabled,
    expectedCadenceDays = expectedCadenceDays,
    forecastHorizon = forecastHorizon,
    seasonalPeriod = seasonalPeriod,
    historyWindowDays = historyWindowDays,
    updatedAt = updatedAt,
)

private fun IntelligenceAnalysisSummaryDto.toModel() = IntelligenceAnalysisSummary(
    id = id,
    publicId = analysisId,
    kpiDefinitionId = kpiDefinitionId,
    code = kpiCode,
    displayName = kpiDisplayName,
    status = status,
    windowStart = windowStart,
    windowEnd = windowEnd,
    generatedAt = generatedAt,
    durationMillis = durationMillis,
    usableObservationCount = usableObservationCount,
    missingObservationCount = missingObservationCount,
    cadenceState = cadenceState,
    cadenceBasis = cadenceBasis,
    cadenceAmbiguity = cadenceAmbiguity,
    trendDirection = trendDirection,
    anomalyState = anomalyState,
    anomalyReason = anomalyReason,
    latestAnomalous = latestAnomalous,
    latestAnomalyScore = latestAnomalyScore,
    forecastState = forecastState,
    forecastReason = forecastReason,
    selectedModelFamily = selectedModelFamily,
    forecastDirection = forecastDirection,
    forecastHorizon = forecastHorizon,
    forecastMetrics = if (forecastMae != null && forecastRmse != null && forecastSmape != null) {
        ForecastMetrics(forecastMae, forecastRmse, forecastSmape, null, null)
    } else null,
    expectationState = expectationState,
    expectationReason = expectationReason,
    latestActualValue = latestActualValue,
    expectedValue = expectedValue,
    expectedLowerBound = expectedLowerBound,
    expectedUpperBound = expectedUpperBound,
    outsideExpectedInterval = outsideExpectedInterval,
    contextualizationStatus = contextualizationStatus,
    technicalFailureCode = technicalFailureCode,
    technicalFailureMessage = technicalFailureMessage,
)

internal fun IntelligenceAnalysisDetailDto.toModel() = IntelligenceAnalysis(
    summary = summary.toModel(),
    result = result?.toModel(),
)

private fun MaintenanceIntelligenceResultDto.toModel() = IntelligenceResult(
    kpi = IntelligenceKpi(kpi.definitionId, kpi.code, kpi.displayName, kpi.unit),
    windowStart = windowStart,
    windowEnd = windowEnd,
    generatedAt = generatedAt,
    preparation = IntelligencePreparation(
        sourceRecordCount = preparation.sourceRecordCount,
        usableObservationCount = preparation.usableObservationCount,
        missingValueCount = preparation.missingValueCount,
        cadence = preparation.cadence.let {
            CadenceAnalysis(
                state = it.state,
                observedCadenceDays = it.observedCadenceDays,
                expectedCadenceDays = it.expectedCadenceDays,
                basis = it.cadenceBasis,
                ambiguity = it.ambiguity,
                distinctDateCount = it.distinctDateCount,
                duplicateDateCount = it.duplicateDateCount,
                missingValueCount = it.missingValueCount,
                resamplingApplied = it.resamplingApplied,
                resamplingPolicy = it.resamplingPolicy,
            )
        },
    ),
    historicalObservations = historicalObservations.map {
        IntelligenceObservation(it.entryId, it.reportId, it.effectiveDate, it.confirmedAt, it.value)
    },
    trend = TrendAnalysis(
        trend.direction,
        trend.slopePerObservation,
        trend.absoluteChange,
        trend.percentageChange,
        trend.observationCount,
    ),
    anomaly = AnomalyAnalysis(
        state = anomaly.state,
        insufficientReason = anomaly.insufficientReason,
        algorithm = anomaly.algorithm,
        featureNames = anomaly.featureNames,
        trainingObservationCount = anomaly.trainingObservationCount,
        threshold = anomaly.anomalyThreshold,
        scoreSemantics = anomaly.scoreSemantics.let {
            AnomalyScoreSemantics(it.kind, it.orientation, it.scope, it.probability, it.severity, it.crossModelComparable)
        },
        points = anomaly.points.map {
            AnomalyPoint(it.entryId, it.reportId, it.effectiveDate, it.confirmedAt, it.value, it.anomalyScore, it.decisionFunction, it.anomalous)
        },
    ),
    forecast = forecast.toModel(),
    latestObservationExpectation = latestObservationExpectation.toModel(),
)

private fun IntelligenceForecastDto.toModel() = ForecastAnalysis(
    state = state,
    insufficientReason = insufficientReason,
    selectedModelFamily = selectedModelFamily,
    selectedModelConfiguration = selectedModelConfiguration,
    trainingObservationCount = trainingObservationCount,
    requestedHorizon = requestedHorizon,
    effectiveEvaluatedHorizons = effectiveEvaluatedHorizons,
    rollingOriginCount = rollingOriginCount,
    generatedAt = generatedAt,
    points = points.map { ForecastPoint(it.effectiveDate, it.value, it.lowerBound, it.upperBound, it.intervalAvailable) },
    selectedMetrics = selectedMetrics?.toModel(),
    candidates = candidates.map(IntelligenceCandidateDto::toModel),
    modelSelection = modelSelection?.toModel(),
    selectedModelDiagnostics = selectedModelDiagnostics?.toModel(),
    selectionReason = selectionReason,
    direction = forecastDirection,
    intervalConfidence = intervalConfidence,
)

private fun IntelligenceCandidateDto.toModel() = ForecastCandidate(
    family = family,
    configuration = configuration,
    state = state,
    reason = reason,
    rollingOriginCount = rollingOriginCount,
    effectiveEvaluatedHorizons = effectiveEvaluatedHorizons,
    metrics = metrics?.toModel(),
    perHorizonMetrics = perHorizonMetrics.map {
        HorizonEvaluation(it.horizonStep, it.observationCount, it.metrics.toModel())
    },
    primaryMetricStandardError = primaryMetricStandardError,
    diagnostics = diagnostics?.toModel(),
)

private fun IntelligenceForecastMetricsDto.toModel() = ForecastMetrics(mae, rmse, smape, nonSeasonalMase, seasonalMase)

private fun IntelligenceModelSelectionDto.toModel() = ModelSelection(
    rawBest = rawBest.toModel(),
    parsimoniousChoice = parsimoniousChoice.toModel(),
    selected = selected.toModel(),
    primaryMetric = primaryMetric,
    rawBestStandardError = rawBestStandardError,
    competitiveThreshold = competitiveThreshold,
    parsimonyChangedSelection = parsimonyChangedSelection,
    finalFallbackApplied = finalFallbackApplied,
    rule = rule,
)

private fun IntelligenceModelReferenceDto.toModel() = ModelReference(family, configuration, metrics.toModel())

private fun IntelligenceModelDiagnosticsDto.toModel() = ModelDiagnostics(
    applicable,
    converged,
    finiteParameters,
    arRootsStable,
    maRootsInvertible,
    ljungBoxPValue,
    residualAutocorrelationWarning,
    warnings,
)

private fun LatestObservationExpectationDto.toModel() = LatestObservationExpectation(
    state = state,
    insufficientReason = insufficientReason,
    entryId = entryId,
    reportId = reportId,
    effectiveDate = effectiveDate,
    actualValue = actualValue,
    trainingObservationCount = trainingObservationCount,
    expectedValue = expectedValue,
    lowerBound = lowerBound,
    upperBound = upperBound,
    intervalAvailable = intervalAvailable,
    outsideInterval = outsideInterval,
    selectedModelFamily = selectedModelFamily,
    selectedModelConfiguration = selectedModelConfiguration,
    selectedMetrics = selectedMetrics?.toModel(),
    modelSelection = modelSelection?.toModel(),
    selectedModelDiagnostics = selectedModelDiagnostics?.toModel(),
)

internal fun IntelligenceAlertDto.toModel() = IntelligenceAlert(
    id = id,
    kpiDefinitionId = kpiDefinitionId,
    code = kpiCode,
    displayName = kpiDisplayName,
    analysisId = analysisId,
    analysisPublicId = analysisPublicId,
    sourceEntryId = sourceEntryId,
    sourceReportId = sourceReportId,
    type = type,
    attentionLevel = attentionLevel,
    observationDate = observationDate,
    actualValue = actualValue,
    anomalous = anomalous,
    anomalyScore = anomalyScore,
    expectedValue = expectedValue,
    expectedLowerBound = expectedLowerBound,
    expectedUpperBound = expectedUpperBound,
    outsideExpectedInterval = outsideExpectedInterval,
    trendContext = trendContext,
    forecastDirectionContext = forecastDirectionContext,
    selectedModelFamily = selectedModelFamily,
    createdAt = createdAt,
    updatedAt = updatedAt,
    notificationStatus = notificationStatus,
    notificationFailure = notificationFailure,
)
