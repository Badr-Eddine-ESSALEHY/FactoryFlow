package com.factoryflow.app.feature.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*

@Composable
fun NotificationsScreen() {
    FlowScreen {
        NotificationsContent(Modifier.weight(1f))
    }
}

@Composable
fun NotificationsContent(modifier: Modifier = Modifier) {
    FlowContentSurface(modifier) {
        Column(Modifier.fillMaxSize().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg)) {
            FlowPageHeader(stringResource(R.string.notifications_title))
            Spacer(Modifier.height(FlowSpacing.lg))
            FlowEmptyState(stringResource(R.string.no_notifications), stringResource(R.string.no_notifications_detail), icon = Icons.Outlined.NotificationsNone)
        }
    }
}
