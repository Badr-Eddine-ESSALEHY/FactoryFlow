package com.factoryflow.app.feature.reports

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.*
import com.factoryflow.app.feature.acquisition.FocusedTopBar

@Composable
fun ReportsScreen(onReport: (Long) -> Unit, onResumeDraft: (Long) -> Unit, onGenerated: (Long) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text(stringResource(R.string.reports_title), style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))
            PrimaryTabRow(tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.maintenance_reports)) })
                Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.generated_documents)) })
            }
        }
        if (tab == 0) MaintenanceReports(onReport, onResumeDraft) else GeneratedReports(onGenerated)
    }
}

@Composable
private fun MaintenanceReports(onReport: (Long) -> Unit, onResumeDraft: (Long) -> Unit, viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null to R.string.all, "DRAFT" to R.string.draft, "PENDING_REVIEW" to R.string.pending_review, "CONFIRMED" to R.string.confirmed).forEach { (value, label) ->
                FilterChip(state.filter == value, { viewModel.filter(value) }, { Text(stringResource(label)) })
            }
        }
        when {
            state.loading -> SkeletonRows(Modifier.padding(20.dp), 5)
            state.reports.isEmpty() -> EmptyPane(stringResource(R.string.no_reports), stringResource(R.string.no_dashboard_data_detail))
            else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 14.dp, 20.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.reports, key = { it.id }) { report -> ReportRow(report) { if (report.status == "CONFIRMED") onReport(report.id) else onResumeDraft(report.id) } }
            }
        }
    }
}

@Composable
private fun ReportRow(report: ReportSummaryDto, onClick: () -> Unit) = FactoryCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(13.dp)) { Icon(Icons.Outlined.Description, null, Modifier.padding(11.dp), tint = MaterialTheme.colorScheme.primary) }
        Spacer(Modifier.width(13.dp)); Column(Modifier.weight(1f)) {
            Text(report.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.kpi_count, report.kpiCount), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.submitted_by, report.submittedBy.name), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
        Column(horizontalAlignment = Alignment.End) {
            StatusPill(reportStatusLabel(report.status), reportStatusColor(report.status))
            if (report.warningCount > 0) { Spacer(Modifier.height(7.dp)); Text(stringResource(R.string.warning_count, report.warningCount), color = Warning, style = MaterialTheme.typography.labelMedium) }
        }
    }
}

@Composable
private fun GeneratedReports(onGenerated: (Long) -> Unit, viewModel: GeneratedListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> SkeletonRows(Modifier.padding(20.dp), 5)
        state.documents.isEmpty() -> EmptyPane(stringResource(R.string.no_documents), stringResource(R.string.no_dashboard_data_detail))
        else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.documents, key = { it.id }) { doc -> DocumentRow(doc) { onGenerated(doc.id) } }
        }
    }
}

@Composable private fun DocumentRow(document: GeneratedReportDto, onClick: () -> Unit) = FactoryCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(if (document.format == "PDF") Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) {
            Text(generatedTypeLabel(document.type), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.document_period, document.periodStart.toFrenchDate(), document.periodEnd.toFrenchDate()), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.generated_on, document.generatedAt.toFrenchInstant()), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
        }
        StatusPill(document.format, MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ReportDetailScreen(onBack: () -> Unit, viewModel: ReportDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.report_detail), onBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.report == null -> ErrorPane(stringResource(state.error?.title ?: R.string.server_error), stringResource(state.error?.detail ?: R.string.server_error), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> ReportDetail(state.report!!, Modifier.padding(padding))
        }
    }
}

@Composable private fun ReportDetail(report: ReportDto, modifier: Modifier) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(report.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.report_number, report.id), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                StatusPill(reportStatusLabel(report.status), reportStatusColor(report.status))
            }
        }
        item { Text(stringResource(if (report.status == "CONFIRMED") R.string.authoritative_values else R.string.review_values), style = MaterialTheme.typography.titleLarge) }
        items(report.entries, key = { it.id }) { entry ->
            FactoryCard(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) { Text(entry.kpiDisplayName ?: entry.kpiCode.orEmpty(), style = MaterialTheme.typography.titleMedium); entry.sourceLine?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 2) } }
                Text((if (report.status == "CONFIRMED") entry.finalValue else entry.currentValue).displayValue(), style = MaterialTheme.typography.headlineSmall)
                entry.capturedUnit?.let { Spacer(Modifier.width(5.dp)); Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } }
        }
        if (report.unrecognizedLines.isNotEmpty()) item { Text(stringResource(R.string.unknown_content), style = MaterialTheme.typography.titleLarge) }
        items(report.unrecognizedLines, key = { it.id }) { line -> FactoryCard(Modifier.fillMaxWidth()) { Row { Text(line.sourceLine, Modifier.weight(1f)); StatusPill(unknownResolutionLabel(line.resolution), MaterialTheme.colorScheme.onSurfaceVariant) } } }
        report.rawText?.takeIf { it.isNotBlank() }?.let { raw -> item { SectionHeader(stringResource(R.string.raw_source)); FactoryCard(Modifier.fillMaxWidth()) { Text(raw, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    }
}

@Composable
fun GeneratedDetailScreen(onBack: () -> Unit, viewModel: GeneratedDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var noViewer by remember { mutableStateOf(false) }
    state.file?.let { file -> LaunchedEffect(file) {
        val document = state.document ?: return@LaunchedEffect
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, if (document.format == "PDF") "application/pdf" else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try { context.startActivity(intent) } catch (_: ActivityNotFoundException) { noViewer = true }
        viewModel.fileHandled()
    } }
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.generated_documents), onBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.document == null -> ErrorPane(stringResource(state.error?.title ?: R.string.server_error), stringResource(state.error?.detail ?: R.string.server_error), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                val doc = state.document!!
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (doc.format == "PDF") Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)); Spacer(Modifier.width(14.dp)); Column { Text(doc.fileName, style = MaterialTheme.typography.headlineSmall); StatusPill(doc.format, MaterialTheme.colorScheme.primary) } }
                Spacer(Modifier.height(24.dp)); FactoryCard(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Metadata(stringResource(R.string.document_period, doc.periodStart.toFrenchDate(), doc.periodEnd.toFrenchDate()), generatedTypeLabel(doc.type))
                    Metadata(stringResource(R.string.generated_on, doc.generatedAt.toFrenchInstant()), generationStatusLabel(doc.generationStatus))
                    Metadata(stringResource(R.string.email_delivery), emailStatusLabel(doc.emailDeliveryStatus))
                } }
                Spacer(Modifier.weight(1f)); state.error?.let { Text(stringResource(it.detail), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                PrimaryAction(stringResource(if (state.downloading) R.string.downloading else R.string.download_open), state.downloading, enabled = doc.generationStatus == "READY", onClick = viewModel::download)
            }
        }
    }
    if (noViewer) AlertDialog(onDismissRequest = { noViewer = false }, title = { Text(stringResource(R.string.no_viewer)) }, confirmButton = { TextButton(onClick = { noViewer = false }) { Text(stringResource(R.string.close)) } })
}

@Composable private fun Metadata(label: String, value: String) { Row(Modifier.fillMaxWidth()) { Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.labelLarge) } }
@Composable private fun reportStatusColor(status: String) = when (status) { "CONFIRMED" -> Success; "DRAFT" -> MaterialTheme.colorScheme.onSurfaceVariant; else -> Warning }
@Composable private fun reportStatusLabel(status: String) = stringResource(when (status) { "CONFIRMED" -> R.string.confirmed; "DRAFT" -> R.string.draft; else -> R.string.pending_review })
@Composable private fun unknownResolutionLabel(value: String) = stringResource(when (value) { "IGNORED" -> R.string.ignore_line; "ASSIGNED" -> R.string.assign_kpi; else -> R.string.keep_unresolved })
@Composable private fun generatedTypeLabel(value: String) = stringResource(when (value) { "DAILY" -> R.string.daily; "WEEKLY" -> R.string.weekly; "MONTHLY" -> R.string.monthly; else -> R.string.manual })
@Composable private fun generationStatusLabel(value: String) = stringResource(if (value == "READY") R.string.status_ready else R.string.status_failed)
@Composable private fun emailStatusLabel(value: String) = stringResource(when (value) { "DELIVERED" -> R.string.email_delivered; "FAILED" -> R.string.email_failed; else -> R.string.not_requested })
