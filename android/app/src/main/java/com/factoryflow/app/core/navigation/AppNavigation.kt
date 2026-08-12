package com.factoryflow.app.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.factoryflow.app.R
import com.factoryflow.app.core.network.dto.UserDto
import com.factoryflow.app.feature.acquisition.*
import com.factoryflow.app.feature.dashboard.DashboardScreen
import com.factoryflow.app.feature.notifications.NotificationsScreen
import com.factoryflow.app.feature.reports.*
import com.factoryflow.app.feature.review.ReviewScreen
import com.factoryflow.app.feature.schedules.ScheduleFormScreen
import com.factoryflow.app.feature.schedules.SchedulesScreen
import com.factoryflow.app.feature.statistics.StatisticsScreen

private object Routes {
    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val CREATE = "create"
    const val NOTIFICATIONS = "notifications"
    const val PASTE = "paste"
    const val MANUAL = "manual"
    const val REVIEW = "review/{reportId}"
    const val REPORT_DETAIL = "report/{reportId}"
    const val GENERATED_DETAIL = "generated/{generatedId}"
    const val STATISTICS = "statistics"
    const val SCHEDULES = "schedules"
    const val SCHEDULE_FORM = "schedule/{scheduleId}"
}

private data class BottomDestination(val route: String, val label: Int, val icon: ImageVector, val selectedIcon: ImageVector)

@Composable
fun AuthenticatedApp(user: UserDto, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val topRoutes = setOf(Routes.DASHBOARD, Routes.REPORTS, Routes.CREATE, Routes.NOTIFICATIONS)
    val showBottom = entry?.destination?.route in topRoutes
    Scaffold(bottomBar = { if (showBottom) FactoryBottomBar(navController) }) { padding ->
        Box(Modifier.padding(bottom = padding.calculateBottomPadding())) {
            NavHost(navController, Routes.DASHBOARD) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(user.name, { navController.navigate(Routes.CREATE) }, { navController.navigate(Routes.PASTE) }, { navController.navigate(Routes.MANUAL) },
                        { navController.navigate("report/$it") }, { navController.navigate("generated/$it") }, { navController.navigate(Routes.STATISTICS) }, { navController.navigate(Routes.SCHEDULES) }, onLogout)
                }
                composable(Routes.REPORTS) { ReportsScreen({ navController.navigate("report/$it") }, { navController.navigate("review/$it") }, { navController.navigate("generated/$it") }) }
                composable(Routes.CREATE) { CreateHubScreen({ navController.navigate(Routes.PASTE) }, { navController.navigate(Routes.MANUAL) }) }
                composable(Routes.NOTIFICATIONS) { NotificationsScreen() }
                composable(Routes.PASTE) { PasteScreen({ navController.popBackStack() }, { navController.navigate("review/$it") }) }
                composable(Routes.MANUAL) { ManualEntryScreen({ navController.popBackStack() }, { navController.navigate("review/$it") }) }
                composable(Routes.REVIEW) { ReviewScreen({ navController.popBackStack() }, { id -> navController.navigate("report/$id") { popUpTo(Routes.REVIEW) { inclusive = true } } }) }
                composable(Routes.REPORT_DETAIL) { ReportDetailScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.GENERATED_DETAIL) { GeneratedDetailScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.STATISTICS) { StatisticsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.SCHEDULES) { SchedulesScreen({ navController.popBackStack() }, { navController.navigate("schedule/0") }, { navController.navigate("schedule/$it") }) }
                composable(Routes.SCHEDULE_FORM) { ScheduleFormScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}

@Composable
private fun FactoryBottomBar(navController: NavHostController) {
    val destinations = listOf(
        BottomDestination(Routes.DASHBOARD, R.string.nav_dashboard, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        BottomDestination(Routes.REPORTS, R.string.nav_reports, Icons.Outlined.Description, Icons.Filled.Description),
        BottomDestination(Routes.CREATE, R.string.nav_create, Icons.Outlined.AddCircleOutline, Icons.Rounded.AddCircle),
        BottomDestination(Routes.NOTIFICATIONS, R.string.nav_notifications, Icons.Outlined.NotificationsNone, Icons.Filled.Notifications),
    )
    val entry by navController.currentBackStackEntryAsState()
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        destinations.forEach { destination ->
            val selected = entry?.destination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(selected, {
                navController.navigate(destination.route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }, icon = { Icon(if (selected) destination.selectedIcon else destination.icon, null) }, label = { Text(stringResource(destination.label)) })
        }
    }
}
