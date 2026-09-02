package com.factoryflow.app.feature.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.NotificationDto

@Composable
fun NotificationsScreen(
    onReport: (Long) -> Unit,
    onGenerated: (Long) -> Unit,
    onIntelligenceAlert: (Long) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    FlowScreen {
        NotificationsContent(
            state = state,
            onRetry = viewModel::load,
            onRead = viewModel::read,
            onOpen = { notification ->
                notification.relatedIntelligenceAlertId?.let(onIntelligenceAlert)
                    ?: notification.relatedGeneratedReportId?.let(onGenerated)
                    ?: notification.relatedReportId?.let(onReport)
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
fun NotificationsContent(
    state: NotificationsUiState,
    onRetry: () -> Unit,
    onRead: (Long) -> Unit,
    onOpen: (NotificationDto) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowContentSurface(modifier) {
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading))
            state.error != null -> ErrorPane(stringResource(state.error.title), stringResource(state.error.detail), stringResource(R.string.retry), onRetry)
            state.items.isEmpty() -> Column(Modifier.fillMaxSize().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg)) {
                FlowPageHeader(stringResource(R.string.notifications_title)); Spacer(Modifier.height(FlowSpacing.lg))
                FlowEmptyState(stringResource(R.string.no_notifications), stringResource(R.string.no_notifications_detail), icon = Icons.Outlined.NotificationsNone)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(), contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.lg, FlowSpacing.xl, FlowSpacing.xxxl),
                verticalArrangement = Arrangement.spacedBy(FlowSpacing.sm),
            ) {
                item { FlowPageHeader(stringResource(R.string.notifications_title)) }
                items(state.items, key = NotificationDto::id) { item ->
                    FlowListRow(
                        icon = icon(item.type), title = item.title, meta = item.message,
                        accent = accent(item.type), modifier = Modifier.fillMaxWidth(), onClick = {
                            onRead(item.id)
                            onOpen(item)
                        },
                        trailing = { if (item.readAt == null) FlowStatusPill(stringResource(R.string.new_notification), FlowBlue, compact = true) },
                    )
                }
            }
        }
    }
}

private fun icon(type: String): ImageVector = when (type) {
    "REPORT_CONFIRMED" -> Icons.Outlined.TaskAlt
    "SCHEDULED_DOCUMENT_READY" -> Icons.Outlined.Description
    "EMAIL_FAILED" -> Icons.Outlined.MarkEmailUnread
    "SCHEDULE_FAILED" -> Icons.Outlined.ErrorOutline
    "MAINTENANCE_INTELLIGENCE_ATTENTION" -> Icons.Outlined.PsychologyAlt
    else -> Icons.Outlined.RateReview
}

private fun accent(type: String) = when (type) {
    "REPORT_CONFIRMED" -> FlowTeal
    "SCHEDULED_DOCUMENT_READY" -> FlowBlue
    "EMAIL_FAILED", "SCHEDULE_FAILED" -> FlowOrange
    "MAINTENANCE_INTELLIGENCE_ATTENTION" -> FlowDanger
    else -> FlowPurple
}
