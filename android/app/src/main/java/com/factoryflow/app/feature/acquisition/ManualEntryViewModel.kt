package com.factoryflow.app.feature.acquisition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.asEditableDecimal
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ManualEntryRow(val definition: KpiDefinitionDto, val value: String = "", val explicitlyMissing: Boolean = false)
data class ManualEntryUiState(
    val loading: Boolean = true, val definitions: List<KpiDefinitionDto> = emptyList(), val entries: List<ManualEntryRow> = emptyList(),
    val effectiveDate: String = LocalDate.now().toString(), val query: String = "", val submitting: Boolean = false,
    val selectionError: Boolean = false, val invalidEntryIds: Set<Long> = emptySet(), val error: UiError? = null,
)

@HiltViewModel
class ManualEntryViewModel @Inject constructor(private val reports: ReportsRepository) : ViewModel() {
    private val _state = MutableStateFlow(ManualEntryUiState())
    val state = _state.asStateFlow()
    private var submissionJob: Job? = null
    init { load() }
    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { reports.definitions() }.onSuccess { list -> _state.update { it.copy(loading = false, definitions = list) } }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }
    fun query(value: String) = _state.update { it.copy(query = value) }
    fun date(value: String) = _state.update { it.copy(effectiveDate = value) }
    fun add(definition: KpiDefinitionDto) = _state.update { state ->
        if (state.entries.any { it.definition.id == definition.id }) state else state.copy(entries = state.entries + ManualEntryRow(definition), selectionError = false, query = "")
    }
    fun remove(id: Long) = _state.update { it.copy(entries = it.entries.filterNot { row -> row.definition.id == id }) }
    fun value(id: Long, value: String) = _state.update { state -> state.copy(
        entries = state.entries.map { if (it.definition.id == id) it.copy(value = value, explicitlyMissing = false) else it },
        invalidEntryIds = state.invalidEntryIds - id,
    ) }
    fun missing(id: Long, missing: Boolean) = _state.update { state -> state.copy(
        entries = state.entries.map { if (it.definition.id == id) it.copy(value = if (missing) "" else it.value, explicitlyMissing = missing) else it },
        invalidEntryIds = state.invalidEntryIds - id,
    ) }
    fun submit(onDraftCreated: (Long) -> Unit) {
        if (submissionJob?.isActive == true) return
        val current = _state.value
        if (current.submitting) return
        if (current.entries.isEmpty()) { _state.update { it.copy(selectionError = true) }; return }
        val invalid = current.entries.filter { !it.explicitlyMissing && it.value.asEditableDecimal() == null }
            .mapTo(linkedSetOf()) { it.definition.id }
        if (invalid.isNotEmpty()) { _state.update { it.copy(invalidEntryIds = invalid) }; return }
        submissionJob = viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            val request = DraftReportRequest(
                current.effectiveDate, "MANUAL", null,
                current.entries.map { row ->
                    val numeric = if (row.explicitlyMissing) null else row.value.asEditableDecimal()
                    val outside = numeric != null && ((row.definition.plausibleMin != null && numeric < row.definition.plausibleMin) || (row.definition.plausibleMax != null && numeric > row.definition.plausibleMax))
                    DraftEntryRequest(row.definition.id, row.definition.displayName, null, numeric, numeric, null, true, row.definition.unit, if (outside) setOf("OUTSIDE_PLAUSIBLE_RANGE") else emptySet())
                }, emptyList(),
            )
            runCatching { reports.createDraft(request) }.onSuccess { draft -> _state.update { it.copy(submitting = false) }; onDraftCreated(draft.id) }
                .onFailure { error -> _state.update { it.copy(submitting = false, error = error.toUiError()) } }
        }
    }
}
