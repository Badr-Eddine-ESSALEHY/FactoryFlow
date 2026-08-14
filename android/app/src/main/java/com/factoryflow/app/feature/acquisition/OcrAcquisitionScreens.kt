package com.factoryflow.app.feature.acquisition

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import java.io.File

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
            OcrResultContent(state, OcrSource.GALLERY, viewModel::editText, { viewModel.analyze(OcrSource.GALLERY, onReview) }, { picker.launch("image/*") }, Modifier.padding(padding))
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
        OcrResultContent(state, OcrSource.SHARE, viewModel::editText, { viewModel.analyze(OcrSource.SHARE, onReview) }, null, Modifier.padding(padding))
    }
}

@Composable
fun CameraOcrScreen(
    onBack: () -> Unit,
    onReview: (Long) -> Unit,
    viewModel: OcrAcquisitionViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    var permitted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permitted = it }
    LaunchedEffect(Unit) { if (!permitted) permission.launch(Manifest.permission.CAMERA) }
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.camera_ocr), onBack) }) { padding ->
        when {
            !permitted -> OcrPickerEmpty(
                Modifier.padding(padding), Icons.Outlined.NoPhotography, stringResource(R.string.camera_required),
                stringResource(R.string.camera_permission_needed), stringResource(R.string.authorize), { permission.launch(Manifest.permission.CAMERA) },
            )
            state.imageUri != null -> OcrResultContent(state, OcrSource.CAMERA, viewModel::editText, { viewModel.analyze(OcrSource.CAMERA, onReview) }, viewModel::clear, Modifier.padding(padding))
            else -> CameraCapture(viewModel::process, Modifier.padding(padding))
        }
    }
}

@Composable
private fun CameraCapture(onCaptured: (Uri) -> Unit, modifier: Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var capture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameraError by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { viewContext ->
                PreviewView(viewContext).also { previewView ->
                    previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                    val providerFuture = ProcessCameraProvider.getInstance(viewContext)
                    providerFuture.addListener({
                        runCatching {
                            val provider = providerFuture.get()
                            val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                            val imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
                            provider.unbindAll()
                            provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
                            capture = imageCapture
                        }.onFailure { cameraError = true }
                    }, ContextCompat.getMainExecutor(viewContext))
                }
            },
        )
        Box(
            Modifier.align(Alignment.Center).fillMaxWidth(.86f).aspectRatio(.78f)
                .background(Color.Transparent, RoundedCornerShape(24.dp)),
        )
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black.copy(alpha = .64f)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.camera_guidance), color = Color.White, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            FloatingActionButton(
                onClick = {
                    val imageCapture = capture ?: return@FloatingActionButton
                    val file = File.createTempFile("factoryflow-capture-", ".jpg", context.cacheDir)
                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture.takePicture(options, ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(result: ImageCapture.OutputFileResults) {
                            onCaptured(FileProvider.getUriForFile(context, "${context.packageName}.files", file))
                        }
                        override fun onError(exception: ImageCaptureException) { cameraError = true }
                    })
                },
                containerColor = FlowBlue,
                contentColor = Color.White,
            ) { Icon(Icons.Outlined.PhotoCamera, stringResource(R.string.capture)) }
            if (cameraError) Text(stringResource(R.string.ocr_failed), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
fun OcrResultContent(
    state: OcrAcquisitionState,
    source: OcrSource,
    onEditText: (String) -> Unit,
    onAnalyze: () -> Unit,
    onReplace: (() -> Unit)?,
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
            if (onReplace != null) TextButton(onClick = onReplace) { Text(if (source == OcrSource.CAMERA) stringResource(R.string.retake) else stringResource(R.string.change)) }
        }
        Spacer(Modifier.height(FlowSpacing.lg))
        FlowCard(Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(FlowSpacing.sm)) {
            TextField(
                value = state.extractedText,
                onValueChange = onEditText,
                modifier = Modifier.fillMaxSize(),
                label = { Text(stringResource(R.string.raw_text_label)) },
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
            Text(stringResource(error.detail), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
