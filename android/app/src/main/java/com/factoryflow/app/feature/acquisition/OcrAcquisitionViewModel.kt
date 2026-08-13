package com.factoryflow.app.feature.acquisition

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.ReportsRepository
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

enum class OcrSource(val apiValue: String) { GALLERY("GALLERY_OCR"), CAMERA("CAMERA_OCR"), SHARE("SHARE_OCR") }

data class OcrAcquisitionState(
    val imageUri: Uri? = null,
    val extractedText: String = "",
    val processing: Boolean = false,
    val creatingDraft: Boolean = false,
    val error: UiError? = null,
    val noTextDetected: Boolean = false,
)

@HiltViewModel
class OcrAcquisitionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reports: ReportsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OcrAcquisitionState())
    val state = _state.asStateFlow()

    fun process(uri: Uri) {
        if (_state.value.processing && _state.value.imageUri == uri) return
        viewModelScope.launch {
            _state.value = OcrAcquisitionState(imageUri = uri, processing = true)
            runCatching { recognize(uri) }
                .onSuccess { text ->
                    _state.update { it.copy(processing = false, extractedText = text, noTextDetected = text.isBlank()) }
                }
                .onFailure { error ->
                    _state.update { it.copy(processing = false, error = error.toUiError()) }
                }
        }
    }

    fun editText(value: String) = _state.update { it.copy(extractedText = value, noTextDetected = false, error = null) }

    fun clear() { _state.value = OcrAcquisitionState() }

    fun analyze(source: OcrSource, onDraftCreated: (Long) -> Unit) {
        val raw = _state.value.extractedText.trim()
        if (raw.isBlank()) { _state.update { it.copy(noTextDetected = true) }; return }
        viewModelScope.launch {
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

    private suspend fun recognize(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromFilePath(context, uri)
        recognizer.process(image)
            .addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(result.text.trim())
                recognizer.close()
            }
            .addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
                recognizer.close()
            }
        continuation.invokeOnCancellation { recognizer.close() }
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
        )
    },
    unrecognizedLines = unrecognizedLines.map { DraftUnknownLineRequest(it.sourceLine) },
)
