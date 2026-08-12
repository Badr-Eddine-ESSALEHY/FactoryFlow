package com.factoryflow.app.feature.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.KpiStatisticsDto
import com.factoryflow.app.core.util.displayValue
import com.factoryflow.app.feature.acquisition.FocusedTopBar
import java.math.BigDecimal

@Composable
fun StatisticsScreen(onBack: () -> Unit, viewModel: StatisticsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.statistics_title), onBack) }) { padding ->
        when {
            state.loading -> SkeletonRows(Modifier.padding(padding).padding(20.dp), 5)
            state.kpis.isEmpty() -> EmptyPane(stringResource(R.string.no_statistics), stringResource(R.string.statistics_integrity), Modifier.padding(padding), Icons.Outlined.Analytics, stringResource(R.string.retry), viewModel::load)
            else -> StatisticsContent(state, viewModel::days, viewModel::select, Modifier.padding(padding))
        }
    }
}

@Composable
private fun StatisticsContent(state: StatisticsUiState, onDays: (Int) -> Unit, onKpi: (Long) -> Unit, modifier: Modifier) {
    val selected = state.selected ?: return
    var menu by remember { mutableStateOf(false) }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(state.days == 30, { onDays(30) }, { Text(stringResource(R.string.period_30_days)) })
                FilterChip(state.days == 90, { onDays(90) }, { Text(stringResource(R.string.period_90_days)) })
            }
        }
        item {
            Box {
                OutlinedButton(onClick = { menu = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text(selected.displayName, Modifier.weight(1f)); Text(selected.unit.orEmpty()) }
                DropdownMenu(menu, { menu = false }) { state.kpis.forEach { kpi -> DropdownMenuItem({ Text(kpi.displayName) }, { onKpi(kpi.kpiDefinitionId); menu = false }) } }
            }
        }
        item {
            FactoryCard(Modifier.fillMaxWidth()) {
                Column {
                    Text(selected.displayName, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(18.dp)); TrendChart(selected, Modifier.fillMaxWidth().height(190.dp))
                    Spacer(Modifier.height(12.dp)); Text(stringResource(R.string.samples, selected.sampleCount), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(stringResource(R.string.latest), selected.latest, selected.unit, Modifier.weight(1f))
                SummaryCard(stringResource(R.string.average), selected.average, selected.unit, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SummaryCard(stringResource(R.string.minimum), selected.minimum, selected.unit, Modifier.weight(1f))
                SummaryCard(stringResource(R.string.maximum), selected.maximum, selected.unit, Modifier.weight(1f))
            }
        }
        item {
            Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(14.dp)) { Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onPrimaryContainer); Spacer(Modifier.width(10.dp)); Column { Text(stringResource(R.string.statistics_integrity), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.bodyMedium); if (selected.missingValueCount > 0) Text(stringResource(R.string.missing_samples, selected.missingValueCount), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelMedium) } }
            }
        }
    }
}

@Composable private fun SummaryCard(label: String, value: BigDecimal?, unit: String?, modifier: Modifier) = FactoryCard(modifier) { Column { Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium); Spacer(Modifier.height(7.dp)); Text(value.displayValue(), style = MaterialTheme.typography.headlineSmall); Text(unit.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) } }

@Composable
private fun TrendChart(statistics: KpiStatisticsDto, modifier: Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val values = statistics.points.map { it.value }
    Canvas(modifier) {
        repeat(4) { index -> val y = size.height * index / 3f; drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx()) }
        if (values.isEmpty()) return@Canvas
        val min = values.minOrNull()!!; val max = values.maxOrNull()!!; val span = (max - min).takeUnless { it.compareTo(BigDecimal.ZERO) == 0 } ?: BigDecimal.ONE
        val points = values.mapIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2 else size.width * index / (values.size - 1)
            val ratio = (value - min).divide(span, 8, java.math.RoundingMode.HALF_UP).toFloat()
            Offset(x, size.height - ratio * size.height)
        }
        if (points.size > 1) { val path = Path().apply { moveTo(points.first().x, points.first().y); points.drop(1).forEach { lineTo(it.x, it.y) } }; drawPath(path, primary, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)) }
        points.forEach { drawCircle(primary, 4.dp.toPx(), it) }
    }
}
