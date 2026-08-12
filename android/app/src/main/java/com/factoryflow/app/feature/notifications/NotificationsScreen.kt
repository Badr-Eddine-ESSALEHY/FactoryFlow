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
import com.factoryflow.app.core.design.EmptyPane

@Composable
fun NotificationsScreen() {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 18.dp)) {
        Text(stringResource(R.string.notifications_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(18.dp))
        EmptyPane(stringResource(R.string.no_notifications), stringResource(R.string.no_notifications_detail), icon = Icons.Outlined.NotificationsNone)
    }
}
