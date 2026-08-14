package com.factoryflow.app.preview

import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.feature.auth.LoginUiState
import com.factoryflow.app.feature.acquisition.ManualEntryRow
import com.factoryflow.app.feature.acquisition.ManualEntryUiState
import com.factoryflow.app.feature.acquisition.OcrAcquisitionState
import com.factoryflow.app.feature.acquisition.PasteUiState
import com.factoryflow.app.feature.reports.ConfirmedReportUiState
import com.factoryflow.app.feature.review.ReviewEntry
import com.factoryflow.app.feature.review.ReviewUnknown
import com.factoryflow.app.feature.review.ReviewUiState
import com.factoryflow.app.feature.statistics.StatisticsUiState
import com.factoryflow.app.feature.schedules.ScheduleFormUiState
import java.math.BigDecimal

/**
 * Android Studio-only visual fixtures. Nothing in this file is packaged into release builds,
 * injected into repositories, or reachable by runtime ViewModels.
 */
object PreviewFixtures {
    val login = LoginUiState(email = "badr.eddine@alfmabrouk.ma", password = "••••••••")
    val loginLoading = login.copy(submitting = true)
    val user = UserDto(1, "Badr-Eddine", "badr.eddine@alfmabrouk.ma", true)
    val paste = PasteUiState(
        text = "Vrac : 42,75 t\nCompresseur 1 : 77108 h - 77 %\nHumidité : ---",
    )

    val dashboard = DashboardDto(
        businessDate = "2026-08-13",
        todayConfirmedReportCount = 2,
        todayDraftOrPendingReportCount = 1,
        todayGeneratedDocumentCount = 2,
        todayConfirmedMissingValueCount = 1,
        todayHasConfirmedReport = true,
        activityTrend = listOf(
            DashboardActivityDto("2026-08-07", 1, 0),
            DashboardActivityDto("2026-08-08", 2, 1),
            DashboardActivityDto("2026-08-09", 1, 0),
            DashboardActivityDto("2026-08-10", 3, 1),
            DashboardActivityDto("2026-08-11", 2, 0),
            DashboardActivityDto("2026-08-12", 4, 2),
            DashboardActivityDto("2026-08-13", 2, 1),
        ),
        latestKpis = listOf(
            LatestKpiDto(1, "VRAC", "Vrac", "t", BigDecimal("42.75"), "2026-08-13", 19, "2026-08-13T08:10:00Z"),
            LatestKpiDto(2, "COMPRESSEUR_1", "Compresseur 1", "h", BigDecimal("77108"), "2026-08-13", 19, "2026-08-13T08:10:00Z"),
            LatestKpiDto(3, "HUMIDITE", "Humidité", "%", BigDecimal("15.8"), "2026-08-12", 18, "2026-08-12T17:42:00Z"),
        ),
        recentReports = listOf(
            RecentReportDto(19, "2026-08-13", "CONFIRMED", "2026-08-13T08:03:00Z", "2026-08-13T08:10:00Z", "Badr-Eddine"),
            RecentReportDto(18, "2026-08-12", "DRAFT", "2026-08-12T17:21:00Z", null, "Badr-Eddine"),
        ),
        recentGeneratedReports = listOf(
            RecentGeneratedReportDto(8, "DAILY", "PDF", "2026-08-13", "2026-08-13", "2026-08-13T08:12:00Z"),
            RecentGeneratedReportDto(7, "DAILY", "EXCEL", "2026-08-13", "2026-08-13", "2026-08-13T08:11:00Z"),
        ),
        upcomingSchedule = UpcomingScheduleDto(4, "DAILY", "2026-08-14T07:00:00Z", true, true, false),
    )

    val emptyDashboard = dashboard.copy(
        todayConfirmedReportCount = 0,
        todayDraftOrPendingReportCount = 0,
        todayGeneratedDocumentCount = 0,
        todayConfirmedMissingValueCount = 0,
        todayHasConfirmedReport = false,
        activityTrend = emptyList(),
        latestKpis = emptyList(),
        recentReports = emptyList(),
        recentGeneratedReports = emptyList(),
        upcomingSchedule = null,
    )

    val submittedBy = SubmittedByDto(1, "Badr-Eddine")
    val reportSummaries = listOf(
        ReportSummaryDto(19, "CONFIRMED", "PASTE", "2026-08-13", "2026-08-13T08:03:00Z", "2026-08-13T08:10:00Z", submittedBy, 5, 0),
        ReportSummaryDto(18, "DRAFT", "GALLERY_OCR", "2026-08-12", "2026-08-12T17:21:00Z", null, submittedBy, 4, 2),
        ReportSummaryDto(17, "PENDING_REVIEW", "MANUAL", "2026-08-11", "2026-08-11T15:12:00Z", null, submittedBy, 3, 1),
    )
    val generatedReports = listOf(
        GeneratedReportDto(8, "DAILY", "PDF", "2026-08-13", "2026-08-13", "MANUAL", "READY", "NOT_REQUESTED", 1, "2026-08-13T08:12:00Z", "rapport-quotidien-2026-08-13.pdf", 1, null, null, listOf(19)),
        GeneratedReportDto(7, "DAILY", "EXCEL", "2026-08-13", "2026-08-13", "MANUAL", "READY", "NOT_REQUESTED", 1, "2026-08-13T08:11:00Z", "rapport-quotidien-2026-08-13.xlsx", 1, null, null, listOf(19)),
    )

    val confirmedReport = ReportDto(
        id = 19,
        status = "CONFIRMED",
        effectiveDate = "2026-08-13",
        source = "PASTE",
        rawText = "Vrac: 42,75 t\nCompresseur 1: 77108-77%\nHumidité: ---",
        submittedAt = "2026-08-13T08:03:00Z",
        updatedAt = "2026-08-13T08:10:00Z",
        confirmedAt = "2026-08-13T08:10:00Z",
        version = 2,
        submittedBy = submittedBy,
        entries = listOf(
            ReportEntryDto(1, 1, "VRAC", "Vrac", "Vrac", "Vrac: 42,75 t", BigDecimal("42.75"), BigDecimal("42.75"), BigDecimal("42.75"), BigDecimal("0.99"), false, "t"),
            ReportEntryDto(2, 2, "COMPRESSEUR_1", "Compresseur 1", "Compresseur 1", "Compresseur 1: 77108-77%", BigDecimal("77108"), BigDecimal("77108"), BigDecimal("77108"), BigDecimal("0.98"), false, "h", secondaryExtractedValue = BigDecimal("77"), secondaryCurrentValue = BigDecimal("77"), secondaryFinalValue = BigDecimal("77"), secondaryUnit = "%"),
            ReportEntryDto(3, 3, "HUMIDITE", "Humidité", "Humidité", "Humidité: ---", null, null, null, BigDecimal("1.0"), false, "%", warnings = setOf("MISSING_VALUE")),
        ),
    )
    val draftReport = confirmedReport.copy(id = 18, status = "DRAFT", confirmedAt = null, version = 1)

    val definitions = listOf(
        KpiDefinitionDto(1, "VRAC", "Vrac", "Production", "t", BigDecimal.ZERO, BigDecimal("200"), listOf("vrac"), true),
        KpiDefinitionDto(2, "COMPRESSEUR_1", "Compresseur 1", "Équipements", "h", BigDecimal.ZERO, null, listOf("compresseur1"), true),
        KpiDefinitionDto(3, "HUMIDITE", "Humidité", "Qualité", "%", BigDecimal.ZERO, BigDecimal("100"), listOf("humidite"), true),
    )

    val manualEntry = ManualEntryUiState(
        loading = false,
        definitions = definitions,
        entries = listOf(
            ManualEntryRow(definitions[0], "42.75"),
            ManualEntryRow(definitions[1], "77108"),
            ManualEntryRow(definitions[2], ""),
        ),
        effectiveDate = "2026-08-13",
    )
    val ocr = OcrAcquisitionState(
        extractedText = "Vrac : 42,75 t\nCompresseur 1 : 77108 h - 77 %\nHumidité : ---",
    )
    val confirmed = ConfirmedReportUiState(loading = false, report = confirmedReport)

    val reviewReady = ReviewUiState(
        loading = false,
        report = draftReport,
        definitions = definitions,
        entries = listOf(
            ReviewEntry(1, 1, "Vrac", "42.75", "42.75", "t", "0.99", emptySet(), "Vrac", "Vrac: 42,75 t", false, null, null, null, null),
            ReviewEntry(2, 2, "Compresseur 1", "77108", "77108", "h", "0.98", emptySet(), "Compresseur 1", "Compresseur 1: 77108-77%", false, null, null, null, null, secondaryValue = "77", secondaryExtractedValue = "77", secondaryUnit = "%"),
            ReviewEntry(3, 3, "Humidité", "", null, "%", "1.0", setOf("MISSING_VALUE"), "Humidité", "Humidité: ---", false, null, null, null, null),
        ),
    )
    val reviewAttention = reviewReady.copy(
        entries = reviewReady.entries + ReviewEntry(
            4, null, "Indicateur non reconnu", "44", "44", null, "0.42", emptySet(),
            "Unexpected metric", "Unexpected metric 44", false, 1, "Vrac", "t", "0.61",
        ),
        unknownLines = listOf(ReviewUnknown(10, "Unexpected metric 44", "UNRESOLVED", null)),
    )

    val statistics = StatisticsUiState(
        loading = false,
        days = 30,
        selectedKpiId = 1,
        kpis = listOf(
            KpiStatisticsDto(
                1, "VRAC", "Vrac", "t",
                BigDecimal("42.75"), BigDecimal("39.2"), BigDecimal("45.4"), BigDecimal("42.18"),
                6, 7, 1,
                listOf(
                    StatisticsPointDto("2026-08-07", 11, BigDecimal("39.2")),
                    StatisticsPointDto("2026-08-08", 12, BigDecimal("41.4")),
                    StatisticsPointDto("2026-08-10", 14, BigDecimal("43.1")),
                    StatisticsPointDto("2026-08-11", 16, BigDecimal("45.4")),
                    StatisticsPointDto("2026-08-12", 18, BigDecimal("41.2")),
                    StatisticsPointDto("2026-08-13", 19, BigDecimal("42.75")),
                ),
            ),
        ),
    )

    val schedules = listOf(
        ReportScheduleDto(4, "DAILY", "07:00", null, "Africa/Casablanca", true, true, true, false, emptyList(), "2026-08-14T07:00:00Z", 1),
        ReportScheduleDto(5, "WEEKLY", "08:00", "MONDAY", "Africa/Casablanca", false, false, true, true, listOf("maintenance@alfmabrouk.ma"), "2026-08-17T08:00:00Z", 2),
    )
    val scheduleForm = ScheduleFormUiState(
        loading = false,
        id = 5,
        type = "WEEKLY",
        time = "08:00",
        dayOfWeek = "MONDAY",
        excel = true,
        pdf = true,
        emailEnabled = true,
        recipients = "maintenance@alfmabrouk.ma",
        enabled = true,
    )
}
