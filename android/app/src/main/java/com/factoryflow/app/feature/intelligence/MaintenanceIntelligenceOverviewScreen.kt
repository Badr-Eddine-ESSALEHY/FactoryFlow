package com.factoryflow.app.feature.intelligence

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.model.IntelligenceAlert
import com.factoryflow.app.core.model.IntelligenceKpiSummary
import com.factoryflow.app.core.model.IntelligenceOverview
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.core.util.toFrenchDate
import com.factoryflow.app.core.util.toFrenchInstant

@Composable
fun MaintenanceIntelligenceOverviewScreen(
    onBack: () -> Unit,
    onKpi: (Long) -> Unit,
    onAlerts: () -> Unit,
    onAlert: (Long) -> Unit,
    viewModel: IntelligenceOverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                FlowPageHeader(
                    title = stringResource(R.string.mi_title),
                    subtitle = stringResource(R.string.mi_subtitle),
                    onBack = onBack,
                    actionIcon = Icons.Outlined.Refresh,
                    actionDescription = stringResource(R.string.mi_refresh),
                    onAction = viewModel::load,
                    modifier = Modifier.statusBarsPadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
                )
            }
        },
    ) { padding ->
        when (val current = state) {
            IntelligenceOverviewUiState.Loading -> SkeletonRows(Modifier.padding(padding).padding(FlowSpacing.xl), 5)
            is IntelligenceOverviewUiState.Error -> ErrorPane(
                stringResource(current.error.title),
                stringResource(current.error.detail),
                stringResource(R.string.retry),
                viewModel::load,
                Modifier.padding(padding).fillMaxSize(),
            )
            is IntelligenceOverviewUiState.Content -> if (current.overview.kpis.isEmpty()) {
                EmptyPane(
                    stringResource(R.string.mi_empty_title),
                    stringResource(R.string.mi_empty_detail),
                    Modifier.padding(padding).padding(FlowSpacing.xl),
                    Icons.Outlined.Analytics,
                )
            } else {
                IntelligenceOverviewContent(
                    overview = current.overview,
                    refreshing = current.refreshing,
                    onKpi = onKpi,
                    onAlerts = onAlerts,
                    onAlert = onAlert,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun IntelligenceOverviewContent(
    overview: IntelligenceOverview,
    refreshing: Boolean,
    onKpi: (Long) -> Unit,
    onAlerts: () -> Unit,
    onAlert: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val successful = overview.kpis.mapNotNull(IntelligenceKpiSummary::latestSuccessfulAnalysis)
    val lastAnalysis = successful.maxByOrNull { it.generatedAt }?.generatedAt
    val anomalous = successful.count { it.latestAnomalous == true }
    val forecasts = successful.count { it.forecastState == "COMPLETED" }
    val insufficient = overview.kpis.count { it.latestSuccessfulAnalysis?.status == "INSUFFICIENT_DATA" }
    val alertCount = overview.kpis.sumOf(IntelligenceKpiSummary::alertCount)

    FlowContentSurface(modifier) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, FlowSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
        ) {
            item {
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.xl)) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FlowIconTile(Icons.Outlined.PsychologyAlt, null, FlowPurple, gradientEnd = FlowBlueDark, size = FlowSize.iconTileLarge)
                            Spacer(Modifier.width(FlowSpacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.mi_overview_hero_title), style = MaterialTheme.typography.titleLarge)
                                Text(stringResource(R.string.mi_overview_hero_detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (refreshing) CircularProgressIndicator(Modifier.size(FlowSize.icon), strokeWidth = FlowSize.progressStroke)
                        }
                        Spacer(Modifier.height(FlowSpacing.lg))
                        Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                            OverviewMetric(stringResource(R.string.mi_kpis_analyzed), successful.size.toString(), FlowBlue, Modifier.weight(1f))
                            OverviewMetric(stringResource(R.string.mi_active_alerts), alertCount.toString(), if (alertCount > 0) FlowDanger else FlowGreen, Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(FlowSpacing.sm))
                        Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                            OverviewMetric(stringResource(R.string.mi_anomalous_kpis), anomalous.toString(), if (anomalous > 0) FlowWarning else FlowGreen, Modifier.weight(1f))
                            OverviewMetric(stringResource(R.string.mi_forecasts_available), forecasts.toString(), FlowPurple, Modifier.weight(1f))
                            OverviewMetric(stringResource(R.string.mi_insufficient_kpis), insufficient.toString(), FlowOrange, Modifier.weight(1f))
                        }
                        lastAnalysis?.let {
                            Spacer(Modifier.height(FlowSpacing.md))
                            Text(stringResource(R.string.mi_last_analysis, it.toFrenchInstant()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { FlowSectionHeader(stringResource(R.string.mi_kpi_section)) }
            items(overview.kpis, key = { overviewKpiKey(it.profile.kpiDefinitionId) }) { item ->
                IntelligenceKpiCard(item, onClick = { onKpi(item.profile.kpiDefinitionId) })
            }
            item {
                FlowSectionHeader(
                    title = stringResource(R.string.mi_recent_alerts),
                    action = stringResource(R.string.mi_view_all_alerts),
                    onAction = onAlerts,
                )
            }
            if (overview.recentAlerts.isEmpty()) {
                item {
                    FlowEmptyState(
                        stringResource(R.string.mi_no_recent_alerts),
                        stringResource(R.string.mi_no_recent_alerts_detail),
                        icon = Icons.Outlined.Verified,
                    )
                }
            } else {
                items(overview.recentAlerts.take(4), key = { overviewAlertKey(it.id) }) { alert ->
                    IntelligenceAlertRow(alert, onClick = { onAlert(alert.id) })
                }
            }
        }
    }
}

internal fun overviewKpiKey(id: Long): String = "kpi:$id"

internal fun overviewAlertKey(id: Long): String = "alert:$id"

@Composable
private fun OverviewMetric(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = color.copy(alpha = FlowOpacity.tint), shape = androidx.compose.foundation.shape.RoundedCornerShape(FlowRadius.control)) {
        Column(Modifier.padding(horizontal = FlowSpacing.md, vertical = FlowSpacing.sm)) {
            Text(value, color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        }
    }
}

@Composable
private fun IntelligenceKpiCard(item: IntelligenceKpiSummary, onClick: () -> Unit) {
    val analysis = item.latestSuccessfulAnalysis
    val attemptFailed = item.latestRefreshAttempt?.status == "TECHNICAL_FAILURE"
    FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg), onClick = onClick) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.Top) {
                FlowIconTile(Icons.Outlined.QueryStats, null, if (analysis == null) FlowOrange else FlowPurple, size = FlowSize.listIconTile)
                Spacer(Modifier.width(FlowSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(item.profile.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(item.profile.code, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                analysis?.latestActualValue?.let {
                    Text(it.displayValue(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(FlowSpacing.md))
            FlowStatusPill(
                label = stringResource(
                    when {
                        attemptFailed -> R.string.mi_analysis_failed
                        analysis != null -> R.string.mi_analysis_ready
                        else -> R.string.mi_analysis_missing
                    },
                ),
                color = when {
                    attemptFailed -> FlowDanger
                    analysis != null -> FlowGreen
                    else -> FlowOrange
                },
                icon = when {
                    attemptFailed -> Icons.Outlined.ErrorOutline
                    analysis != null -> Icons.Outlined.CheckCircle
                    else -> Icons.Outlined.HourglassEmpty
                },
                compact = true,
            )
            if (analysis != null) {
                Spacer(Modifier.height(FlowSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                    FlowStatusPill(trendLabel(analysis.trendDirection), trendColor(analysis.trendDirection), compact = true)
                    FlowStatusPill(
                        stringResource(if (analysis.latestAnomalous == true) R.string.mi_anomaly_detected else R.string.mi_no_anomaly),
                        if (analysis.latestAnomalous == true) FlowDanger else FlowGreen,
                        compact = true,
                    )
                }
                Spacer(Modifier.height(FlowSpacing.sm))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.mi_observations_count, analysis.usableObservationCount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    if (item.alertCount > 0) FlowStatusPill(stringResource(R.string.mi_alert_count, item.alertCount), FlowDanger, compact = true)
                    else Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(FlowSize.icon))
                }
            }
        }
    }
}

@Composable
internal fun IntelligenceAlertRow(alert: IntelligenceAlert, onClick: () -> Unit) {
    val high = alert.attentionLevel == "HIGH"
    FlowListRow(
        icon = if (high) Icons.Outlined.PriorityHigh else Icons.Outlined.Analytics,
        title = alertTypeLabel(alert.type),
        meta = "${alert.displayName} · ${alert.observationDate.toFrenchDate()}",
        accent = if (high) FlowDanger else FlowWarning,
        onClick = onClick,
        trailing = {
            FlowStatusPill(
                stringResource(if (high) R.string.mi_attention_high else R.string.mi_attention_medium),
                if (high) FlowDanger else FlowWarning,
                compact = true,
            )
        },
        titleMaxLines = 2,
        metaMaxLines = 2,
    )
}

@Composable
internal fun alertTypeLabel(type: String): String = stringResource(
    when (type) {
        "STRONG_CONTEXTUAL_DEVIATION" -> R.string.mi_alert_strong
        "ANOMALOUS_OBSERVATION" -> R.string.mi_alert_anomaly
        else -> R.string.mi_alert_forecast
    },
)

@Composable
internal fun trendLabel(direction: String?): String = stringResource(
    when (direction) {
        "INCREASING" -> R.string.mi_trend_increasing
        "DECREASING" -> R.string.mi_trend_decreasing
        "STABLE" -> R.string.mi_trend_stable
        else -> R.string.mi_insufficient_data
    },
)

internal fun trendColor(direction: String?): Color = when (direction) {
    "INCREASING" -> FlowOrange
    "DECREASING" -> FlowPurple
    "STABLE" -> FlowGreen
    else -> FlowTextSecondary
}
