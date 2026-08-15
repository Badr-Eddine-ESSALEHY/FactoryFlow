package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*

@Composable
fun PasteScreen(onBack: () -> Unit, onReview: (Long) -> Unit, initialText: String? = null, viewModel: PasteViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(initialText) { if (state.text.isBlank() && !initialText.isNullOrBlank()) viewModel.text(initialText) }
    val clipboard = LocalClipboardManager.current
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.paste_title), onBack) }) { padding ->
        PasteContent(
            state = state,
            onTextChanged = viewModel::text,
            onPasteClipboard = { clipboard.getText()?.text?.let(viewModel::text) },
            onAnalyze = { viewModel.analyze(onReview) },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
fun PasteContent(
    state: PasteUiState,
    onTextChanged: (String) -> Unit,
    onPasteClipboard: () -> Unit,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowContentSurface(modifier) {
        Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(FlowSpacing.xl)) {
            Text(stringResource(R.string.paste_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(FlowSpacing.lg))
            FlowCard(Modifier.fillMaxWidth(), contentPadding = PaddingValues(FlowSpacing.sm)) {
                TextField(
                    value = state.text, onValueChange = onTextChanged, modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
                    label = { Text(stringResource(R.string.raw_text_label)) }, isError = state.emptyError,
                    supportingText = { if (state.emptyError) Text(stringResource(R.string.raw_text_required)) }, shape = RoundedCornerShape(FlowRadius.control),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    ),
                )
            }
            TextButton(onClick = onPasteClipboard) { Icon(Icons.Outlined.ContentPaste, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.paste_clipboard)) }
            if (state.analysisFailed) {
                Surface(Modifier.fillMaxWidth().padding(vertical = 8.dp), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.Top) {
                        Icon(Icons.Outlined.ErrorOutline, null)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.paste_analysis_failed), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onAnalyze, enabled = !state.analyzing) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAction(stringResource(if (state.analyzing) R.string.analyzing else if (state.analysisFailed) R.string.retry else R.string.analyze), state.analyzing, onClick = onAnalyze)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusedTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlowPageHeader(title = title, onBack = onBack, modifier = Modifier.weight(1f))
            actions()
        }
    }
}
