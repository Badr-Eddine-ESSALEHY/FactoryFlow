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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
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
import com.factoryflow.app.feature.acquisition.FocusedTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(onBack: () -> Unit, onConfirmed: (Long) -> Unit, viewModel: ReviewViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showConfirm by remember { mutableStateOf(false) }
    var showLeave by remember { mutableStateOf(false) }
    BackHandler(state.dirty) { showLeave = true }

    FactoryFlowScaffold(
        topBar = {
            FocusedTopBar(stringResource(R.string.review_title), { if (state.dirty) showLeave = true else onBack() }) {
                IconButton(onClick = { viewModel.save() }, enabled = !state.loading) {
                    Icon(Icons.Outlined.Save, stringResource(R.string.save_draft))
                }
            }
        },
        bottomBar = {
            if (!state.loading && state.report != null) Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = FlowElevation.navigation) {
                Column(Modifier.navigationBarsPadding().padding(FlowSpacing.lg)) {
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
            else -> ReviewContent(
                state = state,
                actions = ReviewContentActions(
                    onAdd = viewModel::add,
                    onEdit = viewModel::edit,
                    onEditSecondary = viewModel::editSecondary,
                    onAssignEntry = viewModel::assignEntry,
                    onRememberAlias = viewModel::rememberEntryAlias,
                    onRemove = viewModel::remove,
                    onResolveUnknown = viewModel::resolve,
                ),
                modifier = Modifier.padding(padding),
            )
        }
    }
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

data class ReviewContentActions(
    val onAdd: (KpiDefinitionDto) -> Unit = {},
    val onEdit: (Long, String) -> Unit = { _, _ -> },
    val onEditSecondary: (Long, String) -> Unit = { _, _ -> },
    val onAssignEntry: (Long, KpiDefinitionDto) -> Unit = { _, _ -> },
    val onRememberAlias: (Long, Boolean) -> Unit = { _, _ -> },
    val onRemove: (Long) -> Unit = {},
    val onResolveUnknown: (Long, String, Long?) -> Unit = { _, _, _ -> },
)

@Composable
fun ReviewContent(
    state: ReviewUiState,
    actions: ReviewContentActions,
    modifier: Modifier = Modifier,
) {
    var section by remember(state.blockingCount) { mutableIntStateOf(if (state.blockingCount > 0) 1 else 0) }
    var showAdd by remember { mutableStateOf(false) }
    var showSource by remember { mutableStateOf(false) }
    val compositeGroups = state.entries
        .groupBy { it.sourceLine }
        .filter { (source, entries) ->
            !source.isNullOrBlank() && entries.size > 1 &&
                entries.any { "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT" in it.warnings }
        }
    val compositeIds = compositeGroups.values.flatten().mapTo(mutableSetOf()) { it.id }

    FlowContentSurface(modifier) {
        LazyColumn(
            Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(horizontal = FlowSpacing.xl, vertical = FlowSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
        ) {
            item { ReviewStatusOverview(state) }
            if (compositeGroups.isNotEmpty()) {
                item { Text(stringResource(R.string.composite_values), style = MaterialTheme.typography.titleLarge) }
                items(compositeGroups.entries.toList(), key = { "composite-" + it.key }) { group ->
                    CompositeReviewRow(group.key.orEmpty(), group.value, state.definitions, actions)
                }
            }
            item {
                FactorySegmentedControl(
                    listOf(
                        stringResource(R.string.review_ready),
                        stringResource(R.string.review_attention),
                        stringResource(R.string.review_missing),
                        stringResource(R.string.review_unresolved),
                    ),
                    section,
                    { section = it },
                )
            }

            val visible = state.entries.filter { entry ->
                entry.id !in compositeIds && entry.reviewState == ReviewState.entries[section]
            }
            if (visible.isEmpty() && !(section == 3 && state.unknownLines.any { it.resolution == "UNRESOLVED" })) {
                item { EmptyPane(stringResource(R.string.review_all_clear), "", icon = Icons.Outlined.CheckCircle) }
            }
            items(visible, key = { "entry-" + it.id }) { entry ->
                ReviewEntryRow(entry, state.definitions, actions)
            }
            if (section == 3) {
                items(state.unknownLines.filter { it.resolution == "UNRESOLVED" }, key = { "unknown-" + it.id }) { line ->
                    UnknownRow(line, state.definitions, actions)
                }
            }
            item {
                OutlinedButton({ showAdd = true }, Modifier.fillMaxWidth().height(FlowSize.touchTarget)) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(FlowSpacing.sm))
                    Text(stringResource(R.string.add_missing_kpi))
                }
            }
            state.report?.rawText?.takeIf { it.isNotBlank() }?.let { raw ->
                item {
                    FlowCard(Modifier.fillMaxWidth().animateContentSize(spring())) {
                        Column {
                            Row(
                                Modifier.fillMaxWidth().clickable { showSource = !showSource },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FlowIconTile(Icons.Outlined.Source, null, FlowBlue)
                                Spacer(Modifier.width(FlowSpacing.md))
                                Text(stringResource(R.string.source_collapsed), Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                                Icon(if (showSource) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, null)
                            }
                            AnimatedVisibility(showSource) {
                                Text(raw, Modifier.padding(top = FlowSpacing.md), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) {
        KpiPicker(state.definitions, "", {}, { actions.onAdd(it); showAdd = false }, { showAdd = false })
    }
}

@Composable
fun ReviewStatusOverview(state: ReviewUiState, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(stringResource(R.string.review_summary_detected, state.detectedCount), style = MaterialTheme.typography.headlineSmall)
        Text(
            if (state.blockingCount == 0) stringResource(R.string.review_all_clear)
            else stringResource(R.string.review_blocking_count, state.blockingCount),
            color = if (state.blockingCount == 0) FlowGreen else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(FlowSpacing.md))
        ReviewSummary(state)
    }
}

@Composable
private fun ReviewSummary(state: ReviewUiState) = FactoryCard(Modifier.fillMaxWidth(), PaddingValues(14.dp)) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Summary(state.readyCount, stringResource(R.string.review_ready), FlowGreen)
        Summary(state.attentionCount, stringResource(R.string.review_attention), FlowOrange)
        Summary(state.missingCount, stringResource(R.string.review_missing), FlowWarning)
        Summary(state.unresolvedCount, stringResource(R.string.review_unresolved), FlowPink)
    }
}

@Composable
private fun Summary(value: Int, label: String, color: Color) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value.toString(), style = MaterialTheme.typography.headlineSmall, color = color)
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
}

@Composable
private fun CompositeReviewRow(
    sourceLine: String,
    entries: List<ReviewEntry>,
    definitions: List<KpiDefinitionDto>,
    actions: ReviewContentActions,
) {
    var mappingEntryId by remember { mutableStateOf<Long?>(null) }
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FactoryIconChip(Icons.Outlined.AllInclusive, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(entries.first().displayName.ifBlank { entries.first().sourceLabel.orEmpty() }, style = MaterialTheme.typography.titleMedium)
                    Text(sourceLine, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                StatusPill(stringResource(R.string.composite_badge), MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(14.dp))
            entries.forEachIndexed { index, entry ->
                if (index > 0) HorizontalDivider(Modifier.padding(vertical = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (index == 0) stringResource(R.string.primary_value) else stringResource(R.string.secondary_value),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (entry.kpiDefinitionId == null) TextButton({ mappingEntryId = entry.id }) {
                        Text(stringResource(R.string.assign_kpi))
                    } else Text(entry.displayName, style = MaterialTheme.typography.labelLarge)
                }
                OutlinedTextField(
                    value = entry.value,
                    onValueChange = { actions.onEdit(entry.id, it) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    label = { Text(stringResource(R.string.value)) },
                    suffix = { entry.unit?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = entry.kpiDefinitionId == null,
                )
                entry.warnings.filterNot { it == "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT" || it == "MISSING_VALUE" }
                    .forEach { Text("• " + warningText(it), color = FactoryFlowWarning, style = MaterialTheme.typography.bodySmall) }
            }
            Text(
                stringResource(R.string.composite_help),
                Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
    mappingEntryId?.let { entryId ->
        KpiPicker(definitions, "", {}, { actions.onAssignEntry(entryId, it); mappingEntryId = null }, { mappingEntryId = null })
    }
}

@Composable
private fun ReviewEntryRow(entry: ReviewEntry, definitions: List<KpiDefinitionDto>, actions: ReviewContentActions) {
    var expanded by remember(entry.id) { mutableStateOf(entry.reviewState != ReviewState.READY) }
    var mapping by remember { mutableStateOf(false) }
    FactoryCard(
        Modifier.fillMaxWidth().animateContentSize(spring()),
        contentPadding = PaddingValues(if (entry.reviewState == ReviewState.READY) FlowSpacing.md else FlowSpacing.lg),
    ) {
        Column {
            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }, verticalAlignment = Alignment.CenterVertically) {
                val icon = when (entry.reviewState) {
                    ReviewState.READY -> Icons.Outlined.CheckCircle
                    ReviewState.ATTENTION -> Icons.Outlined.WarningAmber
                    ReviewState.MISSING -> Icons.Outlined.RemoveCircleOutline
                    ReviewState.UNRESOLVED -> Icons.AutoMirrored.Outlined.HelpOutline
                }
                val color = reviewStateColor(entry.reviewState)
                FactoryIconChip(icon, null, tint = color); Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(entry.displayName.ifBlank { entry.sourceLabel ?: entry.sourceLine.orEmpty() }, style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text((entry.value.ifBlank { stringResource(R.string.not_provided) }) + (entry.unit?.let { " " + it } ?: ""), style = MaterialTheme.typography.titleMedium)
                        entry.secondaryValue?.let { secondary ->
                            Spacer(Modifier.width(8.dp))
                            StatusPill(secondary + (entry.secondaryUnit ?: ""), MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                FlowStatusPill(when (entry.reviewState) {
                    ReviewState.READY -> stringResource(R.string.ready)
                    ReviewState.ATTENTION -> stringResource(R.string.review_attention)
                    ReviewState.MISSING -> stringResource(R.string.missing)
                    ReviewState.UNRESOLVED -> stringResource(R.string.review_unresolved)
                }, color, compact = true)
            }
            AnimatedVisibility(expanded) { Column(Modifier.padding(top = 12.dp)) {
                entry.suggestedKpiDisplayName?.let { suggestion ->
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.suggested_kpi, suggestion), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                            TextButton({ definitions.firstOrNull { it.id == entry.suggestedKpiDefinitionId }?.let { actions.onAssignEntry(entry.id, it) } }) { Text(stringResource(R.string.assign)) }
                        }
                    }
                }
                OutlinedTextField(entry.value, { actions.onEdit(entry.id, it) }, Modifier.fillMaxWidth().padding(top = 8.dp),
                    label = { Text(stringResource(R.string.value)) }, suffix = { entry.unit?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true)
                entry.secondaryValue?.let { secondary ->
                    OutlinedTextField(
                        secondary,
                        { actions.onEditSecondary(entry.id, it) },
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = { Text(stringResource(R.string.secondary_value)) },
                        suffix = { entry.secondaryUnit?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                    )
                }
                entry.sourceLine?.let { Text(it, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                entry.warnings.filterNot { it == "MISSING_VALUE" }.forEach { Text("• " + warningText(it), color = FactoryFlowWarning, style = MaterialTheme.typography.bodySmall) }
                if (entry.kpiDefinitionId == null) TextButton({ mapping = true }) { Text(stringResource(R.string.assign_kpi)) }
                if (entry.edited && entry.kpiDefinitionId != null && !entry.sourceLabel.isNullOrBlank()) Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(entry.rememberAlias, { actions.onRememberAlias(entry.id, it) })
                    Text(stringResource(R.string.remember_alias), style = MaterialTheme.typography.bodySmall)
                }
                TextButton({ actions.onRemove(entry.id) }) { Text(stringResource(R.string.remove_entry), color = MaterialTheme.colorScheme.error) }
            } }
        }
    }
    if (mapping) KpiPicker(definitions, "", {}, { actions.onAssignEntry(entry.id, it); mapping = false }, { mapping = false })
}

@Composable
private fun UnknownRow(line: ReviewUnknown, definitions: List<KpiDefinitionDto>, actions: ReviewContentActions) {
    var mapping by remember { mutableStateOf(false) }
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FactoryIconChip(Icons.AutoMirrored.Outlined.HelpOutline, null, tint = FlowPink, container = FlowPink.copy(alpha = FlowOpacity.tint))
                Spacer(Modifier.width(12.dp)); Text(line.sourceLine, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton({ actions.onResolveUnknown(line.id, "IGNORED", null) }) { Text(stringResource(R.string.ignore_line)) }
                Button({ mapping = true }) { Text(stringResource(R.string.assign)) }
            }
        }
    }
    if (mapping) KpiPicker(definitions, "", {}, { actions.onResolveUnknown(line.id, "ASSIGNED", it.id); mapping = false }, { mapping = false })
}

@Composable
private fun warningText(code: String) = stringResource(when {
    code.contains("RANGE") -> R.string.outside_range
    code.contains("UNIT") -> R.string.warning_unit
    code.contains("DUPLICATE") -> R.string.warning_duplicate
    code.contains("FUZZY") || code.contains("CONFIDENCE") -> R.string.warning_typo
    else -> R.string.warning_generic
})

@Composable
private fun reviewStateColor(state: ReviewState) = when (state) {
    ReviewState.READY -> FlowGreen
    ReviewState.ATTENTION -> FlowOrange
    ReviewState.MISSING -> FlowWarning
    ReviewState.UNRESOLVED -> FlowPink
}
