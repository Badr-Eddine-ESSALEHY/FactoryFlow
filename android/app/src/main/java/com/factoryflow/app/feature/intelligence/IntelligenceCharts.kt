package com.factoryflow.app.feature.intelligence

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.model.*
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.core.util.toFrenchDate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.sqrt

internal enum class AnalyticalMarker { ACTUAL, ANOMALY, FORECAST, LATEST, CONTEXTUAL }

internal data class TimeSeriesSelection(
    val date: String,
    val value: BigDecimal,
    val marker: AnalyticalMarker,
    val score: BigDecimal? = null,
    val reportId: Long? = null,
)

@Composable
internal fun HistoricalForecastChart(
    history: List<IntelligenceObservation>,
    anomaly: List<AnomalyPoint>,
    forecast: List<ForecastPoint>,
    contextualEntryIds: Set<Long>,
    unit: String?,
    description: String,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) return
    val anomalyById = remember(anomaly) { anomaly.associateBy(AnomalyPoint::entryId) }
    val points = remember(history, forecast, anomaly, contextualEntryIds) {
        history.mapIndexed { index, point ->
            val evidence = anomalyById[point.entryId]
            TimeSeriesSelection(
                date = point.effectiveDate,
                value = point.value,
                marker = when {
                    point.entryId in contextualEntryIds -> AnalyticalMarker.CONTEXTUAL
                    evidence?.anomalous == true -> AnalyticalMarker.ANOMALY
                    index == history.lastIndex -> AnalyticalMarker.LATEST
                    else -> AnalyticalMarker.ACTUAL
                },
                score = evidence?.score,
                reportId = point.reportId,
            )
        } + forecast.map { TimeSeriesSelection(it.effectiveDate, it.value, AnalyticalMarker.FORECAST) }
    }
    var selectedIndex by remember(points) { mutableIntStateOf((history.size - 1).coerceAtLeast(0)) }
    val selected = points[selectedIndex.coerceIn(0, points.lastIndex)]
    val actualColor = MaterialTheme.colorScheme.primary
    val forecastColor = FlowPurple
    val anomalyColor = FlowDanger
    val contextualColor = FlowWarning
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val intervalColor = FlowPurple.copy(alpha = 0.14f)

    Column(modifier) {
        ChartSelectionHeader(selected, unit)
        Spacer(Modifier.height(FlowSpacing.sm))
        Box(Modifier.fillMaxWidth().height(FlowSize.analyticsChartHeight)) {
            val allValues = buildList {
                addAll(points.map { it.value.toFloat() })
                forecast.forEach { point ->
                    point.lowerBound?.toFloat()?.let(::add)
                    point.upperBound?.toFloat()?.let(::add)
                }
            }
            val bounds = paddedBounds(allValues)
            val dates = points.mapNotNull { it.date.toEpochDayOrNull() }
            val minDay = dates.minOrNull() ?: 0L
            val maxDay = dates.maxOrNull()?.takeIf { it > minDay } ?: (minDay + 1)

            Canvas(
                Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = description }
                    .pointerInput(points) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            fun select(x: Float) {
                                val target = minDay + ((x / size.width).coerceIn(0f, 1f) * (maxDay - minDay)).toLong()
                                selectedIndex = points.indices.minByOrNull { index ->
                                    abs((points[index].date.toEpochDayOrNull() ?: minDay) - target)
                                } ?: 0
                            }
                            select(down.position.x)
                            do {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                change?.let { select(it.position.x) }
                            } while (change?.pressed == true)
                        }
                    },
            ) {
                val top = 12.dp.toPx()
                val bottom = size.height - 18.dp.toPx()
                fun x(date: String): Float = ((date.toEpochDayOrNull() ?: minDay) - minDay).toFloat() /
                    (maxDay - minDay).toFloat() * size.width
                fun y(value: BigDecimal): Float = bottom - ((value.toFloat() - bounds.first) /
                    (bounds.second - bounds.first)) * (bottom - top)

                drawChartGrid(gridColor, top, bottom)
                if (forecast.isNotEmpty()) {
                    val boundaryX = x(history.last().effectiveDate)
                    drawRect(forecastColor.copy(alpha = 0.035f), Offset(boundaryX, top), androidx.compose.ui.geometry.Size(size.width - boundaryX, bottom - top))
                    drawLine(gridColor, Offset(boundaryX, top), Offset(boundaryX, bottom), 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f)))
                    val intervalPoints = forecast.filter { it.intervalAvailable && it.lowerBound != null && it.upperBound != null }
                    if (intervalPoints.size >= 2) {
                        val band = Path().apply {
                            intervalPoints.forEachIndexed { index, p ->
                                val location = Offset(x(p.effectiveDate), y(checkNotNull(p.upperBound)))
                                if (index == 0) moveTo(location.x, location.y) else lineTo(location.x, location.y)
                            }
                            intervalPoints.asReversed().forEach { p -> lineTo(x(p.effectiveDate), y(checkNotNull(p.lowerBound))) }
                            close()
                        }
                        drawPath(band, intervalColor)
                    }
                }

                drawPolyline(history.map { Offset(x(it.effectiveDate), y(it.value)) }, actualColor, false)
                if (forecast.isNotEmpty()) {
                    val joined = listOf(Offset(x(history.last().effectiveDate), y(history.last().value))) +
                        forecast.map { Offset(x(it.effectiveDate), y(it.value)) }
                    drawPolyline(joined, forecastColor, true)
                }
                points.forEachIndexed { index, point ->
                    val location = Offset(x(point.date), y(point.value))
                    drawAnalyticalMarker(
                        location,
                        point.marker,
                        actualColor,
                        forecastColor,
                        anomalyColor,
                        contextualColor,
                        surfaceColor,
                        selected = index == selectedIndex,
                    )
                }
            }
            Text(
                BigDecimal.valueOf(bounds.second.toDouble()).displayValue(),
                Modifier.align(Alignment.TopStart).background(surfaceColor.copy(alpha = 0.82f), RoundedCornerShape(FlowRadius.compactCard)).padding(horizontal = FlowSpacing.xs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                BigDecimal.valueOf(bounds.first.toDouble()).displayValue(),
                Modifier.align(Alignment.BottomStart).background(surfaceColor.copy(alpha = 0.82f), RoundedCornerShape(FlowRadius.compactCard)).padding(horizontal = FlowSpacing.xs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(points.first().date.toFrenchDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(points.last().date.toFrenchDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(FlowSpacing.sm))
        AnalyticalLegend()
    }
}

@Composable
private fun ChartSelectionHeader(selection: TimeSeriesSelection, unit: String?) {
    Surface(shape = RoundedCornerShape(FlowRadius.control), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = FlowSpacing.md, vertical = FlowSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
            MarkerSwatch(selection.marker)
            Spacer(Modifier.width(FlowSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(selection.date.toFrenchDate(), style = MaterialTheme.typography.labelMedium)
                selection.reportId?.let {
                    Text(stringResource(R.string.mi_selected_point_report, it), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                buildString {
                    append(selection.value.displayValue())
                    if (!unit.isNullOrBlank()) append(" ").append(unit)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
internal fun AnomalyEvidenceChart(
    analysis: AnomalyAnalysis,
    description: String,
    modifier: Modifier = Modifier,
) {
    val points = analysis.points.filter { it.score != null }
    if (points.isEmpty() || analysis.threshold == null) return
    var selected by remember(points) { mutableIntStateOf(points.lastIndex) }
    val values = points.map { checkNotNull(it.score).toFloat() } + analysis.threshold.toFloat()
    val bounds = paddedBounds(values)
    val evidence = FlowPurple
    val anomaly = FlowDanger
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val thresholdColor = FlowWarning
    val surface = MaterialTheme.colorScheme.surface
    val dates = points.mapNotNull { it.effectiveDate.toEpochDayOrNull() }
    val minDay = dates.minOrNull() ?: 0L
    val maxDay = dates.maxOrNull()?.takeIf { it > minDay } ?: (minDay + 1)
    Column(modifier) {
        val point = points[selected]
        Surface(shape = RoundedCornerShape(FlowRadius.control), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.64f)) {
            Row(Modifier.fillMaxWidth().padding(FlowSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                MarkerSwatch(if (point.anomalous == true) AnalyticalMarker.ANOMALY else AnalyticalMarker.ACTUAL)
                Spacer(Modifier.width(FlowSpacing.sm))
                Text(point.effectiveDate.toFrenchDate(), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text(checkNotNull(point.score).displayValue(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(FlowSpacing.sm))
        Canvas(
            Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = description }.pointerInput(points) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    fun selectNearest(x: Float) {
                        val target = minDay + ((x / size.width).coerceIn(0f, 1f) * (maxDay - minDay)).toLong()
                        selected = points.indices.minByOrNull { index ->
                            abs((points[index].effectiveDate.toEpochDayOrNull() ?: minDay) - target)
                        } ?: 0
                    }
                    selectNearest(down.position.x)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                        change?.let { selectNearest(it.position.x) }
                    } while (change?.pressed == true)
                }
            },
        ) {
            val top = 12.dp.toPx(); val bottom = size.height - 12.dp.toPx()
            fun x(date: String) = ((date.toEpochDayOrNull() ?: minDay) - minDay).toFloat() /
                (maxDay - minDay).toFloat() * size.width
            fun y(v: BigDecimal) = bottom - ((v.toFloat() - bounds.first) / (bounds.second - bounds.first)) * (bottom - top)
            drawChartGrid(grid, top, bottom)
            val thresholdY = y(analysis.threshold)
            drawLine(thresholdColor, Offset(0f, thresholdY), Offset(size.width, thresholdY), 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
            drawPolyline(points.map { p -> Offset(x(p.effectiveDate), y(checkNotNull(p.score))) }, evidence, false)
            points.forEachIndexed { index, p ->
                drawAnalyticalMarker(
                    Offset(x(p.effectiveDate), y(checkNotNull(p.score))),
                    if (p.anomalous == true) AnalyticalMarker.ANOMALY else AnalyticalMarker.ACTUAL,
                    evidence,
                    evidence,
                    anomaly,
                    thresholdColor,
                    surface,
                    index == selected,
                )
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(points.first().effectiveDate.toFrenchDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(points.last().effectiveDate.toFrenchDate(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

internal enum class ForecastMetric { SMAPE, MAE, RMSE }

internal fun ForecastMetrics.value(metric: ForecastMetric): BigDecimal = when (metric) {
    ForecastMetric.SMAPE -> smape
    ForecastMetric.MAE -> mae
    ForecastMetric.RMSE -> rmse
}

@Composable
internal fun ModelComparisonChart(
    candidates: List<ForecastCandidate>,
    metric: ForecastMetric,
    selection: ModelSelection?,
    modifier: Modifier = Modifier,
) {
    val evaluated = candidates.filter { it.state == "EVALUATED" && it.metrics != null }
        .sortedBy { checkNotNull(it.metrics).value(metric) }
    if (evaluated.isEmpty()) return
    val maximum = evaluated.maxOf { checkNotNull(it.metrics).value(metric).toFloat() }.takeIf { it > 0f } ?: 1f
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        evaluated.forEach { candidate ->
            val value = checkNotNull(candidate.metrics).value(metric)
            val isSelected = candidate.matches(selection?.selected)
            val isRawBest = candidate.matches(selection?.rawBest)
            Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.xs)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(modelLabel(candidate.family, candidate.configuration), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (isSelected) Icon(Icons.Outlined.CheckCircle, null, tint = FlowGreen, modifier = Modifier.size(FlowSize.iconSmall))
                    if (isRawBest && !isSelected) Icon(Icons.Outlined.StarOutline, null, tint = FlowWarning, modifier = Modifier.size(FlowSize.iconSmall))
                    Spacer(Modifier.width(FlowSpacing.sm))
                    Text(metricValue(value, metric), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                }
                Box(Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(FlowRadius.pill))) {
                    Box(
                        Modifier.fillMaxHeight().fillMaxWidth((value.toFloat() / maximum).coerceIn(0.02f, 1f))
                            .background(if (isSelected) FlowGreen else if (isRawBest) FlowWarning else FlowPurple, RoundedCornerShape(FlowRadius.pill)),
                    )
                }
            }
        }
    }
}

@Composable
internal fun HorizonErrorChart(
    candidates: List<ForecastCandidate>,
    selection: ModelSelection?,
    metric: ForecastMetric,
    description: String,
    modifier: Modifier = Modifier,
) {
    val selected = candidates.firstOrNull { it.matches(selection?.selected) }
    val naive = candidates.firstOrNull { it.family == "NAIVE" && it.state == "EVALUATED" }
    val raw = candidates.firstOrNull { it.matches(selection?.rawBest) && !it.matches(selection?.selected) }
    val series = listOfNotNull(selected, naive?.takeIf { it !== selected }, raw?.takeIf { it !== naive })
        .filter { it.perHorizonMetrics.isNotEmpty() }
    if (series.isEmpty()) return
    val values = series.flatMap { candidate -> candidate.perHorizonMetrics.map { it.metrics.value(metric).toFloat() } }
    val bounds = paddedBounds(values)
    val maximumHorizon = series.maxOf { it.perHorizonMetrics.maxOf(HorizonEvaluation::horizonStep) }
    val colors = listOf(FlowGreen, FlowPurple, FlowWarning)
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = description }) {
            val top = 10.dp.toPx(); val bottom = size.height - 12.dp.toPx()
            drawChartGrid(grid, top, bottom)
            series.forEachIndexed { seriesIndex, candidate ->
                val points = candidate.perHorizonMetrics.map { horizon ->
                    val x = if (maximumHorizon == 1) size.width / 2f else {
                        size.width * (horizon.horizonStep - 1).toFloat() / (maximumHorizon - 1).toFloat()
                    }
                    val value = horizon.metrics.value(metric).toFloat()
                    val y = bottom - ((value - bounds.first) / (bounds.second - bounds.first)) * (bottom - top)
                    Offset(x, y)
                }
                drawPolyline(points, colors[seriesIndex], seriesIndex == 1)
                points.forEach { drawCircle(colors[seriesIndex], 4.dp.toPx(), it) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.mi_horizon_step, 1), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.mi_horizon_step, maximumHorizon), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
            series.forEachIndexed { index, candidate ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).background(colors[index], RoundedCornerShape(FlowRadius.pill)))
                    Spacer(Modifier.width(FlowSpacing.xs))
                    Text(modelLabel(candidate.family, candidate.configuration), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
internal fun TrendHistoryChart(
    observations: List<IntelligenceObservation>,
    trend: TrendAnalysis,
    description: String,
    modifier: Modifier = Modifier,
) {
    if (observations.isEmpty()) return
    val values = remember(observations) { observations.map { it.value } }
    val fitted = remember(observations, trend.slopePerObservation) {
        val mean = values.fold(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(values.size.toLong()), 12, RoundingMode.HALF_UP)
        val xMean = BigDecimal.valueOf((values.size - 1).toLong())
            .divide(BigDecimal.valueOf(2), 12, RoundingMode.HALF_UP)
        trend.slopePerObservation?.let { slope ->
            observations.indices.map { index ->
                mean.add(slope.multiply(BigDecimal.valueOf(index.toLong()).subtract(xMean)))
            }
        }.orEmpty()
    }
    val bounds = remember(values, fitted) { paddedBounds((values + fitted).map(BigDecimal::toFloat)) }
    val actual = MaterialTheme.colorScheme.primary
    val trendColor = FlowOrange
    val grid = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val surface = MaterialTheme.colorScheme.surface
    var selected by remember(observations) { mutableIntStateOf(observations.lastIndex) }
    Column(modifier) {
        ChartSelectionHeader(TimeSeriesSelection(observations[selected].effectiveDate, observations[selected].value, if (selected == observations.lastIndex) AnalyticalMarker.LATEST else AnalyticalMarker.ACTUAL, reportId = observations[selected].reportId), null)
        Spacer(Modifier.height(FlowSpacing.sm))
        Canvas(
            Modifier.fillMaxWidth().height(FlowSize.analyticsChartHeight).semantics { contentDescription = description }.pointerInput(observations) {
                awaitEachGesture {
                    val down = awaitFirstDown(false)
                    fun choose(x: Float) { selected = if (observations.size == 1) 0 else ((x / size.width) * observations.lastIndex).toInt().coerceIn(0, observations.lastIndex) }
                    choose(down.position.x)
                    do {
                        val event = awaitPointerEvent(); val change = event.changes.firstOrNull { it.id == down.id }; change?.let { choose(it.position.x) }
                    } while (change?.pressed == true)
                }
            },
        ) {
            val top = 10.dp.toPx(); val bottom = size.height - 12.dp.toPx()
            fun x(i: Int) = if (observations.size == 1) size.width / 2 else size.width * i / observations.lastIndex
            fun y(v: BigDecimal) = bottom - ((v.toFloat() - bounds.first) / (bounds.second - bounds.first)) * (bottom - top)
            drawChartGrid(grid, top, bottom)
            drawPolyline(values.mapIndexed { index, value -> Offset(x(index), y(value)) }, actual, false)
            if (fitted.size == observations.size) drawPolyline(fitted.mapIndexed { index, value -> Offset(x(index), y(value)) }, trendColor, true)
            values.forEachIndexed { index, value ->
                drawAnalyticalMarker(Offset(x(index), y(value)), if (index == values.lastIndex) AnalyticalMarker.LATEST else AnalyticalMarker.ACTUAL, actual, FlowPurple, FlowDanger, FlowWarning, surface, index == selected)
            }
        }
    }
}

@Composable
internal fun MovementBarsChart(
    observations: List<IntelligenceObservation>,
    description: String,
    modifier: Modifier = Modifier,
) {
    val deltas = remember(observations) {
        observations.zipWithNext { previous, current -> current.value.subtract(previous.value) }
    }
    if (deltas.isEmpty()) return
    val maxScaledMagnitude = sqrt(deltas.maxOf { it.abs().toFloat() }).takeIf { it > 0f } ?: 1f
    val positive = FlowGreen
    val negative = FlowDanger
    val neutral = MaterialTheme.colorScheme.outlineVariant
    Canvas(modifier.fillMaxWidth().height(180.dp).semantics { contentDescription = description }) {
        val zero = size.height / 2f
        drawLine(neutral, Offset(0f, zero), Offset(size.width, zero), 1.dp.toPx())
        val cell = size.width / deltas.size
        deltas.forEachIndexed { index, delta ->
            val height = (sqrt(delta.abs().toFloat()) / maxScaledMagnitude) * (size.height * 0.42f)
            val left = index * cell + cell * 0.18f
            val width = cell * 0.64f
            if (delta.signum() == 0) {
                drawCircle(neutral, radius = minOf(width / 2f, 2.dp.toPx()), center = Offset(left + width / 2f, zero))
                return@forEachIndexed
            }
            val top = if (delta.signum() > 0) zero - height else zero
            drawRoundRect(
                color = if (delta.signum() > 0) positive else negative,
                topLeft = Offset(left, top),
                size = androidx.compose.ui.geometry.Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx()),
            )
        }
    }
}

@Composable
internal fun AnalyticalLegend(modifier: Modifier = Modifier) {
    val items = listOf(
        AnalyticalMarker.ACTUAL to stringResource(R.string.mi_legend_actual),
        AnalyticalMarker.ANOMALY to stringResource(R.string.mi_legend_anomaly),
        AnalyticalMarker.FORECAST to stringResource(R.string.mi_legend_forecast),
        AnalyticalMarker.CONTEXTUAL to stringResource(R.string.mi_legend_contextual),
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(FlowSpacing.xs)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
                row.forEach { (marker, label) ->
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        MarkerSwatch(marker)
                        Spacer(Modifier.width(FlowSpacing.xs))
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkerSwatch(marker: AnalyticalMarker) {
    val color = when (marker) {
        AnalyticalMarker.ACTUAL, AnalyticalMarker.LATEST -> MaterialTheme.colorScheme.primary
        AnalyticalMarker.ANOMALY -> FlowDanger
        AnalyticalMarker.FORECAST -> FlowPurple
        AnalyticalMarker.CONTEXTUAL -> FlowWarning
    }
    val surface = MaterialTheme.colorScheme.surface
    Canvas(Modifier.size(14.dp)) { drawAnalyticalMarker(center, marker, color, color, color, color, surface, false) }
}

private fun DrawScope.drawChartGrid(color: Color, top: Float, bottom: Float) {
    repeat(3) { index ->
        val y = top + (bottom - top) * index / 2f
        drawLine(color, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
    }
}

private fun DrawScope.drawPolyline(points: List<Offset>, color: Color, dashed: Boolean) {
    if (points.size < 2) return
    val path = Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }
    drawPath(path, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null))
}

private fun DrawScope.drawAnalyticalMarker(
    point: Offset,
    marker: AnalyticalMarker,
    actualColor: Color,
    forecastColor: Color,
    anomalyColor: Color,
    contextualColor: Color,
    surfaceColor: Color,
    selected: Boolean,
) {
    val radius = if (selected) 7.dp.toPx() else 5.dp.toPx()
    val markerColor = when (marker) {
        AnalyticalMarker.ACTUAL, AnalyticalMarker.LATEST -> actualColor
        AnalyticalMarker.FORECAST -> forecastColor
        AnalyticalMarker.ANOMALY -> anomalyColor
        AnalyticalMarker.CONTEXTUAL -> contextualColor
    }
    if (selected) drawCircle(markerColor.copy(alpha = 0.16f), radius + 5.dp.toPx(), point)
    when (marker) {
        AnalyticalMarker.ACTUAL, AnalyticalMarker.LATEST -> {
            drawCircle(surfaceColor, radius + 1.dp.toPx(), point)
            drawCircle(actualColor, radius, point)
            if (marker == AnalyticalMarker.ACTUAL) drawCircle(surfaceColor, radius * 0.48f, point)
        }
        AnalyticalMarker.FORECAST -> {
            val path = Path().apply {
                moveTo(point.x, point.y - radius)
                lineTo(point.x + radius, point.y + radius)
                lineTo(point.x - radius, point.y + radius)
                close()
            }
            drawPath(path, forecastColor)
        }
        AnalyticalMarker.ANOMALY -> {
            val path = Path().apply {
                moveTo(point.x, point.y - radius)
                lineTo(point.x + radius, point.y)
                lineTo(point.x, point.y + radius)
                lineTo(point.x - radius, point.y)
                close()
            }
            drawPath(path, anomalyColor)
        }
        AnalyticalMarker.CONTEXTUAL -> {
            val path = Path().apply {
                moveTo(point.x, point.y - radius)
                lineTo(point.x + radius, point.y + radius)
                lineTo(point.x - radius, point.y + radius)
                close()
            }
            drawPath(path, contextualColor)
            drawCircle(surfaceColor, 1.5.dp.toPx(), point)
        }
    }
}

private fun ForecastCandidate.matches(reference: ModelReference?): Boolean = reference != null &&
    family == reference.family && configuration == reference.configuration

@Composable
internal fun modelLabel(family: String?, configuration: Map<String, Any?> = emptyMap()): String = when (family) {
    "NAIVE" -> stringResource(R.string.mi_model_naive)
    "SEASONAL_NAIVE" -> stringResource(R.string.mi_model_seasonal_naive)
    "ETS" -> when (configuration["variant"]?.toString()) {
        "SIMPLE_EXPONENTIAL_SMOOTHING" -> stringResource(R.string.mi_model_ets_simple)
        "HOLT_ADDITIVE_TREND" -> stringResource(R.string.mi_model_holt)
        "HOLT_WINTERS_ADDITIVE" -> stringResource(R.string.mi_model_holt_winters)
        else -> stringResource(R.string.mi_model_ets)
    }
    "ETS_SIMPLE" -> stringResource(R.string.mi_model_ets_simple)
    "HOLT" -> stringResource(R.string.mi_model_holt)
    "HOLT_WINTERS_ADDITIVE" -> stringResource(R.string.mi_model_holt_winters)
    "SARIMA" -> {
        val order = configuration["order"].asOrderLabel()
        val seasonal = configuration["seasonalOrder"].asOrderLabel()
        when {
            order == null -> stringResource(R.string.mi_model_sarima)
            seasonal != null && seasonal != "(0,0,0,0)" -> stringResource(R.string.mi_model_sarima_seasonal, order, seasonal)
            else -> stringResource(R.string.mi_model_sarima_order, order)
        }
    }
    null -> "—"
    else -> family.replace('_', ' ').lowercase().replaceFirstChar(Char::uppercase)
}

private fun Any?.asOrderLabel(): String? = (this as? List<*>)?.joinToString(prefix = "(", postfix = ")", separator = ",") { value ->
    when (value) {
        is Number -> value.toDouble().let { number -> if (number % 1.0 == 0.0) number.toLong().toString() else number.toString() }
        else -> value.toString()
    }
}

internal fun metricValue(value: BigDecimal, metric: ForecastMetric): String =
    if (metric == ForecastMetric.SMAPE) "${value.displayValue()} %" else value.displayValue()

private fun String.toEpochDayOrNull(): Long? = runCatching { LocalDate.parse(this).toEpochDay() }.getOrNull()

private fun paddedBounds(values: List<Float>): Pair<Float, Float> {
    val finite = values.filter(Float::isFinite)
    val minimum = finite.minOrNull() ?: 0f
    val maximum = finite.maxOrNull() ?: 1f
    if (minimum == maximum) {
        val padding = maxOf(abs(minimum) * 0.08f, 1f)
        return minimum - padding to maximum + padding
    }
    val padding = (maximum - minimum) * 0.1f
    return minimum - padding to maximum + padding
}
