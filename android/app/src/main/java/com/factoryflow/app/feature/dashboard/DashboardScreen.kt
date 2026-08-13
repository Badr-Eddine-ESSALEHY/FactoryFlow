package com.factoryflow.app.feature.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    onProfile: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> SkeletonRows(Modifier.fillMaxSize().padding(20.dp), 6)
        state.data == null && state.error != null -> ErrorPane(
            stringResource(state.error!!.title), stringResource(state.error!!.detail), stringResource(R.string.retry),
            viewModel::load, Modifier.fillMaxSize(),
        )
        state.data != null -> DashboardContent(
            userName, state.data!!, onCreate, onPaste, onManual, onReport, onGenerated,
            onStatistics, onSchedules, { viewModel.load(true) }, onProfile,
        )
    }
}

@Composable
private fun DashboardContent(
    userName: String,
    data: DashboardDto,
    onCreate: () -> Unit,
    onPaste: () -> Unit,
    onManual: () -> Unit,
    onReport: (Long) -> Unit,
    onGenerated: (Long) -> Unit,
    onStatistics: () -> Unit,
    onSchedules: () -> Unit,
    onRefresh: () -> Unit,
    onProfile: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 124.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { DashboardHeader(userName, data.businessDate, onRefresh, onProfile) }
        item { DayHero(data) }
        item {
            Column {
                SectionHeader(stringResource(R.string.quick_actions))
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickAction(Icons.Outlined.AddCircleOutline, stringResource(R.string.create_report), onCreate, Modifier.weight(1f), true)
                    QuickAction(Icons.Outlined.ContentPaste, stringResource(R.string.paste_text), onPaste, Modifier.weight(1f))
                    QuickAction(Icons.Outlined.EditNote, stringResource(R.string.manual_entry), onManual, Modifier.weight(1f))
                }
            }
        }
        if (data.activityTrend.isNotEmpty()) item {
            FactoryCard(Modifier.fillMaxWidth()) {
                Column {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.activity_last_7_days), style = MaterialTheme.typography.titleLarge)
                            Text("Rapports confirmés et données manquantes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LegendDot(FactoryFlowGreen, stringResource(R.string.confirmed_short))
                        Spacer(Modifier.width(10.dp))
                        LegendDot(FactoryFlowMagenta, stringResource(R.string.missing_short))
                    }
                    Spacer(Modifier.height(20.dp))
                    ActivityChart(data.activityTrend, Modifier.fillMaxWidth().height(142.dp))
                }
            }
        }
        if (data.latestKpis.isNotEmpty()) item {
            Column {
                SectionHeader(stringResource(R.string.latest_kpis), stringResource(R.string.view_statistics), onStatistics)
                Spacer(Modifier.height(12.dp))
                data.latestKpis.take(6).chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { KpiCard(it, Modifier.weight(1f), onStatistics) }
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
                FactoryCard(Modifier.fillMaxWidth(), PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    Column {
                        data.recentReports.take(4).forEachIndexed { index, report ->
                            RecentReportRow(report, onReport)
                            if (index < data.recentReports.take(4).lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
                    DocumentRow(document, onGenerated)
                    Spacer(Modifier.height(9.dp))
                }
            }
        }
        data.upcomingSchedule?.let { schedule -> item {
            Column {
                SectionHeader(stringResource(R.string.upcoming_schedule), stringResource(R.string.manage_schedules), onSchedules)
                Spacer(Modifier.height(10.dp))
                FactoryCard(Modifier.fillMaxWidth().clickable(onClick = onSchedules)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FactoryIconChip(Icons.Outlined.EventRepeat, null, tint = FactoryFlowGreenDark, container = FactoryFlowGreen.copy(alpha = .12f))
                        Spacer(Modifier.width(13.dp))
                        Column(Modifier.weight(1f)) {
                            Text(scheduleType(schedule.type), style = MaterialTheme.typography.titleMedium)
                            Text(schedule.nextRunAt?.toFrenchInstant() ?: "—", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(listOfNotNull(if (schedule.generateExcel) "Excel" else null, if (schedule.generatePdf) "PDF" else null).joinToString(" · "), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        } }
        if (data.latestKpis.isEmpty() && data.recentReports.isEmpty()) item {
            EmptyPane(stringResource(R.string.no_dashboard_data), stringResource(R.string.no_dashboard_data_detail), action = stringResource(R.string.create_report), onAction = onCreate)
        }
    }
}

@Composable
private fun DashboardHeader(userName: String, businessDate: String, onRefresh: () -> Unit, onProfile: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Bonjour,", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(userName.substringBeforeLast(' '), style = MaterialTheme.typography.headlineLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Situation du ${businessDate.toFrenchDate()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, stringResource(R.string.refresh)) }
        Surface(onClick = onProfile, shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.size(46.dp), contentAlignment = Alignment.Center) {
                Text(userName.trim().split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString(""), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DayHero(data: DashboardDto) {
    FactoryFlowHero(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Situation du jour", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(if (data.todayHasConfirmedReport) "Données confirmées disponibles" else "Aucun rapport confirmé aujourd’hui", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = .78f))
            }
            Icon(if (data.todayHasConfirmedReport) Icons.Outlined.Verified else Icons.Outlined.Schedule, null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            HeroMetric(data.todayConfirmedReportCount.toString(), "Confirmés", Modifier.weight(1f))
            HeroMetric(data.todayDraftOrPendingReportCount.toString(), "À reprendre", Modifier.weight(1f))
            HeroMetric(data.todayConfirmedMissingValueCount.toString(), "Manquants", Modifier.weight(1f))
            HeroMetric(data.todayGeneratedDocumentCount.toString(), "Documents", Modifier.weight(1f))
        }
    }
}

@Composable private fun HeroMetric(value: String, label: String, modifier: Modifier) = Column(modifier) {
    Text(value, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
    Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = .76f), maxLines = 1)
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier, emphasized: Boolean = false) {
    val background = if (emphasized) FactoryFlowMagenta else MaterialTheme.colorScheme.surface
    val foreground = if (emphasized) Color.White else MaterialTheme.colorScheme.onSurface
    Surface(modifier.clip(RoundedCornerShape(FactoryRadius.card)).clickable(onClick = onClick), color = background, shape = RoundedCornerShape(FactoryRadius.card), shadowElevation = if (emphasized) 8.dp else 2.dp) {
        Column(Modifier.padding(horizontal = 13.dp, vertical = 15.dp)) {
            Icon(icon, null, tint = foreground, modifier = Modifier.size(23.dp))
            Spacer(Modifier.height(13.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, color = foreground, maxLines = 2, minLines = 2)
        }
    }
}

@Composable
private fun KpiCard(kpi: LatestKpiDto, modifier: Modifier, onClick: () -> Unit) = FactoryCard(modifier.clickable(onClick = onClick)) {
    Column {
        Text(kpi.displayName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, minLines = 2)
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(kpi.value.displayValue(), style = MaterialTheme.typography.headlineSmall)
            if (!kpi.unit.isNullOrBlank()) { Spacer(Modifier.width(5.dp)); Text(kpi.unit, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(5.dp))
        Text(kpi.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun RecentReportRow(report: RecentReportDto, onClick: (Long) -> Unit) = Row(
    Modifier.fillMaxWidth().clickable { onClick(report.id) }.padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically,
) {
    FactoryIconChip(Icons.Outlined.Description, null, size = 39.dp)
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
        Text(report.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.titleMedium)
        Text(report.submittedBy, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1)
    }
    StatusPill(statusLabel(report.status), statusColor(report.status))
}

@Composable
private fun DocumentRow(document: RecentGeneratedReportDto, onGenerated: (Long) -> Unit) {
    FactoryCard(Modifier.fillMaxWidth().clickable { onGenerated(document.id) }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FactoryIconChip(if (document.format == "PDF") Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView, null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(documentType(document.type), style = MaterialTheme.typography.titleMedium)
                Text("${document.periodStart.toFrenchDate()} — ${document.periodEnd.toFrenchDate()}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            StatusPill(document.format, MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable private fun LegendDot(color: Color, label: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Box(Modifier.size(7.dp).background(color, RoundedCornerShape(10.dp)))
    Spacer(Modifier.width(4.dp)); Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun ActivityChart(points: List<DashboardActivityDto>, modifier: Modifier) {
    val confirmed = FactoryFlowGreen
    val missing = FactoryFlowMagenta
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier) {
        val max = points.maxOfOrNull { maxOf(it.confirmedReportCount, it.missingValueCount) }?.coerceAtLeast(1L) ?: 1L
        repeat(3) { index ->
            val y = size.height * index / 2f
            drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
        }
        if (points.isEmpty()) return@Canvas
        fun offsets(selector: (DashboardActivityDto) -> Long): List<Offset> = points.mapIndexed { index, point ->
            val x = if (points.size == 1) size.width / 2 else size.width * index / (points.size - 1)
            Offset(x, size.height - (selector(point).toFloat() / max) * (size.height - 8.dp.toPx()))
        }
        fun drawSeries(series: List<Offset>, color: Color) {
            if (series.size > 1) {
                val path = Path().apply { moveTo(series.first().x, series.first().y); series.drop(1).forEach { lineTo(it.x, it.y) } }
                drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            }
            series.forEach { drawCircle(color, 3.5.dp.toPx(), it) }
        }
        drawSeries(offsets { it.confirmedReportCount }, confirmed)
        drawSeries(offsets { it.missingValueCount }, missing)
    }
}

@Composable private fun statusColor(status: String) = when (status) { "CONFIRMED" -> Success; "DRAFT" -> MaterialTheme.colorScheme.onSurfaceVariant; else -> Warning }
@Composable private fun statusLabel(status: String) = stringResource(when (status) { "CONFIRMED" -> R.string.confirmed; "DRAFT" -> R.string.draft; else -> R.string.pending_review })
@Composable private fun documentType(type: String) = stringResource(when (type) { "DAILY" -> R.string.daily; "WEEKLY" -> R.string.weekly; "MONTHLY" -> R.string.monthly; else -> R.string.manual })
@Composable private fun scheduleType(type: String) = stringResource(when (type) { "DAILY" -> R.string.schedule_daily; "WEEKLY" -> R.string.schedule_weekly; else -> R.string.schedule_monthly })
