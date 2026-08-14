package com.factoryflow.app.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
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
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.UserDto
import com.factoryflow.app.feature.acquisition.FocusedTopBar

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
    FactoryFlowScaffold(
        topBar = { FocusedTopBar(stringResource(R.string.profile), onBack) },
    ) { padding ->
        ProfileContent(user, themeMode, onThemeMode, { confirmLogout = true }, Modifier.padding(padding))
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            icon = { Icon(Icons.AutoMirrored.Outlined.Logout, null) },
            title = { Text(stringResource(R.string.logout_confirm_title)) },
            text = { Text(stringResource(R.string.logout_confirm_message)) },
            confirmButton = { Button(onClick = onLogout) { Text(stringResource(R.string.logout)) } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
fun ProfileContent(
    user: UserDto,
    themeMode: ThemeMode,
    onThemeMode: (ThemeMode) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowContentSurface(modifier) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
        ) {
            FlowCard(Modifier.fillMaxWidth(), PaddingValues(FlowSpacing.lg)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.layout.Box(
                        Modifier.size(48.dp).background(FlowBlueGradient, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(user.name.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.size(FlowSpacing.md))
                    Column(Modifier.weight(1f)) {
                        Text(user.name, style = MaterialTheme.typography.titleLarge)
                        Text(user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.maintenance_engineer), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Text(stringResource(R.string.appearance), style = MaterialTheme.typography.titleMedium)
            FlowCard(Modifier.fillMaxWidth(), PaddingValues(horizontal = FlowSpacing.md, vertical = FlowSpacing.xs)) {
                Column {
                    ThemeChoice(ThemeMode.SYSTEM, themeMode, Icons.Outlined.SettingsBrightness, stringResource(R.string.theme_system), onThemeMode)
                    ThemeChoice(ThemeMode.LIGHT, themeMode, Icons.Outlined.LightMode, stringResource(R.string.theme_light), onThemeMode)
                    ThemeChoice(ThemeMode.DARK, themeMode, Icons.Outlined.DarkMode, stringResource(R.string.theme_dark), onThemeMode)
                }
            }

            FlowListRow(
                Icons.Outlined.Info,
                stringResource(R.string.about_factoryflow),
                stringResource(R.string.version_format, BuildConfig.VERSION_NAME),
                FlowBlue,
                Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Outlined.Logout, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.logout), color = MaterialTheme.colorScheme.error)
            }
        }
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
        Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlowIconTile(icon, null, if (mode == selected) FlowBlue else MaterialTheme.colorScheme.onSurfaceVariant, size = 34.dp)
        Text(label, Modifier.padding(start = FlowSpacing.md).weight(1f), style = MaterialTheme.typography.bodyMedium)
        RadioButton(selected = mode == selected, onClick = { onSelected(mode) })
    }
}
