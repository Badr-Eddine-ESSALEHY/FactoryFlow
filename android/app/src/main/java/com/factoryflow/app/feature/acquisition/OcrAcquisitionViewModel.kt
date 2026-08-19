package com.factoryflow.app.feature.acquisition

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.data.OcrRepository
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toOcrUiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class OcrSource(val apiValue: String) { GALLERY("GALLERY_OCR"), SHARE("SHARE_OCR") }

data class OcrAcquisitionState(
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val processing: Boolean = false,
    val creatingDraft: Boolean = false,
    val error: UiError? = null,
    val noTextDetected: Boolean = false,
    val confidence: java.math.BigDecimal? = null,
    val engine: String? = null,
    val warnings: List<String> = emptyList(),
)

@HiltViewModel
class OcrAcquisitionViewModel @Inject constructor(
    private val ocr: OcrRepository,
    private val reports: ReportsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OcrAcquisitionState())
    val state = _state.asStateFlow()
    private var recognitionJob: Job? = null
    private var draftJob: Job? = null

    fun process(uri: Uri) = startRecognition(uri, force = false)

    fun retry() {
        _state.value.imageUri?.let { startRecognition(it, force = true) }
    }

    private fun startRecognition(uri: Uri, force: Boolean) {
        val current = _state.value
        if (!force && current.imageUri == uri &&
            (current.processing || current.extractedText.isNotBlank() || current.noTextDetected || current.error != null)
        ) return
        recognitionJob?.cancel()
        recognitionJob = viewModelScope.launch {
            _state.value = OcrAcquisitionState(imageUri = uri, processing = true)
            runCatching { ocr.recognize(uri) }
                .onSuccess { result ->
                    _state.update { it.copy(
                        processing = false,
                        extractedText = result.fullText,
                        noTextDetected = result.fullText.isBlank(),
                        confidence = result.confidence,
                        engine = result.engine,
                        warnings = result.warnings,
                    ) }
                }
                .onFailure { error ->
                    _state.update { it.copy(processing = false, error = error.toOcrUiError()) }
                }
        }
    }

    fun editText(value: String) = _state.update { it.copy(extractedText = value, noTextDetected = false, error = null) }

    fun clear() { _state.value = OcrAcquisitionState() }

    fun analyze(source: OcrSource, onDraftCreated: (Long) -> Unit) {
        if (draftJob?.isActive == true) return
        if (_state.value.creatingDraft) return
        val raw = _state.value.extractedText.trim()
        if (raw.isBlank()) { _state.update { it.copy(noTextDetected = true) }; return }
        draftJob = viewModelScope.launch {
            _state.update { it.copy(creatingDraft = true, error = null) }
            runCatching {
                val analysis = reports.analyze(raw, source.apiValue)
                reports.createDraft(analysis.asDraft(LocalDate.now().toString()))
            }.onSuccess { draft ->
                _state.update { it.copy(creatingDraft = false) }
                onDraftCreated(draft.id)
            }.onFailure { error ->
                _state.update { it.copy(creatingDraft = false, error = error.toUiError()) }
            }
        }
    }

}

private fun AnalyzeReportResponse.asDraft(date: String) = DraftReportRequest(
    effectiveDate = date,
    source = source,
    rawText = rawText,
    entries = entries.map { entry ->
        DraftEntryRequest(
            kpiDefinitionId = entry.kpiDefinitionId,
            sourceLabel = entry.sourceLabel,
            sourceLine = entry.sourceLine,
            extractedValue = entry.extractedValue,
            currentValue = entry.extractedValue,
            confidenceScore = entry.confidenceScore,
            editedByUser = false,
            capturedUnit = entry.capturedUnit ?: entry.expectedUnit,
            warnings = entry.warnings.map { it.code }.toSet(),
            suggestedKpiDefinitionId = entry.suggestions.firstOrNull()?.kpiDefinitionId,
            suggestionScore = entry.suggestions.firstOrNull()?.score,
            secondaryExtractedValue = entry.secondaryExtractedValue,
            secondaryCurrentValue = entry.secondaryExtractedValue,
            secondaryUnit = entry.secondaryUnit,
        )
    },
    unrecognizedLines = unrecognizedLines.map { DraftUnknownLineRequest(it.sourceLine) },
)
