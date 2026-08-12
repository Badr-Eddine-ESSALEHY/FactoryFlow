package com.factoryflow.app.feature.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.*

@Composable
fun DashboardScreen(
    userName: String,
    onCreate: () -> Unit,
    onPaste: () -> Unit,
    onManual: () -> Unit,
    onReport: (Long) -> Unit,
    onGenerated: (Long) -> Unit,
    onStatistics: () -> Unit,
    onSchedules: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> SkeletonRows(Modifier.fillMaxSize().padding(20.dp), 6)
        state.data == null && state.error != null -> ErrorPane(stringResource(state.error!!.title), stringResource(state.error!!.detail), stringResource(R.string.retry), { viewModel.load() }, Modifier.fillMaxSize())
        else -> DashboardContent(userName, state.data!!, onCreate, onPaste, onManual, onReport, onGenerated, onStatistics, onSchedules, { viewModel.load(true) }, onLogout)
    }
}

@Composable
private fun DashboardContent(
    userName: String, data: DashboardDto, onCreate: () -> Unit, onPaste: () -> Unit, onManual: () -> Unit,
    onReport: (Long) -> Unit, onGenerated: (Long) -> Unit, onStatistics: () -> Unit, onSchedules: () -> Unit, onRefresh: () -> Unit, onLogout: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 110.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.dashboard_greeting, userName.substringBefore(' ')), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp)); Text(stringResource(R.string.dashboard_context, data.businessDate.toFrenchDate()), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, stringResource(R.string.refresh)) }
                IconButton(onClick = onLogout) { Icon(Icons.Outlined.AccountCircle, stringResource(R.string.logout)) }
            }
        }
        item {
            FactoryCard(Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (data.todayHasConfirmedReport) Icons.Outlined.TaskAlt else Icons.Outlined.Schedule, null, tint = if (data.todayHasConfirmedReport) Success else Warning)
                        Spacer(Modifier.width(10.dp)); Text(stringResource(R.string.today_status), style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Metric(data.todayConfirmedReportCount.toString(), stringResource(R.string.confirmed_reports), Success)
                        Metric(data.todayDraftOrPendingReportCount.toString(), stringResource(R.string.pending_reports), Warning)
                        Metric(data.todayConfirmedMissingValueCount.toString(), stringResource(R.string.missing_values), MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        item {
            Column {
                SectionHeader(stringResource(R.string.quick_actions))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction(Icons.Outlined.AddCircleOutline, stringResource(R.string.create_report), onCreate, Modifier.weight(1f))
                    QuickAction(Icons.Outlined.ContentPaste, stringResource(R.string.paste_text), onPaste, Modifier.weight(1f))
                    QuickAction(Icons.Outlined.EditNote, stringResource(R.string.manual_entry), onManual, Modifier.weight(1f))
                }
            }
        }
        if (data.latestKpis.isNotEmpty()) item {
            Column {
                SectionHeader(stringResource(R.string.latest_kpis), stringResource(R.string.view_statistics), onStatistics)
                Spacer(Modifier.height(12.dp))
                data.latestKpis.take(4).chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { KpiCard(it, Modifier.weight(1f)) }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
        if (data.recentReports.isNotEmpty()) item {
            Column {
                SectionHeader(stringResource(R.string.recent_reports))
                Spacer(Modifier.height(10.dp))
                FactoryCard(Modifier.fillMaxWidth()) {
                    Column {
                        data.recentReports.take(4).forEachIndexed { index, report ->
                            RecentReportRow(report, onReport)
                            if (index < data.recentReports.take(4).lastIndex) HorizontalDivider(Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }
        }
        if (data.recentGeneratedReports.isNotEmpty()) item {
            Column {
                SectionHeader(stringResource(R.string.recent_documents))
                Spacer(Modifier.height(10.dp))
                data.recentGeneratedReports.take(3).forEach { document ->
                    FactoryCard(Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { onGenerated(document.id) }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (document.format == "PDF") Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
                                Text(documentType(document.type), style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.date_range_compact, document.periodStart.toFrenchDate(), document.periodEnd.toFrenchDate()), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            }
                            StatusPill(document.format, MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        data.upcomingSchedule?.let { schedule -> item {
            Column {
                SectionHeader(stringResource(R.string.upcoming_schedule), stringResource(R.string.manage_schedules), onSchedules)
                Spacer(Modifier.height(10.dp)); FactoryCard(Modifier.fillMaxWidth().clickable(onClick = onSchedules)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.EventRepeat, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) {
                            Text(scheduleType(schedule.type), style = MaterialTheme.typography.titleMedium)
                            Text(schedule.nextRunAt?.toFrenchInstant() ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(listOfNotNull(if (schedule.generateExcel) stringResource(R.string.excel) else null, if (schedule.generatePdf) stringResource(R.string.pdf) else null).joinToString(" · "), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } }
        if (data.latestKpis.isEmpty() && data.recentReports.isEmpty()) item {
            EmptyPane(stringResource(R.string.no_dashboard_data), stringResource(R.string.no_dashboard_data_detail), action = stringResource(R.string.create_report), onAction = onCreate)
        }
    }
}

@Composable private fun Metric(value: String, label: String, color: androidx.compose.ui.graphics.Color) = Column(Modifier.width(92.dp)) {
    Text(value, style = MaterialTheme.typography.headlineSmall, color = color); Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Surface(modifier.clickable(onClick = onClick), color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.Start) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(12.dp)); Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 2)
        }
    }
}

@Composable private fun KpiCard(kpi: LatestKpiDto, modifier: Modifier) = FactoryCard(modifier) {
    Column {
        Text(kpi.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
        Spacer(Modifier.height(10.dp)); Row(verticalAlignment = Alignment.Bottom) {
            Text(kpi.value.displayValue(), style = MaterialTheme.typography.headlineSmall)
            if (!kpi.unit.isNullOrBlank()) { Spacer(Modifier.width(5.dp)); Text(kpi.unit, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(6.dp)); Text(kpi.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun RecentReportRow(report: RecentReportDto, onClick: (Long) -> Unit) = Row(Modifier.fillMaxWidth().clickable { onClick(report.id) }, verticalAlignment = Alignment.CenterVertically) {
    Icon(Icons.Outlined.Description, null, tint = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(report.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.titleMedium); Text(report.submittedBy, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
    StatusPill(statusLabel(report.status), statusColor(report.status))
}

@Composable private fun statusColor(status: String) = when (status) { "CONFIRMED" -> Success; "DRAFT" -> MaterialTheme.colorScheme.onSurfaceVariant; else -> Warning }
@Composable private fun statusLabel(status: String) = stringResource(when (status) { "CONFIRMED" -> R.string.confirmed; "DRAFT" -> R.string.draft; else -> R.string.pending_review })
@Composable private fun documentType(type: String) = stringResource(when (type) { "DAILY" -> R.string.daily; "WEEKLY" -> R.string.weekly; "MONTHLY" -> R.string.monthly; else -> R.string.manual })
@Composable private fun scheduleType(type: String) = stringResource(when (type) { "DAILY" -> R.string.schedule_daily; "WEEKLY" -> R.string.schedule_weekly; else -> R.string.schedule_monthly })
