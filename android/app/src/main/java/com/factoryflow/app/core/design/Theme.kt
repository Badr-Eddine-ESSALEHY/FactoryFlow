package com.factoryflow.app.core.design

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat
import com.factoryflow.app.R

enum class ThemeMode { SYSTEM, LIGHT, DARK }

object ThemePreferences {
    private const val FILE = "factoryflow_display"
    private const val KEY = "theme_mode"

    fun read(context: Context): ThemeMode = runCatching {
        ThemeMode.valueOf(context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getString(KEY, null) ?: "SYSTEM")
    }.getOrDefault(ThemeMode.SYSTEM)

    fun write(context: Context, mode: ThemeMode) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putString(KEY, mode.name).apply()
    }
}

private val Inter = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal),
    Font(R.font.inter_variable, FontWeight.Medium),
    Font(R.font.inter_variable, FontWeight.SemiBold),
    Font(R.font.inter_variable, FontWeight.Bold),
)

private val Cairo = FontFamily(
    Font(R.font.cairo_variable, FontWeight.SemiBold),
    Font(R.font.cairo_variable, FontWeight.Bold),
)

private val LightColors = lightColorScheme(
    primary = FlowBlue,
    onPrimary = Color.White,
    primaryContainer = FlowIndigoTint,
    onPrimaryContainer = FlowNavy,
    secondary = FlowTealDark,
    onSecondary = Color.White,
    secondaryContainer = FlowTealTint,
    onSecondaryContainer = FlowNavy,
    background = FlowCanvas,
    onBackground = FlowTextPrimary,
    surface = FlowSurface,
    onSurface = FlowTextPrimary,
    surfaceVariant = FlowDivider,
    onSurfaceVariant = FlowTextSecondary,
    outline = FlowTextSecondary,
    outlineVariant = FlowDivider,
    error = FlowDanger,
    errorContainer = FlowPinkTint,
    onErrorContainer = FlowDanger,
)

private val DarkColors = darkColorScheme(
    primary = FlowBlueBright,
    onPrimary = Color.White,
    primaryContainer = FlowNavy,
    onPrimaryContainer = FlowTextPrimaryDark,
    secondary = FlowTeal,
    onSecondary = FlowCanvasDark,
    secondaryContainer = FlowSurfaceElevatedDark,
    onSecondaryContainer = FlowTextPrimaryDark,
    background = FlowCanvasDark,
    onBackground = FlowTextPrimaryDark,
    surface = FlowSurfaceDark,
    onSurface = FlowTextPrimaryDark,
    surfaceVariant = FlowSurfaceElevatedDark,
    onSurfaceVariant = FlowTextSecondaryDark,
    outline = FlowTextSecondaryDark,
    outlineVariant = FlowDividerDark,
    error = FlowDanger,
    errorContainer = FlowSurfaceElevatedDark,
    onErrorContainer = FlowPinkTint,
)

private val FactoryTypography = Typography(
    displaySmall = TextStyle(fontFamily = Cairo, fontSize = FlowType.display, lineHeight = FlowType.displayLine, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = Cairo, fontSize = FlowType.screen, lineHeight = FlowType.screenLine, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = Inter, fontSize = FlowType.metric, lineHeight = FlowType.metricLine, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = Inter, fontSize = FlowType.screenCompact, lineHeight = FlowType.screenCompactLine, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = Inter, fontSize = FlowType.section, lineHeight = FlowType.sectionLine, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = Inter, fontSize = FlowType.card, lineHeight = FlowType.cardLine, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = Inter, fontSize = FlowType.row, lineHeight = FlowType.rowLine, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = FlowType.body, lineHeight = FlowType.bodyLine, fontWeight = FontWeight.Medium),
    bodyMedium = TextStyle(fontFamily = Inter, fontSize = FlowType.bodyCompact, lineHeight = FlowType.bodyCompactLine, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = Inter, fontSize = FlowType.meta, lineHeight = FlowType.metaLine, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = Inter, fontSize = FlowType.label, lineHeight = FlowType.labelLine, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = Inter, fontSize = FlowType.labelCompact, lineHeight = FlowType.labelCompactLine, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = Inter, fontSize = FlowType.micro, lineHeight = FlowType.microLine, fontWeight = FontWeight.Normal),
)

private val FactoryShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(FlowRadius.iconTile),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(FlowRadius.control),
    large = androidx.compose.foundation.shape.RoundedCornerShape(FlowRadius.card),
)

// The color setters remain necessary for the supported pre-edge-to-edge Android versions.
@Suppress("DEPRECATION")
@Composable
fun FactoryFlowTheme(mode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) window.isNavigationBarContrastEnforced = false
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = FactoryTypography,
        shapes = FactoryShapes,
        content = content,
    )
}
