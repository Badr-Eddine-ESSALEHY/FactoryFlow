package com.factoryflow.app.feature.reports

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Color
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReportsScreen(onReport: (Long) -> Unit, onGenerated: (Long) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    ReportsContent(tab, { tab = it }) {
        if (tab == 0) MaintenanceReports(onReport) else GeneratedReports(onGenerated)
    }
}

@Composable
fun ReportsContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    FlowScreen(modifier) {
        Column(Modifier.padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg)) {
            FlowPageHeader(title = stringResource(R.string.reports_title))
            Spacer(Modifier.height(FlowSpacing.lg))
            FlowSegmentedControl(
                options = listOf(
                    stringResource(R.string.maintenance_reports),
                    stringResource(R.string.generated_documents),
                ),
                selectedIndex = selectedTab,
                onSelected = onTabSelected,
            )
        }
        Box(Modifier.weight(1f)) { content() }
    }
}

@Composable
private fun MaintenanceReports(onReport: (Long) -> Unit, viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.horizontalScroll(androidx.compose.foundation.rememberScrollState()).padding(horizontal = FlowSpacing.xl), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
            listOf(null to R.string.all, "DRAFT" to R.string.draft, "PENDING_REVIEW" to R.string.pending_review, "CONFIRMED" to R.string.confirmed).forEach { (value, label) ->
                FilterChip(state.filter == value, { viewModel.filter(value) }, { Text(stringResource(label)) })
            }
        }
        when {
            state.loading -> SkeletonRows(Modifier.padding(FlowSpacing.xl), 5)
            state.reports.isEmpty() -> EmptyPane(
                stringResource(R.string.no_reports),
                stringResource(R.string.no_dashboard_data_detail),
                Modifier.padding(FlowSpacing.xl),
            )
            else -> ReportHistoryContent(state.reports, onReport)
        }
    }
}

@Composable
fun ReportHistoryContent(reports: List<ReportSummaryDto>, onReport: (Long) -> Unit, modifier: Modifier = Modifier) {
    FlowContentSurface(modifier) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, 110.dp), verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
            items(reports, key = { it.id }) { report -> ReportRow(report) { onReport(report.id) } }
        }
    }
}

@Composable
private fun ReportRow(report: ReportSummaryDto, onClick: () -> Unit) {
    val accent = reportStatusColor(report.status)
    FlowListRow(
        icon = Icons.Outlined.Description,
        title = report.effectiveDate.toFrenchDate(),
        meta = stringResource(R.string.kpi_count, report.kpiCount) + " · " + stringResource(R.string.submitted_by, report.submittedBy.name),
        accent = accent,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        trailing = {
        Column(horizontalAlignment = Alignment.End) {
                FlowStatusPill(reportStatusLabel(report.status), accent, compact = true)
                if (report.warningCount > 0) {
                    Spacer(Modifier.height(FlowSpacing.micro))
                    Text(stringResource(R.string.warning_count, report.warningCount), color = Warning, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
private fun GeneratedReports(onGenerated: (Long) -> Unit, viewModel: GeneratedListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showGenerator by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.generationCompleted) {
        if (state.generationCompleted) {
            showGenerator = false
            viewModel.generationResultHandled()
        }
    }
    Column(Modifier.fillMaxSize()) {
        FlowCard(
            modifier = Modifier.fillMaxWidth().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.sm),
            contentPadding = PaddingValues(FlowSpacing.lg),
            onClick = { showGenerator = true },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlowIconTile(Icons.Outlined.PostAdd, stringResource(R.string.generate_consolidated), FlowTeal)
                Spacer(Modifier.width(FlowSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.generate_consolidated), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.generate_consolidated_detail),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        state.error?.let { error ->
            Text(
                stringResource(error.detail),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.xs),
            )
        }
        Box(Modifier.weight(1f)) {
            when {
                state.loading -> SkeletonRows(Modifier.padding(FlowSpacing.xl), 5)
                state.documents.isEmpty() -> EmptyPane(
                    stringResource(R.string.no_documents),
                    stringResource(R.string.no_dashboard_data_detail),
                    Modifier.padding(FlowSpacing.xl),
                )
                else -> GeneratedDocumentsContent(state.documents, onGenerated)
            }
        }
    }
    if (showGenerator) {
        ConsolidatedReportSheet(
            generating = state.generating,
            onDismiss = { if (!state.generating) showGenerator = false },
            onGenerate = viewModel::generate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ConsolidatedReportSheet(
    generating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (String, Set<String>, ConsolidatedReportPeriod) -> Unit,
) {
    val businessToday = remember { LocalDate.now(ZoneId.of("Africa/Casablanca")) }
    var type by rememberSaveable { mutableStateOf("DAILY") }
    var selectedDateText by rememberSaveable { mutableStateOf(businessToday.toString()) }
    var customStartText by rememberSaveable { mutableStateOf("") }
    var customEndText by rememberSaveable { mutableStateOf("") }
    var formats by rememberSaveable { mutableStateOf(setOf("PDF", "EXCEL")) }
    var datePickerVisible by remember { mutableStateOf(false) }
    var rangePickerVisible by remember { mutableStateOf(false) }

    val selectedDate = runCatching { LocalDate.parse(selectedDateText) }.getOrDefault(businessToday)
    val customStart = customStartText.takeIf(String::isNotBlank)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val customEnd = customEndText.takeIf(String::isNotBlank)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val period = resolveConsolidatedReportPeriod(type, selectedDate, customStart, customEnd)
    val typeOptions = listOf(
        "DAILY" to stringResource(R.string.daily),
        "WEEKLY" to stringResource(R.string.weekly),
        "MONTHLY" to stringResource(R.string.monthly),
        "CUSTOM" to stringResource(R.string.custom_period),
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = FlowSpacing.xl)
                .padding(bottom = FlowSpacing.xl),
        ) {
            Text(stringResource(R.string.generate_consolidated), style = MaterialTheme.typography.headlineSmall)
            Text(
                stringResource(R.string.generate_consolidated_detail),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(FlowSpacing.lg))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                typeOptions.forEach { (value, label) ->
                    FilterChip(selected = type == value, onClick = { type = value }, label = { Text(label) })
                }
            }
            Spacer(Modifier.height(FlowSpacing.md))
            FlowListRow(
                icon = Icons.Outlined.DateRange,
                title = stringResource(
                    when (type) {
                        "WEEKLY" -> R.string.select_week
                        "MONTHLY" -> R.string.select_month
                        "CUSTOM" -> R.string.select_period
                        else -> R.string.select_date
                    },
                ),
                meta = period?.let {
                    stringResource(R.string.document_period, it.start.toString().toFrenchDate(), it.end.toString().toFrenchDate())
                } ?: stringResource(R.string.select_period),
                accent = FlowTeal,
                modifier = Modifier.fillMaxWidth(),
                onClick = { if (type == "CUSTOM") rangePickerVisible = true else datePickerVisible = true },
            )
            Spacer(Modifier.height(FlowSpacing.lg))
            SectionHeader(stringResource(R.string.formats))
            Row(horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                listOf("EXCEL" to R.string.excel, "PDF" to R.string.pdf).forEach { (value, label) ->
                    FilterChip(
                        selected = value in formats,
                        onClick = { formats = if (value in formats) formats - value else formats + value },
                        label = { Text(stringResource(label)) },
                    )
                }
            }
            Spacer(Modifier.height(FlowSpacing.lg))
            PrimaryAction(
                label = stringResource(R.string.generate_documents),
                loading = generating,
                enabled = period != null && formats.isNotEmpty(),
                onClick = { period?.let { onGenerate(type, formats, it) } },
            )
        }
    }

    if (datePickerVisible) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate.toUtcMillis())
        DatePickerDialog(
            onDismissRequest = { datePickerVisible = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { selectedDateText = it.toUtcLocalDate().toString() }
                    datePickerVisible = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { datePickerVisible = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DatePicker(pickerState) }
    }

    if (rangePickerVisible) {
        val rangeState = rememberDateRangePickerState(
            initialSelectedStartDateMillis = customStart?.toUtcMillis(),
            initialSelectedEndDateMillis = customEnd?.toUtcMillis(),
        )
        DatePickerDialog(
            onDismissRequest = { rangePickerVisible = false },
            confirmButton = {
                TextButton(
                    enabled = rangeState.selectedStartDateMillis != null && rangeState.selectedEndDateMillis != null,
                    onClick = {
                        customStartText = rangeState.selectedStartDateMillis!!.toUtcLocalDate().toString()
                        customEndText = rangeState.selectedEndDateMillis!!.toUtcLocalDate().toString()
                        rangePickerVisible = false
                    },
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { rangePickerVisible = false }) { Text(stringResource(R.string.cancel)) } },
        ) { DateRangePicker(rangeState, modifier = Modifier.height(520.dp)) }
    }
}

private fun LocalDate.toUtcMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
private fun Long.toUtcLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
fun GeneratedDocumentsContent(documents: List<GeneratedReportDto>, onGenerated: (Long) -> Unit, modifier: Modifier = Modifier) {
    FlowContentSurface(modifier) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.sm, FlowSpacing.xl, 110.dp), verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
            items(documents, key = { it.id }) { doc -> DocumentRow(doc) { onGenerated(doc.id) } }
        }
    }
}

@Composable private fun DocumentRow(document: GeneratedReportDto, onClick: () -> Unit) = FlowListRow(
    icon = if (document.format == "PDF") Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView,
    title = generatedTypeLabel(document.type),
    meta = stringResource(R.string.document_period, document.periodStart.toFrenchDate(), document.periodEnd.toFrenchDate()),
    accent = if (document.format == "PDF") FlowPink else FlowTeal,
    modifier = Modifier.fillMaxWidth(),
    onClick = onClick,
    trailing = { FlowStatusPill(document.format, MaterialTheme.colorScheme.primary, compact = true) },
)

@Composable
fun ReportDetailScreen(
    onBack: () -> Unit,
    onResumeDraft: (Long) -> Unit,
    onExport: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: ReportDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }
    FactoryFlowScaffold(
        topBar = { FocusedTopBar(stringResource(R.string.report_detail), onBack) },
        bottomBar = {
            state.report?.let { report ->
                FlowBottomActionBar {
                        if (report.status == "DRAFT") {
                            PrimaryAction(stringResource(R.string.continue_verification), onClick = { onResumeDraft(report.id) })
                            TextButton(
                                onClick = { confirmDelete = true },
                                enabled = !state.deleting,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            ) {
                                if (state.deleting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                else {
                                    Icon(Icons.Outlined.DeleteOutline, null)
                                    Spacer(Modifier.width(7.dp))
                                    Text(stringResource(R.string.delete_draft))
                                }
                            }
                        } else if (report.status == "CONFIRMED") {
                            PrimaryAction(stringResource(R.string.download_share), onClick = { onExport(report.id) })
                        }
                }
            }
        },
    ) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.report == null -> ErrorPane(stringResource(state.error?.title ?: R.string.server_error), stringResource(state.error?.detail ?: R.string.server_error), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> ReportDetailContent(state.report!!, Modifier.padding(padding))
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(Icons.Outlined.DeleteOutline, null) },
            title = { Text(stringResource(R.string.delete_draft_confirm_title)) },
            text = { Text(stringResource(R.string.delete_draft_confirm_detail)) },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
            confirmButton = {
                TextButton(
                    onClick = { confirmDelete = false; viewModel.deleteDraft() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.delete)) }
            },
        )
    }
}

@Composable fun ReportDetailContent(report: ReportDto, modifier: Modifier = Modifier) {
    val ready = report.entries.count { it.kpiDefinitionId != null && it.currentValue != null && it.warnings.isEmpty() }
    val missing = report.entries.count { it.kpiDefinitionId != null && it.currentValue == null }
    val unresolved = report.entries.count { it.kpiDefinitionId == null } + report.unrecognizedLines.count { it.resolution == "UNRESOLVED" }
    val attention = report.entries.size - ready - missing - report.entries.count { it.kpiDefinitionId == null }
    FlowContentSurface(modifier) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.md, FlowSpacing.xl, 112.dp), verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(report.effectiveDate.toFrenchDate(), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.report_number, report.id), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                StatusPill(reportStatusLabel(report.status), reportStatusColor(report.status))
            }
        }
        if (report.status == "DRAFT") item {
            FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                Column {
                    Text(stringResource(R.string.verification_state), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(FlowSpacing.md))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        DraftMetric(ready, stringResource(R.string.review_ready), Success)
                        DraftMetric(attention, stringResource(R.string.review_attention), Warning)
                        DraftMetric(missing, stringResource(R.string.review_missing), MaterialTheme.colorScheme.onSurfaceVariant)
                        DraftMetric(unresolved, stringResource(R.string.review_unresolved), MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
        item { Text(stringResource(if (report.status == "CONFIRMED") R.string.authoritative_values else R.string.review_values), style = MaterialTheme.typography.titleLarge) }
        val groupedEntries = report.entries.groupBy { it.sourceLine?.takeIf(String::isNotBlank) ?: "entry-${it.id}" }.entries.toList()
        items(groupedEntries, key = { it.key }) { group ->
            val entries = group.value
            val primary = entries.first()
            FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                Column {
                    Text(primary.kpiDisplayName ?: primary.kpiCode.orEmpty(), style = MaterialTheme.typography.titleMedium)
                    primary.sourceLine?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium, maxLines = 2) }
                    Spacer(Modifier.height(FlowSpacing.sm))
                    entries.forEachIndexed { index, entry ->
                        if (index > 0) HorizontalDivider(Modifier.padding(vertical = FlowSpacing.sm), color = MaterialTheme.colorScheme.outlineVariant)
                        val value = if (report.status == "CONFIRMED") entry.finalValue else entry.currentValue
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (index > 0) Text(entry.kpiDisplayName ?: entry.kpiCode ?: stringResource(R.string.secondary_value), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            else Spacer(Modifier.weight(1f))
                            Surface(color = if (index == 0) Color.Transparent else MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)) {
                                Row(Modifier.padding(if (index == 0) 0.dp else 8.dp), verticalAlignment = Alignment.Bottom) {
                                    Text(if (value == null) stringResource(R.string.not_provided) else value.displayValue(), style = if (value == null || index > 0) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall, color = if (value == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                                    if (value != null) entry.capturedUnit?.let { Spacer(Modifier.width(5.dp)); Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                        val secondary = if (report.status == "CONFIRMED") entry.secondaryFinalValue else entry.secondaryCurrentValue
                        if (secondary != null) {
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.secondary_value), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                StatusPill(secondary.displayValue() + (entry.secondaryUnit ?: ""), MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
        if (report.unrecognizedLines.isNotEmpty()) item { Text(stringResource(R.string.unknown_content), style = MaterialTheme.typography.titleLarge) }
        items(report.unrecognizedLines, key = { it.id }) { line -> FlowCard(Modifier.fillMaxWidth()) { Row { Text(line.sourceLine, Modifier.weight(1f)); StatusPill(unknownResolutionLabel(line.resolution), MaterialTheme.colorScheme.onSurfaceVariant) } } }
        report.rawText?.takeIf { it.isNotBlank() }?.let { raw -> item { SectionHeader(stringResource(R.string.raw_source)); FlowCard(Modifier.fillMaxWidth()) { Text(raw, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
    }
    }
}

@Composable private fun DraftMetric(value: Int, label: String, color: androidx.compose.ui.graphics.Color) = Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value.toString(), style = MaterialTheme.typography.titleLarge, color = color)
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
}

@Composable
fun GeneratedDetailScreen(onBack: () -> Unit, viewModel: GeneratedDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val emailBody = stringResource(R.string.email_report_body)
    val emailChooserTitle = stringResource(R.string.send_by_email)
    val shareChooserTitle = stringResource(R.string.share_document)
    var noViewer by remember { mutableStateOf(false) }
    var saveFailed by remember { mutableStateOf(false) }
    var pendingSaveFile by remember { mutableStateOf<java.io.File?>(null) }
    val scope = rememberCoroutineScope()
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { destination ->
        val source = pendingSaveFile
        if (destination != null && source != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(destination)?.buffered()?.use { output ->
                            source.inputStream().buffered().use { input -> input.copyTo(output) }
                        } ?: error("Destination unavailable")
                    }.isSuccess
                }
                saveFailed = !saved
                pendingSaveFile = null
            }
        } else {
            pendingSaveFile = null
        }
    }
    state.file?.let { file -> LaunchedEffect(file) {
        val document = state.document ?: return@LaunchedEffect
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.files", file)
        val mime = if (document.format == "PDF") "application/pdf" else "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        if (state.fileAction == GeneratedFileAction.SAVE) {
            pendingSaveFile = file
            saveLauncher.launch(document.fileName)
            viewModel.fileHandled()
            return@LaunchedEffect
        }
        val intent = when (state.fileAction) {
            GeneratedFileAction.SHARE -> Intent(Intent.ACTION_SEND).setType(mime)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_SUBJECT, document.fileName)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            GeneratedFileAction.EMAIL -> Intent(Intent.ACTION_SEND).setType(mime)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .putExtra(Intent.EXTRA_SUBJECT, document.fileName)
                .putExtra(Intent.EXTRA_TEXT, emailBody)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            else -> Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            if (state.fileAction == GeneratedFileAction.OPEN) context.startActivity(intent)
            else context.startActivity(Intent.createChooser(intent, if (state.fileAction == GeneratedFileAction.EMAIL) emailChooserTitle else shareChooserTitle))
        } catch (_: ActivityNotFoundException) { noViewer = true }
        viewModel.fileHandled()
    } }
    FactoryFlowScaffold(topBar = { FocusedTopBar(stringResource(R.string.generated_documents), onBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.document == null -> ErrorPane(stringResource(state.error?.title ?: R.string.server_error), stringResource(state.error?.detail ?: R.string.server_error), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> FlowContentSurface(Modifier.padding(padding)) {
                Column(Modifier.fillMaxSize().padding(FlowSpacing.xl)) {
                val doc = state.document!!
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(if (doc.format == "PDF") Icons.Outlined.PictureAsPdf else Icons.Outlined.TableView, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp)); Spacer(Modifier.width(14.dp)); Column { Text(doc.fileName, style = MaterialTheme.typography.headlineSmall); StatusPill(doc.format, MaterialTheme.colorScheme.primary) } }
                Spacer(Modifier.height(FlowSpacing.xxl)); FlowCard(Modifier.fillMaxWidth()) { Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                    Metadata(stringResource(R.string.document_period, doc.periodStart.toFrenchDate(), doc.periodEnd.toFrenchDate()), generatedTypeLabel(doc.type))
                    Metadata(stringResource(R.string.generated_on, doc.generatedAt.toFrenchInstant()), generationStatusLabel(doc.generationStatus))
                    Metadata(stringResource(R.string.email_delivery), emailStatusLabel(doc.emailDeliveryStatus))
                } }
                Spacer(Modifier.weight(1f)); state.error?.let { Text(stringResource(it.detail), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 8.dp)) }
                PrimaryAction(stringResource(if (state.downloading) R.string.downloading else R.string.download_open), state.downloading, enabled = doc.generationStatus == "READY", onClick = { viewModel.download(GeneratedFileAction.OPEN) })
                Spacer(Modifier.height(8.dp))
                OutlinedButton({ viewModel.download(GeneratedFileAction.SAVE) }, Modifier.fillMaxWidth(), enabled = !state.downloading && doc.generationStatus == "READY") {
                    Icon(Icons.Outlined.SaveAlt, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.save_document))
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton({ viewModel.download(GeneratedFileAction.SHARE) }, Modifier.weight(1f), enabled = !state.downloading && doc.generationStatus == "READY") {
                        Icon(Icons.Outlined.Share, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.share_document))
                    }
                    OutlinedButton({ viewModel.download(GeneratedFileAction.EMAIL) }, Modifier.weight(1f), enabled = !state.downloading && doc.generationStatus == "READY") {
                        Icon(Icons.Outlined.Email, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.send_by_email))
                    }
                }
                }
            }
        }
    }
    if (noViewer) AlertDialog(onDismissRequest = { noViewer = false }, title = { Text(stringResource(R.string.no_viewer)) }, confirmButton = { TextButton(onClick = { noViewer = false }) { Text(stringResource(R.string.close)) } })
    if (saveFailed) AlertDialog(
        onDismissRequest = { saveFailed = false },
        title = { Text(stringResource(R.string.save_failed)) },
        text = { Text(stringResource(R.string.save_failed_detail)) },
        confirmButton = { TextButton(onClick = { saveFailed = false }) { Text(stringResource(R.string.close)) } },
    )
}

@Composable private fun Metadata(label: String, value: String) { Row(Modifier.fillMaxWidth()) { Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.labelLarge) } }
@Composable private fun reportStatusColor(status: String) = when (status) { "CONFIRMED" -> Success; "DRAFT" -> MaterialTheme.colorScheme.onSurfaceVariant; else -> Warning }
@Composable private fun reportStatusLabel(status: String) = stringResource(when (status) { "CONFIRMED" -> R.string.confirmed; "DRAFT" -> R.string.draft; else -> R.string.pending_review })
@Composable private fun unknownResolutionLabel(value: String) = stringResource(when (value) { "IGNORED" -> R.string.ignore_line; "ASSIGNED" -> R.string.assign_kpi; else -> R.string.keep_unresolved })
@Composable private fun generatedTypeLabel(value: String) = stringResource(when (value) {
    "INDIVIDUAL" -> R.string.individual_report
    "DAILY" -> R.string.daily
    "WEEKLY" -> R.string.weekly
    "MONTHLY" -> R.string.monthly
    "CUSTOM", "MANUAL" -> R.string.custom_period
    else -> R.string.manual
})
@Composable private fun generationStatusLabel(value: String) = stringResource(if (value == "READY") R.string.status_ready else R.string.status_failed)
@Composable private fun emailStatusLabel(value: String) = stringResource(when (value) { "DELIVERED" -> R.string.email_delivered; "FAILED" -> R.string.email_failed; else -> R.string.not_requested })
