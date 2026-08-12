package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*

@Composable
fun CreateHubScreen(onPaste: () -> Unit, onManual: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp, 18.dp, 20.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.create_hub_title), style = MaterialTheme.typography.headlineMedium); Spacer(Modifier.height(6.dp)); Text(stringResource(R.string.create_hub_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item { Spacer(Modifier.height(8.dp)); SectionHeader(stringResource(R.string.available_now)) }
        item { AcquisitionCard(Icons.Outlined.ContentPaste, stringResource(R.string.paste_text), stringResource(R.string.paste_description), true, onPaste) }
        item { AcquisitionCard(Icons.Outlined.EditNote, stringResource(R.string.manual_entry), stringResource(R.string.manual_description), true, onManual) }
        item { Spacer(Modifier.height(8.dp)); SectionHeader(stringResource(R.string.coming_next)) }
        item { AcquisitionCard(Icons.Outlined.Image, stringResource(R.string.import_image), stringResource(R.string.ocr_coming_soon), false, {}) }
        item { AcquisitionCard(Icons.Outlined.Share, stringResource(R.string.share_whatsapp), stringResource(R.string.ocr_coming_soon), false, {}) }
        item { AcquisitionCard(Icons.Outlined.PhotoCamera, stringResource(R.string.take_photo), stringResource(R.string.ocr_coming_soon), false, {}) }
    }
}

@Composable
private fun AcquisitionCard(icon: ImageVector, title: String, detail: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        Modifier.fillMaxWidth().then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        color = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(14.dp)) {
                Icon(icon, null, Modifier.padding(12.dp).size(24.dp), tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(3.dp)); Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            if (enabled) Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) else StatusPill(stringResource(R.string.coming_next), MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
