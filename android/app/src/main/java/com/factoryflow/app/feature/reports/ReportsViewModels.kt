package com.factoryflow.app.feature.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.GeneratedReportsRepository
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toDocumentDownloadUiError
import com.factoryflow.app.core.util.toDocumentGenerationUiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ReportsUiState(val loading: Boolean = true, val reports: List<ReportSummaryDto> = emptyList(), val filter: String? = null, val error: UiError? = null)
@HiltViewModel
class ReportsViewModel @Inject constructor(private val repository: ReportsRepository) : ViewModel() {
    private val _state = MutableStateFlow(ReportsUiState()); val state = _state.asStateFlow()
    init { load() }
    fun filter(value: String?) { _state.update { it.copy(filter = value) }; load() }
    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { repository.reports(_state.value.filter) }.onSuccess { page -> _state.update { it.copy(loading = false, reports = page.content) } }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }
}

data class GeneratedListUiState(
    val loading: Boolean = true,
    val documents: List<GeneratedReportDto> = emptyList(),
    val generating: Boolean = false,
    val generationCompleted: Boolean = false,
    val error: UiError? = null,
)
@HiltViewModel
class GeneratedListViewModel @Inject constructor(private val repository: GeneratedReportsRepository) : ViewModel() {
    private val _state = MutableStateFlow(GeneratedListUiState()); val state = _state.asStateFlow()
    init { load() }
    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { repository.list() }.onSuccess { page -> _state.value = GeneratedListUiState(false, page.content) }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }

    fun generate(type: String, formats: Set<String>, period: ConsolidatedReportPeriod) {
        if (_state.value.generating || formats.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(generating = true, generationCompleted = false, error = null) }
            for (format in formats.sorted()) {
                runCatching {
                    repository.generateConsolidated(
                        GenerateReportRequest(type, format, period.start.toString(), period.end.toString()),
                    )
                }.onFailure { error ->
                    val refreshed = runCatching { repository.list().content }.getOrDefault(_state.value.documents)
                    _state.update {
                        it.copy(
                            loading = false,
                            documents = refreshed,
                            generating = false,
                            error = error.toDocumentGenerationUiError(),
                        )
                    }
                    return@launch
                }
            }
            val refreshed = runCatching { repository.list().content }.getOrDefault(_state.value.documents)
            _state.update {
                it.copy(
                    loading = false,
                    documents = refreshed,
                    generating = false,
                    generationCompleted = true,
                )
            }
        }
    }

    fun generationResultHandled() = _state.update { it.copy(generationCompleted = false) }
}

data class ConsolidatedReportPeriod(val start: LocalDate, val end: LocalDate)

fun resolveConsolidatedReportPeriod(
    type: String,
    selectedDate: LocalDate,
    customStart: LocalDate?,
    customEnd: LocalDate?,
): ConsolidatedReportPeriod? = when (type) {
    "DAILY" -> ConsolidatedReportPeriod(selectedDate, selectedDate)
    "WEEKLY" -> selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).let {
        ConsolidatedReportPeriod(it, it.plusDays(6))
    }
    "MONTHLY" -> selectedDate.withDayOfMonth(1).let {
        ConsolidatedReportPeriod(it, it.with(TemporalAdjusters.lastDayOfMonth()))
    }
    "CUSTOM" -> if (customStart != null && customEnd != null && !customStart.isAfter(customEnd)) {
        ConsolidatedReportPeriod(customStart, customEnd)
    } else null
    else -> null
}

data class ReportDetailUiState(
    val loading: Boolean = true,
    val report: ReportDto? = null,
    val deleting: Boolean = false,
    val deleted: Boolean = false,
    val error: UiError? = null,
)
@HiltViewModel
class ReportDetailViewModel @Inject constructor(savedState: SavedStateHandle, private val repository: ReportsRepository) : ViewModel() {
    private val id = checkNotNull(savedState.get<String>("reportId")?.toLongOrNull())
    private val _state = MutableStateFlow(ReportDetailUiState()); val state = _state.asStateFlow()
    init { load() }
    fun load() = viewModelScope.launch {
        _state.value = ReportDetailUiState()
        runCatching { repository.report(id) }.onSuccess { _state.value = ReportDetailUiState(loading = false, report = it) }
            .onFailure { _state.value = ReportDetailUiState(false, error = it.toUiError()) }
    }
    fun deleteDraft() = viewModelScope.launch {
        if (_state.value.report?.status != "DRAFT" || _state.value.deleting) return@launch
        _state.update { it.copy(deleting = true, error = null) }
        runCatching { repository.deleteDraft(id) }
            .onSuccess { _state.update { it.copy(deleting = false, deleted = true) } }
            .onFailure { error -> _state.update { it.copy(deleting = false, error = error.toUiError()) } }
    }
}

enum class GeneratedFileAction { OPEN, SAVE, SHARE, EMAIL }
data class GeneratedDetailUiState(
    val loading: Boolean = true,
    val document: GeneratedReportDto? = null,
    val downloading: Boolean = false,
    val file: File? = null,
    val fileAction: GeneratedFileAction? = null,
    val error: UiError? = null,
)
@HiltViewModel
class GeneratedDetailViewModel @Inject constructor(savedState: SavedStateHandle, private val repository: GeneratedReportsRepository) : ViewModel() {
    private val id = checkNotNull(savedState.get<String>("generatedId")?.toLongOrNull())
    private val _state = MutableStateFlow(GeneratedDetailUiState()); val state = _state.asStateFlow()
    private var downloadJob: Job? = null
    init { load() }
    fun load() = viewModelScope.launch {
        _state.value = GeneratedDetailUiState()
        runCatching { repository.detail(id) }.onSuccess { _state.value = GeneratedDetailUiState(loading = false, document = it) }
            .onFailure { _state.value = GeneratedDetailUiState(loading = false, error = it.toUiError()) }
    }
    fun download(action: GeneratedFileAction = GeneratedFileAction.OPEN) {
        if (downloadJob?.isActive == true) return
        downloadJob = viewModelScope.launch {
        if (_state.value.downloading) return@launch
        val document = _state.value.document ?: return@launch
        _state.update { it.copy(downloading = true, error = null, file = null, fileAction = action) }
        runCatching { repository.download(document) }.onSuccess { file -> _state.update { it.copy(downloading = false, file = file) } }
            .onFailure { error -> _state.update { it.copy(downloading = false, error = error.toDocumentDownloadUiError()) } }
        }
    }
    fun fileHandled() = _state.update { it.copy(file = null, fileAction = null) }
}
