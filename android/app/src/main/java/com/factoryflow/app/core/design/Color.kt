package com.factoryflow.app.core.design

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val FlowCanvas = Color(0xFFF4F6FB)
val FlowSurface = Color(0xFFFFFFFF)
val FlowTextPrimary = Color(0xFF171A2B)
val FlowTextSecondary = Color(0xFF7D8296)
val FlowDivider = Color(0xFFEEF0F6)

val FlowBlue = Color(0xFF4E7FFF)
val FlowBlueBright = Color(0xFF6D92FF)
val FlowBlueDark = Color(0xFF3355E0)
val FlowNavy = Color(0xFF2B3467)

val FlowOrange = Color(0xFFFF9A5A)
val FlowOrangeDark = Color(0xFFFF7A3D)
val FlowPurple = Color(0xFF8C6FF0)
val FlowPurpleDark = Color(0xFF7A5AE0)
val FlowTeal = Color(0xFF34D9C4)
val FlowTealDark = Color(0xFF22C3AE)
val FlowGreen = Color(0xFF34D399)
val FlowIndigo = Color(0xFF4A4E9E)
val FlowPink = Color(0xFFFF6FA8)
val FlowWarning = Color(0xFFFFB74D)
val FlowDanger = Color(0xFFE96565)

val FlowOrangeTint = Color(0xFFFFF1E6)
val FlowPurpleTint = Color(0xFFF1EDFF)
val FlowTealTint = Color(0xFFE4FBF8)
val FlowGreenTint = Color(0xFFE7FBF2)
val FlowIndigoTint = Color(0xFFECEDFB)
val FlowPinkTint = Color(0xFFFFEEF5)

val FlowCanvasDark = Color(0xFF11141B)
val FlowSurfaceDark = Color(0xFF191D27)
val FlowSurfaceElevatedDark = Color(0xFF202532)
val FlowTextPrimaryDark = Color(0xFFF5F6FA)
val FlowTextSecondaryDark = Color(0xFFA8ADBD)
val FlowDividerDark = Color(0xFF2A3040)

val FlowBlueGradient = Brush.linearGradient(listOf(FlowBlueBright, FlowBlueDark))
val FlowOrangeGradient = Brush.linearGradient(listOf(FlowWarning, FlowOrangeDark))

// Compatibility aliases keep untouched screens compiling while they are migrated separately.
val FlowPrimary = FlowBlue
val FlowPrimaryLight = FlowBlueBright
val FlowSecondary = FlowTealDark
val FlowSecondaryLight = FlowTeal
val FlowAccent = FlowPurple
val FlowAccentSoft = FlowPurpleTint
val FlowInk = FlowTextPrimary
val FlowInkSoft = FlowNavy
val FlowBackgroundLight = FlowCanvas
val FlowSurfaceLight = FlowSurface
val FlowSurfaceSoftLight = FlowDivider
val FlowBackgroundDark = FlowCanvasDark
val FlowSurfaceHighDark = FlowSurfaceElevatedDark
val FlowTextMutedLight = FlowTextSecondary
val FlowTextMutedDark = FlowTextSecondaryDark
val FlowSuccess = FlowGreen
val FlowAccentGradient = FlowBlueGradient

val FactoryFlowMagenta = FlowBlue
val FactoryFlowMagentaLight = FlowBlueBright
val FactoryFlowGreen = FlowTeal
val FactoryFlowGreenDark = FlowTealDark
val FactoryFlowInk = FlowTextPrimary
val FactoryFlowInkSoft = FlowNavy
val FactoryFlowCream = FlowCanvas
val FactoryFlowTextMuted = FlowTextSecondary
val FactoryFlowSuccess = FlowGreen
val FactoryFlowWarning = FlowWarning
val FactoryFlowDanger = FlowDanger
val FactoryFlowGradient = FlowBlueGradient
