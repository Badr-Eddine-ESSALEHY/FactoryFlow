package com.factoryflow.app.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.factoryflow.app.R

val Success = FlowGreen
val Warning = FlowWarning
val Info = FlowBlue

data class FlowNavigationItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
)

@Composable
fun FlowScreen(
    modifier: Modifier = Modifier,
    applySafeDrawingInsets: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .then(
                if (applySafeDrawingInsets) {
                    Modifier.windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    )
                }
                else Modifier,
            ),
        content = content,
    )
}

@Composable
fun FlowContentSurface(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        content = content,
    )
}

@Composable
fun FlowTopBar(
    greeting: String,
    title: String,
    subtitle: String,
    initials: String,
    actionIcon: ImageVector,
    actionDescription: String,
    onAction: () -> Unit,
    onProfile: () -> Unit,
    actionLoading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Surface(
            modifier = Modifier
                .size(FlowSize.avatar)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onProfile),
            shape = CircleShape,
            color = FlowIndigoTint,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = initials,
                    color = FlowBlueDark,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.width(FlowSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(greeting, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(FlowSpacing.xs))
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(FlowSpacing.micro))
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Surface(
            modifier = Modifier
                .size(FlowSize.topAction)
                .clip(RoundedCornerShape(FlowRadius.control))
                .clickable(role = Role.Button, onClick = onAction),
            shape = RoundedCornerShape(FlowRadius.control),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = FlowElevation.control,
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (actionLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(FlowSize.topActionIcon),
                        strokeWidth = FlowSize.progressStroke,
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Icon(
                        actionIcon,
                        actionDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FlowSize.topActionIcon),
                    )
                }
            }
        }
    }
}

@Composable
fun FlowSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (action != null && onAction != null) {
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = FlowSpacing.sm, vertical = FlowSpacing.xs)) {
                Text(action, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(FlowSpacing.xs))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(FlowSize.iconSmall))
            }
        }
    }
}

@Composable
fun FlowPageHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionDescription: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            Surface(
                modifier = Modifier
                    .size(FlowSize.topAction)
                    .clip(RoundedCornerShape(FlowRadius.control))
                    .clickable(role = Role.Button, onClick = onBack),
                shape = RoundedCornerShape(FlowRadius.control),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = FlowElevation.control,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FlowSize.topActionIcon),
                    )
                }
            }
            Spacer(Modifier.width(FlowSpacing.md))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Spacer(Modifier.height(FlowSpacing.micro))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (actionIcon != null && onAction != null) {
            Spacer(Modifier.width(FlowSpacing.md))
            Surface(
                modifier = Modifier
                    .size(FlowSize.topAction)
                    .clip(RoundedCornerShape(FlowRadius.control))
                    .clickable(role = Role.Button, onClick = onAction),
                shape = RoundedCornerShape(FlowRadius.control),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = FlowElevation.control,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        actionIcon,
                        contentDescription = actionDescription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FlowSize.topActionIcon),
                    )
                }
            }
        }
    }
}

@Composable
fun FlowCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(FlowSpacing.lg),
    onClick: (() -> Unit)? = null,
    radius: Dp = FlowRadius.card,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) FlowOpacity.pressedScale else 1f,
        animationSpec = spring(FlowMotion.springDamping, FlowMotion.springStiffness),
        label = "flow-card-scale",
    )
    val shape = RoundedCornerShape(radius)
    val clickableModifier = if (onClick == null) Modifier else Modifier.clickable(interaction, null, role = Role.Button, onClick = onClick)
    Card(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = FlowElevation.card,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = FlowOpacity.cardShadow),
                spotColor = Color.Black.copy(alpha = FlowOpacity.cardShadow),
            )
            .then(clickableModifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = FlowElevation.none),
    ) {
        Box(Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun FlowIconTile(
    icon: ImageVector,
    contentDescription: String?,
    accent: Color,
    modifier: Modifier = Modifier,
    gradientEnd: Color? = null,
    size: Dp = FlowSize.iconTile,
) {
    val brush = gradientEnd?.let { Brush.linearGradient(listOf(accent, it)) }
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(FlowRadius.iconTile))
            .then(if (brush == null) Modifier.background(accent) else Modifier.background(brush)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, tint = Color.White, modifier = Modifier.size(FlowSize.icon))
    }
}

@Composable
fun FlowCategoryCard(
    icon: ImageVector,
    title: String,
    meta: String,
    accent: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    gradientEnd: Color? = null,
    onClick: (() -> Unit)? = null,
    showAction: Boolean = onClick != null,
) {
    FlowCard(
        modifier = modifier.height(FlowSize.categoryCardHeight),
        contentPadding = PaddingValues(FlowSpacing.sm),
        onClick = onClick,
    ) {
        Column(Modifier.fillMaxHeight()) {
            FlowIconTile(icon, null, accent, gradientEnd = gradientEnd)
            Spacer(Modifier.height(FlowSpacing.sm))
            Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(FlowSpacing.micro))
            Text(
                meta,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (showAction) {
                Spacer(Modifier.weight(1f))
                Surface(shape = CircleShape, color = tint, modifier = Modifier.size(FlowSize.categoryAction)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Add, null, tint = accent, modifier = Modifier.size(FlowSize.iconSmall))
                    }
                }
            }
        }
    }
}

@Composable
fun FlowListRow(
    icon: ImageVector,
    title: String,
    meta: String,
    accent: Color,
    modifier: Modifier = Modifier,
    gradientEnd: Color? = null,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    FlowCard(modifier, PaddingValues(horizontal = FlowSpacing.md, vertical = FlowSpacing.sm), onClick) {
        Row(Modifier.fillMaxWidth().heightIn(min = FlowSize.listRowMinHeight), verticalAlignment = Alignment.CenterVertically) {
            FlowIconTile(icon, null, accent, gradientEnd = gradientEnd, size = FlowSize.listIconTile)
            Spacer(Modifier.width(FlowSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(FlowSpacing.micro))
                Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (trailing != null) {
                Spacer(Modifier.width(FlowSpacing.sm))
                trailing()
            }
        }
    }
}

@Composable
fun FlowMetricBadge(label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier, color = color.copy(alpha = FlowOpacity.tint), shape = RoundedCornerShape(FlowRadius.pill)) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = FlowSpacing.sm, vertical = FlowSpacing.micro),
            color = color,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}

@Composable
fun FlowStatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    compact: Boolean = false,
) {
    Surface(modifier, color = color.copy(alpha = FlowOpacity.tint), shape = RoundedCornerShape(FlowRadius.pill)) {
        Row(
            Modifier.padding(
                horizontal = if (compact) FlowSpacing.sm else FlowSpacing.md,
                vertical = if (compact) FlowSpacing.xs else FlowSpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(FlowSize.iconSmall))
                Spacer(Modifier.width(FlowSpacing.xs))
            }
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
fun FlowProgressRing(
    progress: Float,
    color: Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    val inspection = LocalInspectionMode.current
    var entered by remember { mutableStateOf(inspection) }
    LaunchedEffect(Unit) { entered = true }
    val sweep by animateFloatAsState(
        targetValue = if (entered) progress.coerceIn(0f, 1f) else 0f,
        animationSpec = tween(FlowMotion.chart),
        label = "flow-ring",
    )
    Box(
        modifier = modifier
            .size(FlowSize.progressRing)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.outlineVariant,
            strokeWidth = FlowSize.progressStroke,
        )
        CircularProgressIndicator(
            progress = { sweep },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = FlowSize.progressStroke,
            strokeCap = StrokeCap.Round,
        )
    }
}

@Composable
fun FlowSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(FlowRadius.pill)) {
        Row(Modifier.padding(FlowSpacing.micro)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val color by animateColorAsState(
                    targetValue = if (selected) FlowBlue else Color.Transparent,
                    animationSpec = tween(FlowMotion.standard),
                    label = "flow-segment",
                )
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(FlowRadius.pill))
                        .clickable { onSelected(index) },
                    color = color,
                    shape = RoundedCornerShape(FlowRadius.pill),
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = FlowSpacing.xs, vertical = FlowSpacing.xs),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
fun FlowMiniChart(
    primaryValues: List<Float>,
    secondaryValues: List<Float>,
    description: String,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null,
    primaryColor: Color = FlowBlue,
    secondaryColor: Color = FlowGreen,
) {
    val inspection = LocalInspectionMode.current
    var entered by remember { mutableStateOf(inspection) }
    LaunchedEffect(primaryValues, secondaryValues) { entered = true }
    val reveal by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(FlowMotion.chart),
        label = "flow-chart-reveal",
    )
    Canvas(modifier.height(FlowSize.chartHeight).semantics { contentDescription = description }) {
        val allValues = primaryValues + secondaryValues
        if (allValues.isEmpty()) return@Canvas
        val minimum = allValues.minOrNull() ?: 0f
        val maximum = allValues.maxOrNull() ?: 1f
        val range = (maximum - minimum).takeIf { it > 0f } ?: 1f

        fun offsets(values: List<Float>): List<Offset> = values.mapIndexed { index, value ->
            val x = if (values.size == 1) size.width / 2f else size.width * index / values.lastIndex
            val normalized = (value - minimum) / range
            Offset(x, size.height - normalized * size.height * FlowOpacity.secondary - size.height * FlowOpacity.tint)
        }

        fun drawSeries(values: List<Float>, color: Color) {
            val points = offsets(values)
            if (points.isEmpty()) return
            val path = flowSmoothPath(points)
            if (points.size > 1) {
                val measure = PathMeasure().apply { setPath(path, false) }
                val visible = Path()
                measure.getSegment(0f, measure.length * reveal, visible, true)
                drawPath(visible, color, style = Stroke(FlowSize.chartStroke.toPx(), cap = StrokeCap.Round))
            }
            val targetIndex = selectedIndex?.coerceIn(0, points.lastIndex) ?: points.lastIndex
            val markerIndex = ((targetIndex * reveal).toInt()).coerceIn(0, points.lastIndex)
            drawCircle(color, FlowSize.chartMarker.toPx(), points[markerIndex])
            drawCircle(Color.White, FlowSize.progressStroke.toPx(), points[markerIndex])
        }

        drawSeries(primaryValues, primaryColor)
        drawSeries(secondaryValues, secondaryColor)
    }
}

@Composable
fun FlowHeroBanner(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    brush: Brush = FlowBlueGradient,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
        shape = RoundedCornerShape(FlowRadius.hero),
        shadowElevation = FlowElevation.hero,
    ) {
        Row(
            Modifier
                .background(brush)
                .padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(FlowSpacing.xs))
                Text(subtitle, color = Color.White.copy(alpha = FlowOpacity.secondary), style = MaterialTheme.typography.bodySmall)
            }
            Surface(color = Color.White.copy(alpha = FlowOpacity.tint), shape = CircleShape, modifier = Modifier.size(FlowSize.iconTileLarge)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(FlowSize.iconLarge))
                }
            }
        }
    }
}

@Composable
fun FlowFab(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) FlowOpacity.fabPressedScale else 1f,
        animationSpec = spring(FlowMotion.springDamping, FlowMotion.springStiffness),
        label = "flow-fab-scale",
    )
    Surface(
        modifier = modifier
            .size(FlowSize.fab)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .shadow(
                elevation = if (pressed) FlowElevation.fabPressed else FlowElevation.fab,
                shape = CircleShape,
                ambientColor = FlowBlue.copy(alpha = FlowOpacity.tint),
                spotColor = FlowBlue.copy(alpha = FlowOpacity.secondary),
            )
            .clip(CircleShape)
            .clickable(interaction, null, role = Role.Button, onClick = onClick),
        shape = CircleShape,
        color = FlowBlue,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.Add, contentDescription, tint = Color.White, modifier = Modifier.size(FlowSize.iconLarge))
        }
    }
}

@Composable
fun FlowBottomNavigation(
    items: List<FlowNavigationItem>,
    selectedKey: String,
    createDescription: String,
    onItemSelected: (String) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.size == 4) { "FlowBottomNavigation requires four destinations" }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(FlowSize.navigationContainer),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(FlowSize.navigationBar)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = FlowRadius.navigation, topEnd = FlowRadius.navigation),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = FlowElevation.navigation,
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = FlowSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.take(2).forEach { FlowNavigationDestination(it, selectedKey == it.key, onItemSelected) }
                Spacer(Modifier.weight(1f))
                items.drop(2).forEach { FlowNavigationDestination(it, selectedKey == it.key, onItemSelected) }
            }
        }
        FlowFab(createDescription, onCreate, Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun RowScope.FlowNavigationDestination(
    item: FlowNavigationItem,
    selected: Boolean,
    onSelected: (String) -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) FlowBlue else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(FlowMotion.fast),
        label = "flow-nav-color",
    )
    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(role = Role.Tab) { onSelected(item.key) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = item.label,
            tint = tint,
            modifier = Modifier.size(FlowSize.navigationIcon),
        )
    }
}

@Composable
fun FlowEmptyState(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    FlowCard(modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = FlowSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            FlowIconTile(icon, null, FlowBlue, gradientEnd = FlowBlueDark, size = FlowSize.iconTileLarge)
            Spacer(Modifier.height(FlowSpacing.lg))
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(FlowSpacing.sm))
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            if (action != null && onAction != null) {
                Spacer(Modifier.height(FlowSpacing.lg))
                TextButton(onClick = onAction) { Text(action) }
            }
        }
    }
}

private fun flowSmoothPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)
    for (index in 0 until points.lastIndex) {
        val current = points[index]
        val next = points[index + 1]
        val midpoint = (current.x + next.x) / 2f
        cubicTo(midpoint, current.y, midpoint, next.y, next.x, next.y)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FactoryFlowScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = topBar,
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = content,
    )
}

@Composable
fun FactoryCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(FlowSpacing.lg),
    content: @Composable BoxScope.() -> Unit,
) = FlowCard(modifier, contentPadding, content = content)

@Composable
fun FactoryIconChip(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    size: Dp = FlowSize.iconTile,
) {
    Surface(modifier.size(size), color = container, shape = RoundedCornerShape(FlowRadius.iconTile)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(FlowSize.icon))
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) =
    FlowSectionHeader(title = title, action = action, onAction = onAction)

@Composable
fun StatusPill(label: String, color: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) =
    FlowStatusPill(label, color, modifier, icon)

@Composable
fun FactoryFlowHero(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, color = Color.Transparent, shape = RoundedCornerShape(FlowRadius.hero), shadowElevation = FlowElevation.hero) {
        Column(Modifier.background(FlowBlueGradient).padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg), content = content)
    }
}

@Composable
fun LoadingPane(label: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(FlowSpacing.xxxl), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(strokeWidth = FlowSize.progressStroke, modifier = Modifier.size(FlowSpacing.xxxl), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(FlowSpacing.lg))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SkeletonRows(modifier: Modifier = Modifier, count: Int = 4) {
    val transition = rememberInfiniteTransition(label = "factoryflow-skeleton")
    val alpha by transition.animateFloat(
        initialValue = FlowOpacity.tint,
        targetValue = FlowOpacity.disabled,
        animationSpec = infiniteRepeatable(tween(FlowMotion.chart), androidx.compose.animation.core.RepeatMode.Reverse),
        label = "alpha",
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
        repeat(count) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(if (it == 0) FlowSize.categoryCardHeight else FlowSize.listRowMinHeight)
                    .alpha(alpha)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(FlowRadius.card)),
            )
        }
    }
}

@Composable
fun EmptyPane(
    title: String,
    detail: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) = FlowEmptyState(title, detail, modifier, icon, action, onAction)

@Composable
fun ErrorPane(title: String, detail: String, retry: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    FlowEmptyState(title, detail, modifier, Icons.Outlined.CloudOff, retry, onRetry)
}

@Composable
@Suppress("ModifierParameter")
fun PrimaryAction(
    label: String,
    loading: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val opacity = if (enabled && !loading) 1f else FlowOpacity.disabled
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = FlowSize.touchTarget)
            .alpha(opacity)
            .clip(RoundedCornerShape(FlowRadius.control))
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(FlowRadius.control),
        shadowElevation = if (enabled) FlowElevation.card else FlowElevation.none,
    ) {
        Box(
            Modifier.fillMaxWidth().background(FlowBlueGradient).padding(horizontal = FlowSpacing.lg, vertical = FlowSpacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = loading,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "button-state",
            ) { busy ->
                if (busy) CircularProgressIndicator(Modifier.size(FlowSize.icon), strokeWidth = FlowSize.progressStroke, color = Color.White)
                else Text(label, color = Color.White, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun FactorySegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) = FlowSegmentedControl(options, selectedIndex, onSelected, modifier)
