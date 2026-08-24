package com.factoryflow.app.feature.review

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
    val snackbarHostState = remember { SnackbarHostState() }
    val draftSavedMessage = stringResource(R.string.draft_saved)
    BackHandler { if (state.dirty) showLeave = true else onBack() }

    val actions = remember(viewModel) {
        ReviewContentActions(
            onAdd = viewModel::add,
            onEdit = viewModel::edit,
            onEditSecondary = viewModel::editSecondary,
            onAssignEntry = viewModel::assignEntry,
            onAddDetectedKpi = viewModel::addDetectedKpi,
            onValidate = viewModel::validate,
            onCancelMissingCorrection = viewModel::cancelMissingCorrection,
            onSelectTab = viewModel::selectTab,
            onRememberAlias = viewModel::rememberEntryAlias,
            onRemove = viewModel::remove,
            onResolveUnknown = viewModel::resolve,
            onIgnoreSafeUnknownLines = viewModel::ignoreSafeUnrecognizedLines,
        )
    }

    FactoryFlowScaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            FocusedTopBar(stringResource(R.string.review_title), { if (state.dirty) showLeave = true else onBack() }) {
                IconButton(onClick = { viewModel.save() }, enabled = !state.loading && !state.saving && !state.confirming) {
                    if (state.saving) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Outlined.Save, stringResource(R.string.save_draft))
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.loading && state.report != null) {
                FlowBottomActionBar {
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
                actions = actions,
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
        dismissButton = {
            Row {
                TextButton({ showLeave = false; onBack() }) { Text(stringResource(R.string.leave_without_saving)) }
                TextButton({ showLeave = false }) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
    LaunchedEffect(state.savedNotice) {
        if (state.savedNotice) {
            snackbarHostState.showSnackbar(draftSavedMessage)
            viewModel.clearNotice()
        }
    }
}

data class ReviewContentActions(
    val onAdd: (KpiDefinitionDto) -> Unit = {},
    val onEdit: (Long, String) -> Unit = { _, _ -> },
    val onEditSecondary: (Long, String) -> Unit = { _, _ -> },
    val onAssignEntry: (Long, KpiDefinitionDto) -> Unit = { _, _ -> },
    val onAddDetectedKpi: (Long) -> Unit = {},
    val onValidate: (Long) -> Unit = {},
    val onCancelMissingCorrection: (Long) -> Unit = {},
    val onSelectTab: (ReviewState) -> Unit = {},
    val onRememberAlias: (Long, Boolean) -> Unit = { _, _ -> },
    val onRemove: (Long) -> Unit = {},
    val onResolveUnknown: (Long, String, Long?) -> Unit = { _, _, _ -> },
    val onIgnoreSafeUnknownLines: () -> Unit = {},
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewContent(
    state: ReviewUiState,
    actions: ReviewContentActions,
    modifier: Modifier = Modifier,
) {
    var showAdd by remember { mutableStateOf(false) }
    var showSource by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val compositeGroups = remember(state.entries) {
        state.entries
            .groupBy { it.sourceLine }
            .filter { (source, entries) ->
                !source.isNullOrBlank() && entries.size > 1 &&
                    entries.any { "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT" in it.warnings }
            }
    }
    val compositeIds = remember(compositeGroups) {
        compositeGroups.values.flatten().mapTo(mutableSetOf()) { it.id }
    }
    val visibleEntries = remember(state.entries, state.selectedTab, compositeIds) {
        state.entries.filter { entry ->
            entry.id !in compositeIds && entry.reviewState == state.selectedTab
        }
    }
    val unresolvedUnknownLines = remember(state.unknownLines) {
        state.unknownLines.filter { it.resolution == "UNRESOLVED" }
    }
    val unresolvedKpiLines = remember(unresolvedUnknownLines) {
        unresolvedUnknownLines.filterNot { it.presentationType == ReviewPresentationType.SAFE_NOISE_PENDING }
    }
    val safeNoiseLines = remember(unresolvedUnknownLines) {
        unresolvedUnknownLines.filter { it.presentationType == ReviewPresentationType.SAFE_NOISE_PENDING }
    }

    FlowContentSurface(modifier) {
        LazyColumn(
            Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(horizontal = FlowSpacing.xl, vertical = FlowSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
        ) {
            item { ReviewStatusOverview(state) }
            state.error?.let { error ->
                item(key = "review-error") {
                    FlowCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(error.title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                        Text(stringResource(error.detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
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
                    state.selectedTab.ordinal,
                    { actions.onSelectTab(ReviewState.entries[it]) },
                )
            }

            if (state.selectedTab == ReviewState.UNRESOLVED && state.bulkIgnorableUnknownCount > 0) {
                stickyHeader(key = "ignore-safe-lines") {
                    IgnoreSafeUnknownLinesAction(
                        count = state.bulkIgnorableUnknownCount,
                        loading = state.ignoringSafeLines,
                        onClick = actions.onIgnoreSafeUnknownLines,
                    )
                }
            }
            if (visibleEntries.isEmpty() && !(state.selectedTab == ReviewState.UNRESOLVED && unresolvedUnknownLines.isNotEmpty())) {
                item { EmptyPane(stringResource(R.string.review_all_clear), "", icon = Icons.Outlined.CheckCircle) }
            }
            items(
                visibleEntries,
                key = { "entry-" + it.id },
                contentType = { "entry-" + it.presentationType.name },
            ) { entry ->
                ReviewEntryRow(
                    entry = entry,
                    definitions = state.definitions,
                    creatingDefinition = entry.id in state.creatingDefinitionIds,
                    processing = entry.id in state.processingEntryIds,
                    actions = actions,
                )
            }
            if (state.selectedTab == ReviewState.UNRESOLVED) {
                if (unresolvedKpiLines.isNotEmpty()) {
                    item(key = "unresolved-kpi-heading") {
                        ReviewGroupHeading(
                            stringResource(R.string.unresolved_kpi_group),
                            stringResource(R.string.unresolved_kpi_group_detail),
                        )
                    }
                    items(unresolvedKpiLines, key = { "unknown-kpi-" + it.id }, contentType = { "unknown-kpi" }) { line ->
                        UnknownRow(
                            line, state.definitions, line.id in state.processingUnknownIds, actions,
                        )
                    }
                }
                if (safeNoiseLines.isNotEmpty()) {
                    item(key = "safe-noise-heading") {
                        ReviewGroupHeading(
                            stringResource(R.string.safe_noise_group),
                            stringResource(R.string.safe_noise_group_detail),
                        )
                    }
                    items(safeNoiseLines, key = { "safe-noise-" + it.id }, contentType = { "safe-noise" }) { line ->
                        UnknownRow(
                            line, state.definitions, line.id in state.processingUnknownIds, actions,
                        )
                    }
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
private fun IgnoreSafeUnknownLinesAction(count: Int, loading: Boolean, onClick: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = FlowSpacing.xs)
                .heightIn(min = FlowSize.touchTarget),
            enabled = !loading,
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(FlowSpacing.sm))
            }
            Text(stringResource(R.string.ignore_all_safe_noise_count, count))
        }
    }
}

@Composable
private fun ReviewGroupHeading(title: String, detail: String) = Column {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
private fun ReviewEntryRow(
    entry: ReviewEntry,
    definitions: List<KpiDefinitionDto>,
    creatingDefinition: Boolean,
    processing: Boolean,
    actions: ReviewContentActions,
) {
    when (entry.presentationType) {
        ReviewPresentationType.READY -> ReadyReviewCard(entry, processing, actions)
        ReviewPresentationType.ATTENTION_ACKNOWLEDGE,
        ReviewPresentationType.ATTENTION_DUPLICATE -> AttentionReviewCard(entry, processing, actions)
        ReviewPresentationType.MISSING,
        ReviewPresentationType.MISSING_CORRECTED -> MissingReviewCard(entry, processing, actions)
        ReviewPresentationType.UNRESOLVED_STRONG_SUGGESTION,
        ReviewPresentationType.UNRESOLVED_WEAK_SUGGESTION,
        ReviewPresentationType.UNRESOLVED_NEW -> UnresolvedReviewCard(
            entry, definitions, creatingDefinition, processing, actions,
        )
        ReviewPresentationType.SAFE_NOISE_PENDING,
        ReviewPresentationType.SAFE_NOISE_IGNORED -> Unit
    }
}

@Composable
private fun ReadyReviewCard(entry: ReviewEntry, processing: Boolean, actions: ReviewContentActions) {
    var expanded by remember(entry.id) { mutableStateOf(false) }
    FactoryCard(Modifier.fillMaxWidth().animateContentSize(spring()), PaddingValues(FlowSpacing.md)) {
        Column {
            ReviewEntryHeader(entry, Modifier.clickable { expanded = !expanded })
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = FlowSpacing.sm)) {
                    ReviewSourceAndWarnings(entry)
                    ReviewRemoveAction(entry, processing, actions)
                }
            }
        }
    }
}

@Composable
private fun AttentionReviewCard(entry: ReviewEntry, processing: Boolean, actions: ReviewContentActions) {
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            ReviewEntryHeader(entry)
            ReviewValueEditor(entry, actions)
            ReviewSourceAndWarnings(entry)
            RememberAliasOption(entry, actions)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReviewRemoveAction(entry, processing, actions, Modifier.weight(1f), outlined = true)
                Button(
                    onClick = { actions.onValidate(entry.id) },
                    enabled = entry.canValidate && !processing,
                    modifier = Modifier.weight(1f).heightIn(min = FlowSize.touchTarget),
                    contentPadding = PaddingValues(horizontal = FlowSpacing.sm),
                ) {
                    Icon(Icons.Outlined.Check, null)
                    Spacer(Modifier.width(FlowSpacing.xs))
                    Text(stringResource(
                        if (entry.presentationType == ReviewPresentationType.ATTENTION_DUPLICATE)
                            R.string.validate_observation else R.string.validate_value,
                    ), maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun MissingReviewCard(entry: ReviewEntry, processing: Boolean, actions: ReviewContentActions) {
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            ReviewEntryHeader(entry)
            ReviewValueEditor(entry, actions)
            ReviewSourceAndWarnings(entry)
            if (entry.presentationType == ReviewPresentationType.MISSING) {
                Text(
                    stringResource(R.string.missing_is_valid),
                    Modifier.padding(top = FlowSpacing.sm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ReviewRemoveAction(entry, processing, actions)
            } else {
                RememberAliasOption(entry, actions)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { actions.onCancelMissingCorrection(entry.id) },
                        modifier = Modifier.weight(1f),
                    ) { Text(stringResource(R.string.cancel_missing_correction)) }
                    Button(onClick = { actions.onValidate(entry.id) }, enabled = entry.canValidate) {
                        Icon(Icons.Outlined.Check, null)
                        Spacer(Modifier.width(FlowSpacing.xs))
                        Text(stringResource(R.string.validate_value))
                    }
                }
            }
        }
    }
}

@Composable
private fun UnresolvedReviewCard(
    entry: ReviewEntry,
    definitions: List<KpiDefinitionDto>,
    creatingDefinition: Boolean,
    processing: Boolean,
    actions: ReviewContentActions,
) {
    var mapping by remember { mutableStateOf(false) }
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            ReviewEntryHeader(entry)
            ReviewValueEditor(entry, actions)
            ReviewSourceAndWarnings(entry)
            val suggestion = entry.suggestedKpiDisplayName
            if (entry.presentationType == ReviewPresentationType.UNRESOLVED_STRONG_SUGGESTION && suggestion != null) {
                SuggestionCard(entry, strong = true) {
                    definitions.firstOrNull { it.id == entry.suggestedKpiDefinitionId }
                        ?.let { actions.onAssignEntry(entry.id, it) }
                }
            } else {
                Button(
                    onClick = { actions.onAddDetectedKpi(entry.id) },
                    enabled = !creatingDefinition && !processing,
                    modifier = Modifier.fillMaxWidth().padding(top = FlowSpacing.sm),
                ) {
                    if (creatingDefinition) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(FlowSpacing.sm))
                    Text(stringResource(R.string.add_new_detected_kpi))
                }
                if (suggestion != null) SuggestionCard(entry, strong = false) {
                    definitions.firstOrNull { it.id == entry.suggestedKpiDefinitionId }
                        ?.let { actions.onAssignEntry(entry.id, it) }
                }
            }
            TextButton(onClick = { mapping = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (suggestion == null) R.string.assign_kpi else R.string.choose_another_kpi))
            }
            if (entry.presentationType == ReviewPresentationType.UNRESOLVED_STRONG_SUGGESTION) {
                TextButton(
                    onClick = { actions.onAddDetectedKpi(entry.id) },
                    enabled = !creatingDefinition,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.add_new_detected_kpi)) }
            }
            ReviewRemoveAction(entry, processing, actions)
        }
    }
    if (mapping) KpiPicker(
        definitions, "", {},
        { actions.onAssignEntry(entry.id, it); mapping = false },
        { mapping = false },
    )
}

@Composable
private fun SuggestionCard(entry: ReviewEntry, strong: Boolean, onAssign: () -> Unit) {
    Surface(
        color = if (strong) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().padding(top = FlowSpacing.sm),
    ) {
        Column(Modifier.padding(FlowSpacing.md)) {
            Text(
                if (strong) stringResource(R.string.reliable_suggestion) else stringResource(R.string.possible_suggestion),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(
                    R.string.suggestion_with_score,
                    entry.suggestedKpiDisplayName.orEmpty(),
                    entry.suggestionScore ?: "—",
                ),
                style = MaterialTheme.typography.titleSmall,
            )
            TextButton(onClick = onAssign, modifier = Modifier.align(Alignment.End)) {
                Text(stringResource(R.string.assign_suggestion))
            }
        }
    }
}

@Composable
private fun ReviewEntryHeader(entry: ReviewEntry, modifier: Modifier = Modifier) {
    val state = entry.reviewState
    val icon = when (state) {
        ReviewState.READY -> Icons.Outlined.CheckCircle
        ReviewState.ATTENTION -> Icons.Outlined.WarningAmber
        ReviewState.MISSING -> Icons.Outlined.RemoveCircleOutline
        ReviewState.UNRESOLVED -> Icons.AutoMirrored.Outlined.HelpOutline
    }
    val color = reviewStateColor(state)
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        FactoryIconChip(icon, null, tint = color)
        Spacer(Modifier.width(FlowSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(entry.displayName.ifBlank { entry.sourceLabel ?: entry.sourceLine.orEmpty() }, style = MaterialTheme.typography.titleSmall)
            Text(
                entry.value.ifBlank { stringResource(R.string.not_provided) } + (entry.unit?.let { " $it" } ?: ""),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        FlowStatusPill(
            when (state) {
                ReviewState.READY -> stringResource(R.string.ready)
                ReviewState.ATTENTION -> stringResource(R.string.review_attention)
                ReviewState.MISSING -> stringResource(R.string.missing)
                ReviewState.UNRESOLVED -> stringResource(R.string.review_unresolved)
            },
            color,
            compact = true,
        )
    }
}

@Composable
private fun ReviewValueEditor(entry: ReviewEntry, actions: ReviewContentActions) {
    OutlinedTextField(
        value = entry.value,
        onValueChange = { actions.onEdit(entry.id, it) },
        modifier = Modifier.fillMaxWidth().padding(top = FlowSpacing.md),
        label = { Text(stringResource(R.string.value)) },
        suffix = { entry.unit?.let { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
    )
    entry.secondaryValue?.let { secondary ->
        OutlinedTextField(
            value = secondary,
            onValueChange = { actions.onEditSecondary(entry.id, it) },
            modifier = Modifier.fillMaxWidth().padding(top = FlowSpacing.sm),
            label = { Text(stringResource(R.string.secondary_value)) },
            suffix = { entry.secondaryUnit?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
    }
}

@Composable
private fun ReviewSourceAndWarnings(entry: ReviewEntry) {
    entry.sourceLine?.takeIf { it.isNotBlank() }?.let {
        Text(it, Modifier.padding(top = FlowSpacing.sm), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
    }
    entry.warnings.filterNot { it == "MISSING_VALUE" }.forEach {
        Text("• " + warningText(it), color = FactoryFlowWarning, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun RememberAliasOption(entry: ReviewEntry, actions: ReviewContentActions) {
    if (entry.edited && entry.kpiDefinitionId != null && !entry.sourceLabel.isNullOrBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(entry.rememberAlias, { actions.onRememberAlias(entry.id, it) })
            Text(stringResource(R.string.remember_alias), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ReviewRemoveAction(
    entry: ReviewEntry,
    processing: Boolean,
    actions: ReviewContentActions,
    modifier: Modifier = Modifier,
    outlined: Boolean = false,
) {
    val content: @Composable RowScope.() -> Unit = {
        if (processing) {
            CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(FlowSpacing.xs))
        }
        Text(
            stringResource(if (entry.kpiDefinitionId == null) R.string.ignore_line else R.string.remove_entry),
            color = MaterialTheme.colorScheme.error,
            maxLines = 2,
        )
    }
    if (outlined) {
        OutlinedButton(
            onClick = { actions.onRemove(entry.id) },
            enabled = !processing,
            modifier = modifier.heightIn(min = FlowSize.touchTarget),
            contentPadding = PaddingValues(horizontal = FlowSpacing.sm),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            content = content,
        )
    } else {
        TextButton(onClick = { actions.onRemove(entry.id) }, enabled = !processing, modifier = modifier, content = content)
    }
}

@Composable
private fun UnknownRow(
    line: ReviewUnknown,
    definitions: List<KpiDefinitionDto>,
    processing: Boolean,
    actions: ReviewContentActions,
) {
    var mapping by remember { mutableStateOf(false) }
    val safeNoise = line.presentationType == ReviewPresentationType.SAFE_NOISE_PENDING
    FactoryCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.md)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FactoryIconChip(
                    if (safeNoise) Icons.Outlined.FilterAltOff else Icons.AutoMirrored.Outlined.HelpOutline,
                    null,
                    tint = if (safeNoise) MaterialTheme.colorScheme.onSurfaceVariant else FlowPink,
                    container = if (safeNoise) MaterialTheme.colorScheme.surfaceVariant else FlowPink.copy(alpha = FlowOpacity.tint),
                )
                Spacer(Modifier.width(FlowSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(line.sourceLine, style = MaterialTheme.typography.bodyMedium)
                    Text(line.classificationReason, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = { actions.onResolveUnknown(line.id, "IGNORED", null) },
                    enabled = !processing,
                ) {
                    if (processing) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text(stringResource(R.string.ignore_line))
                }
                if (!safeNoise) Button(onClick = { mapping = true }, enabled = !processing) {
                    Text(stringResource(R.string.assign))
                }
            }
        }
    }
    if (mapping) KpiPicker(
        definitions, "", {},
        { actions.onResolveUnknown(line.id, "ASSIGNED", it.id); mapping = false },
        { mapping = false },
    )
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
