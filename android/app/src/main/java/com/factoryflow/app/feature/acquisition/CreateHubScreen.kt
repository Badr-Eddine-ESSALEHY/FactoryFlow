package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*

@Composable
fun CreateHubScreen(onPaste: () -> Unit, onManual: () -> Unit, onGallery: () -> Unit) {
    FlowScreen {
        CreateHubContent(onPaste, onManual, onGallery, Modifier.weight(1f))
    }
}

@Composable
fun CreateHubContent(onPaste: () -> Unit, onManual: () -> Unit, onGallery: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(horizontal = FlowSpacing.xl, vertical = FlowSpacing.lg),
    ) {
        FlowPageHeader(
            stringResource(R.string.create_hub_title),
            subtitle = stringResource(R.string.create_hub_subtitle),
        )
        LazyColumn(
            Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(vertical = FlowSpacing.md),
            verticalArrangement = Arrangement.Center,
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(FlowSpacing.md)) {
                    FlowSectionHeader(stringResource(R.string.available_now))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FlowSpacing.sm)) {
                        FlowCategoryCard(Icons.Outlined.ContentPaste, stringResource(R.string.dashboard_paste_title), stringResource(R.string.dashboard_paste_meta), FlowOrange, FlowOrangeTint, Modifier.weight(1f), onClick = onPaste)
                        FlowCategoryCard(Icons.Outlined.EditNote, stringResource(R.string.dashboard_manual_title), stringResource(R.string.dashboard_manual_meta), FlowPurple, FlowPurpleTint, Modifier.weight(1f), onClick = onManual)
                        FlowCategoryCard(Icons.Outlined.Image, stringResource(R.string.import_image), stringResource(R.string.advanced_ocr), FlowTeal, FlowTealTint, Modifier.weight(1f), onClick = onGallery)
                    }
                    Spacer(Modifier.height(FlowSpacing.sm))
                    FlowSectionHeader(stringResource(R.string.share_into_factoryflow))
                    FlowListRow(
                        Icons.Outlined.Share,
                        stringResource(R.string.share_whatsapp),
                        stringResource(R.string.share_supported_detail),
                        FlowIndigo,
                        Modifier.fillMaxWidth(),
                        trailing = { FlowStatusPill(stringResource(R.string.available_now), FlowIndigo, compact = true) },
                    )
                }
            }
        }
    }
}
