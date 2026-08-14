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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ReviewState { READY, ATTENTION, MISSING, UNRESOLVED }

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
    val rememberAlias: Boolean = false,
    val secondaryValue: String? = null,
    val secondaryExtractedValue: String? = null,
    val secondaryUnit: String? = null,
) {
    val reviewState: ReviewState
        get() = when {
            kpiDefinitionId == null -> ReviewState.UNRESOLVED
            warnings.contains("MISSING_VALUE") || value.isBlank() -> ReviewState.MISSING
            warnings.isNotEmpty() -> ReviewState.ATTENTION
            else -> ReviewState.READY
        }
}

data class ReviewUnknown(
    val id: Long,
    val sourceLine: String,
    val resolution: String,
    val resolvedKpiDefinitionId: Long?,
    val rememberAlias: Boolean = false,
)

data class ReviewUiState(
    val loading: Boolean = true,
    val report: ReportDto? = null,
    val entries: List<ReviewEntry> = emptyList(),
    val unknownLines: List<ReviewUnknown> = emptyList(),
    val definitions: List<KpiDefinitionDto> = emptyList(),
    val dirty: Boolean = false,
    val saving: Boolean = false,
    val confirming: Boolean = false,
    val savedNotice: Boolean = false,
    val error: UiError? = null,
) {
    val readyCount get() = entries.count { it.reviewState == ReviewState.READY }
    val attentionCount get() = entries.count { it.reviewState == ReviewState.ATTENTION }
    val missingCount get() = entries.count { it.reviewState == ReviewState.MISSING }
    val unresolvedCount get() = entries.count { it.reviewState == ReviewState.UNRESOLVED } + unknownLines.count { it.resolution == "UNRESOLVED" }
    val detectedCount get() = entries.size
    val blockingCount get() = unresolvedCount
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

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            val report = async { reports.draft(reportId) }
            val definitions = async { reports.definitions() }
            report.await() to definitions.await()
        }.onSuccess { (report, definitions) ->
            _state.value = ReviewUiState(
                loading = false,
                report = report,
                definitions = definitions,
                entries = report.entries.map { it.toReview() },
                unknownLines = report.unrecognizedLines.map {
                    ReviewUnknown(it.id, it.sourceLine, it.resolution, it.resolvedKpiDefinitionId)
                },
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

    fun remove(id: Long) = _state.update {
        it.copy(dirty = true, entries = it.entries.filterNot { entry -> entry.id == id })
    }

    fun add(definition: KpiDefinitionDto) = _state.update { state ->
        if (state.entries.any { it.kpiDefinitionId == definition.id }) state else state.copy(
            dirty = true,
            entries = state.entries + ReviewEntry(
                -definition.id, definition.id, definition.displayName, "", null, definition.unit,
                null, emptySet(), definition.displayName, null, true, null, null, null, null,
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
                    warnings = entry.warnings - "UNKNOWN_KPI" - "ADDITIONAL_VALUE_REQUIRES_ASSIGNMENT" - "LOW_CONFIDENCE",
                )
            },
        )
    }

    fun rememberEntryAlias(id: Long, remember: Boolean) = _state.update { state ->
        state.copy(dirty = true, entries = state.entries.map { if (it.id == id) it.copy(rememberAlias = remember) else it })
    }

    fun resolve(id: Long, resolution: String, definitionId: Long? = null) = _state.update { state ->
        state.copy(
            dirty = true,
            unknownLines = state.unknownLines.map {
                if (it.id == id) it.copy(resolution = resolution, resolvedKpiDefinitionId = definitionId) else it
            },
        )
    }

    fun rememberUnknownAlias(id: Long, remember: Boolean) = _state.update { state ->
        state.copy(dirty = true, unknownLines = state.unknownLines.map { if (it.id == id) it.copy(rememberAlias = remember) else it })
    }

    fun clearNotice() = _state.update { it.copy(savedNotice = false) }

    fun save(onSaved: (() -> Unit)? = null) = viewModelScope.launch {
        val current = _state.value
        val report = current.report ?: return@launch
        _state.update { it.copy(saving = true, error = null) }
        runCatching {
            val updated = reports.updateDraft(reportId, current.toDraftRequest(report))
            approveAliases(current)
            updated
        }.onSuccess { updated ->
            _state.update { it.copy(report = updated, saving = false, dirty = false, savedNotice = true) }
            onSaved?.invoke()
        }.onFailure { error -> _state.update { it.copy(saving = false, error = error.toUiError()) } }
    }

    fun confirm(onConfirmed: (Long) -> Unit) = viewModelScope.launch {
        val current = _state.value
        if (!current.canConfirm) return@launch
        _state.update { it.copy(confirming = true, error = null) }
        runCatching {
            if (current.dirty) reports.updateDraft(reportId, current.toDraftRequest(checkNotNull(current.report)))
            approveAliases(current)
            reports.confirm(
                reportId,
                ConfirmReportRequest(
                    current.entries.map {
                        ConfirmationEntryRequest(
                            checkNotNull(it.kpiDefinitionId),
                            it.value.asEditableDecimal(),
                            it.secondaryValue?.asEditableDecimal(),
                        )
                    },
                    current.unknownLines.map { UnknownLineResolutionRequest(it.id, it.resolution, it.resolvedKpiDefinitionId) },
                ),
            )
        }.onSuccess { report ->
            _state.update { it.copy(confirming = false, dirty = false) }
            onConfirmed(report.id)
        }.onFailure { error -> _state.update { it.copy(confirming = false, error = error.toUiError()) } }
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
    displayName = kpiDisplayName ?: suggestedKpiDisplayName ?: sourceLabel.orEmpty(),
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
    suggestionScore = suggestionScore?.multiply(java.math.BigDecimal("100"))?.setScale(0)?.toPlainString(),
    secondaryValue = (secondaryCurrentValue ?: secondaryExtractedValue)?.stripTrailingZeros()?.toPlainString(),
    secondaryExtractedValue = secondaryExtractedValue?.stripTrailingZeros()?.toPlainString(),
    secondaryUnit = secondaryUnit,
)

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
            entry.secondaryExtractedValue?.toBigDecimalOrNull(),
            entry.secondaryValue?.asEditableDecimal(),
            entry.secondaryUnit,
        )
    },
    unknownLines.map { DraftUnknownLineRequest(it.sourceLine, it.resolution, it.resolvedKpiDefinitionId) },
)

private fun String.labelPart(): String = substringBefore(':').substringBefore('=').substringBefore("->").trim()
