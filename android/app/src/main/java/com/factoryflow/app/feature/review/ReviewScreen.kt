package com.factoryflow.app.feature.review

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import com.factoryflow.app.feature.acquisition.KpiPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(onBack: () -> Unit, onConfirmed: (Long) -> Unit, viewModel: ReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var section by remember(state.blockingCount) { mutableIntStateOf(if (state.blockingCount > 0) 1 else 0) }
    var showConfirm by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }
    var showAdd by remember { mutableStateOf(false) }
    var showSource by remember { mutableStateOf(false) }
    BackHandler(state.dirty) { showLeave = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = { IconButton(onClick = { if (state.dirty) showLeave = true else onBack() }) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                } },
                actions = { IconButton(onClick = { viewModel.save() }, enabled = !state.loading) {
                    Icon(Icons.Outlined.Save, stringResource(R.string.save_draft))
                } },
            )
        },
        bottomBar = {
            if (!state.loading && state.report != null) Surface(shadowElevation = 12.dp) {
                Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    if (!state.canConfirm) Text(
                        stringResource(R.string.review_blocking_count, state.blockingCount),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    PrimaryAction(stringResource(R.string.confirm_report), state.confirming, state.canConfirm, { showConfirm = true })
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.error != null && state.report == null -> ErrorPane(
                stringResource(state.error!!.title), stringResource(state.error!!.detail),
                stringResource(R.string.retry), viewModel::load, Modifier.padding(padding),
            )
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).imePadding(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    Text(stringResource(R.string.review_summary_detected, state.detectedCount), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (state.blockingCount == 0) stringResource(R.string.review_all_clear)
                        else stringResource(R.string.review_blocking_count, state.blockingCount),
                        color = if (state.blockingCount == 0) FactoryFlowSuccess else MaterialTheme.colorScheme.error,
                    )
                }
                item { ReviewSummary(state) }
                item { FactorySegmentedControl(listOf(
                    stringResource(R.string.review_ready), stringResource(R.string.review_attention),
                    stringResource(R.string.review_missing), stringResource(R.string.review_unresolved),
                ), section, { section = it }) }

                val visible = state.entries.filter { entry -> entry.reviewState == ReviewState.entries[section] }
                if (visible.isEmpty() && !(section == 3 && state.unknownLines.any { it.resolution == "UNRESOLVED" })) {
                    item { EmptyPane(stringResource(R.string.review_all_clear), "", icon = Icons.Outlined.CheckCircle) }
                }
                items(visible, key = { "entry-" + it.id }) { entry ->
                    ReviewEntryRow(entry, state.definitions, viewModel)
                }
                if (section == 3) items(state.unknownLines.filter { it.resolution == "UNRESOLVED" }, key = { "unknown-" + it.id }) { line ->
                    UnknownRow(line, state.definitions, viewModel)
                }
                item { OutlinedButton({ showAdd = true }, Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.add_missing_kpi))
                } }
                state.report?.rawText?.takeIf { it.isNotBlank() }?.let { raw -> item {
                    FactoryCard(Modifier.fillMaxWidth().animateContentSize(spring())) {
                        Column {
                            Row(Modifier.fillMaxWidth().clickable { showSource = !showSource }, verticalAlignment = Alignment.CenterVertically) {
                                FactoryIconChip(Icons.Outlined.Source, null); Spacer(Modifier.width(12.dp))
                                Text(stringResource(R.string.source_collapsed), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                Icon(if (showSource) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null)
                            }
                            AnimatedVisibility(showSource) { Text(raw, Modifier.padding(top = 14.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                } }
            }
        }
    }
    if (showAdd) KpiPicker(state.definitions, "", {}, { viewModel.add(it); showAdd = false }, { showAdd = false })
    if (showConfirm) AlertDialog(
        onDismissRequest = { showConfirm = false }, icon = { Icon(Icons.Outlined.Verified, null) },
        title = { Text(stringResource(R.string.confirm_dialog_title)) }, text = { Text(stringResource(R.string.confirm_dialog_message)) },
        confirmButton = { Button({ showConfirm = false; viewModel.confirm(onConfirmed) }) { Text(stringResource(R.string.confirm_intent)) } },
        dismissButton = { TextButton({ showConfirm = false }) { Text(stringResource(R.string.cancel)) } },
    )
    if (showLeave) AlertDialog(
        onDismissRequest = { showLeave = false }, title = { Text(stringResource(R.string.unsaved_title)) },
        text = { Text(stringResource(R.string.unsaved_message)) },
        confirmButton = { Button({ showLeave = false; viewModel.save(onBack) }) { Text(stringResource(R.string.save_draft)) } },
        dismissButton = { TextButton({ showLeave = false; onBack() }) { Text(stringResource(R.string.leave_without_saving)) } },
    )
    if (state.savedNotice) LaunchedEffect(Unit) { kotlinx.coroutines.delay(1600); viewModel.clearNotice() }
}

@Composable
private fun ReviewSummary(state: ReviewUiState) = FactoryCard(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Summary(state.readyCount, stringResource(R.string.review_ready), FactoryFlowSuccess)
        Summary(state.attentionCount, stringResource(R.string.review_attention), FactoryFlowWarning)
        Summary(state.missingCount, stringResource(R.string.review_missing), MaterialTheme.colorScheme.onSurfaceVariant)
        Summary(state.unresolvedCount, stringResource(R.string.review_unresolved), MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun Summary(value: Int, label: String, color: Color) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = color)
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
}

@Composable
private fun ReviewEntryRow(entry: ReviewEntry, definitions: List<KpiDefinitionDto>, viewModel: ReviewViewModel) {
    var expanded by remember(entry.id) { mutableStateOf(entry.reviewState != ReviewState.READY) }
    var mapping by remember { mutableStateOf(false) }
    FactoryCard(Modifier.fillMaxWidth().animateContentSize(spring())) {
        Column {
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                val icon = when (entry.reviewState) {
                    ReviewState.READY -> Icons.Outlined.CheckCircle
                    ReviewState.ATTENTION -> Icons.Outlined.WarningAmber
                    ReviewState.MISSING -> Icons.Outlined.RemoveCircleOutline
                    ReviewState.UNRESOLVED -> Icons.Outlined.HelpOutline
                }
                val color = when (entry.reviewState) {
                    ReviewState.READY -> FactoryFlowSuccess
                    ReviewState.ATTENTION -> FactoryFlowWarning
                    ReviewState.MISSING -> MaterialTheme.colorScheme.onSurfaceVariant
                    ReviewState.UNRESOLVED -> MaterialTheme.colorScheme.error
                }
                FactoryIconChip(icon, null, tint = color); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.displayName.ifBlank { entry.sourceLabel ?: entry.sourceLine.orEmpty() }, style = MaterialTheme.typography.titleSmall)
                    Text((entry.value.ifBlank { "—" }) + (entry.unit?.let { " " + it } ?: ""), style = MaterialTheme.typography.titleMedium)
                }
                StatusPill(when (entry.reviewState) {
                    ReviewState.READY -> stringResource(R.string.ready)
                    ReviewState.ATTENTION -> stringResource(R.string.review_attention)
                    ReviewState.MISSING -> stringResource(R.string.missing)
                    ReviewState.UNRESOLVED -> stringResource(R.string.review_unresolved)
                }, color)
            }
            AnimatedVisibility(expanded) { Column(Modifier.padding(top = 12.dp)) {
                entry.suggestedKpiDisplayName?.let { suggestion ->
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.suggested_kpi, suggestion), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            TextButton({ definitions.firstOrNull { it.id == entry.suggestedKpiDefinitionId }?.let { viewModel.assignEntry(entry.id, it) } }) { Text(stringResource(R.string.assign)) }
                        }
                    }
                }
                OutlinedTextField(entry.value, { viewModel.edit(entry.id, it) }, Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text(stringResource(R.string.value)) }, suffix = { entry.unit?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                entry.sourceLine?.let { Text(it, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                entry.warnings.filterNot { it == "MISSING_VALUE" }.forEach { Text("• " + warningText(it), color = FactoryFlowWarning, style = MaterialTheme.typography.bodySmall) }
                if (entry.kpiDefinitionId == null) TextButton({ mapping = true }) { Text(stringResource(R.string.assign_kpi)) }
                if (entry.edited && entry.kpiDefinitionId != null && !entry.sourceLabel.isNullOrBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(entry.rememberAlias, { viewModel.rememberEntryAlias(entry.id, it) })
                    Text(stringResource(R.string.remember_alias), style = MaterialTheme.typography.bodySmall)
                }
                TextButton({ viewModel.remove(entry.id) }) { Text(stringResource(R.string.remove_entry), color = MaterialTheme.colorScheme.error) }
            } }
        }
    }
    if (mapping) KpiPicker(definitions, "", {}, { viewModel.assignEntry(entry.id, it); mapping = false }, { mapping = false })
}

@Composable
private fun UnknownRow(line: ReviewUnknown, definitions: List<KpiDefinitionDto>, viewModel: ReviewViewModel) {
    var mapping by remember { mutableStateOf(false) }
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FactoryIconChip(Icons.Outlined.HelpOutline, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(12.dp)); Text(line.sourceLine, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton({ viewModel.resolve(line.id, "IGNORED") }) { Text(stringResource(R.string.ignore_line)) }
                Button({ mapping = true }) { Text(stringResource(R.string.assign)) }
            }
        }
    }
    if (mapping) KpiPicker(definitions, "", {}, { viewModel.resolve(line.id, "ASSIGNED", it.id); mapping = false }, { mapping = false })
}

@Composable
private fun warningText(code: String) = stringResource(when {
    code.contains("RANGE") -> R.string.outside_range
    code.contains("UNIT") -> R.string.warning_unit
    code.contains("DUPLICATE") -> R.string.warning_duplicate
    code.contains("FUZZY") || code.contains("CONFIDENCE") -> R.string.warning_typo
    else -> R.string.warning_generic
})
