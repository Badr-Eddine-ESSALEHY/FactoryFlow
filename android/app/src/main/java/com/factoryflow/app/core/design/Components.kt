package com.factoryflow.app.core.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val Success = FactoryFlowSuccess
val Warning = FactoryFlowWarning
val Info = FactoryFlowMagenta

object FactorySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xlg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
}

object FactoryRadius {
    val card = 22.dp
    val control = 13.dp
    val pill = 100.dp
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
    contentPadding: PaddingValues = PaddingValues(FactorySpacing.lg),
    content: @Composable BoxScope.() -> Unit,
) {
    Card(
        modifier = modifier.shadow(14.dp, RoundedCornerShape(FactoryRadius.card), ambientColor = Color.Black.copy(alpha = 0.035f), spotColor = Color.Black.copy(alpha = 0.055f)),
        shape = RoundedCornerShape(FactoryRadius.card),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun FactoryIconChip(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    size: Dp = 42.dp,
) {
    Surface(modifier.size(size), color = container, shape = RoundedCornerShape(FactoryRadius.control)) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(size * .52f))
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, maxLines = 1) }
        }
    }
}

@Composable
fun StatusPill(label: String, color: Color, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    Surface(modifier, color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(FactoryRadius.pill)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(5.dp))
            }
            Text(label, color = color, style = MaterialTheme.typography.labelMedium, maxLines = 1)
        }
    }
}

@Composable
fun FactoryFlowHero(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier, color = Color.Transparent, shape = RoundedCornerShape(FactoryRadius.card), shadowElevation = 10.dp) {
        Column(
            Modifier.background(FactoryFlowGradient).padding(horizontal = 20.dp, vertical = 18.dp),
            content = content,
        )
    }
}

@Composable
fun LoadingPane(label: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(FactorySpacing.xxl), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(30.dp), color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(FactorySpacing.lg))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SkeletonRows(modifier: Modifier = Modifier, count: Int = 4) {
    val transition = rememberInfiniteTransition(label = "factoryflow-skeleton")
    val alpha by transition.animateFloat(0.34f, 0.72f, infiniteRepeatable(tween(850), RepeatMode.Reverse), label = "alpha")
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) {
            Box(
                Modifier.fillMaxWidth().height(if (it == 0) 128.dp else 78.dp).alpha(alpha)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(FactoryRadius.card)),
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
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 42.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FactoryIconChip(icon, null, size = 58.dp, tint = MaterialTheme.colorScheme.secondary, container = MaterialTheme.colorScheme.secondaryContainer)
        Spacer(Modifier.height(18.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(7.dp))
        Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        if (action != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            OutlinedButton(onClick = onAction, shape = RoundedCornerShape(FactoryRadius.control)) { Text(action) }
        }
    }
}

@Composable
fun ErrorPane(title: String, detail: String, retry: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    EmptyPane(title, detail, modifier, Icons.Outlined.CloudOff, retry, onRetry)
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
    val opacity = if (enabled && !loading) 1f else .48f
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = 54.dp).alpha(opacity)
            .clip(RoundedCornerShape(FactoryRadius.control))
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(FactoryRadius.control),
        shadowElevation = if (enabled) 8.dp else 0.dp,
    ) {
        Box(
            Modifier.fillMaxWidth().background(FactoryFlowGradient).padding(horizontal = 18.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(loading, transitionSpec = { spring<Float>().let { fadeIn() togetherWith fadeOut() } }, label = "button-state") { busy ->
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
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
) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(FactoryRadius.pill)) {
        Row(Modifier.padding(4.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                val color by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    spring(), label = "segment",
                )
                Surface(
                    Modifier.weight(1f).clip(RoundedCornerShape(FactoryRadius.pill)).clickable { onSelected(index) },
                    color = color,
                    shape = RoundedCornerShape(FactoryRadius.pill),
                    shadowElevation = if (selected) 5.dp else 0.dp,
                ) {
                    Text(
                        label,
                        Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
