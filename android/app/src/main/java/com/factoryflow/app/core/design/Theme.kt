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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    primary = FactoryFlowMagenta,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DFED),
    onPrimaryContainer = Color(0xFF5C0A3F),
    secondary = FactoryFlowGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE2EED9),
    onSecondaryContainer = Color(0xFF243E13),
    background = FactoryFlowCream,
    onBackground = FactoryFlowInk,
    surface = Color.White,
    onSurface = FactoryFlowInk,
    surfaceVariant = Color(0xFFF3EDE8),
    onSurfaceVariant = FactoryFlowTextMuted,
    outline = Color(0xFFD2C7CC),
    outlineVariant = Color(0xFFE9E0E3),
    error = FactoryFlowDanger,
    errorContainer = Color(0xFFF8E1DE),
    onErrorContainer = Color(0xFF641B15),
)

private val DarkColors = darkColorScheme(
    primary = FactoryFlowMagentaLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF5E1647),
    onPrimaryContainer = Color(0xFFFAD9EB),
    secondary = FactoryFlowGreen,
    onSecondary = Color(0xFF102806),
    secondaryContainer = Color(0xFF2B4D19),
    onSecondaryContainer = Color(0xFFDCEECF),
    background = FactoryFlowInk,
    onBackground = FactoryFlowCream,
    surface = FactoryFlowInkSoft,
    onSurface = FactoryFlowCream,
    surfaceVariant = Color(0xFF4A3E46),
    onSurfaceVariant = Color(0xFFD0C5CA),
    outline = Color(0xFF75686F),
    outlineVariant = Color(0xFF51454C),
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF7B2A22),
    onErrorContainer = Color(0xFFFFDAD5),
)

private val FactoryTypography = Typography(
    displaySmall = TextStyle(fontFamily = Cairo, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontFamily = Cairo, fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = Cairo, fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = Cairo, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = Cairo, fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = Inter, fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontFamily = Inter, fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = Inter, fontSize = 15.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = Inter, fontSize = 14.sp, lineHeight = 21.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = Inter, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontFamily = Inter, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontFamily = Inter, fontSize = 12.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontFamily = Inter, fontSize = 11.sp, lineHeight = 15.sp, fontWeight = FontWeight.Normal),
)

private val FactoryShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
)

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
