package com.factoryflow.app.feature.schedules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.toFrenchInstant
import com.factoryflow.app.feature.acquisition.FocusedTopBar

@Composable
fun SchedulesScreen(onBack: () -> Unit, onNew: () -> Unit, onEdit: (Long) -> Unit, viewModel: SchedulesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.schedules_title), onBack) { IconButton(onClick = onNew) { Icon(Icons.Outlined.Add, stringResource(R.string.new_schedule)) } } }, floatingActionButton = { ExtendedFloatingActionButton(onClick = onNew, icon = { Icon(Icons.Outlined.Add, null) }, text = { Text(stringResource(R.string.new_schedule)) }) }) { padding ->
        when {
            state.loading -> SkeletonRows(Modifier.padding(padding).padding(20.dp), 4)
            state.schedules.isEmpty() -> EmptyPane(stringResource(R.string.no_schedules), stringResource(R.string.upcoming_schedule), Modifier.padding(padding), Icons.Outlined.EventRepeat, stringResource(R.string.new_schedule), onNew)
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.schedules, key = { it.id }) { schedule -> ScheduleCard(schedule, state.togglingId == schedule.id, { viewModel.toggle(schedule) }, { onEdit(schedule.id) }) }
            }
        }
    }
}

@Composable
private fun ScheduleCard(schedule: ReportScheduleDto, toggling: Boolean, onToggle: () -> Unit, onClick: () -> Unit) = FactoryCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(13.dp)) { Icon(Icons.Outlined.EventRepeat, null, Modifier.padding(11.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer) }
            Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(scheduleTypeLabel(schedule.type), style = MaterialTheme.typography.titleMedium); Text(schedule.time.take(5), style = MaterialTheme.typography.headlineSmall) }
            if (toggling) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp) else Switch(schedule.enabled, { onToggle() })
        }
        Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            if (schedule.generateExcel) StatusPill(stringResource(R.string.excel), MaterialTheme.colorScheme.primary)
            if (schedule.generatePdf) StatusPill(stringResource(R.string.pdf), MaterialTheme.colorScheme.primary)
            if (schedule.emailEnabled) StatusPill(stringResource(R.string.email_short), Info)
        }
        schedule.nextRunAt?.let { Spacer(Modifier.height(10.dp)); Text(stringResource(R.string.next_run, it.toFrenchInstant()), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
fun ScheduleFormScreen(onBack: () -> Unit, viewModel: ScheduleFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var timePicker by remember { mutableStateOf(false) }
    Scaffold(topBar = { FocusedTopBar(stringResource(if (state.id == null) R.string.new_schedule else R.string.edit_schedule), onBack) }) { padding ->
        if (state.loading) LoadingPane(stringResource(R.string.loading), Modifier.padding(padding)) else LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            item {
                SectionHeader(stringResource(R.string.schedules_title)); Spacer(Modifier.height(10.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    listOf("DAILY" to R.string.daily, "WEEKLY" to R.string.weekly, "MONTHLY" to R.string.monthly).forEachIndexed { index, (value, label) -> SegmentedButton(state.type == value, { viewModel.type(value) }, SegmentedButtonDefaults.itemShape(index, 3)) { Text(stringResource(label)) } }
                }
            }
            item {
                OutlinedButton(onClick = { timePicker = true }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Schedule, null); Spacer(Modifier.width(10.dp)); Text(stringResource(R.string.execution_time_value, stringResource(R.string.execution_time), state.time)) }
            }
            if (state.type == "WEEKLY") item { DaySelector(state.dayOfWeek ?: "MONDAY", viewModel::day) }
            item {
                SectionHeader(stringResource(R.string.formats)); Spacer(Modifier.height(8.dp))
                Row { FilterChip(state.excel, { viewModel.excel(!state.excel) }, { Text(stringResource(R.string.excel)) }, leadingIcon = { Icon(Icons.Outlined.TableView, null, Modifier.size(18.dp)) }); Spacer(Modifier.width(10.dp)); FilterChip(state.pdf, { viewModel.pdf(!state.pdf) }, { Text(stringResource(R.string.pdf)) }, leadingIcon = { Icon(Icons.Outlined.PictureAsPdf, null, Modifier.size(18.dp)) }) }
                if (state.formatError) Text(stringResource(R.string.schedule_invalid_format), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            item {
                FactoryCard(Modifier.fillMaxWidth()) { Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(stringResource(R.string.email_delivery), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.email_optional), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }; Switch(state.emailEnabled, viewModel::email) }
                    AnimatedVisibility(state.emailEnabled) { OutlinedTextField(state.recipients, viewModel::recipients, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text(stringResource(R.string.recipients)) }, placeholder = { Text(stringResource(R.string.recipients_hint)) }, isError = state.recipientError, supportingText = { if (state.recipientError) Text(stringResource(R.string.schedule_invalid_recipient)) }, shape = RoundedCornerShape(13.dp)) }
                } }
            }
            item { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(stringResource(R.string.enabled), style = MaterialTheme.typography.titleMedium); Text(stringResource(if (state.enabled) R.string.enabled else R.string.disabled), color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(state.enabled, viewModel::enabled) } }
            state.error?.let { error -> item { Text(stringResource(error.detail), color = MaterialTheme.colorScheme.error) } }
            item { PrimaryAction(stringResource(R.string.save_schedule), state.saving, onClick = { viewModel.save(onBack) }) }
            if (state.runs.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)); SectionHeader(stringResource(R.string.schedule_runs)) }
                items(state.runs, key = { it.id }) { run -> RunRow(run) }
            }
        }
    }
    if (timePicker) TimePickerModal(state.time, { viewModel.time(it); timePicker = false }, { timePicker = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TimePickerModal(value: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val parts = value.split(':'); val state = rememberTimePickerState(parts.getOrNull(0)?.toIntOrNull() ?: 8, parts.getOrNull(1)?.toIntOrNull() ?: 0, true)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.execution_time)) }, text = { TimePicker(state) }, confirmButton = { TextButton(onClick = { onSelect("%02d:%02d".format(state.hour, state.minute)) }) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@Composable private fun DaySelector(selected: String, onSelect: (String) -> Unit) {
    val days = listOf("MONDAY" to R.string.monday, "TUESDAY" to R.string.tuesday, "WEDNESDAY" to R.string.wednesday, "THURSDAY" to R.string.thursday, "FRIDAY" to R.string.friday, "SATURDAY" to R.string.saturday, "SUNDAY" to R.string.sunday)
    var open by remember { mutableStateOf(false) }; Box { OutlinedButton({ open = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Text(stringResource(R.string.weekday), Modifier.weight(1f)); Text(stringResource(days.first { it.first == selected }.second)) }; DropdownMenu(open, { open = false }) { days.forEach { (value, label) -> DropdownMenuItem({ Text(stringResource(label)) }, { onSelect(value); open = false }) } } }
}

@Composable private fun RunRow(run: ScheduleRunDto) = FactoryCard(Modifier.fillMaxWidth()) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (run.status == "SUCCEEDED") Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline, null, tint = if (run.status == "SUCCEEDED") Success else Warning); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)) { Text(runStatusLabel(run.status), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.date_range_compact, run.periodStart, run.periodEnd), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }; StatusPill(run.format, MaterialTheme.colorScheme.primary) } }
@Composable private fun scheduleTypeLabel(value: String) = stringResource(when (value) { "DAILY" -> R.string.daily; "WEEKLY" -> R.string.weekly; else -> R.string.monthly })
@Composable private fun runStatusLabel(value: String) = stringResource(when (value) { "SUCCEEDED" -> R.string.run_succeeded; "PARTIAL_SUCCESS" -> R.string.run_partial; "FAILED" -> R.string.run_failed; else -> R.string.run_skipped })
