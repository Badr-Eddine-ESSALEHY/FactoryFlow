package com.factoryflow.app.feature.statistics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.PsychologyAlt
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
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
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun StatisticsScreen(
    onOpenIntelligence: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(topBar = { StatisticsTopBar() }) { padding ->
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
            else -> StatisticsContent(state, viewModel::days, viewModel::select, onOpenIntelligence, Modifier.padding(padding))
        }
    }
}

@Composable
private fun StatisticsTopBar() {
    Surface(color = MaterialTheme.colorScheme.background) {
        com.factoryflow.app.core.design.FlowPageHeader(
            title = stringResource(R.string.statistics_title),
            modifier = Modifier.statusBarsPadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
        )
    }
}

@Composable
fun StatisticsContent(
    state: StatisticsUiState,
    onDays: (Int) -> Unit,
    onKpi: (Long) -> Unit,
    onOpenIntelligence: () -> Unit,
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
                FlowCard(
                    Modifier.fillMaxWidth(),
                    PaddingValues(FlowSpacing.lg),
                    onClick = onOpenIntelligence,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FlowIconTile(Icons.Outlined.PsychologyAlt, null, FlowPurple, size = FlowSize.iconTileLarge)
                        Spacer(Modifier.width(FlowSpacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.mi_open), style = MaterialTheme.typography.titleMedium)
                            Text(
                                stringResource(R.string.mi_open_detail),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
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
            item { StatisticsDataQualityCard(selected) }
        }
    }
}

@Composable
private fun StatisticsAnalyticsCard(statistics: KpiStatisticsDto) {
    val hasTrendData = statistics.validObservationCount() >= 2L && statistics.points.size >= 2
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
                        stringResource(
                            if (hasTrendData) {
                                R.string.statistics_touch_hint
                            } else {
                                R.string.statistics_insufficient
                            },
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
            }
            Spacer(Modifier.height(FlowSpacing.lg))
            if (!hasTrendData) {
                StatisticsInsufficientData(statistics.validObservationCount())
            } else {
                StatisticsTrendChart(
                    points = statistics.points,
                    selectedIndex = selectedIndex,
                    onSelected = { selectedIndex = it },
                    description = statistics.displayName,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(FlowSpacing.md))
                StatisticsMetrics(statistics)
            }
        }
    }
}

@Composable
private fun StatisticsInsufficientData(validCount: Long) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FlowRadius.control),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Row(Modifier.padding(FlowSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
            FlowIconTile(Icons.Outlined.QueryStats, null, FlowOrange, size = FlowSize.listIconTile)
            Spacer(Modifier.width(FlowSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.statistics_insufficient), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(FlowSpacing.xs))
                Text(
                    stringResource(R.string.statistics_insufficient_detail, validCount),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
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
    val middle = minimum + (maximum - minimum) / 2f
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
            val axisWidth = 44.dp
            val tooltipWidth = 112.dp
            val plotWidth = maxWidth - axisWidth
            val selectedX = if (points.size == 1) axisWidth + plotWidth / 2
                else axisWidth + plotWidth * selectedIndex.coerceIn(0, points.lastIndex) / points.lastIndex
            val tooltipX = (selectedX - tooltipWidth / 2).coerceIn(0.dp, maxWidth - tooltipWidth)

            Column(
                Modifier.width(axisWidth).fillMaxHeight().padding(top = 38.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                listOf(maximum, middle, minimum).forEach { value ->
                    Text(
                        BigDecimal.valueOf(value.toDouble()).displayValue(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                    )
                }
            }

            Canvas(
                Modifier
                    .padding(start = axisWidth)
                    .fillMaxSize()
                    .semantics { contentDescription = description }
                    .pointerInput(points) {
                        fun selectNearest(x: Float) {
                            onSelected(if (points.size == 1) 0 else {
                                (x / size.width.toFloat() * points.lastIndex)
                                    .roundToInt()
                                    .coerceIn(0, points.lastIndex)
                            })
                        }
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            selectNearest(down.position.x)
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                change?.let { selectNearest(it.position.x) }
                            } while (change?.pressed == true)
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
                val selectedPoint = points[selectedIndex.coerceIn(0, points.lastIndex)]
                Column(Modifier.padding(horizontal = FlowSpacing.sm, vertical = FlowSpacing.xs)) {
                    Text(
                        text = selectedPoint.value.displayValue(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = selectedPoint.effectiveDate.toFrenchDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 44.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            chartDateLabels(points).forEach { label ->
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun StatisticsMetrics(statistics: KpiStatisticsDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FlowRadius.control),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
    ) {
        Column(Modifier.fillMaxWidth().padding(vertical = FlowSpacing.sm)) {
            MetricPairRow(
                stringResource(R.string.latest), statistics.latest.withUnit(statistics.unit),
                stringResource(R.string.average), statistics.average.withUnit(statistics.unit),
            )
            HorizontalDivider(Modifier.padding(horizontal = FlowSpacing.md), color = MaterialTheme.colorScheme.outlineVariant)
            MetricPairRow(
                stringResource(R.string.statistics_minimum_maximum),
                stringResource(R.string.statistics_minimum_maximum_value, statistics.minimum.displayValue(), statistics.maximum.displayValue(), statistics.unit.orEmpty()),
                stringResource(R.string.statistics_range), statistics.range.withUnit(statistics.unit),
            )
            if (statistics.standardDeviation != null || statistics.periodDelta != null) {
                HorizontalDivider(Modifier.padding(horizontal = FlowSpacing.md), color = MaterialTheme.colorScheme.outlineVariant)
                MetricPairRow(
                    stringResource(R.string.statistics_standard_deviation), statistics.standardDeviation.withUnit(statistics.unit),
                    stringResource(R.string.statistics_previous_delta), statistics.periodDelta.signedWithUnit(statistics.unit),
                )
            }
            HorizontalDivider(Modifier.padding(horizontal = FlowSpacing.md), color = MaterialTheme.colorScheme.outlineVariant)
            MetricPairRow(
                stringResource(R.string.statistics_trend), trendLabel(statistics.trend),
                stringResource(R.string.statistics_completeness), statistics.completenessRate.asPercentage(),
            )
        }
    }
}

@Composable
private fun MetricPairRow(firstLabel: String, firstValue: String, secondLabel: String, secondValue: String) {
    Row(Modifier.fillMaxWidth()) {
        MetricText(firstLabel, firstValue, Modifier.weight(1f))
        Box(Modifier.padding(vertical = FlowSpacing.sm).width(1.dp).height(44.dp).background(MaterialTheme.colorScheme.outlineVariant))
        MetricText(secondLabel, secondValue, Modifier.weight(1f))
    }
}

@Composable
private fun MetricText(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = FlowSpacing.md, vertical = FlowSpacing.sm)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(FlowSpacing.xs))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatisticsDataQualityCard(statistics: KpiStatisticsDto) {
    FlowCard(Modifier.fillMaxWidth(), PaddingValues(horizontal = FlowSpacing.md, vertical = FlowSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FlowIconTile(Icons.Outlined.Info, null, FlowOrange, size = FlowSize.listIconTile)
            Spacer(Modifier.width(FlowSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.statistics_data_quality), style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(FlowSpacing.xs))
                Text(
                    stringResource(R.string.statistics_observation_context, statistics.validObservationCount(), statistics.reportCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    stringResource(R.string.statistics_integrity),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (statistics.missingValueCount > 0) {
                    Spacer(Modifier.height(FlowSpacing.xs))
                    FlowStatusPill(stringResource(R.string.missing_samples, statistics.missingValueCount), FlowWarning, compact = true)
                }
            }
        }
    }
}

private fun KpiStatisticsDto.validObservationCount(): Long = validCount.takeIf { it > 0 } ?: sampleCount

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

@Composable
private fun trendLabel(trend: String): String = stringResource(
    when (trend) {
        "INCREASING" -> R.string.statistics_trend_increasing
        "DECREASING" -> R.string.statistics_trend_decreasing
        "STABLE" -> R.string.statistics_trend_stable
        else -> R.string.statistics_insufficient
    },
)

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
