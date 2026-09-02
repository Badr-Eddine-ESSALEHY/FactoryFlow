package com.factoryflow.app.feature.intelligence

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.model.*
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.core.util.toFrenchDate
import com.factoryflow.app.core.util.toFrenchInstant
import java.math.BigDecimal

@Composable
fun KpiIntelligenceWorkspaceScreen(
    onBack: () -> Unit,
    onAlert: (Long) -> Unit,
    onReport: (Long) -> Unit,
    viewModel: KpiIntelligenceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column {
                    FlowPageHeader(
                        title = stringResource(R.string.mi_workspace_title),
                        onBack = onBack,
                        actionIcon = Icons.Outlined.Refresh,
                        actionDescription = stringResource(R.string.mi_run_analysis),
                        onAction = viewModel::refresh,
                        modifier = Modifier.statusBarsPadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
                    )
                    if (state.refreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> SkeletonRows(Modifier.padding(padding).padding(FlowSpacing.xl), 6)
            state.noAnalysis -> FirstAnalysisState(viewModel::refresh, Modifier.padding(padding).padding(FlowSpacing.xl))
            state.error != null && state.analysis == null -> ErrorPane(
                stringResource(state.error!!.title),
                stringResource(
                    if (state.error!!.detail == state.error!!.title) R.string.mi_technical_error_detail
                    else state.error!!.detail,
                ),
                stringResource(R.string.retry),
                viewModel::load,
                Modifier.padding(padding).fillMaxSize(),
            )
            state.analysis?.result == null -> EmptyPane(
                stringResource(R.string.mi_no_analysis_title),
                stringResource(R.string.mi_no_analysis_detail),
                Modifier.padding(padding).padding(FlowSpacing.xl),
                Icons.Outlined.Analytics,
                stringResource(R.string.mi_run_analysis),
                viewModel::refresh,
            )
            else -> KpiWorkspaceContent(
                state = state,
                onPage = viewModel::selectPage,
                onAlert = onAlert,
                onReport = onReport,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@Composable
private fun FirstAnalysisState(onAnalyze: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        FlowEmptyState(
            stringResource(R.string.mi_no_analysis_title),
            stringResource(R.string.mi_no_analysis_detail),
            icon = Icons.Outlined.Science,
            action = stringResource(R.string.mi_run_analysis),
            onAction = onAnalyze,
        )
    }
}

@Composable
private fun KpiWorkspaceContent(
    state: KpiIntelligenceUiState,
    onPage: (IntelligenceWorkspacePage) -> Unit,
    onAlert: (Long) -> Unit,
    onReport: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val analysis = checkNotNull(state.analysis)
    val result = checkNotNull(analysis.result)
    FlowContentSurface(modifier) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, FlowSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
        ) {
            item { WorkspaceHeader(analysis) }
            if (state.error != null) {
                item {
                    InlineWarning(
                        stringResource(state.error.title),
                        stringResource(
                            if (state.error.detail == state.error.title) R.string.mi_refresh_failure_detail
                            else state.error.detail,
                        ),
                        FlowDanger,
                    )
                }
            }
            if (state.alertsUnavailable) {
                item { InlineWarning(stringResource(R.string.mi_partial_alerts), null, FlowWarning) }
            }
            item { WorkspaceTabs(state.page, onPage) }
            item {
                when (state.page) {
                    IntelligenceWorkspacePage.OVERVIEW -> IntelligenceOverviewPage(result, state.alerts, onAlert, onReport)
                    IntelligenceWorkspacePage.ANOMALIES -> AnomalyAnalysisPage(result, state.alerts, onReport)
                    IntelligenceWorkspacePage.FORECAST -> ForecastAnalysisPage(result)
                    IntelligenceWorkspacePage.TREND -> TrendAnalysisPage(result)
                    IntelligenceWorkspacePage.QUALITY -> ModelQualityPage(result)
                }
            }
        }
    }
}

@Composable
private fun WorkspaceHeader(analysis: IntelligenceAnalysis) {
    val result = checkNotNull(analysis.result)
    val latest = result.historicalObservations.lastOrNull()
    FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.xl)) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                FlowIconTile(Icons.Outlined.Analytics, null, FlowPurple, gradientEnd = FlowBlueDark, size = FlowSize.iconTileLarge)
                Spacer(Modifier.width(FlowSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(result.kpi.displayName, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(result.kpi.code, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(latest?.value.displayValue(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                    result.kpi.unit?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Spacer(Modifier.height(FlowSpacing.md))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(FlowSpacing.xs),
            ) {
                FlowStatusPill(trendLabel(result.trend.direction), trendColor(result.trend.direction), compact = true)
                FlowStatusPill(
                    stringResource(if (analysis.summary.latestAnomalous == true) R.string.mi_anomaly_detected else R.string.mi_no_anomaly),
                    if (analysis.summary.latestAnomalous == true) FlowDanger else FlowGreen,
                    compact = true,
                )
                FlowStatusPill(
                    stringResource(if (result.forecast.state == "COMPLETED") R.string.mi_forecast_available else R.string.mi_forecast_unavailable),
                    if (result.forecast.state == "COMPLETED") FlowPurple else FlowOrange,
                    compact = true,
                )
            }
            Spacer(Modifier.height(FlowSpacing.md))
            Text(stringResource(R.string.mi_generated_at, result.generatedAt.toFrenchInstant()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                stringResource(R.string.mi_window, result.windowStart.toFrenchDate(), result.windowEnd.toFrenchDate()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WorkspaceTabs(selected: IntelligenceWorkspacePage, onSelected: (IntelligenceWorkspacePage) -> Unit) {
    val tabs = listOf(
        IntelligenceWorkspacePage.OVERVIEW to R.string.mi_workspace_overview,
        IntelligenceWorkspacePage.ANOMALIES to R.string.mi_workspace_anomalies,
        IntelligenceWorkspacePage.FORECAST to R.string.mi_workspace_forecast,
        IntelligenceWorkspacePage.TREND to R.string.mi_workspace_trend,
        IntelligenceWorkspacePage.QUALITY to R.string.mi_workspace_quality,
    )
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
        tabs.forEach { (page, label) ->
            val active = page == selected
            Surface(
                modifier = Modifier.heightIn(min = FlowSize.touchTarget).clickable { onSelected(page) },
                shape = RoundedCornerShape(FlowRadius.pill),
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shadowElevation = if (active) FlowElevation.control else FlowElevation.none,
            ) {
                Box(Modifier.padding(horizontal = FlowSpacing.lg, vertical = FlowSpacing.sm), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(label),
                        color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun IntelligenceOverviewPage(
    result: IntelligenceResult,
    alerts: List<IntelligenceAlert>,
    onAlert: (Long) -> Unit,
    onReport: (Long) -> Unit,
) {
    val contextualIds = alerts.filter { it.type == "STRONG_CONTEXTUAL_DEVIATION" }.mapTo(mutableSetOf(), IntelligenceAlert::sourceEntryId)
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        SectionCard(stringResource(R.string.mi_validated_history), stringResource(R.string.mi_validated_history_detail)) {
            HistoricalForecastChart(
                history = result.historicalObservations,
                anomaly = result.anomaly.points,
                forecast = result.forecast.points,
                contextualEntryIds = contextualIds,
                unit = result.kpi.unit,
                description = stringResource(R.string.mi_chart_history_description, result.kpi.displayName),
            )
        }
        SectionCard(stringResource(R.string.mi_operational_summary)) {
            MetricGrid(
                listOf(
                    MetricItem(stringResource(R.string.mi_latest_validated_value), result.historicalObservations.lastOrNull()?.value.withUnit(result.kpi.unit), FlowBlue),
                    MetricItem(stringResource(R.string.mi_trend_direction), trendLabel(result.trend.direction), trendColor(result.trend.direction)),
                    MetricItem(stringResource(R.string.mi_expected_next), result.forecast.points.firstOrNull()?.value.withUnit(result.kpi.unit), FlowPurple),
                    MetricItem(stringResource(R.string.mi_selected_model), modelLabel(result.forecast.selectedModelFamily, result.forecast.selectedModelConfiguration), FlowPurple),
                    MetricItem(stringResource(R.string.mi_data_points), result.preparation.usableObservationCount.toString(), FlowGreen),
                    MetricItem(stringResource(R.string.mi_active_alerts), alerts.size.toString(), if (alerts.isEmpty()) FlowGreen else FlowDanger),
                ),
            )
        }
        LatestExpectationCard(result.latestObservationExpectation, result.kpi.unit, onReport)
        SectionCard(stringResource(R.string.mi_recent_alerts)) {
            if (alerts.isEmpty()) {
                FlowEmptyState(stringResource(R.string.mi_no_recent_alerts), stringResource(R.string.mi_no_recent_alerts_detail), icon = Icons.Outlined.Verified)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                    alerts.take(3).forEach { alert -> IntelligenceAlertRow(alert, onClick = { onAlert(alert.id) }) }
                }
            }
        }
    }
}

@Composable
private fun LatestExpectationCard(expectation: LatestObservationExpectation, unit: String?, onReport: (Long) -> Unit) {
    SectionCard(stringResource(R.string.mi_contextual_evidence), stringResource(R.string.mi_contextual_evidence_detail)) {
        if (expectation.state != "COMPLETED") {
            AbstentionState(abstentionReason(expectation.insufficientReason))
            return@SectionCard
        }
        MetricGrid(
            listOf(
                MetricItem(stringResource(R.string.mi_actual_value), expectation.actualValue.withUnit(unit), FlowBlue),
                MetricItem(stringResource(R.string.mi_expected_value), expectation.expectedValue.withUnit(unit), FlowPurple),
                MetricItem(
                    stringResource(R.string.mi_expected_range),
                    if (expectation.intervalAvailable) "${expectation.lowerBound.displayValue()} — ${expectation.upperBound.displayValue()} ${unit.orEmpty()}" else "—",
                    FlowPurple,
                ),
                MetricItem(
                    stringResource(R.string.mi_interval_status),
                    stringResource(if (expectation.outsideInterval == true) R.string.mi_outside_interval else R.string.mi_inside_interval),
                    if (expectation.outsideInterval == true) FlowDanger else FlowGreen,
                ),
                MetricItem(
                    stringResource(R.string.mi_selected_model),
                    modelLabel(expectation.selectedModelFamily, expectation.selectedModelConfiguration),
                    FlowPurple,
                ),
                MetricItem(
                    stringResource(R.string.mi_training_observations_label),
                    stringResource(R.string.mi_observation_count_value, expectation.trainingObservationCount),
                    FlowBlue,
                ),
            ),
        )
        if (!expectation.intervalAvailable) {
            Spacer(Modifier.height(FlowSpacing.md))
            InlineWarning(stringResource(R.string.mi_interval_unavailable), null, FlowOrange)
        }
        expectation.reportId?.let { reportId ->
            Spacer(Modifier.height(FlowSpacing.md))
            TextButton(onClick = { onReport(reportId) }) {
                Icon(Icons.Outlined.Description, null)
                Spacer(Modifier.width(FlowSpacing.xs))
                Text(stringResource(R.string.mi_open_report))
            }
        }
    }
}

@Composable
private fun AnomalyAnalysisPage(result: IntelligenceResult, alerts: List<IntelligenceAlert>, onReport: (Long) -> Unit) {
    val analysis = result.anomaly
    val contextualIds = alerts.filter { it.type == "STRONG_CONTEXTUAL_DEVIATION" }.mapTo(mutableSetOf(), IntelligenceAlert::sourceEntryId)
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        if (analysis.state != "COMPLETED") {
            AbstentionState(stringResource(R.string.mi_anomaly_insufficient, abstentionReason(analysis.insufficientReason)))
        }
        SectionCard(stringResource(R.string.mi_anomaly_history_title), stringResource(R.string.mi_anomaly_history_detail)) {
            HistoricalForecastChart(
                history = result.historicalObservations,
                anomaly = analysis.points,
                forecast = emptyList(),
                contextualEntryIds = contextualIds,
                unit = result.kpi.unit,
                description = stringResource(R.string.mi_chart_history_description, result.kpi.displayName),
            )
        }
        if (analysis.state == "COMPLETED") {
            SectionCard(stringResource(R.string.mi_anomaly_evidence_title), stringResource(R.string.mi_anomaly_evidence_detail)) {
                AnomalyEvidenceChart(analysis, stringResource(R.string.mi_chart_anomaly_description, result.kpi.displayName))
                analysis.threshold?.let {
                    Spacer(Modifier.height(FlowSpacing.sm))
                    Text(stringResource(R.string.mi_anomaly_threshold, it.displayValue()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        SectionCard(stringResource(R.string.mi_anomaly_features_title), stringResource(R.string.mi_anomaly_features_detail)) {
            Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                analysis.featureNames.forEach { feature -> FeatureRow(feature) }
            }
        }
        val unusual = analysis.points.filter { it.anomalous == true }
        if (unusual.isNotEmpty()) {
            SectionCard(stringResource(R.string.mi_anomaly_detected)) {
                unusual.asReversed().take(5).forEach { point ->
                    FlowListRow(
                        icon = Icons.Outlined.ChangeHistory,
                        title = point.value.withUnit(result.kpi.unit),
                        meta = point.effectiveDate.toFrenchDate(),
                        accent = FlowDanger,
                        onClick = { onReport(point.reportId) },
                        trailing = { Text(point.score.displayValue(), style = MaterialTheme.typography.labelMedium, color = FlowDanger) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastAnalysisPage(result: IntelligenceResult) {
    val forecast = result.forecast
    var metric by remember { mutableStateOf(ForecastMetric.SMAPE) }
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        if (forecast.state != "COMPLETED") {
            AbstentionState(stringResource(R.string.mi_forecast_insufficient, abstentionReason(forecast.insufficientReason)))
            return@Column
        }
        SectionCard(stringResource(R.string.mi_forecast_hero_title), stringResource(R.string.mi_forecast_hero_detail)) {
            HistoricalForecastChart(
                history = result.historicalObservations,
                anomaly = result.anomaly.points,
                forecast = forecast.points,
                contextualEntryIds = emptySet(),
                unit = result.kpi.unit,
                description = stringResource(R.string.mi_chart_history_description, result.kpi.displayName),
            )
            if (forecast.points.none(ForecastPoint::intervalAvailable)) {
                Spacer(Modifier.height(FlowSpacing.md))
                InlineWarning(stringResource(R.string.mi_interval_unavailable), null, FlowOrange)
            }
        }
        SectionCard(stringResource(R.string.mi_model_comparison_title), stringResource(R.string.mi_model_comparison_detail)) {
            MetricSelector(metric) { metric = it }
            Spacer(Modifier.height(FlowSpacing.lg))
            ModelComparisonChart(forecast.candidates, metric, forecast.modelSelection)
        }
        SectionCard(stringResource(R.string.mi_horizon_error_title), stringResource(R.string.mi_horizon_error_detail)) {
            HorizonErrorChart(
                forecast.candidates,
                forecast.modelSelection,
                metric,
                stringResource(R.string.mi_chart_horizon_description, result.kpi.displayName),
            )
        }
        ModelSelectionCard(forecast)
        forecast.selectedMetrics?.let { metrics ->
            SectionCard(stringResource(R.string.mi_quality_metrics_title)) { ForecastMetricsGrid(metrics) }
        }
    }
}

@Composable
private fun TrendAnalysisPage(result: IntelligenceResult) {
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        SectionCard(stringResource(R.string.mi_trend_history_title), stringResource(R.string.mi_trend_history_detail)) {
            TrendHistoryChart(result.historicalObservations, result.trend, stringResource(R.string.mi_chart_trend_description, result.kpi.displayName))
        }
        SectionCard(stringResource(R.string.mi_movement_title), stringResource(R.string.mi_movement_detail)) {
            MovementBarsChart(result.historicalObservations, stringResource(R.string.mi_chart_movement_description, result.kpi.displayName))
        }
        SectionCard(stringResource(R.string.mi_operational_summary)) {
            MetricGrid(
                listOf(
                    MetricItem(stringResource(R.string.mi_trend_direction), trendLabel(result.trend.direction), trendColor(result.trend.direction)),
                    MetricItem(stringResource(R.string.mi_slope), result.trend.slopePerObservation.withUnit(result.kpi.unit), FlowOrange),
                    MetricItem(stringResource(R.string.mi_absolute_change), result.trend.absoluteChange.signedWithUnit(result.kpi.unit), FlowBlue),
                    MetricItem(stringResource(R.string.mi_percentage_change), result.trend.percentageChange.asPercentage(), FlowPurple),
                ),
            )
        }
    }
}

@Composable
private fun ModelQualityPage(result: IntelligenceResult) {
    val cadence = result.preparation.cadence
    val forecast = result.forecast
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        SectionCard(stringResource(R.string.mi_quality_data_title)) {
            MetricGrid(
                listOf(
                    MetricItem(stringResource(R.string.mi_source_records), result.preparation.sourceRecordCount.toString(), FlowBlue),
                    MetricItem(stringResource(R.string.mi_usable_observations), result.preparation.usableObservationCount.toString(), FlowGreen),
                    MetricItem(stringResource(R.string.mi_missing_observations), result.preparation.missingValueCount.toString(), FlowOrange),
                    MetricItem(stringResource(R.string.mi_duplicate_dates), cadence.duplicateDateCount.toString(), if (cadence.duplicateDateCount > 0) FlowDanger else FlowGreen),
                ),
            )
        }
        SectionCard(stringResource(R.string.mi_quality_cadence_title)) {
            InfoRow(stringResource(R.string.mi_expected_cadence), cadence.expectedCadenceDays?.let { pluralStringResource(R.plurals.mi_days_value, it, it) } ?: stringResource(R.string.mi_unknown))
            InfoRow(stringResource(R.string.mi_observed_cadence), cadence.observedCadenceDays?.let { pluralStringResource(R.plurals.mi_days_value, it, it) } ?: stringResource(R.string.mi_unknown))
            InfoRow(stringResource(R.string.mi_cadence_basis), cadenceLabel(cadence.basis))
            InfoRow(stringResource(R.string.mi_cadence_ambiguity), abstentionReason(cadence.ambiguity))
            InlineWarning(stringResource(R.string.mi_no_resampling), null, FlowBlue)
        }
        SectionCard(stringResource(R.string.mi_quality_model_title)) {
            InfoRow(stringResource(R.string.mi_selected_model), modelLabel(forecast.selectedModelFamily, forecast.selectedModelConfiguration))
            InfoRow(stringResource(R.string.mi_forecast_horizon_label), stringResource(R.string.mi_steps_count, forecast.requestedHorizon))
            InfoRow(stringResource(R.string.mi_training_observations_label), stringResource(R.string.mi_observation_count_value, forecast.trainingObservationCount))
            InfoRow(stringResource(R.string.mi_rolling_origins_label), stringResource(R.string.mi_origins_count, forecast.rollingOriginCount))
        }
        forecast.selectedMetrics?.let { SectionCard(stringResource(R.string.mi_quality_metrics_title)) { ForecastMetricsGrid(it) } }
        forecast.selectedModelDiagnostics?.let { diagnostics ->
            if (diagnostics.applicable) {
                SectionCard(stringResource(R.string.mi_quality_diagnostics_title)) {
                    InfoRow(stringResource(R.string.mi_diagnostic_convergence), booleanLabel(diagnostics.converged))
                    InfoRow(stringResource(R.string.mi_diagnostic_parameters), booleanLabel(diagnostics.finiteParameters))
                    InfoRow(stringResource(R.string.mi_diagnostic_ar), booleanLabel(diagnostics.arRootsStable))
                    InfoRow(stringResource(R.string.mi_diagnostic_ma), booleanLabel(diagnostics.maRootsInvertible))
                    InfoRow(stringResource(R.string.mi_diagnostic_residual_warning), booleanLabel(diagnostics.residualAutocorrelationWarning))
                }
            }
        }
    }
}

@Composable
private fun ModelSelectionCard(forecast: ForecastAnalysis) {
    val selection = forecast.modelSelection ?: return
    SectionCard(stringResource(R.string.mi_selected_by_parsimony)) {
        val message = when {
            selection.finalFallbackApplied -> stringResource(R.string.mi_final_fallback)
            selection.parsimonyChangedSelection -> stringResource(R.string.mi_selected_by_parsimony_detail)
            else -> stringResource(R.string.mi_raw_best_retained)
        }
        InlineWarning(message, null, if (selection.finalFallbackApplied) FlowWarning else FlowGreen)
        Spacer(Modifier.height(FlowSpacing.md))
        InfoRow(stringResource(R.string.mi_selected_model), modelLabel(selection.selected.family, selection.selected.configuration))
        InfoRow(stringResource(R.string.mi_metric_smape), selection.selected.metrics.smape.asPercentage())
    }
}

@Composable
private fun ForecastMetricsGrid(metrics: ForecastMetrics) {
    MetricGrid(
        listOf(
            MetricItem(stringResource(R.string.mi_metric_smape), metrics.smape.asPercentage(), FlowPurple),
            MetricItem(stringResource(R.string.mi_metric_mae), metrics.mae.displayValue(), FlowBlue),
            MetricItem(stringResource(R.string.mi_metric_rmse), metrics.rmse.displayValue(), FlowOrange),
            MetricItem(stringResource(R.string.mi_mase_non_seasonal), metrics.nonSeasonalMase.displayValue(), FlowGreen),
            MetricItem(stringResource(R.string.mi_mase_seasonal), metrics.seasonalMase.displayValue(), FlowTealDark),
        ),
    )
}

@Composable
private fun MetricSelector(selected: ForecastMetric, onSelected: (ForecastMetric) -> Unit) {
    val values = ForecastMetric.entries
    FlowSegmentedControl(
        options = listOf(stringResource(R.string.mi_metric_smape), stringResource(R.string.mi_metric_mae), stringResource(R.string.mi_metric_rmse)),
        selectedIndex = values.indexOf(selected),
        onSelected = { onSelected(values[it]) },
    )
}

private data class MetricItem(val label: String, val value: String, val color: Color)

@Composable
private fun MetricGrid(items: List<MetricItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
        items.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                row.forEach { item -> MetricTile(item, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricTile(item: MetricItem, modifier: Modifier = Modifier) {
    Surface(modifier, color = item.color.copy(alpha = FlowOpacity.tint), shape = RoundedCornerShape(FlowRadius.control)) {
        Column(Modifier.padding(FlowSpacing.md)) {
            Text(item.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Spacer(Modifier.height(FlowSpacing.xs))
            Text(item.value, style = MaterialTheme.typography.titleMedium, color = item.color, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SectionCard(title: String, detail: String? = null, content: @Composable ColumnScope.() -> Unit) {
    FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
        Column(Modifier.fillMaxWidth()) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            detail?.let {
                Spacer(Modifier.height(FlowSpacing.xs))
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(FlowSpacing.lg))
            content()
        }
    }
}

@Composable
private fun InlineWarning(title: String, detail: String?, color: Color) {
    Surface(Modifier.fillMaxWidth(), color = color.copy(alpha = FlowOpacity.tint), shape = RoundedCornerShape(FlowRadius.control)) {
        Row(Modifier.padding(FlowSpacing.md), verticalAlignment = Alignment.Top) {
            Icon(Icons.Outlined.Info, null, tint = color, modifier = Modifier.size(FlowSize.icon))
            Spacer(Modifier.width(FlowSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, color = color)
                detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun AbstentionState(reason: String) {
    InlineWarning(reason, null, FlowOrange)
}

@Composable
private fun FeatureRow(feature: String) {
    val (icon, label) = when (feature) {
        "confirmed_value" -> Icons.Outlined.Numbers to stringResource(R.string.mi_feature_confirmed_value)
        "change_per_day" -> Icons.Outlined.Speed to stringResource(R.string.mi_feature_change_per_day)
        "deviation_from_trailing_median" -> Icons.Outlined.AlignVerticalCenter to stringResource(R.string.mi_feature_trailing_median)
        else -> Icons.Outlined.DataObject to feature.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        FlowIconTile(icon, null, FlowPurple, size = FlowSize.listIconTile)
        Spacer(Modifier.width(FlowSpacing.md))
        Text(label, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = FlowSpacing.sm), verticalAlignment = Alignment.Top) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(FlowSpacing.md))
        Text(value, Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, textAlign = androidx.compose.ui.text.style.TextAlign.End)
    }
}

@Composable
private fun booleanLabel(value: Boolean?): String = stringResource(
    when (value) {
        true -> R.string.mi_yes
        false -> R.string.mi_no
        null -> R.string.mi_not_applicable
    },
)

@Composable
private fun cadenceLabel(value: String): String = when (value) {
    "CONFIGURED_EXPECTED" -> stringResource(R.string.mi_expected_cadence)
    "INFERRED_OBSERVED" -> stringResource(R.string.mi_observed_cadence)
    else -> stringResource(R.string.mi_unknown)
}

@Composable
private fun abstentionReason(reason: String?): String = when (reason) {
    "ANOMALY_MINIMUM_HISTORY_NOT_MET", "FORECAST_MINIMUM_HISTORY_NOT_MET", "LATEST_EXPECTATION_MINIMUM_HISTORY_NOT_MET",
    "HORIZON_ALIGNED_BACKTEST_HISTORY_NOT_MET", "BACKTEST_MINIMUM_ORIGINS_NOT_MET" -> stringResource(R.string.mi_reason_history)
    "DUPLICATE_EFFECTIVE_DATES", "DUPLICATE_OBSERVATION_DATES" -> stringResource(R.string.mi_reason_duplicate_dates)
    "MISSING_OBSERVATIONS_REQUIRE_RESAMPLING", "CADENCE_AMBIGUOUS_DUE_TO_MISSING_OBSERVATIONS", "MISSING_OBSERVATIONS" -> stringResource(R.string.mi_reason_missing_cadence)
    "IRREGULAR_SAMPLING", "IRREGULAR_OBSERVED_SPACING" -> stringResource(R.string.mi_reason_irregular_cadence)
    "OBSERVED_CADENCE_DIFFERS_FROM_EXPECTED", "OBSERVED_SPACING_DIFFERS_FROM_EXPECTED" -> stringResource(R.string.mi_reason_cadence_mismatch)
    "CADENCE_BASIS_UNKNOWN", "INSUFFICIENT_OBSERVATIONS", null -> stringResource(R.string.mi_reason_unknown_cadence)
    "NO_ELIGIBLE_MODEL", "ALL_FINAL_MODEL_FITS_FAILED" -> stringResource(R.string.mi_reason_model_unavailable)
    "NONE" -> stringResource(R.string.mi_no)
    else -> stringResource(R.string.mi_insufficient_data)
}

private fun BigDecimal?.withUnit(unit: String?): String = buildString {
    append(this@withUnit.displayValue())
    if (this@withUnit != null && !unit.isNullOrBlank()) append(" ").append(unit)
}

private fun BigDecimal?.signedWithUnit(unit: String?): String {
    if (this == null) return "—"
    return buildString {
        if (signum() > 0) append("+")
        append(this@signedWithUnit.displayValue())
        if (!unit.isNullOrBlank()) append(" ").append(unit)
    }
}

private fun BigDecimal?.asPercentage(): String = if (this == null) "—" else "${displayValue()} %"
