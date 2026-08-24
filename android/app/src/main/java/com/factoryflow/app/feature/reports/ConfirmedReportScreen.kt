package com.factoryflow.app.feature.reports

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.TableView
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.util.toFrenchDate
import com.factoryflow.app.feature.acquisition.FocusedTopBar

@Composable
fun ConfirmedReportScreen(
    onBack: () -> Unit,
    onOpenReport: (Long) -> Unit,
    onReports: () -> Unit,
    viewModel: ConfirmedReportViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var noViewer by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    state.sharedFile?.let { file ->
        LaunchedEffect(file) {
            val document = state.generatedDocument ?: return@LaunchedEffect
            val type = if (document.format == "PDF") "application/pdf"
            else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            try {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
                context.startActivity(
                    Intent(Intent.ACTION_VIEW)
                        .setDataAndType(uri, type)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION),
                )
            } catch (_: ActivityNotFoundException) {
                noViewer = true
            } catch (_: IllegalArgumentException) {
                noViewer = true
            }
            viewModel.fileHandled()
        }
    }

    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.report_confirmed), onBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            else -> ConfirmedReportContent(
                state = state,
                onExport = viewModel::export,
                onOpenReport = onOpenReport,
                onReports = onReports,
                modifier = Modifier.padding(padding),
            )
        }
    }
    if (noViewer) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { noViewer = false },
            title = { Text(stringResource(R.string.no_viewer)) },
            confirmButton = {
                TextButton(onClick = { noViewer = false }) { Text(stringResource(R.string.close)) }
            },
        )
    }
}

@Composable
fun ConfirmedReportContent(
    state: ConfirmedReportUiState,
    onExport: (String) -> Unit,
    onOpenReport: (Long) -> Unit,
    onReports: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowContentSurface(modifier) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(FlowSpacing.xl)) {
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FlowIconTile(Icons.Outlined.CheckCircle, null, FlowGreen)
                        Spacer(Modifier.width(FlowSpacing.md))
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.report_confirmed), style = MaterialTheme.typography.titleLarge)
                            Text(state.report?.effectiveDate?.toFrenchDate().orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.report_confirmed_detail), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(FlowSpacing.xl))
                FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                    Column {
                        Text(stringResource(R.string.export_now), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.export_now_detail), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(FlowSpacing.lg))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                            ExportButton(
                                label = stringResource(R.string.pdf),
                                icon = Icons.Outlined.PictureAsPdf,
                                loading = state.exportingFormat == "PDF",
                                enabled = state.exportingFormat == null,
                                onClick = { onExport("PDF") },
                                modifier = Modifier.weight(1f),
                            )
                            ExportButton(
                                label = stringResource(R.string.excel),
                                icon = Icons.Outlined.TableView,
                                loading = state.exportingFormat == "EXCEL",
                                enabled = state.exportingFormat == null,
                                onClick = { onExport("EXCEL") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
                state.error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(it.detail), color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(FlowSpacing.xl))
                PrimaryAction(
                    stringResource(R.string.open_confirmed_report),
                    onClick = { state.report?.id?.let(onOpenReport) },
                    enabled = state.report != null,
                )
                TextButton(onClick = onReports, Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Description, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.back_to_reports))
                }
    }
    }
}

@Composable
private fun ExportButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowCard(
        modifier = modifier.height(96.dp),
        contentPadding = PaddingValues(FlowSpacing.md),
        onClick = if (enabled) onClick else null,
    ) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (loading) androidx.compose.material3.CircularProgressIndicator(Modifier.size(FlowSize.icon), strokeWidth = FlowSize.progressStroke)
            else FlowIconTile(icon, null, if (label == "PDF") FlowPink else FlowTeal, size = FlowSize.listIconTile)
            Spacer(Modifier.height(FlowSpacing.xs))
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
