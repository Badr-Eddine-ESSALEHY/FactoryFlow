package com.factoryflow.app.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.ConfirmReportRequest
import com.factoryflow.app.core.network.dto.ConfirmationEntryRequest
import com.factoryflow.app.core.network.dto.DraftEntryRequest
import com.factoryflow.app.core.network.dto.DraftReportRequest
import com.factoryflow.app.core.network.dto.DraftUnknownLineRequest
import com.factoryflow.app.core.network.dto.KpiDefinitionDto
import com.factoryflow.app.core.network.dto.ReportDto
import com.factoryflow.app.core.network.dto.ReportEntryDto
import com.factoryflow.app.core.network.dto.UnknownLineResolutionRequest
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.asEditableDecimal
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReviewState { READY, ATTENTION, MISSING, UNRESOLVED }

enum class ReviewPresentationType {
    READY,
    ATTENTION_ACKNOWLEDGE,
    ATTENTION_DUPLICATE,
    MISSING,
    MISSING_CORRECTED,
    UNRESOLVED_STRONG_SUGGESTION,
    UNRESOLVED_WEAK_SUGGESTION,
    UNRESOLVED_NEW,
    SAFE_NOISE_PENDING,
    SAFE_NOISE_IGNORED,
}

data class ReviewEntry(
    val id: Long,
    val kpiDefinitionId: Long?,
    val displayName: String,
    val value: String,
    val extractedValue: String?,
    val unit: String?,
    val confidenceScore: String?,
    val warnings: Set<String>,
    val sourceLabel: String?,
    val sourceLine: String?,
    val edited: Boolean,
    val suggestedKpiDefinitionId: Long?,
    val suggestedKpiDisplayName: String?,
    val suggestedKpiUnit: String?,
    val suggestionScore: String?,
    val suggestionStrength: String? = null,
    val suggestionMatchMethod: String? = null,
    val rememberAlias: Boolean = false,
    val secondaryValue: String? = null,
    val secondaryExtractedValue: String? = null,
    val secondaryUnit: String? = null,
) {
    val presentationType: ReviewPresentationType
        get() = when {
            kpiDefinitionId == null && suggestedKpiDefinitionId != null && suggestionStrength == "STRONG" ->
                ReviewPresentationType.UNRESOLVED_STRONG_SUGGESTION
            kpiDefinitionId == null && suggestedKpiDefinitionId != null ->
                ReviewPresentationType.UNRESOLVED_WEAK_SUGGESTION
            kpiDefinitionId == null -> ReviewPresentationType.UNRESOLVED_NEW
            "MISSING_VALUE" in warnings && value.isBlank() -> ReviewPresentationType.MISSING
            "MISSING_VALUE" in warnings -> ReviewPresentationType.MISSING_CORRECTED
            "DUPLICATE_KPI" in warnings -> ReviewPresentationType.ATTENTION_DUPLICATE
            warnings.isNotEmpty() -> ReviewPresentationType.ATTENTION_ACKNOWLEDGE
            else -> ReviewPresentationType.READY
        }

    val canValidate: Boolean
        get() = value.isNotBlank() && presentationType in setOf(
            ReviewPresentationType.ATTENTION_ACKNOWLEDGE,
            ReviewPresentationType.ATTENTION_DUPLICATE,
            ReviewPresentationType.MISSING_CORRECTED,
        )

    val reviewState: ReviewState
        get() = when {
            presentationType == ReviewPresentationType.READY -> ReviewState.READY
            presentationType == ReviewPresentationType.MISSING || presentationType == ReviewPresentationType.MISSING_CORRECTED -> ReviewState.MISSING
            presentationType.name.startsWith("UNRESOLVED") -> ReviewState.UNRESOLVED
            else -> ReviewState.ATTENTION
        }

    val blocksConfirmation: Boolean
        get() = presentationType != ReviewPresentationType.READY && presentationType != ReviewPresentationType.MISSING
}

data class ReviewUnknown(
    val id: Long,
    val sourceLine: String,
    val resolution: String,
    val resolvedKpiDefinitionId: Long?,
    val kind: String = "KPI_LIKE",
    val classificationReason: String = "UNCLASSIFIED",
    val safeToIgnore: Boolean = false,
    val rememberAlias: Boolean = false,
) {
    val presentationType: ReviewPresentationType
        get() = when {
            resolution != "UNRESOLVED" -> ReviewPresentationType.SAFE_NOISE_IGNORED
            kind == "SAFE_NOISE" && safeToIgnore -> ReviewPresentationType.SAFE_NOISE_PENDING
            else -> ReviewPresentationType.UNRESOLVED_NEW
        }
    val blocksConfirmation get() = resolution == "UNRESOLVED"
}

data class ReviewUiState(
    val loading: Boolean = true,
    val report: ReportDto? = null,
    val entries: List<ReviewEntry> = emptyList(),
    val unknownLines: List<ReviewUnknown> = emptyList(),
    val definitions: List<KpiDefinitionDto> = emptyList(),
    val selectedTab: ReviewState = ReviewState.ATTENTION,
    val creatingDefinitionIds: Set<Long> = emptySet(),
    val processingEntryIds: Set<Long> = emptySet(),
    val processingUnknownIds: Set<Long> = emptySet(),
    val ignoringSafeLines: Boolean = false,
    val dirty: Boolean = false,
    val saving: Boolean = false,
    val confirming: Boolean = false,
    val savedNotice: Boolean = false,
    val error: UiError? = null,
) {
    val readyCount get() = entries.count { it.presentationType == ReviewPresentationType.READY }
    val attentionCount get() = entries.count { it.reviewState == ReviewState.ATTENTION }
    val missingCount get() = entries.count { it.reviewState == ReviewState.MISSING }
    val unresolvedCount get() = entries.count { it.reviewState == ReviewState.UNRESOLVED } + unknownLines.count { it.blocksConfirmation }
    val detectedCount get() = entries.size
    val blockingCount get() = entries.count { it.blocksConfirmation } + unknownLines.count { it.blocksConfirmation }
    val bulkIgnorableUnknownCount get() = unknownLines.count { it.resolution == "UNRESOLVED" && it.safeToIgnore }
    val canConfirm get() = report != null && entries.isNotEmpty() && blockingCount == 0
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val reports: ReportsRepository,
) : ViewModel() {
    private val reportId: Long = checkNotNull(savedStateHandle.get<String>("reportId")?.toLongOrNull())
    private val _state = MutableStateFlow(ReviewUiState())
    val state = _state.asStateFlow()
    private var saveJob: Job? = null
    private var confirmJob: Job? = null

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            val report = async { reports.draft(reportId) }
            val definitions = async { reports.definitions() }
            report.await() to definitions.await()
        }.onSuccess { (report, definitions) ->
            val entries = report.entries.map { it.toReview() }
            val unknownLines = report.unrecognizedLines.map {
                it.toReview()
            }
            _state.value = ReviewUiState(
                loading = false,
                report = report,
                definitions = definitions,
                entries = entries,
                unknownLines = unknownLines,
                selectedTab = initialReviewTab(entries, unknownLines),
            )
        }.onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }

    fun edit(id: Long, value: String) = _state.update { state ->
        state.copy(
            dirty = true,
            savedNotice = false,
            entries = state.entries.map { if (it.id == id) it.copy(value = value, edited = true) else it },
        )
    }

    fun editSecondary(id: Long, value: String) = _state.update { state ->
        state.copy(
            dirty = true,
            savedNotice = false,
            entries = state.entries.map { if (it.id == id) it.copy(secondaryValue = value, edited = true) else it },
        )
    }

    fun selectTab(tab: ReviewState) = _state.update { it.copy(selectedTab = tab) }

    fun cancelMissingCorrection(id: Long) = _state.update { state ->
        state.copy(
            dirty = true,
            savedNotice = false,
            entries = state.entries.map { entry ->
                if (entry.id == id) entry.copy(value = "", warnings = entry.warnings + "MISSING_VALUE", edited = true)
                else entry
            },
        )
    }

    fun validate(id: Long) = _state.update { state ->
        state.copy(
            dirty = true,
            entries = state.entries.map { entry ->
                if (entry.id == id && entry.canValidate) entry.copy(warnings = emptySet()) else entry
            },
        )
    }

    fun remove(id: Long) {
        if (id < 0) {
            _state.update { state -> state.copy(dirty = true, entries = state.entries.filterNot { it.id == id }) }
            return
        }
        if (id in _state.value.processingEntryIds) return
        val removed = _state.value.entries.firstOrNull { it.id == id } ?: return
        val transientTrace = removed.sourceLine?.takeIf { it.isNotBlank() }?.let {
            ReviewUnknown(
                id = -id,
                sourceLine = it,
                resolution = "IGNORED",
                resolvedKpiDefinitionId = null,
                kind = "KPI_LIKE",
                classificationReason = "REMOVED_EXTRACTION",
            )
        }
        _state.update { state -> state.copy(
            processingEntryIds = state.processingEntryIds + id,
            error = null,
            entries = state.entries.filterNot { it.id == id },
            unknownLines = if (transientTrace == null) state.unknownLines else state.unknownLines + transientTrace,
        ) }
        viewModelScope.launch {
            runCatching { reports.removeDraftEntry(reportId, id) }
                .onSuccess { report -> _state.update { state -> state.copy(
                    report = report,
                    entries = state.entries.filterNot { it.id == id },
                    unknownLines = report.unrecognizedLines.map { it.toReview() },
                    processingEntryIds = state.processingEntryIds - id,
                ) } }
                .onFailure { error -> _state.update {
                    it.copy(
                        processingEntryIds = it.processingEntryIds - id,
                        entries = (it.entries + removed).sortedBy { entry -> entry.id },
                        unknownLines = it.unknownLines.filterNot { line -> line.id == -id },
                        error = error.toUiError(),
                    )
                } }
        }
    }

    fun add(definition: KpiDefinitionDto) = _state.update { state ->
        if (state.entries.any { it.kpiDefinitionId == definition.id }) state else state.copy(
            dirty = true,
            entries = state.entries + ReviewEntry(
                id = -definition.id,
                kpiDefinitionId = definition.id,
                displayName = definition.displayName,
                value = "",
                extractedValue = null,
                unit = definition.unit,
                confidenceScore = null,
                warnings = setOf("MISSING_VALUE"),
                sourceLabel = definition.displayName,
                sourceLine = null,
                edited = true,
                suggestedKpiDefinitionId = null,
                suggestedKpiDisplayName = null,
                suggestedKpiUnit = null,
                suggestionScore = null,
            ),
        )
    }

    fun assignEntry(id: Long, definition: KpiDefinitionDto) = _state.update { state ->
        state.copy(
            dirty = true,
            entries = state.entries.map { entry ->
                if (entry.id != id) entry else entry.copy(
                    kpiDefinitionId = definition.id,
                    displayName = definition.displayName,
                    unit = definition.unit,
                    edited = true,
                    suggestedKpiDefinitionId = null,
                    suggestedKpiDisplayName = null,
                    suggestedKpiUnit = null,
                    suggestionScore = null,
                    suggestionStrength = null,
                    suggestionMatchMethod = null,
                    warnings = entry.warnings - setOf(
                        "UNKNOWN_KPI",
                        "AMBIGUOUS_KPI",
                        "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT",
                        "LOW_CONFIDENCE",
                        "MATCH_REQUIRES_REVIEW",
                        "OCR_LABEL_CORRECTION",
                    ),
                )
            },
        )
    }

    fun addDetectedKpi(id: Long) {
        if (id in _state.value.creatingDefinitionIds) return
        viewModelScope.launch {
            _state.update { it.copy(creatingDefinitionIds = it.creatingDefinitionIds + id, error = null) }
            runCatching {
                val report = reports.addDetectedKpi(reportId, id)
                report to reports.definitions()
            }.onSuccess { (report, definitions) ->
                val persisted = report.entries.firstOrNull { it.id == id }
                _state.update { state ->
                    state.copy(
                        report = report,
                        definitions = definitions,
                        creatingDefinitionIds = state.creatingDefinitionIds - id,
                        entries = state.entries.map { entry ->
                            if (entry.id != id || persisted == null) entry else entry.copy(
                                kpiDefinitionId = persisted.kpiDefinitionId,
                                displayName = persisted.kpiDisplayName ?: entry.displayName,
                                unit = persisted.capturedUnit ?: definitions.firstOrNull { it.id == persisted.kpiDefinitionId }?.unit,
                                warnings = persisted.warnings,
                                edited = true,
                                suggestedKpiDefinitionId = null,
                                suggestedKpiDisplayName = null,
                                suggestedKpiUnit = null,
                                suggestionScore = null,
                                suggestionStrength = null,
                                suggestionMatchMethod = null,
                            )
                        },
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(creatingDefinitionIds = it.creatingDefinitionIds - id, error = error.toUiError()) }
            }
        }
    }

    fun rememberEntryAlias(id: Long, remember: Boolean) = _state.update { state ->
        state.copy(dirty = true, entries = state.entries.map { if (it.id == id) it.copy(rememberAlias = remember) else it })
    }

    fun resolve(id: Long, resolution: String, definitionId: Long? = null) {
        if (id in _state.value.processingUnknownIds) return
        val previous = _state.value.unknownLines.firstOrNull { it.id == id } ?: return
        _state.update { state -> state.copy(
            processingUnknownIds = state.processingUnknownIds + id,
            error = null,
            unknownLines = state.unknownLines.map {
                if (it.id == id) it.copy(resolution = resolution, resolvedKpiDefinitionId = definitionId) else it
            },
        ) }
        viewModelScope.launch {
            runCatching {
                reports.resolveUnrecognizedLine(
                    reportId,
                    UnknownLineResolutionRequest(id, resolution, definitionId),
                )
            }.onSuccess { report -> _state.update { state -> state.copy(
                report = report,
                unknownLines = report.unrecognizedLines.map { it.toReview() },
                processingUnknownIds = state.processingUnknownIds - id,
            ) } }.onFailure { error -> _state.update {
                it.copy(
                    processingUnknownIds = it.processingUnknownIds - id,
                    unknownLines = it.unknownLines.map { line -> if (line.id == id) previous else line },
                    error = error.toUiError(),
                )
            } }
        }
    }

    fun ignoreSafeUnrecognizedLines() {
        if (_state.value.ignoringSafeLines) return
        viewModelScope.launch {
            _state.update { it.copy(ignoringSafeLines = true, error = null) }
            runCatching { reports.ignoreSafeUnrecognizedLines(reportId) }
                .onSuccess { report ->
                    _state.update { state ->
                        state.copy(
                            report = report,
                            ignoringSafeLines = false,
                            unknownLines = report.unrecognizedLines.map { it.toReview() },
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(ignoringSafeLines = false, error = error.toUiError()) }
                }
        }
    }

    fun rememberUnknownAlias(id: Long, remember: Boolean) = _state.update { state ->
        state.copy(dirty = true, unknownLines = state.unknownLines.map { if (it.id == id) it.copy(rememberAlias = remember) else it })
    }

    fun clearNotice() = _state.update { it.copy(savedNotice = false) }

    fun save(onSaved: (() -> Unit)? = null) {
        if (saveJob?.isActive == true || confirmJob?.isActive == true) return
        if (!_state.value.dirty) {
            _state.update { it.copy(savedNotice = true) }
            onSaved?.invoke()
            return
        }
        saveJob = viewModelScope.launch {
        val current = _state.value
        if (current.saving || current.confirming) return@launch
        val report = current.report ?: return@launch
        _state.update { it.copy(saving = true, error = null) }
        runCatching {
            val updated = reports.updateDraft(reportId, current.toDraftRequest(report))
            approveAliases(current)
            updated
        }.onSuccess { updated ->
            _state.update { state -> state.withReport(updated).copy(saving = false, dirty = false, savedNotice = true) }
            onSaved?.invoke()
        }.onFailure { error -> _state.update { it.copy(saving = false, error = error.toUiError()) } }
        }
    }

    fun confirm(onConfirmed: (Long) -> Unit) {
        if (confirmJob?.isActive == true || saveJob?.isActive == true) return
        confirmJob = viewModelScope.launch {
        val current = _state.value
        if (!current.canConfirm || current.saving || current.confirming) return@launch
        _state.update { it.copy(confirming = true, error = null) }
        runCatching {
            val persisted = if (current.dirty) {
                reports.updateDraft(reportId, current.toDraftRequest(checkNotNull(current.report)))
            } else {
                checkNotNull(current.report)
            }
            approveAliases(current)
            reports.confirm(
                reportId,
                ConfirmReportRequest(
                    persisted.entries.mapIndexed { index, persistedEntry ->
                        val reviewedEntry = current.entries[index]
                        ConfirmationEntryRequest(
                            kpiDefinitionId = checkNotNull(persistedEntry.kpiDefinitionId),
                            finalValue = reviewedEntry.value.asEditableDecimal(),
                            secondaryFinalValue = reviewedEntry.secondaryValue?.asEditableDecimal(),
                            entryId = persistedEntry.id,
                        )
                    },
                    persisted.unrecognizedLines.map {
                        UnknownLineResolutionRequest(it.id, it.resolution, it.resolvedKpiDefinitionId)
                    },
                ),
            )
        }.onSuccess { report ->
            _state.update { it.copy(confirming = false, dirty = false) }
            onConfirmed(report.id)
        }.onFailure { error -> _state.update { it.copy(confirming = false, error = error.toUiError()) } }
        }
    }

    private suspend fun approveAliases(state: ReviewUiState) {
        state.entries.filter { it.rememberAlias && it.kpiDefinitionId != null && !it.sourceLabel.isNullOrBlank() }
            .forEach { reports.approveAlias(checkNotNull(it.kpiDefinitionId), checkNotNull(it.sourceLabel)) }
        state.unknownLines.filter { it.rememberAlias && it.resolvedKpiDefinitionId != null }
            .forEach { reports.approveAlias(checkNotNull(it.resolvedKpiDefinitionId), it.sourceLine.labelPart()) }
    }
}

private fun ReportEntryDto.toReview() = ReviewEntry(
    id = id,
    kpiDefinitionId = kpiDefinitionId,
    displayName = if (kpiDefinitionId == null) sourceLabel.orEmpty() else kpiDisplayName ?: sourceLabel.orEmpty(),
    value = (currentValue ?: extractedValue)?.stripTrailingZeros()?.toPlainString().orEmpty(),
    extractedValue = extractedValue?.stripTrailingZeros()?.toPlainString(),
    unit = capturedUnit ?: suggestedKpiUnit,
    confidenceScore = confidenceScore?.stripTrailingZeros()?.toPlainString(),
    warnings = warnings,
    sourceLabel = sourceLabel,
    sourceLine = sourceLine,
    edited = editedByUser,
    suggestedKpiDefinitionId = suggestedKpiDefinitionId,
    suggestedKpiDisplayName = suggestedKpiDisplayName,
    suggestedKpiUnit = suggestedKpiUnit,
    suggestionScore = suggestionScore?.multiply(java.math.BigDecimal("100"))?.setScale(0, java.math.RoundingMode.HALF_UP)?.toPlainString(),
    suggestionStrength = suggestionStrength,
    suggestionMatchMethod = suggestionMatchMethod,
    secondaryValue = (secondaryCurrentValue ?: secondaryExtractedValue)?.stripTrailingZeros()?.toPlainString(),
    secondaryExtractedValue = secondaryExtractedValue?.stripTrailingZeros()?.toPlainString(),
    secondaryUnit = secondaryUnit,
)

private fun com.factoryflow.app.core.network.dto.UnknownLineDto.toReview() = ReviewUnknown(
    id = id,
    sourceLine = sourceLine,
    resolution = resolution,
    resolvedKpiDefinitionId = resolvedKpiDefinitionId,
    kind = kind,
    classificationReason = classificationReason,
    safeToIgnore = safeToIgnore,
)

private fun ReviewUiState.withReport(updated: ReportDto): ReviewUiState = copy(
    report = updated,
    entries = updated.entries.map { serverEntry ->
        serverEntry.toReview().copy(
            rememberAlias = entries.firstOrNull { local -> local.id == serverEntry.id }?.rememberAlias ?: false,
        )
    },
    unknownLines = updated.unrecognizedLines.map { it.toReview() },
)

private fun initialReviewTab(entries: List<ReviewEntry>, unknownLines: List<ReviewUnknown>): ReviewState = when {
    entries.any { it.reviewState == ReviewState.ATTENTION } -> ReviewState.ATTENTION
    entries.any { it.reviewState == ReviewState.MISSING } -> ReviewState.MISSING
    entries.any { it.reviewState == ReviewState.UNRESOLVED } || unknownLines.any { it.resolution == "UNRESOLVED" } -> ReviewState.UNRESOLVED
    else -> ReviewState.READY
}

private fun ReviewUiState.toDraftRequest(report: ReportDto) = DraftReportRequest(
    report.effectiveDate,
    report.source,
    report.rawText,
    entries.map { entry ->
        DraftEntryRequest(
            entry.kpiDefinitionId,
            entry.sourceLabel,
            entry.sourceLine,
            entry.extractedValue?.toBigDecimalOrNull(),
            entry.value.asEditableDecimal(),
            entry.confidenceScore?.toBigDecimalOrNull(),
            entry.edited,
            entry.unit,
            entry.warnings,
            entry.suggestedKpiDefinitionId,
            entry.suggestionScore?.toBigDecimalOrNull()?.movePointLeft(2),
            entry.suggestionStrength,
            entry.suggestionMatchMethod,
            entry.secondaryExtractedValue?.toBigDecimalOrNull(),
            entry.secondaryValue?.asEditableDecimal(),
            entry.secondaryUnit,
        )
    },
    unknownLines.map {
        DraftUnknownLineRequest(
            sourceLine = it.sourceLine,
            resolution = it.resolution,
            resolvedKpiDefinitionId = it.resolvedKpiDefinitionId,
            kind = it.kind,
            classificationReason = it.classificationReason,
            safeToIgnore = it.safeToIgnore,
        )
    },
)

private fun String.labelPart(): String = substringBefore(':').substringBefore('=').substringBefore("->").trim()
