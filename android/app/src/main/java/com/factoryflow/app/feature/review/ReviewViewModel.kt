package com.factoryflow.app.feature.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.*
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

data class ReviewEntry(
    val id: Long, val kpiDefinitionId: Long, val displayName: String, val value: String,
    val extractedValue: String?, val unit: String?, val confidenceScore: String?, val warnings: Set<String>,
    val sourceLine: String?, val edited: Boolean,
)
data class ReviewUnknown(val id: Long, val sourceLine: String, val resolution: String, val resolvedKpiDefinitionId: Long?)
data class ReviewUiState(
    val loading: Boolean = true, val report: ReportDto? = null, val entries: List<ReviewEntry> = emptyList(),
    val unknownLines: List<ReviewUnknown> = emptyList(), val definitions: List<KpiDefinitionDto> = emptyList(),
    val dirty: Boolean = false, val saving: Boolean = false, val confirming: Boolean = false,
    val savedNotice: Boolean = false, val error: UiError? = null,
) {
    val canConfirm get() = report != null && entries.isNotEmpty() && unknownLines.none { it.resolution == "UNRESOLVED" }
}

@HiltViewModel
class ReviewViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val reports: ReportsRepository) : ViewModel() {
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
                loading = false, report = report, definitions = definitions,
                entries = report.entries.filter { it.kpiDefinitionId != null }.map { it.toReview() },
                unknownLines = report.unrecognizedLines.map { ReviewUnknown(it.id, it.sourceLine, it.resolution, it.resolvedKpiDefinitionId) },
            )
        }.onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }

    fun edit(id: Long, value: String) = _state.update { state -> state.copy(dirty = true, savedNotice = false, entries = state.entries.map { if (it.id == id) it.copy(value = value, edited = true) else it }) }
    fun remove(id: Long) = _state.update { it.copy(dirty = true, entries = it.entries.filterNot { entry -> entry.id == id }) }
    fun add(definition: KpiDefinitionDto) = _state.update { state ->
        if (state.entries.any { it.kpiDefinitionId == definition.id }) state else state.copy(
            dirty = true,
            entries = state.entries + ReviewEntry(-definition.id, definition.id, definition.displayName, "", null, definition.unit, null, emptySet(), null, true),
        )
    }
    fun resolve(id: Long, resolution: String, definitionId: Long? = null) = _state.update { state -> state.copy(
        dirty = true,
        unknownLines = state.unknownLines.map { if (it.id == id) it.copy(resolution = resolution, resolvedKpiDefinitionId = definitionId) else it },
    ) }
    fun clearNotice() = _state.update { it.copy(savedNotice = false) }

    fun save(onSaved: (() -> Unit)? = null) = viewModelScope.launch {
        val current = _state.value
        val report = current.report ?: return@launch
        _state.update { it.copy(saving = true, error = null) }
        runCatching { reports.updateDraft(reportId, current.toDraftRequest(report)) }
            .onSuccess { updated -> _state.update { it.copy(report = updated, saving = false, dirty = false, savedNotice = true) }; onSaved?.invoke() }
            .onFailure { error -> _state.update { it.copy(saving = false, error = error.toUiError()) } }
    }

    fun confirm(onConfirmed: (Long) -> Unit) = viewModelScope.launch {
        val current = _state.value
        if (!current.canConfirm) return@launch
        _state.update { it.copy(confirming = true, error = null) }
        runCatching {
            if (current.dirty) reports.updateDraft(reportId, current.toDraftRequest(checkNotNull(current.report)))
            reports.confirm(reportId, ConfirmReportRequest(
                current.entries.map { ConfirmationEntryRequest(it.kpiDefinitionId, it.value.asEditableDecimal()) },
                current.unknownLines.map { UnknownLineResolutionRequest(it.id, it.resolution, it.resolvedKpiDefinitionId) },
            ))
        }.onSuccess { report -> _state.update { it.copy(confirming = false, dirty = false) }; onConfirmed(report.id) }
            .onFailure { error -> _state.update { it.copy(confirming = false, error = error.toUiError()) } }
    }
}

private fun ReportEntryDto.toReview() = ReviewEntry(
    id, checkNotNull(kpiDefinitionId), kpiDisplayName ?: kpiCode.orEmpty(),
    (currentValue ?: extractedValue)?.stripTrailingZeros()?.toPlainString().orEmpty(),
    extractedValue?.stripTrailingZeros()?.toPlainString(), capturedUnit,
    confidenceScore?.stripTrailingZeros()?.toPlainString(), warnings, sourceLine, editedByUser,
)

private fun ReviewUiState.toDraftRequest(report: ReportDto) = DraftReportRequest(
    report.effectiveDate, report.source, report.rawText,
    entries.map { entry -> DraftEntryRequest(
        entry.kpiDefinitionId, entry.displayName, entry.sourceLine, entry.extractedValue?.toBigDecimalOrNull(),
        entry.value.asEditableDecimal(), entry.confidenceScore?.toBigDecimalOrNull(), entry.edited,
        entry.unit, entry.warnings,
    ) },
    unknownLines.map { DraftUnknownLineRequest(it.sourceLine, it.resolution, it.resolvedKpiDefinitionId) },
)
