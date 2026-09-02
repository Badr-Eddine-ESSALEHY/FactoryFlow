package com.factoryflow.app.feature.intelligence

import com.factoryflow.app.MainDispatcherRule
import com.factoryflow.app.core.data.MaintenanceIntelligenceRepository
import com.factoryflow.app.core.model.*
import com.factoryflow.app.core.network.AppError
import java.math.BigDecimal
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MaintenanceIntelligenceViewModelTest {
    @get:Rule val dispatcher = MainDispatcherRule()

    @Test
    fun `overview exposes real summaries and retained refresh state`() = runTest(dispatcher.dispatcher) {
        val repository = FakeIntelligenceRepository()
        val viewModel = IntelligenceOverviewViewModel(repository)
        advanceUntilIdle()

        val content = viewModel.state.value as IntelligenceOverviewUiState.Content
        assertEquals(7L, content.overview.kpis.single().profile.kpiDefinitionId)
        assertFalse(content.refreshing)

        viewModel.load()
        assertTrue((viewModel.state.value as IntelligenceOverviewUiState.Content).refreshing)
        advanceUntilIdle()
        assertFalse((viewModel.state.value as IntelligenceOverviewUiState.Content).refreshing)
    }

    @Test
    fun `workspace loads analysis and alerts then preserves analytical page choice`() = runTest(dispatcher.dispatcher) {
        val viewModel = KpiIntelligenceViewModel(androidx.lifecycle.SavedStateHandle(mapOf("kpiId" to 7L)), FakeIntelligenceRepository())
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.analysis?.result)
        assertEquals(1, viewModel.state.value.alerts.size)
        viewModel.selectPage(IntelligenceWorkspacePage.FORECAST)
        assertEquals(IntelligenceWorkspacePage.FORECAST, viewModel.state.value.page)
    }

    @Test
    fun `workspace represents missing persisted analysis as first-analysis state`() = runTest(dispatcher.dispatcher) {
        val repository = FakeIntelligenceRepository().apply {
            detailFailure = AppError.Server("INTELLIGENCE_ANALYSIS_NOT_FOUND", "missing")
        }
        val viewModel = KpiIntelligenceViewModel(androidx.lifecycle.SavedStateHandle(mapOf("kpiId" to 7L)), repository)
        advanceUntilIdle()

        assertTrue(viewModel.state.value.noAnalysis)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `manual refresh replaces analysis and requests the selected KPI only`() = runTest(dispatcher.dispatcher) {
        val repository = FakeIntelligenceRepository()
        val viewModel = KpiIntelligenceViewModel(androidx.lifecycle.SavedStateHandle(mapOf("kpiId" to 7L)), repository)
        advanceUntilIdle()
        repository.refreshed = analysis(latest = BigDecimal("162"))

        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(listOf(7L), repository.refreshCalls)
        assertEquals(BigDecimal("162"), viewModel.state.value.analysis?.summary?.latestActualValue)
    }

    @Test
    fun `alert filter is sent to backend repository`() = runTest(dispatcher.dispatcher) {
        val repository = FakeIntelligenceRepository()
        val viewModel = IntelligenceAlertsViewModel(repository)
        advanceUntilIdle()
        viewModel.filter("HIGH")
        advanceUntilIdle()
        assertEquals("HIGH", repository.alertLevels.last())
    }
}

private class FakeIntelligenceRepository : MaintenanceIntelligenceRepository {
    var detailFailure: Throwable? = null
    var refreshed = analysis()
    val refreshCalls = mutableListOf<Long>()
    val alertLevels = mutableListOf<String?>()

    override suspend fun overview() = IntelligenceOverview(
        listOf(IntelligenceKpiSummary(profile(), analysis().summary, analysis().summary, 1)),
        listOf(alert()),
    )
    override suspend fun detail(kpiId: Long) = detailFailure?.let { throw it } ?: analysis()
    override suspend fun refresh(kpiId: Long): IntelligenceAnalysis {
        refreshCalls += kpiId
        return refreshed
    }
    override suspend fun alerts(kpiId: Long?, type: String?, attentionLevel: String?, page: Int): IntelligencePage<IntelligenceAlert> {
        alertLevels += attentionLevel
        return IntelligencePage(listOf(alert()), 0, 1, 1, true)
    }
    override suspend fun alert(id: Long) = alert()
}

private fun profile() = IntelligenceProfile(1, 7, "EAU", "Consommation eau", true, 1, 3, 7, 90, "2026-09-01T09:00:00Z")

private fun summary(latest: BigDecimal = BigDecimal("108")) = IntelligenceAnalysisSummary(
    id = 21,
    publicId = "analysis-21",
    kpiDefinitionId = 7,
    code = "EAU",
    displayName = "Consommation eau",
    status = "COMPLETED",
    windowStart = "2026-08-01",
    windowEnd = "2026-09-01",
    generatedAt = "2026-09-01T09:00:00Z",
    durationMillis = 530,
    usableObservationCount = 3,
    missingObservationCount = 0,
    cadenceState = "REGULAR",
    cadenceBasis = "CONFIGURED_EXPECTED",
    cadenceAmbiguity = "NONE",
    trendDirection = "INCREASING",
    anomalyState = "COMPLETED",
    anomalyReason = null,
    latestAnomalous = true,
    latestAnomalyScore = BigDecimal("0.12"),
    forecastState = "COMPLETED",
    forecastReason = null,
    selectedModelFamily = "HOLT",
    forecastDirection = "INCREASING",
    forecastHorizon = 3,
    forecastMetrics = metrics(),
    expectationState = "COMPLETED",
    expectationReason = null,
    latestActualValue = latest,
    expectedValue = BigDecimal("108"),
    expectedLowerBound = BigDecimal("99"),
    expectedUpperBound = BigDecimal("117"),
    outsideExpectedInterval = latest > BigDecimal("117"),
    contextualizationStatus = "COMPLETED",
    technicalFailureCode = null,
    technicalFailureMessage = null,
)

private fun analysis(latest: BigDecimal = BigDecimal("108")): IntelligenceAnalysis {
    val observations = listOf(
        IntelligenceObservation(1, 101, "2026-08-30", "2026-08-30T09:00:00Z", BigDecimal("100")),
        IntelligenceObservation(2, 102, "2026-08-31", "2026-08-31T09:00:00Z", BigDecimal("103")),
        IntelligenceObservation(3, 103, "2026-09-01", "2026-09-01T09:00:00Z", latest),
    )
    val anomalyPoints = observations.mapIndexed { index, point ->
        AnomalyPoint(point.entryId, point.reportId, point.effectiveDate, point.confirmedAt, point.value, BigDecimal("0.0${index + 1}"), BigDecimal("-0.0${index + 1}"), index == 2)
    }
    val candidate = ForecastCandidate(
        "HOLT", emptyMap(), "EVALUATED", null, 3, listOf(1), metrics(),
        listOf(HorizonEvaluation(1, 3, metrics())), BigDecimal("0.4"), null,
    )
    return IntelligenceAnalysis(
        summary(latest),
        IntelligenceResult(
            IntelligenceKpi(7, "EAU", "Consommation eau", "m³"),
            "2026-08-01", "2026-09-01", "2026-09-01T09:00:00Z",
            IntelligencePreparation(3, 3, 0, CadenceAnalysis("REGULAR", 1, 1, "CONFIGURED_EXPECTED", "NONE", 3, 0, 0, false, "NONE")),
            observations,
            TrendAnalysis("INCREASING", BigDecimal("4"), latest - BigDecimal("100"), BigDecimal("8"), 3),
            AnomalyAnalysis("COMPLETED", null, "SKLEARN_ISOLATION_FOREST", listOf("confirmed_value", "change_per_day", "deviation_from_trailing_median"), 3, BigDecimal.ZERO, AnomalyScoreSemantics("MODEL_RELATIVE_EVIDENCE", "HIGHER_IS_MORE_ANOMALOUS", "FITTED_KPI_WINDOW", false, false, false), anomalyPoints),
            ForecastAnalysis("COMPLETED", null, "HOLT", emptyMap(), 3, 1, listOf(1), 3, "2026-09-01T09:00:00Z", listOf(ForecastPoint("2026-09-02", BigDecimal("110"), BigDecimal("104"), BigDecimal("116"), true)), metrics(), listOf(candidate), null, null, "ONE_STANDARD_ERROR_THEN_LOWEST_COMPLEXITY", "INCREASING", BigDecimal("0.95")),
            LatestObservationExpectation("COMPLETED", null, 3, 103, "2026-09-01", latest, 2, BigDecimal("108"), BigDecimal("99"), BigDecimal("117"), true, latest > BigDecimal("117"), "HOLT", emptyMap(), metrics(), null, null),
        ),
    )
}

private fun metrics() = ForecastMetrics(BigDecimal("2.1"), BigDecimal("2.8"), BigDecimal("5.9"), BigDecimal("0.7"), null)

private fun alert() = IntelligenceAlert(
    31, 7, "EAU", "Consommation eau", 21, "analysis-21", 3, 103,
    "STRONG_CONTEXTUAL_DEVIATION", "HIGH", "2026-09-01", BigDecimal("162"), true,
    BigDecimal("0.21"), BigDecimal("108"), BigDecimal("99"), BigDecimal("117"), true,
    "INCREASING", "INCREASING", "HOLT", "2026-09-01T09:00:00Z", "2026-09-01T09:00:00Z", "SENT", null,
)
