package com.factoryflow.app.feature.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.EmptyPane
import com.factoryflow.app.core.design.ErrorPane
import com.factoryflow.app.core.design.FactoryFlowScaffold
import com.factoryflow.app.core.design.FlowCard
import com.factoryflow.app.core.design.FlowContentSurface
import com.factoryflow.app.core.design.FlowIconTile
import com.factoryflow.app.core.design.FlowMotion
import com.factoryflow.app.core.design.FlowOrange
import com.factoryflow.app.core.design.FlowPurple
import com.factoryflow.app.core.design.FlowRadius
import com.factoryflow.app.core.design.FlowSegmentedControl
import com.factoryflow.app.core.design.FlowSize
import com.factoryflow.app.core.design.FlowSpacing
import com.factoryflow.app.core.design.FlowStatusPill
import com.factoryflow.app.core.design.FlowWarning
import com.factoryflow.app.core.design.SkeletonRows
import com.factoryflow.app.core.network.dto.KpiStatisticsDto
import com.factoryflow.app.core.network.dto.StatisticsPointDto
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.core.util.toFrenchDate
import com.factoryflow.app.feature.acquisition.FocusedTopBar
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(onBack: () -> Unit, viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.statistics_title), onBack) }) { padding ->
        when {
            state.loading -> SkeletonRows(Modifier.padding(padding).padding(FlowSpacing.xl), 5)
            state.error != null && state.kpis.isEmpty() -> ErrorPane(
                stringResource(state.error!!.title),
                stringResource(state.error!!.detail),
                stringResource(R.string.retry),
                viewModel::load,
                Modifier.padding(padding).fillMaxSize(),
            )
            state.kpis.isEmpty() -> EmptyPane(
                stringResource(R.string.no_statistics),
                stringResource(R.string.statistics_integrity),
                Modifier.padding(padding),
                Icons.Outlined.Analytics,
                stringResource(R.string.retry),
                viewModel::load,
            )
            else -> StatisticsContent(state, viewModel::days, viewModel::select, Modifier.padding(padding))
        }
    }
}

@Composable
fun StatisticsContent(
    state: StatisticsUiState,
    onDays: (Int) -> Unit,
    onKpi: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = state.selected ?: return
    var menuExpanded by remember { mutableStateOf(false) }
    val periods = listOf(7, 30, 90)

    FlowContentSurface(modifier) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, FlowSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
        ) {
            item {
                FlowSegmentedControl(
                    options = listOf(
                        stringResource(R.string.period_7_days),
                        stringResource(R.string.period_30_days),
                        stringResource(R.string.period_90_days),
                    ),
                    selectedIndex = periods.indexOf(state.days).coerceAtLeast(0),
                    onSelected = { onDays(periods[it]) },
                )
            }
            item {
                Box {
                    FlowCard(
                        Modifier.fillMaxWidth(),
                        PaddingValues(horizontal = FlowSpacing.lg, vertical = FlowSpacing.md),
                        onClick = { menuExpanded = true },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FlowIconTile(Icons.Outlined.Analytics, null, FlowPurple, size = FlowSize.listIconTile)
                            Spacer(Modifier.width(FlowSpacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(selected.displayName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    stringResource(R.string.samples, selected.sampleCount),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            if (!selected.unit.isNullOrBlank()) {
                                Text(selected.unit, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.width(FlowSpacing.xs))
                            }
                            Icon(Icons.Outlined.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    DropdownMenu(menuExpanded, { menuExpanded = false }) {
                        state.kpis.forEach { kpi ->
                            DropdownMenuItem(
                                text = { Text(kpi.displayName) },
                                onClick = {
                                    onKpi(kpi.kpiDefinitionId)
                                    menuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            item { StatisticsAnalyticsCard(selected) }
            item { StatisticsIntegrityRow(selected.missingValueCount) }
        }
    }
}

@Composable
private fun StatisticsAnalyticsCard(statistics: KpiStatisticsDto) {
    var selectedIndex by remember(statistics.kpiDefinitionId, statistics.points) {
        mutableIntStateOf(statistics.points.lastIndex.coerceAtLeast(0))
    }
    val selectedPoint = statistics.points.getOrNull(selectedIndex)

    FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.statistics_confirmed_trend), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(FlowSpacing.xs))
                    Text(
                        selectedPoint?.effectiveDate?.toFrenchDate().orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            selectedPoint?.value.displayValue(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (!statistics.unit.isNullOrBlank()) {
                            Spacer(Modifier.width(FlowSpacing.xs))
                            Text(
                                statistics.unit,
                                modifier = Modifier.padding(bottom = FlowSpacing.xs),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }
                    Text(
                        stringResource(R.string.statistics_touch_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.height(FlowSpacing.lg))
            StatisticsTrendChart(
                points = statistics.points,
                selectedIndex = selectedIndex,
                onSelected = { selectedIndex = it },
                description = statistics.displayName,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(FlowSpacing.md))
            StatisticsSummaryStrip(statistics)
        }
    }
}

@Composable
private fun StatisticsTrendChart(
    points: List<StatisticsPointDto>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    description: String,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Box(modifier.height(FlowSize.analyticsChartHeight), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_statistics), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val values = points.map { it.value.toFloat() }
    val minimum = values.minOrNull() ?: 0f
    val maximum = values.maxOrNull() ?: minimum
    val range = (maximum - minimum).takeIf { it > 0f } ?: 1f
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val surface = MaterialTheme.colorScheme.surface
    val inspection = LocalInspectionMode.current
    var entered by remember(points) { mutableStateOf(inspection) }
    LaunchedEffect(points) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(FlowMotion.chart),
        label = "statistics-chart-reveal",
    )

    Column(modifier) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(FlowSize.analyticsChartHeight)) {
            val tooltipWidth = 92.dp
            val selectedX = if (points.size == 1) maxWidth / 2 else maxWidth * selectedIndex.coerceIn(0, points.lastIndex) / points.lastIndex
            val tooltipX = (selectedX - tooltipWidth / 2).coerceIn(0.dp, maxWidth - tooltipWidth)

            Canvas(
                Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = description }
                    .pointerInput(points) {
                        detectTapGestures { tap ->
                            val index = if (points.size == 1) 0 else {
                                (tap.x / size.width.toFloat() * points.lastIndex).roundToInt().coerceIn(0, points.lastIndex)
                            }
                            onSelected(index)
                        }
                    },
            ) {
                val topInset = 34.dp.toPx()
                val bottomInset = 12.dp.toPx()
                val chartHeight = size.height - topInset - bottomInset
                fun pointAt(index: Int, value: Float): Offset {
                    val x = if (values.size == 1) size.width / 2f else size.width * index / values.lastIndex
                    val normalized = (value - minimum) / range
                    return Offset(x, topInset + chartHeight * (1f - normalized * 0.82f) - chartHeight * 0.09f)
                }
                val offsets = values.mapIndexed(::pointAt)

                repeat(3) { index ->
                    val y = topInset + chartHeight * index / 2f
                    drawLine(grid, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }

                val linePath = smoothStatisticsPath(offsets)
                val areaPath = smoothStatisticsPath(offsets).apply {
                    lineTo(offsets.last().x, size.height - bottomInset)
                    lineTo(offsets.first().x, size.height - bottomInset)
                    close()
                }
                clipRect(right = size.width * reveal) {
                    drawPath(
                        areaPath,
                        brush = Brush.verticalGradient(
                            listOf(primary.copy(alpha = 0.28f), primary.copy(alpha = 0.02f)),
                            startY = topInset,
                            endY = size.height,
                        ),
                    )
                }
                val pathMeasure = PathMeasure().apply { setPath(linePath, false) }
                val visiblePath = Path()
                pathMeasure.getSegment(0f, pathMeasure.length * reveal, visiblePath, true)
                drawPath(visiblePath, primary, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))

                val selected = offsets[selectedIndex.coerceIn(0, offsets.lastIndex)]
                drawLine(
                    primary.copy(alpha = 0.2f),
                    Offset(selected.x, topInset),
                    Offset(selected.x, size.height - bottomInset),
                    strokeWidth = 1.dp.toPx(),
                )
                drawCircle(surface, 7.dp.toPx(), selected)
                drawCircle(primary, 4.dp.toPx(), selected)
            }

            Surface(
                modifier = Modifier.padding(start = tooltipX, top = FlowSpacing.micro).width(tooltipWidth),
                shape = RoundedCornerShape(FlowRadius.control),
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Text(
                    text = points[selectedIndex.coerceIn(0, points.lastIndex)].value.displayValue(),
                    modifier = Modifier.padding(horizontal = FlowSpacing.sm, vertical = FlowSpacing.xs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            chartDateLabels(points).forEach { label ->
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatisticsSummaryStrip(statistics: KpiStatisticsDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FlowRadius.control),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Row(Modifier.fillMaxWidth().height(74.dp)) {
            SummaryMetric(stringResource(R.string.latest), statistics.latest, statistics.unit, Modifier.weight(1f))
            SummaryDivider()
            SummaryMetric(stringResource(R.string.average), statistics.average, statistics.unit, Modifier.weight(1f))
            SummaryDivider()
            SummaryMetric(stringResource(R.string.minimum), statistics.minimum, statistics.unit, Modifier.weight(1f))
            SummaryDivider()
            SummaryMetric(stringResource(R.string.maximum), statistics.maximum, statistics.unit, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: BigDecimal?, unit: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight().padding(horizontal = FlowSpacing.sm, vertical = FlowSpacing.md),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        Spacer(Modifier.height(FlowSpacing.xs))
        Text(
            buildString {
                append(value.displayValue())
                if (!unit.isNullOrBlank()) append(" ").append(unit)
            },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        Modifier
            .padding(vertical = FlowSpacing.md)
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun StatisticsIntegrityRow(missingCount: Long) {
    FlowCard(Modifier.fillMaxWidth(), PaddingValues(horizontal = FlowSpacing.md, vertical = FlowSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlowIconTile(Icons.Outlined.Info, null, FlowOrange, size = FlowSize.listIconTile)
            Spacer(Modifier.width(FlowSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.statistics_integrity),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (missingCount > 0) {
                    Spacer(Modifier.height(FlowSpacing.xs))
                    FlowStatusPill(stringResource(R.string.missing_samples, missingCount), FlowWarning, compact = true)
                }
            }
        }
    }
}

private fun smoothStatisticsPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)
    for (index in 0 until points.lastIndex) {
        val current = points[index]
        val next = points[index + 1]
        val midpoint = (current.x + next.x) / 2f
        cubicTo(midpoint, current.y, midpoint, next.y, next.x, next.y)
    }
}

private fun chartDateLabels(points: List<StatisticsPointDto>): List<String> {
    val indexes = listOf(0, points.lastIndex / 2, points.lastIndex).distinct()
    return indexes.map { index ->
        runCatching {
            val date = LocalDate.parse(points[index].effectiveDate)
            String.format(Locale.FRANCE, "%02d/%02d", date.dayOfMonth, date.monthValue)
        }.getOrDefault(points[index].effectiveDate)
    }
}
