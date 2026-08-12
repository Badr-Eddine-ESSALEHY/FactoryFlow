package com.factoryflow.app.core.design

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

val FactoryBlue = Color(0xFF0867C9)
val Success = Color(0xFF277A52)
val Warning = Color(0xFF9A6500)
val Info = Color(0xFF3C668F)
val LightBackground = Color(0xFFF7F8FA)
val LightSurfaceMuted = Color(0xFFF0F3F6)
val DarkBackground = Color(0xFF111418)
val DarkSurface = Color(0xFF191D22)

private val LightColors = lightColorScheme(
    primary = FactoryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8E9FF),
    onPrimaryContainer = Color(0xFF00315F),
    secondary = Color(0xFF526478),
    background = LightBackground,
    onBackground = Color(0xFF171A1F),
    surface = Color.White,
    onSurface = Color(0xFF171A1F),
    surfaceVariant = LightSurfaceMuted,
    onSurfaceVariant = Color(0xFF59616B),
    outline = Color(0xFFB8C0CA),
    outlineVariant = Color(0xFFDDE2E7),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF95C4FF),
    onPrimary = Color(0xFF00315F),
    primaryContainer = Color(0xFF004987),
    onPrimaryContainer = Color(0xFFD6E8FF),
    secondary = Color(0xFFBBC8D8),
    background = DarkBackground,
    onBackground = Color(0xFFE3E6EB),
    surface = DarkSurface,
    onSurface = Color(0xFFE3E6EB),
    surfaceVariant = Color(0xFF242A31),
    onSurfaceVariant = Color(0xFFB9C1CB),
    outline = Color(0xFF7F8995),
    outlineVariant = Color(0xFF363D45),
    error = Color(0xFFFFB4AB),
)

private val FactoryTypography = androidx.compose.material3.Typography(
    displaySmall = TextStyle(fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun FactoryFlowTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
        }
    }
    MaterialTheme(colorScheme = colors, typography = FactoryTypography, content = content)
}
