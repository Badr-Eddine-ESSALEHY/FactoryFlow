package com.factoryflow.app.feature.acquisition

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*

@Composable
fun GalleryOcrScreen(
    onBack: () -> Unit,
    onReview: (Long) -> Unit,
    viewModel: OcrAcquisitionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(viewModel::process) }
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.gallery_ocr), onBack) }) { padding ->
        if (state.imageUri == null) {
            OcrPickerEmpty(
                modifier = Modifier.padding(padding),
                icon = Icons.Outlined.ImageSearch,
                title = stringResource(R.string.import_reading),
                detail = stringResource(R.string.import_reading_detail),
                action = stringResource(R.string.select_image),
                onAction = { picker.launch("image/*") },
            )
        } else {
            OcrResultContent(state, OcrSource.GALLERY, viewModel::editText, { viewModel.analyze(OcrSource.GALLERY, onReview) }, { picker.launch("image/*") }, viewModel::retry, Modifier.padding(padding))
        }
    }
}

@Composable
fun SharedImageOcrScreen(
    uri: Uri,
    onBack: () -> Unit,
    onReview: (Long) -> Unit,
    viewModel: OcrAcquisitionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(uri) { viewModel.process(uri) }
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.shared_content), onBack) }) { padding ->
        OcrResultContent(state, OcrSource.SHARE, viewModel::editText, { viewModel.analyze(OcrSource.SHARE, onReview) }, null, viewModel::retry, Modifier.padding(padding))
    }
}

@Composable
fun OcrResultContent(
    state: OcrAcquisitionState,
    source: OcrSource,
    onEditText: (String) -> Unit,
    onAnalyze: () -> Unit,
    onReplace: (() -> Unit)?,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    FlowContentSurface(modifier) {
    Column(Modifier.fillMaxSize().imePadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg)) {
        if (state.processing) {
            FlowCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(FlowSpacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = FlowBlue, strokeWidth = 2.dp)
                    Spacer(Modifier.width(FlowSpacing.md))
                    Column { Text(stringResource(R.string.ocr_processing), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.ocr_extracting), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                }
            }
            Spacer(Modifier.height(FlowSpacing.lg))
            SkeletonRows(Modifier.fillMaxWidth(), 4)
            return@Column
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FlowIconTile(Icons.Outlined.DocumentScanner, null, FlowTeal)
            Spacer(Modifier.width(FlowSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.detected_text), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.review_before_analysis), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            if (onReplace != null) TextButton(onClick = onReplace) { Text(stringResource(R.string.change)) }
        }
        if (state.engine != null) {
            Spacer(Modifier.height(FlowSpacing.sm))
            FlowStatusPill(
                label = stringResource(R.string.ocr_confidence, state.confidence?.multiply(java.math.BigDecimal(100))?.toInt() ?: 0),
                color = if ("LOW_CONFIDENCE" in state.warnings) FlowOrange else FlowTeal,
                compact = true,
            )
        }
        Spacer(Modifier.height(FlowSpacing.lg))
        FlowCard(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(FlowSpacing.sm)) {
            TextField(
                value = state.extractedText,
                onValueChange = onEditText,
                modifier = Modifier.fillMaxSize(),
                label = { Text(stringResource(R.string.ocr_extracted_text_label)) },
                shape = RoundedCornerShape(FlowRadius.control),
                isError = state.noTextDetected,
                supportingText = { if (state.noTextDetected) Text(stringResource(R.string.ocr_empty)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
            )
        }
        state.error?.let { error ->
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(error.detail), Modifier.weight(1f), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
        }
        Spacer(Modifier.height(14.dp))
        PrimaryAction(stringResource(R.string.analyze_and_review), state.creatingDraft, state.extractedText.isNotBlank(), onAnalyze)
    }
    }
}

@Composable
private fun OcrPickerEmpty(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    action: String,
    onAction: () -> Unit,
) {
    Box(modifier.fillMaxSize().padding(FlowSpacing.xl), contentAlignment = Alignment.Center) {
        FlowEmptyState(title, detail, icon = icon, action = action, onAction = onAction)
    }
}
