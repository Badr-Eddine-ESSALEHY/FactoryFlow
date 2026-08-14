package com.factoryflow.app.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.FlowBlue
import com.factoryflow.app.core.design.FlowBlueDark
import com.factoryflow.app.core.design.FlowCard
import com.factoryflow.app.core.design.FlowCategoryCard
import com.factoryflow.app.core.design.FlowEmptyState
import com.factoryflow.app.core.design.FlowGreen
import com.factoryflow.app.core.design.FlowIconTile
import com.factoryflow.app.core.design.FlowIndigo
import com.factoryflow.app.core.design.FlowIndigoTint
import com.factoryflow.app.core.design.FlowListRow
import com.factoryflow.app.core.design.FlowMetricBadge
import com.factoryflow.app.core.design.FlowMiniChart
import com.factoryflow.app.core.design.FlowMotion
import com.factoryflow.app.core.design.FlowOrange
import com.factoryflow.app.core.design.FlowOrangeDark
import com.factoryflow.app.core.design.FlowOrangeTint
import com.factoryflow.app.core.design.FlowPurple
import com.factoryflow.app.core.design.FlowPurpleDark
import com.factoryflow.app.core.design.FlowPurpleTint
import com.factoryflow.app.core.design.FlowPink
import com.factoryflow.app.core.design.FlowRadius
import com.factoryflow.app.core.design.FlowScreen
import com.factoryflow.app.core.design.FlowSectionHeader
import com.factoryflow.app.core.design.FlowSegmentedControl
import com.factoryflow.app.core.design.FlowSize
import com.factoryflow.app.core.design.FlowSpacing
import com.factoryflow.app.core.design.FlowStatusPill
import com.factoryflow.app.core.design.FlowTeal
import com.factoryflow.app.core.design.FlowTealDark
import com.factoryflow.app.core.design.FlowTopBar
import com.factoryflow.app.core.design.FlowWarning
import com.factoryflow.app.core.design.ErrorPane
import com.factoryflow.app.core.design.SkeletonRows
import com.factoryflow.app.core.network.dto.DashboardActivityDto
import com.factoryflow.app.core.network.dto.DashboardDto
import com.factoryflow.app.core.network.dto.LatestKpiDto
import com.factoryflow.app.core.network.dto.RecentGeneratedReportDto
import com.factoryflow.app.core.network.dto.RecentReportDto
import com.factoryflow.app.core.network.dto.UpcomingScheduleDto
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.core.util.toFrenchDate
import com.factoryflow.app.core.util.toFrenchInstant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
        state.loading -> DashboardLoading()
        state.data == null && state.error != null -> ErrorPane(
            title = stringResource(state.error!!.title),
            detail = stringResource(state.error!!.detail),
            retry = stringResource(R.string.retry),
            onRetry = viewModel::load,
            modifier = Modifier.fillMaxSize().padding(FlowSpacing.xl),
        )
        state.data != null -> DashboardContent(
            userName = userName,
            data = state.data!!,
            onCreate = onCreate,
            onPaste = onPaste,
            onManual = onManual,
            onReport = onReport,
            onGenerated = onGenerated,
            onStatistics = onStatistics,
            onSchedules = onSchedules,
            onRefresh = { viewModel.load(true) },
            onProfile = onProfile,
            refreshing = state.refreshing,
        )
    }
}

@Composable
fun DashboardContent(
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
    refreshing: Boolean = false,
) {
    FlowScreen {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = FlowSpacing.xl,
                end = FlowSpacing.xl,
                top = FlowSpacing.md,
                bottom = FlowSpacing.screenBottom,
            ),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.lg),
        ) {
            item {
                FlowTopBar(
                    greeting = stringResource(R.string.dashboard_greeting, userName),
                    title = stringResource(R.string.dashboard_your_day),
                    subtitle = dashboardDateLabel(data.businessDate),
                    initials = userInitials(userName),
                    actionIcon = Icons.Outlined.Refresh,
                    actionDescription = stringResource(R.string.refresh),
                    onAction = onRefresh,
                    onProfile = onProfile,
                    actionLoading = refreshing,
                )
            }

            item { EnteringSection { DailySituation(data) } }
            item { EnteringSection { QuickActions(onCreate, onPaste, onManual) } }

            if (data.activityTrend.isNotEmpty()) {
                item { EnteringSection { ActivitySection(data.activityTrend, onStatistics) } }
            }
            if (data.latestKpis.isNotEmpty()) {
                item { EnteringSection { LatestKpisSection(data.latestKpis, onStatistics) } }
            }
            if (data.recentReports.isNotEmpty()) {
                item { EnteringSection { RecentReportsSection(data.recentReports, onReport) } }
            }
            if (data.recentGeneratedReports.isNotEmpty()) {
                item { EnteringSection { RecentDocumentsSection(data.recentGeneratedReports, onGenerated) } }
            }
            data.upcomingSchedule?.let { schedule ->
                item { EnteringSection { UpcomingScheduleSection(schedule, onSchedules) } }
            }
            if (data.latestKpis.isEmpty() && data.recentReports.isEmpty() && data.recentGeneratedReports.isEmpty()) {
                item {
                    FlowEmptyState(
                        title = stringResource(R.string.no_dashboard_data),
                        detail = stringResource(R.string.no_dashboard_data_detail),
                        action = stringResource(R.string.create_report),
                        onAction = onCreate,
                    )
                }
            }
        }
    }
}

@Composable
private fun EnteringSection(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(FlowMotion.standard)) + slideInVertically(tween(FlowMotion.standard)) { it / 12 },
    ) { content() }
}

@Composable
private fun DailySituation(data: DashboardDto) {
    Column {
        FlowSectionHeader(stringResource(R.string.dashboard_situation))
        Spacer(Modifier.height(FlowSpacing.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
            SituationCard(
                title = stringResource(R.string.dashboard_reports),
                meta = dashboardQuantity(R.plurals.dashboard_confirmed_count, data.todayConfirmedReportCount),
                icon = Icons.Outlined.AssignmentTurnedIn,
                accent = FlowBlue,
                modifier = Modifier.weight(1f),
            )
            SituationCard(
                title = stringResource(R.string.pending_review),
                meta = dashboardQuantity(R.plurals.dashboard_review_count, data.todayDraftOrPendingReportCount),
                icon = Icons.Outlined.EditNote,
                accent = FlowOrange,
                modifier = Modifier.weight(1f),
            )
            SituationCard(
                title = stringResource(R.string.documents_short),
                meta = dashboardQuantity(R.plurals.dashboard_generated_count, data.todayGeneratedDocumentCount),
                icon = Icons.Outlined.FolderOpen,
                accent = FlowTealDark,
                modifier = Modifier.weight(1f),
            )
        }
        if (data.todayConfirmedMissingValueCount > 0L) {
            Spacer(Modifier.height(FlowSpacing.sm))
            FlowStatusPill(
                label = dashboardQuantity(
                    R.plurals.dashboard_missing_value_count,
                    data.todayConfirmedMissingValueCount,
                ),
                color = FlowWarning,
                icon = Icons.Outlined.Schedule,
                compact = true,
            )
        }
    }
}

@Composable
private fun SituationCard(
    title: String,
    meta: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    FlowCard(
        modifier = modifier.height(FlowSize.situationCardHeight),
        contentPadding = PaddingValues(FlowSpacing.sm),
        radius = FlowRadius.compactCard,
    ) {
        Column {
            FlowIconTile(icon, null, accent)
            Spacer(Modifier.height(FlowSpacing.sm))
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(FlowSpacing.micro))
            Text(
                meta,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun QuickActions(onCreate: () -> Unit, onPaste: () -> Unit, onManual: () -> Unit) {
    Column {
        FlowSectionHeader(stringResource(R.string.quick_actions))
        Spacer(Modifier.height(FlowSpacing.sm))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
            FlowCategoryCard(
                icon = Icons.Outlined.Description,
                title = stringResource(R.string.dashboard_new_report),
                meta = stringResource(R.string.dashboard_new_report_meta),
                accent = FlowBlue,
                tint = FlowIndigoTint,
                gradientEnd = FlowBlueDark,
                onClick = onCreate,
                modifier = Modifier.weight(1f),
            )
            FlowCategoryCard(
                icon = Icons.Outlined.ContentPaste,
                title = stringResource(R.string.dashboard_paste_title),
                meta = stringResource(R.string.dashboard_paste_meta),
                accent = FlowOrange,
                tint = FlowOrangeTint,
                gradientEnd = FlowOrangeDark,
                onClick = onPaste,
                modifier = Modifier.weight(1f),
            )
            FlowCategoryCard(
                icon = Icons.Outlined.EditNote,
                title = stringResource(R.string.dashboard_manual_title),
                meta = stringResource(R.string.dashboard_manual_meta),
                accent = FlowPurple,
                tint = FlowPurpleTint,
                gradientEnd = FlowPurpleDark,
                onClick = onManual,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ActivitySection(points: List<DashboardActivityDto>, onStatistics: () -> Unit) {
    var selectedIndex by remember(points) { mutableIntStateOf(points.lastIndex) }
    Column {
        FlowSectionHeader(
            title = stringResource(R.string.activity_last_7_days),
            action = stringResource(R.string.nav_statistics),
            onAction = onStatistics,
        )
        Spacer(Modifier.height(FlowSpacing.sm))
        FlowCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(FlowSpacing.md),
        ) {
            Column {
                FlowSegmentedControl(
                    options = points.map { chartDayLabel(it.date) },
                    selectedIndex = selectedIndex,
                    onSelected = { selectedIndex = it },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FlowSpacing.sm))
                val selected = points[selectedIndex]
                Text(
                    text = stringResource(
                        R.string.dashboard_activity_point,
                        selected.date.toFrenchDate(),
                        selected.confirmedReportCount,
                        selected.missingValueCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(FlowSpacing.sm))
                FlowMiniChart(
                    primaryValues = points.map { it.confirmedReportCount.toFloat() },
                    secondaryValues = points.map { it.missingValueCount.toFloat() },
                    selectedIndex = selectedIndex,
                    description = stringResource(R.string.activity_chart_detail),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FlowSpacing.sm))
                Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
                    ChartLegend(FlowBlue, stringResource(R.string.confirmed_short))
                    ChartLegend(FlowGreen, stringResource(R.string.missing_short))
                }
            }
        }
    }
}

@Composable
private fun ChartLegend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(FlowSpacing.sm).background(color, CircleShape))
        Spacer(Modifier.size(FlowSpacing.sm))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LatestKpisSection(items: List<LatestKpiDto>, onStatistics: () -> Unit) {
    Column {
        FlowSectionHeader(
            title = stringResource(R.string.latest_kpis),
            action = stringResource(R.string.nav_statistics),
            onAction = onStatistics,
        )
        Spacer(Modifier.height(FlowSpacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
            items.take(3).forEachIndexed { index, kpi ->
                val accent = listOf(FlowGreen, FlowIndigo, FlowPink)[index % 3]
                FlowListRow(
                    icon = Icons.Outlined.AutoGraph,
                    title = kpi.displayName,
                    meta = kpi.effectiveDate.toFrenchDate(),
                    accent = accent,
                    onClick = onStatistics,
                    trailing = {
                        FlowMetricBadge(
                            label = listOfNotNull(kpi.value.displayValue(), kpi.unit).joinToString(" "),
                            color = accent,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentReportsSection(reports: List<RecentReportDto>, onReport: (Long) -> Unit) {
    Column {
        FlowSectionHeader(stringResource(R.string.recent_reports))
        Spacer(Modifier.height(FlowSpacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
            reports.take(3).forEach { report ->
                val confirmed = report.status == "CONFIRMED"
                FlowListRow(
                    icon = if (confirmed) Icons.Outlined.Verified else Icons.Outlined.EditNote,
                    title = report.effectiveDate.toFrenchDate(),
                    meta = report.submittedBy,
                    accent = if (confirmed) FlowGreen else FlowOrange,
                    onClick = { onReport(report.id) },
                    trailing = {
                        FlowStatusPill(
                            label = statusLabel(report.status),
                            color = statusColor(report.status),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun RecentDocumentsSection(documents: List<RecentGeneratedReportDto>, onGenerated: (Long) -> Unit) {
    Column {
        FlowSectionHeader(stringResource(R.string.recent_documents))
        Spacer(Modifier.height(FlowSpacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
            documents.take(3).forEach { document ->
                val pdf = document.format == "PDF"
                FlowListRow(
                    icon = if (pdf) Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView,
                    title = documentType(document.type),
                    meta = document.generatedAt.toFrenchInstant(),
                    accent = if (pdf) FlowIndigo else FlowPurple,
                    onClick = { onGenerated(document.id) },
                    trailing = { FlowMetricBadge(document.format, if (pdf) FlowIndigo else FlowPurple) },
                )
            }
        }
    }
}

@Composable
private fun UpcomingScheduleSection(schedule: UpcomingScheduleDto, onSchedules: () -> Unit) {
    Column {
        FlowSectionHeader(
            title = stringResource(R.string.upcoming_schedule),
            action = stringResource(R.string.manage_schedules),
            onAction = onSchedules,
        )
        Spacer(Modifier.height(FlowSpacing.sm))
        FlowListRow(
            icon = Icons.Outlined.EventRepeat,
            title = scheduleType(schedule.type),
            meta = schedule.nextRunAt?.toFrenchInstant() ?: stringResource(R.string.not_provided),
            accent = FlowTealDark,
            gradientEnd = FlowTeal,
            onClick = onSchedules,
            trailing = {
                FlowMetricBadge(
                    label = scheduleFormats(schedule),
                    color = FlowTealDark,
                )
            },
        )
    }
}

@Composable
private fun DashboardLoading() {
    FlowScreen {
        SkeletonRows(
            modifier = Modifier.fillMaxWidth().padding(FlowSpacing.xl),
            count = 7,
        )
    }
}

private fun userInitials(userName: String): String = userName
    .trim()
    .split(" ", "-")
    .filter(String::isNotBlank)
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercase() }
    .joinToString("")

private fun dashboardDateLabel(value: String): String = runCatching {
    LocalDate.parse(value)
        .format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE))
        .replaceFirstChar { it.titlecase(Locale.FRANCE) }
}.getOrElse { value.toFrenchDate() }

private fun chartDayLabel(value: String): String = runCatching {
    LocalDate.parse(value).dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.FRANCE)
}.getOrDefault(value)

@Composable
private fun dashboardQuantity(resourceId: Int, count: Long): String {
    val quantity = count.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
    return pluralStringResource(resourceId, quantity, quantity)
}

@Composable
private fun statusLabel(status: String): String = stringResource(
    when (status) {
        "CONFIRMED" -> R.string.confirmed
        "DRAFT" -> R.string.draft
        else -> R.string.pending_review
    },
)

private fun statusColor(status: String): Color = when (status) {
    "CONFIRMED" -> FlowGreen
    "DRAFT" -> FlowIndigo
    else -> FlowWarning
}

@Composable
private fun documentType(type: String): String = stringResource(
    when (type) {
        "DAILY" -> R.string.daily
        "WEEKLY" -> R.string.weekly
        "MONTHLY" -> R.string.monthly
        else -> R.string.manual
    },
)

@Composable
private fun scheduleType(type: String): String = stringResource(
    when (type) {
        "DAILY" -> R.string.schedule_daily
        "WEEKLY" -> R.string.schedule_weekly
        else -> R.string.schedule_monthly
    },
)

@Composable
private fun scheduleFormats(schedule: UpcomingScheduleDto): String = listOfNotNull(
    stringResource(R.string.excel).takeIf { schedule.generateExcel },
    stringResource(R.string.pdf).takeIf { schedule.generatePdf },
).joinToString(" · ").ifBlank { stringResource(R.string.not_provided) }
