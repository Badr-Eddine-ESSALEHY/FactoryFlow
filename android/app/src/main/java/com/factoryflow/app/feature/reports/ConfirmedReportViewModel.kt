package com.factoryflow.app.feature.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.GeneratedReportsRepository
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.IndividualReportExportRequest
import com.factoryflow.app.core.network.dto.GeneratedReportDto
import com.factoryflow.app.core.network.dto.ReportDto
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toDocumentDownloadUiError
import com.factoryflow.app.core.util.toDocumentGenerationUiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ConfirmedReportUiState(
    val loading: Boolean = true,
    val report: ReportDto? = null,
    val exportingFormat: String? = null,
    val generatedDocument: GeneratedReportDto? = null,
    val sharedFile: File? = null,
    val error: UiError? = null,
)

@HiltViewModel
class ConfirmedReportViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val reports: ReportsRepository,
    private val generatedReports: GeneratedReportsRepository,
) : ViewModel() {
    private val reportId = checkNotNull(savedState.get<String>("reportId")?.toLongOrNull())
    private val _state = MutableStateFlow(ConfirmedReportUiState())
    val state = _state.asStateFlow()
    private var exportJob: Job? = null

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { reports.report(reportId) }
            .onSuccess { report -> _state.update { it.copy(loading = false, report = report) } }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }

    fun export(format: String) {
        if (exportJob?.isActive == true) return
        exportJob = viewModelScope.launch {
        val report = _state.value.report?.takeIf { it.status == "CONFIRMED" } ?: return@launch
        if (format !in setOf("PDF", "EXCEL") || _state.value.exportingFormat != null) return@launch
        _state.update { it.copy(exportingFormat = format, error = null, sharedFile = null) }
        val generated = runCatching {
            generatedReports.generateIndividual(
                IndividualReportExportRequest(
                    reportId = report.id,
                    format = format,
                ),
            )
        }.getOrElse { error ->
            _state.update { it.copy(exportingFormat = null, error = error.toDocumentGenerationUiError()) }
            return@launch
        }
        if (generated.generationStatus != "READY") {
            _state.update { it.copy(exportingFormat = null, generatedDocument = generated, error = UiError(com.factoryflow.app.R.string.invalid_data, com.factoryflow.app.R.string.document_generation_failed)) }
            return@launch
        }
        _state.update { it.copy(generatedDocument = generated) }
        runCatching { generatedReports.download(generated) }
            .onSuccess { file ->
                _state.update { it.copy(exportingFormat = null, sharedFile = file) }
            }
            .onFailure { error ->
                _state.update { it.copy(exportingFormat = null, error = error.toDocumentDownloadUiError()) }
            }
        }
    }

    fun fileHandled() = _state.update { it.copy(sharedFile = null) }
}
