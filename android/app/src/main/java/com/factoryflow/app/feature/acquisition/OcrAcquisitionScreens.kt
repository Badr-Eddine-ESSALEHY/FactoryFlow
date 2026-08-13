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
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.gallery_ocr), onBack) }) { padding ->
        if (state.imageUri == null) {
            OcrPickerEmpty(
                modifier = Modifier.padding(padding),
                icon = Icons.Outlined.ImageSearch,
                title = "Importer un relevé",
                detail = "Choisissez une capture WhatsApp ou une photo lisible. Le texte reste traité sur cet appareil.",
                action = stringResource(R.string.select_image),
                onAction = { picker.launch("image/*") },
            )
        } else {
            OcrResultContent(state, OcrSource.GALLERY, viewModel, onReview, { picker.launch("image/*") }, Modifier.padding(padding))
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
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.shared_content), onBack) }) { padding ->
        OcrResultContent(state, OcrSource.SHARE, viewModel, onReview, null, Modifier.padding(padding))
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
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.camera_ocr), onBack) }) { padding ->
        when {
            !permitted -> OcrPickerEmpty(
                Modifier.padding(padding), Icons.Outlined.NoPhotography, "Appareil photo requis",
                stringResource(R.string.camera_permission_needed), "Autoriser", { permission.launch(Manifest.permission.CAMERA) },
            )
            state.imageUri != null -> OcrResultContent(state, OcrSource.CAMERA, viewModel, onReview, viewModel::clear, Modifier.padding(padding))
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
            Text("Cadrez le message complet et évitez les reflets", color = Color.White, style = MaterialTheme.typography.bodyMedium)
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
                containerColor = FactoryFlowMagenta,
                contentColor = Color.White,
            ) { Icon(Icons.Outlined.PhotoCamera, stringResource(R.string.capture)) }
            if (cameraError) Text(stringResource(R.string.ocr_failed), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun OcrResultContent(
    state: OcrAcquisitionState,
    source: OcrSource,
    viewModel: OcrAcquisitionViewModel,
    onReview: (Long) -> Unit,
    onReplace: (() -> Unit)?,
    modifier: Modifier,
) {
    Column(modifier.fillMaxSize().imePadding().padding(horizontal = 20.dp, vertical = 16.dp)) {
        if (state.processing) {
            FactoryFlowHero(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(Modifier.width(13.dp))
                    Column { Text(stringResource(R.string.ocr_processing), color = Color.White, style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.ocr_extracting), color = Color.White.copy(alpha = .76f), style = MaterialTheme.typography.bodySmall) }
                }
            }
            Spacer(Modifier.height(18.dp))
            SkeletonRows(Modifier.fillMaxWidth(), 4)
            return@Column
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FactoryIconChip(Icons.Outlined.DocumentScanner, null, tint = FactoryFlowGreenDark, container = FactoryFlowGreen.copy(alpha = .13f))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Texte détecté", style = MaterialTheme.typography.titleLarge)
                Text("Relisez si nécessaire avant l’analyse", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            if (onReplace != null) TextButton(onClick = onReplace) { Text(if (source == OcrSource.CAMERA) stringResource(R.string.retake) else "Changer") }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = state.extractedText,
            onValueChange = viewModel::editText,
            modifier = Modifier.fillMaxWidth().weight(1f),
            label = { Text(stringResource(R.string.raw_text_label)) },
            shape = RoundedCornerShape(FactoryRadius.card),
            isError = state.noTextDetected,
            supportingText = { if (state.noTextDetected) Text(stringResource(R.string.ocr_empty)) },
        )
        state.error?.let { error ->
            Spacer(Modifier.height(10.dp))
            Text(stringResource(error.detail), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(14.dp))
        PrimaryAction("Analyser et vérifier", state.creatingDraft, state.extractedText.isNotBlank(), { viewModel.analyze(source, onReview) })
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
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            FactoryIconChip(icon, null, size = 72.dp, tint = FactoryFlowMagenta, container = FactoryFlowMagenta.copy(alpha = .11f))
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            PrimaryAction(action, onClick = onAction)
        }
    }
}
