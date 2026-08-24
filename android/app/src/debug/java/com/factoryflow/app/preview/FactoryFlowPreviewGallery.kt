package com.factoryflow.app.preview

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.factoryflow.app.R
import com.factoryflow.app.core.design.FactoryFlowScaffold
import com.factoryflow.app.core.design.FactoryFlowTheme
import com.factoryflow.app.core.design.FlowScreen
import com.factoryflow.app.core.design.ThemeMode
import com.factoryflow.app.core.navigation.FactoryFlowAppShell
import com.factoryflow.app.feature.acquisition.CreateHubContent
import com.factoryflow.app.feature.acquisition.FocusedTopBar
import com.factoryflow.app.feature.auth.LoginContent
import com.factoryflow.app.feature.acquisition.ManualEntryContent
import com.factoryflow.app.feature.acquisition.OcrResultContent
import com.factoryflow.app.feature.acquisition.OcrSource
import com.factoryflow.app.feature.acquisition.PasteContent
import com.factoryflow.app.feature.dashboard.DashboardContent
import com.factoryflow.app.feature.notifications.NotificationsContent
import com.factoryflow.app.feature.profile.ProfileContent
import com.factoryflow.app.feature.reports.ConfirmedReportContent
import com.factoryflow.app.feature.reports.GeneratedDocumentsContent
import com.factoryflow.app.feature.reports.ReportHistoryContent
import com.factoryflow.app.feature.reports.ReportDetailContent
import com.factoryflow.app.feature.reports.ReportsContent
import com.factoryflow.app.feature.review.ReviewContent
import com.factoryflow.app.feature.review.ReviewContentActions
import com.factoryflow.app.feature.schedules.ScheduleListContent
import com.factoryflow.app.feature.schedules.ScheduleFormActions
import com.factoryflow.app.feature.schedules.ScheduleFormContent
import com.factoryflow.app.feature.statistics.StatisticsContent


// ============================================================================
// LOGIN
// ============================================================================

@Preview(
    name = "Login - Light",
    showBackground = true,
    showSystemUi = true,
    widthDp = 411,
    heightDp = 891
)
@Composable
fun FactoryFlowLoginLightPreview() {
    FactoryFlowTheme(mode = ThemeMode.LIGHT) {
        LoginContent(
            state = PreviewFixtures.login,
            onEmailChanged = {},
            onPasswordChanged = {},
            onLogin = {}
        )
    }
}


@Preview(
    name = "Login - Dark",
    showBackground = true,
    showSystemUi = true,
    widthDp = 411,
    heightDp = 891
)
@Composable
fun FactoryFlowLoginDarkPreview() {
    FactoryFlowTheme(mode = ThemeMode.DARK) {
        LoginContent(
            state = PreviewFixtures.login,
            onEmailChanged = {},
            onPasswordChanged = {},
            onLogin = {}
        )
    }
}


// ============================================================================
// DASHBOARD
// ============================================================================

@Preview(
    name = "Dashboard - Light",
    showBackground = true,
    showSystemUi = true,
    widthDp = 411,
    heightDp = 891
)
@Composable
fun FactoryFlowDashboardLightPreview() {
    FactoryFlowTheme(mode = ThemeMode.LIGHT) {
        DashboardShellPreviewContent()
    }
}


@Preview(
    name = "Dashboard - Dark",
    showBackground = true,
    showSystemUi = true,
    widthDp = 411,
    heightDp = 891
)
@Composable
fun FactoryFlowDashboardDarkPreview() {
    FactoryFlowTheme(mode = ThemeMode.DARK) {
        DashboardShellPreviewContent()
    }
}
@Preview(
    name = "Dashboard - Full page",
    showBackground = true,
    showSystemUi = false,
    widthDp = 411,
    heightDp = 2800
)
@Composable
fun FactoryFlowDashboardFullPreview() {
    FactoryFlowTheme(
        mode = ThemeMode.LIGHT
    ) {
        DashboardFixtureContent()
    }
}

@Composable
private fun DashboardShellPreviewContent() {
    FactoryFlowAppShell(
        selectedDestination = "dashboard",
        onDestinationSelected = {},
        onCreateClick = {},
    ) { padding ->
        Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            DashboardFixtureContent()
        }
    }
}

@Composable
private fun DashboardFixtureContent() {
    DashboardContent(
        userName = "Badr-Eddine",
        data = PreviewFixtures.dashboard,
        onCreate = {},
        onPaste = {},
        onManual = {},
        onReport = {},
        onGenerated = {},
        onStatistics = {},
        onSchedules = {},
        onRefresh = {},
        onProfile = {},
        refreshing = false,
    )
}

// ============================================================================
// REMAINING PRODUCTION FLOW SCREENS
// ============================================================================

@Preview(name = "Create - Light", showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowCreateLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FlowScreen { CreateHubContent({}, {}, {}, Modifier.weight(1f)) }
}

@Preview(name = "Create - Dark", showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowCreateDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FlowScreen { CreateHubContent({}, {}, {}, Modifier.weight(1f)) }
}

@Preview(name = "Paste - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowPasteLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.paste_title)) { modifier ->
        PasteContent(PreviewFixtures.paste, {}, {}, {}, modifier)
    }
}

@Preview(name = "Paste - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowPasteDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.paste_title)) { modifier ->
        PasteContent(PreviewFixtures.paste, {}, {}, {}, modifier)
    }
}

@Preview(name = "Manual entry - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowManualLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.manual_title)) { modifier ->
        ManualEntryContent(PreviewFixtures.manualEntry, {}, { _, _ -> }, { _, _ -> }, {}, {}, {}, {}, modifier)
    }
}

@Preview(name = "Manual entry - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowManualDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.manual_title)) { modifier ->
        ManualEntryContent(PreviewFixtures.manualEntry, {}, { _, _ -> }, { _, _ -> }, {}, {}, {}, {}, modifier)
    }
}

@Preview(name = "OCR result - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowOcrLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.gallery_ocr)) { modifier ->
        OcrResultContent(PreviewFixtures.ocr, OcrSource.GALLERY, {}, {}, {}, {}, modifier)
    }
}

@Preview(name = "OCR result - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowOcrDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.gallery_ocr)) { modifier ->
        OcrResultContent(PreviewFixtures.ocr, OcrSource.GALLERY, {}, {}, {}, {}, modifier)
    }
}

@Preview(name = "Report detail - Light", showBackground = true, widthDp = 411, heightDp = 1100)
@Composable
fun FactoryFlowReportDetailLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.report_detail)) { modifier ->
        ReportDetailContent(PreviewFixtures.confirmedReport, modifier)
    }
}

@Preview(name = "Report detail - Dark", showBackground = true, widthDp = 411, heightDp = 1100)
@Composable
fun FactoryFlowReportDetailDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.report_detail)) { modifier ->
        ReportDetailContent(PreviewFixtures.draftReport, modifier)
    }
}

@Preview(name = "Reports - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowReportHistoryLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    ReportsContent(0, {}) { ReportHistoryContent(PreviewFixtures.reportSummaries, {}) }
}

@Preview(name = "Reports - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowReportHistoryDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    ReportsContent(0, {}) { ReportHistoryContent(PreviewFixtures.reportSummaries, {}) }
}

@Preview(name = "Generated documents - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowGeneratedLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    ReportsContent(1, {}) { GeneratedDocumentsContent(PreviewFixtures.generatedReports, {}) }
}

@Preview(name = "Generated documents - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowGeneratedDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    ReportsContent(1, {}) { GeneratedDocumentsContent(PreviewFixtures.generatedReports, {}) }
}

@Preview(name = "Review - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowReviewLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.review_title)) { modifier ->
        ReviewContent(PreviewFixtures.reviewAttention, ReviewContentActions(), modifier)
    }
}

@Preview(name = "Review - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowReviewDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.review_title)) { modifier ->
        ReviewContent(PreviewFixtures.reviewReady, ReviewContentActions(), modifier)
    }
}

@Preview(name = "Confirmation - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowConfirmationLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.report_confirmed)) { modifier ->
        ConfirmedReportContent(PreviewFixtures.confirmed, {}, {}, {}, modifier)
    }
}

@Preview(name = "Confirmation - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowConfirmationDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.report_confirmed)) { modifier ->
        ConfirmedReportContent(PreviewFixtures.confirmed, {}, {}, {}, modifier)
    }
}

@Preview(name = "Statistics - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowStatisticsLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.statistics_title)) { modifier ->
        StatisticsContent(PreviewFixtures.statistics, {}, {}, modifier)
    }
}

@Preview(name = "Statistics - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowStatisticsDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.statistics_title)) { modifier ->
        StatisticsContent(PreviewFixtures.statistics, {}, {}, modifier)
    }
}

@Preview(name = "Schedules - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowSchedulesLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.schedules_title)) { modifier ->
        ScheduleListContent(PreviewFixtures.schedules, null, {}, {}, modifier)
    }
}

@Preview(name = "Schedules - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowSchedulesDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.schedules_title)) { modifier ->
        ScheduleListContent(PreviewFixtures.schedules, null, {}, {}, modifier)
    }
}

@Preview(name = "Schedule form - Light", showBackground = true, widthDp = 411, heightDp = 1100)
@Composable
fun FactoryFlowScheduleFormLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.edit_schedule)) { modifier ->
        ScheduleFormContent(
            PreviewFixtures.scheduleForm,
            ScheduleFormActions({}, {}, {}, {}, {}, {}, {}, {}),
            modifier,
        )
    }
}

@Preview(name = "Schedule form - Dark", showBackground = true, widthDp = 411, heightDp = 1100)
@Composable
fun FactoryFlowScheduleFormDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.edit_schedule)) { modifier ->
        ScheduleFormContent(
            PreviewFixtures.scheduleForm,
            ScheduleFormActions({}, {}, {}, {}, {}, {}, {}, {}),
            modifier,
        )
    }
}

@Preview(name = "Notifications - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowNotificationsLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    NotificationsContent(com.factoryflow.app.feature.notifications.NotificationsUiState(loading = false), {}, {}, {})
}

@Preview(name = "Notifications - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowNotificationsDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    NotificationsContent(com.factoryflow.app.feature.notifications.NotificationsUiState(loading = false), {}, {}, {})
}

@Preview(name = "Profile - Light", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowProfileLightPreview() = FactoryFlowTheme(mode = ThemeMode.LIGHT) {
    FocusedPreviewFrame(stringResource(R.string.profile)) { modifier ->
        ProfileContent(PreviewFixtures.user, ThemeMode.SYSTEM, {}, {}, modifier)
    }
}

@Preview(name = "Profile - Dark", showBackground = true, widthDp = 411, heightDp = 891)
@Composable
fun FactoryFlowProfileDarkPreview() = FactoryFlowTheme(mode = ThemeMode.DARK) {
    FocusedPreviewFrame(stringResource(R.string.profile)) { modifier ->
        ProfileContent(PreviewFixtures.user, ThemeMode.DARK, {}, {}, modifier)
    }
}

@Composable
private fun FocusedPreviewFrame(
    title: String,
    content: @Composable (Modifier) -> Unit,
) {
    FactoryFlowScaffold(topBar = { FocusedTopBar(title, {}) }) { padding ->
        content(Modifier.padding(padding))
    }
}
