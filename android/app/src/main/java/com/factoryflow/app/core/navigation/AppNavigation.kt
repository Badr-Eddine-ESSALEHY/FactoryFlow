package com.factoryflow.app.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.factoryflow.app.core.design.FactoryFlowGradient
import com.factoryflow.app.core.design.FactoryRadius
import com.factoryflow.app.core.design.ThemeMode
import com.factoryflow.app.core.network.dto.UserDto
import com.factoryflow.app.feature.acquisition.CreateHubScreen
import com.factoryflow.app.feature.acquisition.ManualEntryScreen
import com.factoryflow.app.feature.acquisition.PasteScreen
import com.factoryflow.app.feature.dashboard.DashboardScreen
import com.factoryflow.app.feature.notifications.NotificationsScreen
import com.factoryflow.app.feature.profile.ProfileScreen
import com.factoryflow.app.feature.reports.GeneratedDetailScreen
import com.factoryflow.app.feature.reports.ReportDetailScreen
import com.factoryflow.app.feature.reports.ReportsScreen
import com.factoryflow.app.feature.review.ReviewScreen
import com.factoryflow.app.feature.schedules.ScheduleFormScreen
import com.factoryflow.app.feature.schedules.SchedulesScreen
import com.factoryflow.app.feature.statistics.StatisticsScreen

private object Routes {
    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val CREATE = "create"
    const val STATISTICS = "statistics"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val PASTE = "paste"
    const val MANUAL = "manual"
    const val REVIEW = "review/{reportId}"
    const val REPORT_DETAIL = "report/{reportId}"
    const val GENERATED_DETAIL = "generated/{generatedId}"
    const val SCHEDULES = "schedules"
    const val SCHEDULE_FORM = "schedule/{scheduleId}"
}

private data class BottomDestination(val route: String, val label: Int, val icon: ImageVector, val selectedIcon: ImageVector)

@Composable
fun AuthenticatedApp(
    user: UserDto,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val topRoutes = setOf(Routes.DASHBOARD, Routes.REPORTS, Routes.CREATE, Routes.STATISTICS, Routes.NOTIFICATIONS)
    val showBottom = entry?.destination?.route in topRoutes
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { if (showBottom) FactoryBottomBar(navController) },
    ) { padding ->
        Box(Modifier.padding(bottom = if (showBottom) padding.calculateBottomPadding() else 0.dp)) {
            NavHost(navController, Routes.DASHBOARD) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        user.name,
                        { navController.navigate(Routes.CREATE) },
                        { navController.navigate(Routes.PASTE) },
                        { navController.navigate(Routes.MANUAL) },
                        { navController.navigate("report/$it") },
                        { navController.navigate("generated/$it") },
                        { navController.navigate(Routes.STATISTICS) },
                        { navController.navigate(Routes.SCHEDULES) },
                        { navController.navigate(Routes.PROFILE) },
                    )
                }
                composable(Routes.REPORTS) {
                    ReportsScreen(
                        { navController.navigate("report/$it") },
                        { navController.navigate("review/$it") },
                        { navController.navigate("generated/$it") },
                    )
                }
                composable(Routes.CREATE) { CreateHubScreen({ navController.navigate(Routes.PASTE) }, { navController.navigate(Routes.MANUAL) }) }
                composable(Routes.STATISTICS) { StatisticsScreen(onBack = {}) }
                composable(Routes.NOTIFICATIONS) { NotificationsScreen() }
                composable(Routes.PROFILE) { ProfileScreen(user, themeMode, onThemeMode, { navController.popBackStack() }, onLogout) }
                composable(Routes.PASTE) { PasteScreen({ navController.popBackStack() }, { navController.navigate("review/$it") }) }
                composable(Routes.MANUAL) { ManualEntryScreen({ navController.popBackStack() }, { navController.navigate("review/$it") }) }
                composable(Routes.REVIEW) {
                    ReviewScreen(
                        { navController.popBackStack() },
                        { id -> navController.navigate("report/$id") { popUpTo(Routes.REVIEW) { inclusive = true } } },
                    )
                }
                composable(Routes.REPORT_DETAIL) { ReportDetailScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.GENERATED_DETAIL) { GeneratedDetailScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.SCHEDULES) {
                    SchedulesScreen(
                        { navController.popBackStack() },
                        { navController.navigate("schedule/0") },
                        { navController.navigate("schedule/$it") },
                    )
                }
                composable(Routes.SCHEDULE_FORM) { ScheduleFormScreen(onBack = { navController.popBackStack() }) }
            }
        }
    }
}

@Composable
private fun FactoryBottomBar(navController: NavHostController) {
    val destinations = listOf(
        BottomDestination(Routes.DASHBOARD, R.string.nav_home, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        BottomDestination(Routes.REPORTS, R.string.nav_reports, Icons.Outlined.Description, Icons.Filled.Description),
        BottomDestination(Routes.STATISTICS, R.string.nav_statistics, Icons.Outlined.Assessment, Icons.Filled.Assessment),
        BottomDestination(Routes.NOTIFICATIONS, R.string.nav_alerts, Icons.Outlined.NotificationsNone, Icons.Filled.Notifications),
    )
    val entry by navController.currentBackStackEntryAsState()
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 12.dp) {
        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().height(72.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            destinations.take(2).forEach { BottomItem(it, entry?.destination?.hierarchy?.any { node -> node.route == it.route } == true, navController) }
            CreateItem(entry?.destination?.route == Routes.CREATE) { navigateTop(navController, Routes.CREATE) }
            destinations.drop(2).forEach { BottomItem(it, entry?.destination?.hierarchy?.any { node -> node.route == it.route } == true, navController) }
        }
    }
}

@Composable
private fun RowScope.BottomItem(destination: BottomDestination, selected: Boolean, navController: NavHostController) {
    Box(
        Modifier.weight(1f).height(58.dp).clickable { navigateTop(navController, destination.route) },
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(FactoryRadius.pill),
            ) {
                Icon(
                    if (selected) destination.selectedIcon else destination.icon,
                    stringResource(destination.label),
                    Modifier.padding(horizontal = 14.dp, vertical = 5.dp).size(21.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                stringResource(destination.label),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RowScope.CreateItem(selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.weight(1.05f).height(68.dp), contentAlignment = Alignment.TopCenter) {
        Surface(
            Modifier.size(56.dp).clickable(onClick = onClick),
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = 10.dp,
        ) {
            Box(Modifier.background(FactoryFlowGradient), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Add, stringResource(R.string.nav_create), tint = Color.White, modifier = Modifier.size(28.dp))
            }
        }
    }
}

private fun navigateTop(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
