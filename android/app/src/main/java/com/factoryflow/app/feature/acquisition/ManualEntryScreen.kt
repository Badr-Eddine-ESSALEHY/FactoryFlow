package com.factoryflow.app.feature.acquisition

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.factoryflow.app.R
import com.factoryflow.app.core.design.*
import com.factoryflow.app.core.network.dto.KpiDefinitionDto

@Composable
fun ManualEntryScreen(onBack: () -> Unit, onReview: (Long) -> Unit, viewModel: ManualEntryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selecting by remember { mutableStateOf(false) }
    Scaffold(topBar = { FocusedTopBar(stringResource(R.string.manual_title), onBack) }) { padding ->
        when {
            state.loading -> LoadingPane(stringResource(R.string.loading), Modifier.padding(padding))
            state.error != null && state.definitions.isEmpty() -> ErrorPane(stringResource(state.error!!.title), stringResource(state.error!!.detail), stringResource(R.string.retry), viewModel::load, Modifier.padding(padding))
            else -> Column(Modifier.fillMaxSize().padding(padding).imePadding()) {
                LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Text(stringResource(R.string.manual_description), color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)) }
                    item {
                        OutlinedTextField(state.effectiveDate, viewModel::date, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.effective_date)) }, leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) }, singleLine = true, shape = RoundedCornerShape(14.dp))
                    }
                    items(state.entries, key = { it.definition.id }) { row -> ManualRow(row.definition, row.value, { viewModel.value(row.definition.id, it) }, { viewModel.remove(row.definition.id) }) }
                    item {
                        OutlinedButton(onClick = { selecting = true }, Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.add_kpi)) }
                        if (state.selectionError) Text(stringResource(R.string.no_kpi_selected), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
                Surface(shadowElevation = 8.dp) { Column(Modifier.navigationBarsPadding().padding(16.dp)) {
                    state.error?.let { Text(stringResource(it.detail), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 8.dp)) }
                    PrimaryAction(stringResource(R.string.continue_review), state.submitting, onClick = { viewModel.submit(onReview) })
                } }
            }
        }
    }
    if (selecting) KpiPicker(state.definitions, state.query, viewModel::query, { viewModel.add(it); selecting = false }, { selecting = false })
}

@Composable
private fun ManualRow(definition: KpiDefinitionDto, value: String, onValue: (String) -> Unit, onRemove: () -> Unit) {
    FactoryCard(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(definition.displayName, style = MaterialTheme.typography.titleMedium); definition.category?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium) } }
                IconButton(onClick = onRemove) { Icon(Icons.Outlined.Close, stringResource(R.string.remove_entry)) }
            }
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value, onValue, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.value)) }, suffix = { definition.unit?.let { Text(it) } }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), supportingText = { if (value.isBlank()) Text(stringResource(R.string.value_missing)) }, shape = RoundedCornerShape(13.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KpiPicker(definitions: List<KpiDefinitionDto>, query: String, onQuery: (String) -> Unit, onSelect: (KpiDefinitionDto) -> Unit, onDismiss: () -> Unit) {
    val filtered = definitions.filter { it.displayName.contains(query, true) || it.code.contains(query, true) }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text(stringResource(R.string.add_kpi), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(14.dp)); OutlinedTextField(query, onQuery, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.search_kpi)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, shape = RoundedCornerShape(14.dp))
            Spacer(Modifier.height(12.dp)); LazyColumn(Modifier.fillMaxWidth().heightIn(max = 440.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                items(filtered, key = { it.id }) { definition ->
                    Row(Modifier.fillMaxWidth().clickable { onSelect(definition) }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(definition.displayName, style = MaterialTheme.typography.titleMedium); Text(listOfNotNull(definition.category, definition.unit).joinToString(" · "), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        Icon(Icons.Outlined.AddCircleOutline, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}
