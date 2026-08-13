package com.factoryflow.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.factoryflow.app.BuildConfig
import com.factoryflow.app.R
import com.factoryflow.app.core.design.FactoryCard
import com.factoryflow.app.core.design.FactoryFlowGradient
import com.factoryflow.app.core.design.FactoryRadius
import com.factoryflow.app.core.design.FactorySpacing
import com.factoryflow.app.core.design.ThemeMode
import com.factoryflow.app.core.network.dto.UserDto

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ProfileScreen(
    user: UserDto,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    var confirmLogout by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FactoryCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box(
                        Modifier.size(62.dp).background(FactoryFlowGradient, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(user.name.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    }
                    Spacer(Modifier.size(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(user.name, style = MaterialTheme.typography.titleLarge)
                        Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.maintenance_engineer), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
            FactoryCard(Modifier.fillMaxWidth()) {
                Column {
                    ThemeChoice(ThemeMode.SYSTEM, themeMode, Icons.Outlined.SettingsBrightness, stringResource(R.string.theme_system), onThemeMode)
                    ThemeChoice(ThemeMode.LIGHT, themeMode, Icons.Outlined.LightMode, stringResource(R.string.theme_light), onThemeMode)
                    ThemeChoice(ThemeMode.DARK, themeMode, Icons.Outlined.DarkMode, stringResource(R.string.theme_dark), onThemeMode)
                }
            }

            FactoryCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(stringResource(R.string.about_factoryflow), style = MaterialTheme.typography.titleSmall)
                        Text(stringResource(R.string.version_format, BuildConfig.VERSION_NAME), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Logout, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            icon = { Icon(Icons.Outlined.Logout, null) },
            title = { Text(stringResource(R.string.logout_confirm_title)) },
            text = { Text(stringResource(R.string.logout_confirm_message)) },
            confirmButton = { Button(onClick = onLogout) { Text(stringResource(R.string.logout)) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun ThemeChoice(
    mode: ThemeMode,
    selected: ThemeMode,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onSelected: (ThemeMode) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = if (mode == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, Modifier.padding(start = 12.dp).weight(1f), style = MaterialTheme.typography.bodyLarge)
        RadioButton(selected = mode == selected, onClick = { onSelected(mode) })
    }
}
