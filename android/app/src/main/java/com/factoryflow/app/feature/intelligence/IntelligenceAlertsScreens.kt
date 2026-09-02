package com.factoryflow.app.feature.intelligence

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.model.IntelligenceAlert
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.core.util.toFrenchDate
import com.factoryflow.app.core.util.toFrenchInstant

@Composable
fun IntelligenceAlertsScreen(
    onBack: () -> Unit,
    onAlert: (Long) -> Unit,
    viewModel: IntelligenceAlertsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                FlowPageHeader(
                    stringResource(R.string.mi_alerts_title),
                    subtitle = stringResource(R.string.mi_alerts_subtitle),
                    onBack = onBack,
                    actionIcon = Icons.Outlined.Refresh,
                    actionDescription = stringResource(R.string.mi_refresh),
                    onAction = { viewModel.load() },
                    modifier = Modifier.statusBarsPadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
                )
            }
        },
    ) { padding ->
        when {
            state.loading -> SkeletonRows(Modifier.padding(padding).padding(FlowSpacing.xl), 6)
            state.error != null -> ErrorPane(
                stringResource(checkNotNull(state.error).title), stringResource(checkNotNull(state.error).detail), stringResource(R.string.retry), { viewModel.load() },
                Modifier.padding(padding).fillMaxSize(),
            )
            else -> IntelligenceAlertsContent(state, viewModel::filter, viewModel::loadMore, onAlert, Modifier.padding(padding))
        }
    }
}

@Composable
private fun IntelligenceAlertsContent(
    state: IntelligenceAlertsUiState,
    onFilter: (String?) -> Unit,
    onLoadMore: () -> Unit,
    onAlert: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filters = listOf(
        null to stringResource(R.string.mi_alert_filter_all),
        "HIGH" to stringResource(R.string.mi_alert_filter_high),
        "MEDIUM" to stringResource(R.string.mi_alert_filter_medium),
    )
    FlowContentSurface(modifier) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, FlowSpacing.xxxl),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                    filters.forEach { (value, label) ->
                        FilterChip(selected = state.attentionLevel == value, onClick = { onFilter(value) }, label = { Text(label) })
                    }
                }
            }
            if (state.items.isEmpty()) {
                item {
                    FlowEmptyState(
                        stringResource(R.string.mi_alert_empty_title),
                        stringResource(R.string.mi_alert_empty_detail),
                        icon = Icons.Outlined.Verified,
                    )
                }
            } else {
                items(state.items, key = IntelligenceAlert::id) { alert -> IntelligenceAlertRow(alert) { onAlert(alert.id) } }
                if (state.hasMore || state.loadingMore) {
                    item {
                        TextButton(onClick = onLoadMore, enabled = !state.loadingMore, modifier = Modifier.fillMaxWidth()) {
                            if (state.loadingMore) {
                                CircularProgressIndicator(Modifier.size(FlowSize.icon), strokeWidth = FlowSize.progressStroke)
                            } else Text(stringResource(R.string.mi_load_more))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntelligenceAlertDetailScreen(
    onBack: () -> Unit,
    onKpi: (Long) -> Unit,
    onReport: (Long) -> Unit,
    viewModel: IntelligenceAlertDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                FlowPageHeader(
                    stringResource(R.string.mi_alert_detail_title),
                    onBack = onBack,
                    modifier = Modifier.statusBarsPadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
                )
            }
        },
    ) { padding ->
        when (val current = state) {
            IntelligenceAlertDetailUiState.Loading -> SkeletonRows(Modifier.padding(padding).padding(FlowSpacing.xl), 5)
            is IntelligenceAlertDetailUiState.Error -> ErrorPane(
                stringResource(current.error.title), stringResource(current.error.detail), stringResource(R.string.retry), viewModel::load,
                Modifier.padding(padding).fillMaxSize(),
            )
            is IntelligenceAlertDetailUiState.Content -> AlertDetailContent(current.alert, onKpi, onReport, Modifier.padding(padding))
        }
    }
}

@Composable
private fun AlertDetailContent(
    alert: IntelligenceAlert,
    onKpi: (Long) -> Unit,
    onReport: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val high = alert.attentionLevel == "HIGH"
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
                            FlowIconTile(if (high) Icons.Outlined.PriorityHigh else Icons.Outlined.Analytics, null, if (high) FlowDanger else FlowWarning, size = FlowSize.iconTileLarge)
                            Spacer(Modifier.width(FlowSpacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(alertTypeLabel(alert.type), style = MaterialTheme.typography.titleLarge)
                                Text(alert.displayName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FlowStatusPill(
                                stringResource(if (high) R.string.mi_attention_high else R.string.mi_attention_medium),
                                if (high) FlowDanger else FlowWarning,
                                compact = true,
                            )
                        }
                        Spacer(Modifier.height(FlowSpacing.lg))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(alert.actualValue.displayValue(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.weight(1f))
                            Text(alert.observationDate.toFrenchDate(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(alert.createdAt.toFrenchInstant(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.mi_alert_evidence_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(FlowSpacing.lg))
                        if (alert.anomalous) EvidenceStep(Icons.Outlined.ChangeHistory, stringResource(R.string.mi_alert_anomaly_evidence), FlowDanger)
                        if (alert.anomalous && alert.outsideExpectedInterval == true) EvidenceConnector()
                        if (alert.outsideExpectedInterval == true) EvidenceStep(Icons.Outlined.VerticalAlignCenter, stringResource(R.string.mi_alert_interval_evidence), FlowPurple)
                        Spacer(Modifier.height(FlowSpacing.md))
                        Surface(color = (if (high) FlowDanger else FlowWarning).copy(alpha = FlowOpacity.tint), shape = androidx.compose.foundation.shape.RoundedCornerShape(FlowRadius.control)) {
                            Text(
                                stringResource(if (high) R.string.mi_attention_high else R.string.mi_attention_medium),
                                Modifier.fillMaxWidth().padding(FlowSpacing.md),
                                color = if (high) FlowDanger else FlowWarning,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                }
            }
            item {
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.mi_contextual_evidence), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(FlowSpacing.md))
                        AlertValueRow(stringResource(R.string.mi_actual_value), alert.actualValue.displayValue())
                        AlertValueRow(stringResource(R.string.mi_expected_value), alert.expectedValue.displayValue())
                        AlertValueRow(
                            stringResource(R.string.mi_expected_range),
                            if (alert.expectedLowerBound != null && alert.expectedUpperBound != null) {
                                "${alert.expectedLowerBound.displayValue()} — ${alert.expectedUpperBound.displayValue()}"
                            } else "—",
                        )
                        AlertValueRow(stringResource(R.string.mi_anomaly_evidence_title), alert.anomalyScore.displayValue())
                        AlertValueRow(stringResource(R.string.mi_selected_model), modelLabel(alert.selectedModelFamily))
                        AlertValueRow(stringResource(R.string.mi_trend_direction), trendLabel(alert.trendContext))
                    }
                }
            }
            item {
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.mi_alert_source_title), style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(FlowSpacing.md))
                        Text(stringResource(R.string.mi_alert_source_report, alert.sourceReportId), style = MaterialTheme.typography.bodyMedium)
                        Text(stringResource(R.string.mi_alert_source_entry, alert.sourceEntryId), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(FlowSpacing.lg))
                        PrimaryAction(stringResource(R.string.mi_open_kpi), onClick = { onKpi(alert.kpiDefinitionId) })
                        Spacer(Modifier.height(FlowSpacing.sm))
                        OutlinedButton(onClick = { onReport(alert.sourceReportId) }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Outlined.Description, null)
                            Spacer(Modifier.width(FlowSpacing.xs))
                            Text(stringResource(R.string.mi_open_report))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvidenceStep(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FlowIconTile(icon, null, color, size = FlowSize.listIconTile)
        Spacer(Modifier.width(FlowSpacing.md))
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EvidenceConnector() {
    Box(Modifier.padding(start = 19.dp).width(2.dp).height(FlowSpacing.lg).background(MaterialTheme.colorScheme.outlineVariant))
}

@Composable
private fun AlertValueRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = FlowSpacing.sm)) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}
