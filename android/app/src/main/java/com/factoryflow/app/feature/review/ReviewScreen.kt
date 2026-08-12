package com.factoryflow.app.feature.review

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.KpiDefinitionDto
import com.factoryflow.app.feature.acquisition.FocusedTopBar
import com.factoryflow.app.feature.acquisition.KpiPicker

@Composable
fun ReviewScreen(onBack: () -> Unit, onConfirmed: (Long) -> Unit, viewModel: ReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showLeave by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var showKpiPicker by remember { mutableStateOf(false) }
    fun requestBack() { if (state.dirty) showLeave = true else onBack() }
    BackHandler(onBack = ::requestBack)
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.review_title), ::requestBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.report == null -> ErrorPane(stringResource(state.error?.title ?: R.string.server_error), stringResource(state.error?.detail ?: R.string.server_error), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> Column(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    item {
                        Text(stringResource(R.string.review_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(14.dp))
                        val completed = state.entries.count { it.value.isNotBlank() }
                        Text(stringResource(R.string.review_progress, completed, state.entries.size), style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(8.dp)); LinearProgressIndicator(progress = { if (state.entries.isEmpty()) 0f else completed.toFloat() / state.entries.size }, Modifier.fillMaxWidth(), trackColor = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusPill(state.report!!.effectiveDate, MaterialTheme.colorScheme.primary)
                            StatusPill(if (state.report!!.source == "MANUAL") stringResource(R.string.manual) else stringResource(R.string.paste_text), MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    state.report!!.rawText?.takeIf { it.isNotBlank() }?.let { raw -> item {
                        FactoryCard(Modifier.fillMaxWidth()) { Column { Text(stringResource(R.string.raw_source), style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(8.dp)); Text(raw, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 6) } }
                    } }
                    items(state.entries, key = { it.id }) { entry -> ReviewEntryCard(entry, { viewModel.edit(entry.id, it) }, { viewModel.remove(entry.id) }) }
                    item {
                        OutlinedButton(onClick = { showKpiPicker = true }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.AddCircleOutline, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.add_missing_kpi)) }
                    }
                    if (state.unknownLines.isNotEmpty()) {
                        item { Spacer(Modifier.height(8.dp)); SectionHeader(stringResource(R.string.unknown_content)); Text(stringResource(R.string.unknown_content_detail), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
                        items(state.unknownLines, key = { it.id }) { line -> UnknownLineCard(line, state.definitions, viewModel::resolve) }
                    }
                    if (!state.canConfirm && state.unknownLines.any { it.resolution == "UNRESOLVED" }) item {
                        Text(stringResource(R.string.unknown_must_resolve), color = Warning, style = MaterialTheme.typography.bodyMedium)
                    }
                    item { Spacer(Modifier.height(10.dp)) }
                }
                Surface(shadowElevation = 10.dp) {
                    Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                        state.error?.let { Text(stringResource(it.detail), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { viewModel.save() }, enabled = !state.saving && !state.confirming, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                                if (state.saving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.save_draft))
                            }
                            Button(onClick = { showConfirm = true }, enabled = state.canConfirm && !state.saving && !state.confirming, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(14.dp)) {
                                if (state.confirming) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary) else Text(stringResource(R.string.confirm_report))
                            }
                        }
                    }
                }
            }
        }
    }
    if (showKpiPicker) KpiPicker(state.definitions.filter { definition -> state.entries.none { it.kpiDefinitionId == definition.id } }, "", {}, { viewModel.add(it); showKpiPicker = false }, { showKpiPicker = false })
    if (showConfirm) AlertDialog(onDismissRequest = { showConfirm = false }, icon = { Icon(Icons.Outlined.Verified, null) }, title = { Text(stringResource(R.string.confirm_dialog_title)) }, text = { Text(stringResource(R.string.confirm_dialog_message)) }, confirmButton = { Button(onClick = { showConfirm = false; viewModel.confirm(onConfirmed) }) { Text(stringResource(R.string.confirm_intent)) } }, dismissButton = { TextButton(onClick = { showConfirm = false }) { Text(stringResource(R.string.cancel)) } })
    if (showLeave) AlertDialog(onDismissRequest = { showLeave = false }, title = { Text(stringResource(R.string.unsaved_title)) }, text = { Text(stringResource(R.string.unsaved_message)) }, confirmButton = { Button(onClick = { showLeave = false; viewModel.save(onBack) }) { Text(stringResource(R.string.save_draft)) } }, dismissButton = { TextButton(onClick = { showLeave = false; onBack() }) { Text(stringResource(R.string.leave_without_saving)) } })
    if (state.savedNotice) LaunchedEffect(Unit) { kotlinx.coroutines.delay(1800); viewModel.clearNotice() }
    AnimatedVisibility(state.savedNotice) { Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.BottomCenter) { Snackbar { Text(stringResource(R.string.draft_saved)) } } }
}

@Composable
private fun ReviewEntryCard(entry: ReviewEntry, onValue: (String) -> Unit, onRemove: () -> Unit) {
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(entry.displayName, style = MaterialTheme.typography.titleMedium); entry.unit?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) } }
                if (entry.edited) StatusPill(stringResource(R.string.edited), MaterialTheme.colorScheme.primary)
                IconButton(onClick = onRemove) { Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.remove_entry)) }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(entry.value, onValue, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.value)) }, suffix = { entry.unit?.let { Text(it) } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, supportingText = { if (entry.value.isBlank()) Text(stringResource(R.string.value_missing)) }, shape = RoundedCornerShape(13.dp))
            if (entry.extractedValue != null || entry.sourceLine != null) {
                Spacer(Modifier.height(10.dp)); Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        entry.extractedValue?.let { Text(stringResource(R.string.source_value_format, it), style = MaterialTheme.typography.bodyMedium) }
                        entry.sourceLine?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
            if (entry.warnings.isNotEmpty()) { Spacer(Modifier.height(10.dp)); entry.warnings.forEach { WarningRow(it) } }
        }
    }
}

@Composable private fun WarningRow(code: String) {
    val label = when {
        code.contains("RANGE") -> R.string.outside_range
        code.contains("UNIT") -> R.string.warning_unit
        code.contains("DUPLICATE") -> R.string.warning_duplicate
        code.contains("FUZZY") || code.contains("TYPO") -> R.string.warning_typo
        else -> R.string.warning_generic
    }
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.WarningAmber, null, tint = Warning, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text(stringResource(label), color = Warning, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun UnknownLineCard(line: ReviewUnknown, definitions: List<KpiDefinitionDto>, onResolve: (Long, String, Long?) -> Unit) {
    var assign by remember { mutableStateOf(false) }
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.Top) { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, tint = Warning); Spacer(Modifier.width(10.dp)); Text(line.sourceLine, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge) }
            Spacer(Modifier.height(12.dp)); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(line.resolution == "IGNORED", { onResolve(line.id, "IGNORED", null) }, { Text(stringResource(R.string.ignore_line)) })
                FilterChip(line.resolution == "ASSIGNED", { assign = true }, { Text(stringResource(R.string.assign_kpi)) })
            }
            if (line.resolution == "ASSIGNED") Text(definitions.firstOrNull { it.id == line.resolvedKpiDefinitionId }?.displayName.orEmpty(), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
    }
    if (assign) KpiPicker(definitions, "", {}, { onResolve(line.id, "ASSIGNED", it.id); assign = false }, { assign = false })
}
