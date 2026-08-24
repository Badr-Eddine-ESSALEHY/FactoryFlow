package com.factoryflow.app.core.design

import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object FlowSpacing {
    val none = 0.dp
    val micro = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
    val screenBottom = 32.dp
}

object FlowRadius {
    val compactCard = 20.dp
    val card = 22.dp
    val hero = 24.dp
    val iconTile = 13.dp
    val control = 14.dp
    val navigation = 26.dp
    val pill = 100.dp
}

object FlowSize {
    val hairline = 1.dp
    val iconSmall = 14.dp
    val icon = 20.dp
    val iconLarge = 24.dp
    val iconTile = 40.dp
    val iconTileLarge = 46.dp
    val listIconTile = 38.dp
    val avatar = 34.dp
    val topAction = 38.dp
    val topActionIcon = 18.dp
    val touchTarget = 48.dp
    val situationCardHeight = 114.dp
    val categoryCardHeight = 128.dp
    val listRowMinHeight = 42.dp
    val categoryAction = 22.dp
    val progressRing = 42.dp
    val progressStroke = 3.dp
    val chartHeight = 108.dp
    val analyticsChartHeight = 220.dp
    val chartStroke = 3.dp
    val chartMarker = 4.dp
    val fab = 58.dp
    val navigationIcon = 22.dp
    val navigationBar = 60.dp
    val navigationContainer = 70.dp
}

object FlowElevation {
    val none = 0.dp
    val control = 4.dp
    val card = 8.dp
    val hero = 10.dp
    val navigation = 12.dp
    val fab = 14.dp
    val fabPressed = 7.dp
}

object FlowOpacity {
    const val cardShadow = 0.08f
    const val navShadow = 0.08f
    const val tint = 0.12f
    const val secondary = 0.76f
    const val disabled = 0.48f
    const val pressedScale = 0.97f
    const val fabPressedScale = 0.95f
}

object FlowMotion {
    const val fast = 140
    const val standard = 220
    const val chart = 650
    const val springDamping = Spring.DampingRatioMediumBouncy
    const val springStiffness = Spring.StiffnessMedium
}

object FlowType {
    val display = 32.sp
    val displayLine = 38.sp
    val screen = 26.sp
    val screenLine = 32.sp
    val metric = 24.sp
    val metricLine = 30.sp
    val screenCompact = 22.sp
    val screenCompactLine = 28.sp
    val section = 17.sp
    val sectionLine = 23.sp
    val card = 15.sp
    val cardLine = 21.sp
    val row = 14.sp
    val rowLine = 20.sp
    val body = 14.sp
    val bodyLine = 21.sp
    val bodyCompact = 13.sp
    val bodyCompactLine = 19.sp
    val meta = 12.sp
    val metaLine = 18.sp
    val label = 14.sp
    val labelLine = 20.sp
    val labelCompact = 12.sp
    val labelCompactLine = 17.sp
    val micro = 11.sp
    val microLine = 15.sp
}

object FactorySpacing {
    val xs = FlowSpacing.xs
    val sm = FlowSpacing.sm
    val md = FlowSpacing.md
    val lg = FlowSpacing.lg
    val xlg = FlowSpacing.xl
    val xl = FlowSpacing.xxl
    val xxl = FlowSpacing.xxxl
}

object FactoryRadius {
    val card = FlowRadius.card
    val control = FlowRadius.control
    val pill = FlowRadius.pill
}
