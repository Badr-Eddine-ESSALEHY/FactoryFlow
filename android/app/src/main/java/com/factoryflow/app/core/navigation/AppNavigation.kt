package com.factoryflow.app.core.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.FlowBottomNavigation
import com.factoryflow.app.core.design.FlowNavigationItem
import com.factoryflow.app.core.design.FlowSpacing
import com.factoryflow.app.core.design.ThemeMode
import com.factoryflow.app.core.network.dto.UserDto
import com.factoryflow.app.feature.acquisition.CreateHubScreen
import com.factoryflow.app.feature.acquisition.ManualEntryScreen
import com.factoryflow.app.feature.acquisition.PasteScreen
import com.factoryflow.app.feature.acquisition.GalleryOcrScreen
import com.factoryflow.app.feature.acquisition.SharedImageOcrScreen
import com.factoryflow.app.feature.acquisition.SharedAcquisition
import com.factoryflow.app.feature.acquisition.SharedAcquisitionStore
import com.factoryflow.app.feature.dashboard.DashboardScreen
import com.factoryflow.app.feature.notifications.NotificationsScreen
import com.factoryflow.app.feature.profile.ProfileScreen
import com.factoryflow.app.feature.reports.ConfirmedReportScreen
import com.factoryflow.app.feature.reports.GeneratedDetailScreen
import com.factoryflow.app.feature.reports.ReportDetailScreen
import com.factoryflow.app.feature.reports.ReportsScreen
import com.factoryflow.app.feature.review.ReviewScreen
import com.factoryflow.app.feature.schedules.ScheduleFormScreen
import com.factoryflow.app.feature.schedules.SchedulesScreen
import com.factoryflow.app.feature.statistics.StatisticsScreen
import com.factoryflow.app.feature.intelligence.MaintenanceIntelligenceOverviewScreen
import com.factoryflow.app.feature.intelligence.KpiIntelligenceWorkspaceScreen
import com.factoryflow.app.feature.intelligence.IntelligenceAlertsScreen
import com.factoryflow.app.feature.intelligence.IntelligenceAlertDetailScreen
import androidx.compose.material.icons.outlined.BarChart

internal object Routes {
    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val CREATE = "create"
    const val STATISTICS = "statistics"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val PASTE = "paste"
    const val MANUAL = "manual"
    const val GALLERY = "gallery-ocr"
    const val SHARED_IMAGE = "shared-image-ocr"
    const val REVIEW = "review/{reportId}"
    const val CONFIRMED = "confirmed/{reportId}"
    const val REPORT_DETAIL = "report/{reportId}"
    const val GENERATED_DETAIL = "generated/{generatedId}"
    const val SCHEDULES = "schedules"
    const val SCHEDULE_FORM = "schedule/{scheduleId}"
    const val INTELLIGENCE_OVERVIEW = "maintenance-intelligence"
    const val INTELLIGENCE_WORKSPACE = "maintenance-intelligence/kpi/{kpiId}"
    const val INTELLIGENCE_ALERTS = "maintenance-intelligence/alerts"
    const val INTELLIGENCE_ALERT_DETAIL = "maintenance-intelligence/alerts/{alertId}"
}

private val topLevelRoutes = setOf(
    Routes.DASHBOARD, Routes.REPORTS, Routes.CREATE, Routes.STATISTICS, Routes.NOTIFICATIONS,
)

private val bottomNavigationRoutes = setOf(
    Routes.DASHBOARD,
    Routes.REPORTS,
    Routes.CREATE,
    Routes.STATISTICS,
    Routes.NOTIFICATIONS,
)
internal fun showsBottomNavigation(route: String?): Boolean = route in bottomNavigationRoutes

internal fun selectedTopLevelRoute(route: String?): String = when (route) {
    Routes.REPORTS, Routes.CONFIRMED, Routes.REPORT_DETAIL, Routes.GENERATED_DETAIL, Routes.SCHEDULES, Routes.SCHEDULE_FORM -> Routes.REPORTS
    Routes.CREATE, Routes.PASTE, Routes.MANUAL, Routes.GALLERY, Routes.SHARED_IMAGE, Routes.REVIEW -> Routes.CREATE
    Routes.STATISTICS -> Routes.STATISTICS
    Routes.INTELLIGENCE_OVERVIEW, Routes.INTELLIGENCE_WORKSPACE, Routes.INTELLIGENCE_ALERTS,
    Routes.INTELLIGENCE_ALERT_DETAIL -> Routes.STATISTICS
    Routes.NOTIFICATIONS -> Routes.NOTIFICATIONS
    else -> Routes.DASHBOARD
}

@Composable
fun AuthenticatedApp(
    user: UserDto,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    sharedAcquisitions: SharedAcquisitionStore,
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val incoming by sharedAcquisitions.content.collectAsStateWithLifecycle()
    var pendingShared by remember { mutableStateOf<SharedAcquisition?>(null) }
    val showBottomNavigation = showsBottomNavigation(currentRoute)

    LaunchedEffect(incoming) {
        val acquisition = sharedAcquisitions.consume() ?: return@LaunchedEffect
        pendingShared = acquisition
        when (acquisition) {
            is SharedAcquisition.Text -> navController.navigate(Routes.PASTE) { launchSingleTop = true }
            is SharedAcquisition.Image -> navController.navigate(Routes.SHARED_IMAGE) { launchSingleTop = true }
            is SharedAcquisition.Invalid -> {
                pendingShared = null
                navigateTopLevel(navController, Routes.CREATE)
            }
        }
    }

    BackHandler(
        enabled = currentRoute != null && currentRoute != Routes.DASHBOARD &&
            currentRoute != Routes.REVIEW && currentRoute != Routes.MANUAL,
    ) {
        if (currentRoute == Routes.CONFIRMED) {
            navigateTopLevel(navController, Routes.REPORTS)
        } else {
            navigateBack(navController)
        }
    }

    FactoryFlowAppShell(
        selectedDestination = selectedTopLevelRoute(currentRoute),
        showBottomNavigation = showBottomNavigation,
        onDestinationSelected = { navigateTopLevel(navController, it) },
        onCreateClick = { navigateTopLevel(navController, Routes.CREATE) },
    ) { padding ->
        Box(Modifier.padding(bottom = if (showBottomNavigation) padding.calculateBottomPadding() else FlowSpacing.none)) {
            NavHost(navController, Routes.DASHBOARD) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        user.name,
                        { navigateTopLevel(navController, Routes.CREATE) },
                        { navController.navigate(Routes.PASTE) },
                        { navController.navigate(Routes.MANUAL) },
                        { navController.navigate("report/$it") },
                        { navController.navigate("generated/$it") },
                        { navigateTopLevel(navController, Routes.STATISTICS) },
                        { navController.navigate(Routes.SCHEDULES) },
                        { navController.navigate(Routes.PROFILE) },
                    )
                }
                composable(Routes.REPORTS) {
                    ReportsScreen(
                        { navController.navigate("report/$it") },
                        { navController.navigate("generated/$it") },
                    )
                }
                composable(Routes.CREATE) {
                    CreateHubScreen(
                        { navController.navigate(Routes.PASTE) },
                        { navController.navigate(Routes.MANUAL) },
                        { navController.navigate(Routes.GALLERY) },
                    )
                }
                composable(Routes.STATISTICS) {
                    StatisticsScreen(onOpenIntelligence = { navController.navigate(Routes.INTELLIGENCE_OVERVIEW) })
                }
                composable(Routes.NOTIFICATIONS) {
                    NotificationsScreen(
                        onReport = { navController.navigate("report/$it") },
                        onGenerated = { navController.navigate("generated/$it") },
                        onIntelligenceAlert = { navController.navigate("maintenance-intelligence/alerts/$it") },
                    )
                }
                composable(Routes.INTELLIGENCE_OVERVIEW) {
                    MaintenanceIntelligenceOverviewScreen(
                        onBack = { navigateBack(navController) },
                        onKpi = { navController.navigate("maintenance-intelligence/kpi/$it") },
                        onAlerts = { navController.navigate(Routes.INTELLIGENCE_ALERTS) },
                        onAlert = { navController.navigate("maintenance-intelligence/alerts/$it") },
                    )
                }
                composable(
                    route = Routes.INTELLIGENCE_WORKSPACE,
                    arguments = listOf(navArgument("kpiId") { type = NavType.LongType }),
                ) {
                    KpiIntelligenceWorkspaceScreen(
                        onBack = { navigateBack(navController) },
                        onAlert = { navController.navigate("maintenance-intelligence/alerts/$it") },
                        onReport = { navController.navigate("report/$it") },
                    )
                }
                composable(Routes.INTELLIGENCE_ALERTS) {
                    IntelligenceAlertsScreen(
                        onBack = { navigateBack(navController) },
                        onAlert = { navController.navigate("maintenance-intelligence/alerts/$it") },
                    )
                }
                composable(
                    route = Routes.INTELLIGENCE_ALERT_DETAIL,
                    arguments = listOf(navArgument("alertId") { type = NavType.LongType }),
                ) {
                    IntelligenceAlertDetailScreen(
                        onBack = { navigateBack(navController) },
                        onKpi = { navController.navigate("maintenance-intelligence/kpi/$it") },
                        onReport = { navController.navigate("report/$it") },
                    )
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(user, themeMode, onThemeMode, { navigateBack(navController) }, onLogout)
                }
                composable(Routes.PASTE) {
                    PasteScreen(
                        onBack = { pendingShared = null; navigateBack(navController) },
                        initialText = (pendingShared as? SharedAcquisition.Text)?.value,
                        onReview = { reportId ->
                            pendingShared = null
                            navController.navigate("review/$reportId") { launchSingleTop = true }
                        },
                    )
                }
                composable(Routes.GALLERY) {
                    GalleryOcrScreen(
                        onBack = { navigateBack(navController) },
                        onReview = { reportId -> navController.navigate("review/$reportId") { launchSingleTop = true } },
                    )
                }
                composable(Routes.SHARED_IMAGE) {
                    val image = pendingShared as? SharedAcquisition.Image
                    if (image != null) {
                        SharedImageOcrScreen(
                            uri = image.uri,
                            onBack = { pendingShared = null; navigateBack(navController) },
                            onReview = { reportId ->
                                pendingShared = null
                                navController.navigate("review/$reportId") { launchSingleTop = true }
                            },
                        )
                    } else {
                        LaunchedEffect(Unit) { navigateTopLevel(navController, Routes.CREATE) }
                    }
                }
                composable(Routes.MANUAL) {
                    ManualEntryScreen(
                        { navigateBack(navController) },
                        { reportId -> navController.navigate("review/$reportId") { launchSingleTop = true } },
                    )
                }
                composable(Routes.REVIEW) {
                    ReviewScreen(
                        { navigateBack(navController) },
                        { id ->
                            navController.navigate("confirmed/$id") {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(Routes.CONFIRMED) {
                    ConfirmedReportScreen(
                        onBack = { navigateTopLevel(navController, Routes.REPORTS) },
                        onOpenReport = { id ->
                            navController.navigate("report/$id") {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        onReports = { navigateTopLevel(navController, Routes.REPORTS) },
                    )
                }
                composable(Routes.REPORT_DETAIL) {
                    ReportDetailScreen(
                        onBack = { navigateBack(navController) },
                        onResumeDraft = { navController.navigate("review/$it") },
                        onExport = { navController.navigate("confirmed/$it") },
                        onDeleted = { navigateTopLevel(navController, Routes.REPORTS) },
                    )
                }
                composable(Routes.GENERATED_DETAIL) {
                    GeneratedDetailScreen(onBack = { navigateBack(navController) })
                }
                composable(Routes.SCHEDULES) {
                    SchedulesScreen(
                        { navigateBack(navController) },
                        { navController.navigate("schedule/0") },
                        { navController.navigate("schedule/$it") },
                    )
                }
                composable(Routes.SCHEDULE_FORM) {
                    ScheduleFormScreen(onBack = { navigateBack(navController) })
                }
            }
        }
    }
}

/**
 * Pure application chrome shared by runtime navigation and full-screen previews.
 * It intentionally knows nothing about NavController, Hilt, networking, or persistence.
 */
@Composable
fun FactoryFlowAppShell(
    selectedDestination: String,
    showBottomNavigation: Boolean = true,
    onDestinationSelected: (String) -> Unit,
    onCreateClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomNavigation) {
                FactoryFlowBottomNavigation(
                    selectedRoute = selectedDestination,
                    onDestinationSelected = onDestinationSelected,
                    onCreateClick = onCreateClick,
                )
            }
        },
        content = content,
    )
}

private fun navigateBack(navController: NavHostController) {
    if (!navController.popBackStack()) {
        navigateTopLevel(navController, Routes.DASHBOARD)
    }
}

internal fun navigateTopLevel(navController: NavHostController, route: String) {
    require(route in topLevelRoutes) { "Unknown top-level route: $route" }
    if (navController.currentDestination?.route == route) {
        navController.popBackStack(route, inclusive = false)
        return
    }
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun FactoryFlowBottomNavigation(
    selectedRoute: String,
    onDestinationSelected: (String) -> Unit,
    onCreateClick: () -> Unit,
) {
    FlowBottomNavigation(
        items = listOf(
            FlowNavigationItem(
                key = Routes.DASHBOARD,
                label = stringResource(R.string.nav_home),
                icon = Icons.Outlined.Dashboard,
            ),
            FlowNavigationItem(
                key = Routes.REPORTS,
                label = stringResource(R.string.nav_reports),
                icon = Icons.Outlined.Description,
            ),
            FlowNavigationItem(
                key = Routes.STATISTICS,
                label = stringResource(R.string.nav_statistics),
                icon = Icons.Outlined.BarChart,
            ),
            FlowNavigationItem(
                key = Routes.NOTIFICATIONS,
                label = stringResource(R.string.nav_alerts),
                icon = Icons.Outlined.NotificationsNone,
            ),
        ),
        selectedKey = selectedRoute,
        createDescription = stringResource(R.string.nav_create),
        onItemSelected = onDestinationSelected,
        onCreate = onCreateClick,
    )
}
