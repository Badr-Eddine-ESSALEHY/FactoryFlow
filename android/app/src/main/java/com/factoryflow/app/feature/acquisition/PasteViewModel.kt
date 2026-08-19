package com.factoryflow.app.feature.acquisition

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.BuildConfig
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class PasteUiState(
    val text: String = "",
    val emptyError: Boolean = false,
    val analyzing: Boolean = false,
    val analysisFailed: Boolean = false,
    val error: UiError? = null,
)

@HiltViewModel
class PasteViewModel @Inject constructor(private val reports: ReportsRepository) : ViewModel() {
    private val _state = MutableStateFlow(PasteUiState())
    val state = _state.asStateFlow()
    private var analysisJob: Job? = null
    fun text(value: String) = _state.update { it.copy(text = value, emptyError = false, analysisFailed = false, error = null) }
    fun analyze(onDraftCreated: (Long) -> Unit) {
        if (analysisJob?.isActive == true) return
        if (_state.value.analyzing) return
        val raw = _state.value.text.trim()
        if (raw.isBlank()) { _state.update { it.copy(emptyError = true) }; return }
        analysisJob = viewModelScope.launch {
            _state.update { it.copy(analyzing = true, analysisFailed = false, error = null) }
            runCatching {
                val analysis = reports.analyze(raw)
                reports.createDraft(analysis.toDraft(LocalDate.now().toString())).also { draft ->
                    require(draft.id > 0) { "The backend returned an invalid draft identifier." }
                }
            }.onSuccess { draft ->
                _state.update { it.copy(analyzing = false, analysisFailed = false) }
                onDraftCreated(draft.id)
            }.onFailure { error ->
                _state.update { it.copy(analyzing = false, analysisFailed = true, error = error.toUiError()) }
                if (BuildConfig.DEBUG) {
                    runCatching { Log.e("FactoryFlowPaste", "Analyze/draft workflow failed", error) }
                }
            }
        }
    }
}

private fun AnalyzeReportResponse.toDraft(date: String) = DraftReportRequest(
    effectiveDate = date, source = source, rawText = rawText,
    entries = entries.map { entry -> DraftEntryRequest(
        kpiDefinitionId = entry.kpiDefinitionId, sourceLabel = entry.sourceLabel, sourceLine = entry.sourceLine,
        extractedValue = entry.extractedValue, currentValue = entry.extractedValue, confidenceScore = entry.confidenceScore,
        editedByUser = false, capturedUnit = entry.capturedUnit ?: entry.expectedUnit,
        warnings = entry.warnings.map { it.code }.toSet(),
        suggestedKpiDefinitionId = entry.suggestions.firstOrNull()?.kpiDefinitionId, suggestionScore = entry.suggestions.firstOrNull()?.score,
        secondaryExtractedValue = entry.secondaryExtractedValue,
        secondaryCurrentValue = entry.secondaryExtractedValue,
        secondaryUnit = entry.secondaryUnit,
    ) },
    unrecognizedLines = unrecognizedLines.map { DraftUnknownLineRequest(it.sourceLine) },
)
