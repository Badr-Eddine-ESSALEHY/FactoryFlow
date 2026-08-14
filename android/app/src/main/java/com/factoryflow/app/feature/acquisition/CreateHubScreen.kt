package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*

@Composable
fun CreateHubScreen(onPaste: () -> Unit, onManual: () -> Unit) {
    FlowScreen {
        CreateHubContent(onPaste, onManual, Modifier.weight(1f))
    }
}

@Composable
fun CreateHubContent(onPaste: () -> Unit, onManual: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.fillMaxSize(),
        contentPadding = PaddingValues(FlowSpacing.xl, FlowSpacing.lg, FlowSpacing.xl, 110.dp),
        verticalArrangement = Arrangement.spacedBy(FlowSpacing.md),
    ) {
        item { FlowPageHeader(stringResource(R.string.create_hub_title), subtitle = stringResource(R.string.create_hub_subtitle)) }
        item { FlowSectionHeader(stringResource(R.string.available_now)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                FlowCategoryCard(Icons.Outlined.ContentPaste, stringResource(R.string.dashboard_paste_title), stringResource(R.string.dashboard_paste_meta), FlowOrange, FlowOrangeTint, Modifier.weight(1f), onClick = onPaste)
                FlowCategoryCard(Icons.Outlined.EditNote, stringResource(R.string.dashboard_manual_title), stringResource(R.string.dashboard_manual_meta), FlowPurple, FlowPurpleTint, Modifier.weight(1f), onClick = onManual)
                FlowCategoryCard(Icons.Outlined.Image, stringResource(R.string.import_image), stringResource(R.string.coming_next), FlowTeal, FlowTealTint, Modifier.weight(1f), showAction = false)
            }
        }
        item { FlowSectionHeader(stringResource(R.string.coming_next)) }
        item {
            FlowListRow(Icons.Outlined.PhotoCamera, stringResource(R.string.take_photo), stringResource(R.string.ocr_coming_soon), FlowTeal, Modifier.fillMaxWidth(), trailing = { FlowStatusPill(stringResource(R.string.coming_next), FlowTealDark, compact = true) })
        }
        item {
            FlowListRow(Icons.Outlined.Share, stringResource(R.string.share_whatsapp), stringResource(R.string.ocr_coming_soon), FlowIndigo, Modifier.fillMaxWidth(), trailing = { FlowStatusPill(stringResource(R.string.coming_next), FlowIndigo, compact = true) })
        }
    }
}
