package com.factoryflow.app.feature.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.GeneratedReportsRepository
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.GenerateReportRequest
import com.factoryflow.app.core.network.dto.GeneratedReportDto
import com.factoryflow.app.core.network.dto.ReportDto
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { reports.report(reportId) }
            .onSuccess { report -> _state.update { it.copy(loading = false, report = report) } }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }

    fun export(format: String) = viewModelScope.launch {
        val report = _state.value.report?.takeIf { it.status == "CONFIRMED" } ?: return@launch
        if (_state.value.exportingFormat != null) return@launch
        _state.update { it.copy(exportingFormat = format, error = null, sharedFile = null) }
        runCatching {
            val generated = generatedReports.generate(
                GenerateReportRequest(
                    type = "DAILY",
                    format = format,
                    periodStart = report.effectiveDate,
                    periodEnd = report.effectiveDate,
                ),
            )
            generated to generatedReports.download(generated)
        }.onSuccess { (document, file) ->
            _state.update { it.copy(exportingFormat = null, generatedDocument = document, sharedFile = file) }
        }.onFailure { error ->
            _state.update { it.copy(exportingFormat = null, error = error.toUiError()) }
        }
    }

    fun fileHandled() = _state.update { it.copy(sharedFile = null) }
}
