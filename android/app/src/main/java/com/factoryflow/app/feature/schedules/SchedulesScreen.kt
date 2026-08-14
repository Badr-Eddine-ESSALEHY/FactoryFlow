package com.factoryflow.app.feature.schedules

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Color
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
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.schedules_title), onBack) { IconButton(onClick = onNew) { Icon(Icons.Outlined.Add, stringResource(R.string.new_schedule)) } } }, floatingActionButton = { FlowFab(stringResource(R.string.new_schedule), onNew) }) { padding ->
        when {
            state.loading -> SkeletonRows(Modifier.padding(padding).padding(20.dp), 4)
            state.schedules.isEmpty() -> EmptyPane(stringResource(R.string.no_schedules), stringResource(R.string.upcoming_schedule), Modifier.padding(padding), Icons.Outlined.EventRepeat, stringResource(R.string.new_schedule), onNew)
            else -> ScheduleListContent(state.schedules, state.togglingId, viewModel::toggle, onEdit, Modifier.padding(padding))
        }
    }
}

@Composable
fun ScheduleListContent(
    schedules: List<ReportScheduleDto>,
    togglingId: Long?,
    onToggle: (ReportScheduleDto) -> Unit,
    onEdit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowContentSurface(modifier) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, 100.dp), verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
        items(schedules, key = { it.id }) { schedule ->
            ScheduleCard(schedule, togglingId == schedule.id, { onToggle(schedule) }, { onEdit(schedule.id) })
        }
    }
    }
}

@Composable
private fun ScheduleCard(schedule: ReportScheduleDto, toggling: Boolean, onToggle: () -> Unit, onClick: () -> Unit) = FlowListRow(
    icon = Icons.Outlined.EventRepeat,
    title = scheduleTypeLabel(schedule.type) + " · " + schedule.time.take(5),
    meta = schedule.nextRunAt?.let { stringResource(R.string.next_run, it.toFrenchInstant()) }
        ?: stringResource(if (schedule.enabled) R.string.enabled else R.string.disabled),
    accent = FlowTeal,
    modifier = Modifier.fillMaxWidth(),
    onClick = onClick,
    trailing = {
        if (toggling) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = FlowSize.progressStroke, color = FlowTeal)
        else Switch(schedule.enabled, { onToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlowTeal))
    },
)

@Composable
fun ScheduleFormScreen(onBack: () -> Unit, viewModel: ScheduleFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(if (state.id == null) R.string.new_schedule else R.string.edit_schedule), onBack) }) { padding ->
        if (state.loading) LoadingPane(stringResource(R.string.loading), Modifier.padding(padding)) else ScheduleFormContent(
            state,
            ScheduleFormActions(
                onType = viewModel::type,
                onTime = viewModel::time,
                onDay = viewModel::day,
                onExcel = viewModel::excel,
                onPdf = viewModel::pdf,
                onEmailEnabled = viewModel::email,
                onRecipients = viewModel::recipients,
                onEnabled = viewModel::enabled,
                onSave = { viewModel.save(onBack) },
            ),
            Modifier.padding(padding),
        )
    }
}

data class ScheduleFormActions(
    val onType: (String) -> Unit,
    val onTime: (String) -> Unit,
    val onDay: (String) -> Unit,
    val onExcel: (Boolean) -> Unit,
    val onPdf: (Boolean) -> Unit,
    val onEmailEnabled: (Boolean) -> Unit,
    val onRecipients: (String) -> Unit,
    val onEnabled: (Boolean) -> Unit,
    val onSave: () -> Unit,
)

@Composable
fun ScheduleFormContent(state: ScheduleFormUiState, actions: ScheduleFormActions, modifier: Modifier = Modifier) {
    var timePicker by remember { mutableStateOf(false) }
    FlowContentSurface(modifier) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, FlowSpacing.xxl), verticalArrangement = Arrangement.spacedBy(FlowSpacing.lg)) {
            item {
                SectionHeader(stringResource(R.string.schedules_title)); Spacer(Modifier.height(FlowSpacing.sm))
                val types = listOf("DAILY", "WEEKLY", "MONTHLY")
                FlowSegmentedControl(
                    listOf(stringResource(R.string.daily), stringResource(R.string.weekly), stringResource(R.string.monthly)),
                    types.indexOf(state.type).coerceAtLeast(0),
                    { actions.onType(types[it]) },
                )
            }
            item {
                FlowListRow(Icons.Outlined.Schedule, stringResource(R.string.execution_time), state.time, FlowTeal, Modifier.fillMaxWidth(), onClick = { timePicker = true })
            }
            if (state.type == "WEEKLY") item { DaySelector(state.dayOfWeek ?: "MONDAY", actions.onDay) }
            item {
                SectionHeader(stringResource(R.string.formats)); Spacer(Modifier.height(8.dp))
                Row { FilterChip(state.excel, { actions.onExcel(!state.excel) }, { Text(stringResource(R.string.excel)) }, leadingIcon = { Icon(Icons.Outlined.TableView, null, Modifier.size(18.dp)) }); Spacer(Modifier.width(10.dp)); FilterChip(state.pdf, { actions.onPdf(!state.pdf) }, { Text(stringResource(R.string.pdf)) }, leadingIcon = { Icon(Icons.Outlined.PictureAsPdf, null, Modifier.size(18.dp)) }) }
                if (state.formatError) Text(stringResource(R.string.schedule_invalid_format), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            item {
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) { Column {
                    Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(stringResource(R.string.email_delivery), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.email_optional), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }; Switch(state.emailEnabled, actions.onEmailEnabled) }
                    AnimatedVisibility(state.emailEnabled) { TextField(state.recipients, actions.onRecipients, Modifier.fillMaxWidth().padding(top = FlowSpacing.md), label = { Text(stringResource(R.string.recipients)) }, placeholder = { Text(stringResource(R.string.recipients_hint)) }, isError = state.recipientError, supportingText = { if (state.recipientError) Text(stringResource(R.string.schedule_invalid_recipient)) }, shape = RoundedCornerShape(FlowRadius.control), colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, errorIndicatorColor = Color.Transparent)) }
                } }
            }
            item { FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.md)) { Row(verticalAlignment = Alignment.CenterVertically) { FlowIconTile(Icons.Outlined.PowerSettingsNew, null, FlowTeal, size = FlowSize.listIconTile); Spacer(Modifier.width(FlowSpacing.md)); Column(Modifier.weight(1f)) { Text(stringResource(R.string.enabled), style = MaterialTheme.typography.titleMedium); Text(stringResource(if (state.enabled) R.string.enabled else R.string.disabled), color = MaterialTheme.colorScheme.onSurfaceVariant) }; Switch(state.enabled, actions.onEnabled, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = FlowTeal)) } } }
            state.error?.let { error -> item { Text(stringResource(error.detail), color = MaterialTheme.colorScheme.error) } }
            item { PrimaryAction(stringResource(R.string.save_schedule), state.saving, onClick = actions.onSave) }
            if (state.runs.isNotEmpty()) {
                item { Spacer(Modifier.height(8.dp)); SectionHeader(stringResource(R.string.schedule_runs)) }
                items(state.runs, key = { it.id }) { run -> RunRow(run) }
            }
    }
    }
    if (timePicker) TimePickerModal(state.time, { actions.onTime(it); timePicker = false }, { timePicker = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun TimePickerModal(value: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val parts = value.split(':'); val state = rememberTimePickerState(parts.getOrNull(0)?.toIntOrNull() ?: 8, parts.getOrNull(1)?.toIntOrNull() ?: 0, true)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.execution_time)) }, text = { TimePicker(state) }, confirmButton = { TextButton(onClick = { onSelect("%02d:%02d".format(state.hour, state.minute)) }) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@Composable private fun DaySelector(selected: String, onSelect: (String) -> Unit) {
    val days = listOf("MONDAY" to R.string.monday, "TUESDAY" to R.string.tuesday, "WEDNESDAY" to R.string.wednesday, "THURSDAY" to R.string.thursday, "FRIDAY" to R.string.friday, "SATURDAY" to R.string.saturday, "SUNDAY" to R.string.sunday)
    var open by remember { mutableStateOf(false) }; Box { FlowListRow(Icons.Outlined.Today, stringResource(R.string.weekday), stringResource(days.first { it.first == selected }.second), FlowTeal, Modifier.fillMaxWidth(), onClick = { open = true }); DropdownMenu(open, { open = false }) { days.forEach { (value, label) -> DropdownMenuItem({ Text(stringResource(label)) }, { onSelect(value); open = false }) } } }
}

@Composable private fun RunRow(run: ScheduleRunDto) = FlowListRow(if (run.status == "SUCCEEDED") Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline, runStatusLabel(run.status), stringResource(R.string.date_range_compact, run.periodStart, run.periodEnd), if (run.status == "SUCCEEDED") FlowGreen else FlowOrange, Modifier.fillMaxWidth(), trailing = { FlowStatusPill(run.format, FlowBlue, compact = true) })
@Composable private fun scheduleTypeLabel(value: String) = stringResource(when (value) { "DAILY" -> R.string.daily; "WEEKLY" -> R.string.weekly; else -> R.string.monthly })
@Composable private fun runStatusLabel(value: String) = stringResource(when (value) { "SUCCEEDED" -> R.string.run_succeeded; "PARTIAL_SUCCESS" -> R.string.run_partial; "FAILED" -> R.string.run_failed; else -> R.string.run_skipped })
