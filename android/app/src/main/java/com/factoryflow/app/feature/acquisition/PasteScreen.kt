package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
fun PasteScreen(onBack: () -> Unit, onReview: (Long) -> Unit, viewModel: PasteViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.paste_title), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(stringResource(R.string.paste_description), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = state.text, onValueChange = viewModel::text, modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
                label = { Text(stringResource(R.string.raw_text_label)) }, isError = state.emptyError,
                supportingText = { if (state.emptyError) Text(stringResource(R.string.raw_text_required)) }, shape = RoundedCornerShape(16.dp),
            )
            TextButton(onClick = { clipboard.getText()?.text?.let(viewModel::text) }) { Icon(Icons.Outlined.ContentPaste, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.paste_clipboard)) }
            state.error?.let { error ->
                Surface(Modifier.fillMaxWidth().padding(vertical = 8.dp), color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(14.dp)) { Icon(Icons.Outlined.ErrorOutline, null); Spacer(Modifier.width(10.dp)); Text(stringResource(error.detail)) }
                }
            }
            Spacer(Modifier.height(20.dp))
            PrimaryAction(stringResource(if (state.analyzing) R.string.analyzing else R.string.analyze), state.analyzing, onClick = { viewModel.analyze(onReview) })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusedTopBar(title: String, onBack: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { Text(title, style = MaterialTheme.typography.titleLarge) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) } }, actions = actions)
}
