package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.KpiDefinitionDto

@Composable
fun ManualEntryScreen(onBack: () -> Unit, onReview: (Long) -> Unit, viewModel: ManualEntryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmExit by remember { mutableStateOf(false) }
    val requestBack = { if (state.entries.isEmpty()) onBack() else confirmExit = true }
    BackHandler(onBack = requestBack)
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.manual_title), requestBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.error != null && state.definitions.isEmpty() -> ErrorPane(stringResource(state.error!!.title), stringResource(state.error!!.detail), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> ManualEntryContent(
                state = state,
                onDateChanged = viewModel::date,
                onValueChanged = viewModel::value,
                onMissingChanged = viewModel::missing,
                onRemove = viewModel::remove,
                onQueryChanged = viewModel::query,
                onAdd = viewModel::add,
                onSubmit = { viewModel.submit(onReview) },
                modifier = Modifier.padding(padding),
            )
        }
    }
    if (confirmExit) AlertDialog(
        onDismissRequest = { confirmExit = false },
        title = { Text(stringResource(R.string.manual_unsaved_title)) },
        text = { Text(stringResource(R.string.manual_unsaved_detail)) },
        confirmButton = { TextButton(onClick = onBack) { Text(stringResource(R.string.leave)) } },
        dismissButton = { TextButton(onClick = { confirmExit = false }) { Text(stringResource(R.string.stay)) } },
    )
}

@Composable
fun ManualEntryContent(
    state: ManualEntryUiState,
    onDateChanged: (String) -> Unit,
    onValueChanged: (Long, String) -> Unit,
    onMissingChanged: (Long, Boolean) -> Unit,
    onRemove: (Long) -> Unit,
    onQueryChanged: (String) -> Unit,
    onAdd: (KpiDefinitionDto) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selecting by remember { mutableStateOf(false) }
    FlowContentSurface(modifier) {
    Column(Modifier.fillMaxSize().imePadding()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(FlowSpacing.xl), verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
                    item {
                        Text(stringResource(R.string.manual_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (state.entries.isNotEmpty()) Text(
                            stringResource(R.string.manual_progress, state.entries.count { it.explicitlyMissing || it.value.isNotBlank() }, state.entries.size),
                            color = FlowBlue, style = MaterialTheme.typography.labelLarge,
                        )
                        Spacer(Modifier.height(FlowSpacing.xs))
                    }
                    item {
                        TextField(
                            state.effectiveDate,
                            onDateChanged,
                            Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.effective_date)) },
                            leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, tint = FlowBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(FlowRadius.control),
                            colors = flowInputColors(),
                        )
                    }
                    items(state.entries, key = { it.definition.id }) { row -> ManualRow(
                        row = row,
                        invalid = row.definition.id in state.invalidEntryIds,
                        onValue = { onValueChanged(row.definition.id, it) },
                        onMissing = { onMissingChanged(row.definition.id, it) },
                        onRemove = { onRemove(row.definition.id) },
                    ) }
                    item {
                        OutlinedButton(onClick = { selecting = true }, Modifier.fillMaxWidth().height(FlowSize.touchTarget), shape = RoundedCornerShape(FlowRadius.control)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(FlowSpacing.sm)); Text(stringResource(R.string.add_kpi)) }
                        if (state.selectionError) Text(stringResource(R.string.no_kpi_selected), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = FlowElevation.navigation) { Column(Modifier.navigationBarsPadding().padding(FlowSpacing.lg)) {
                    state.error?.let { Text(stringResource(it.detail), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                    PrimaryAction(stringResource(R.string.continue_review), state.submitting, onClick = onSubmit)
                } }
    }
    }
    if (selecting) KpiPicker(state.definitions, state.query, onQueryChanged, { onAdd(it); selecting = false }, { selecting = false })
}

@Composable
private fun ManualRow(row: ManualEntryRow, invalid: Boolean, onValue: (String) -> Unit, onMissing: (Boolean) -> Unit, onRemove: () -> Unit) {
    val definition = row.definition
    FlowCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(FlowSpacing.lg)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlowIconTile(Icons.Outlined.Tune, null, FlowPurple, size = FlowSize.listIconTile)
                Spacer(Modifier.width(FlowSpacing.md))
                Column(Modifier.weight(1f)) { Text(definition.displayName, style = MaterialTheme.typography.titleMedium); definition.category?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) } }
                IconButton(onClick = onRemove) { Icon(Icons.Outlined.Close, stringResource(R.string.remove_entry)) }
            }
            Spacer(Modifier.height(FlowSpacing.sm))
            TextField(
                row.value,
                onValue,
                Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.value)) },
                suffix = { definition.unit?.let { FlowStatusPill(it, FlowPurple, compact = true) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                enabled = !row.explicitlyMissing,
                isError = invalid,
                supportingText = { if (invalid) Text(stringResource(R.string.invalid_decimal)) else if (row.explicitlyMissing) Text(stringResource(R.string.not_provided)) },
                shape = RoundedCornerShape(FlowRadius.control),
                colors = flowInputColors(),
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = row.explicitlyMissing, onCheckedChange = onMissing)
                Text(stringResource(R.string.mark_not_provided), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpiPicker(definitions: List<KpiDefinitionDto>, query: String, onQuery: (String) -> Unit, onSelect: (KpiDefinitionDto) -> Unit, onDismiss: () -> Unit) {
    val filtered = definitions.filter { it.displayName.contains(query, true) || it.code.contains(query, true) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(stringResource(R.string.add_kpi), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(FlowSpacing.md)); TextField(query, onQuery, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search_kpi)) }, leadingIcon = { Icon(Icons.Outlined.Search, null, tint = FlowBlue) }, singleLine = true, shape = RoundedCornerShape(FlowRadius.control), colors = flowInputColors())
            Spacer(Modifier.height(12.dp)); LazyColumn(Modifier.fillMaxWidth().heightIn(max = 440.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                items(filtered, key = { it.id }) { definition ->
                    Row(Modifier.fillMaxWidth().clickable { onSelect(definition) }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(definition.displayName, style = MaterialTheme.typography.titleMedium); Text(listOfNotNull(definition.category, definition.unit).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Icon(Icons.Outlined.AddCircleOutline, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun flowInputColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surface,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
)
