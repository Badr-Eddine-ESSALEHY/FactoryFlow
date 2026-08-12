package com.factoryflow.app.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.StatisticsRepository
import com.factoryflow.app.core.network.dto.KpiStatisticsDto
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatisticsUiState(
    val loading: Boolean = true, val days: Int = 30, val kpis: List<KpiStatisticsDto> = emptyList(),
    val selectedKpiId: Long? = null, val error: UiError? = null,
) { val selected get() = kpis.firstOrNull { it.kpiDefinitionId == selectedKpiId } ?: kpis.firstOrNull() }

@HiltViewModel
class StatisticsViewModel @Inject constructor(private val repository: StatisticsRepository) : ViewModel() {
    private val _state = MutableStateFlow(StatisticsUiState()); val state = _state.asStateFlow()
    private var request: Job? = null
    init { load() }
    fun days(value: Int) { _state.update { it.copy(days = value) }; load() }
    fun select(id: Long) = _state.update { it.copy(selectedKpiId = id) }
    fun load() {
        request?.cancel(); request = viewModelScope.launch {
            val today = LocalDate.now(); val from = today.minusDays(_state.value.days.toLong() - 1)
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.statistics(null, from.toString(), today.toString()) }
                .onSuccess { data -> _state.update { state -> state.copy(loading = false, kpis = data.kpis, selectedKpiId = state.selectedKpiId?.takeIf { id -> data.kpis.any { it.kpiDefinitionId == id } } ?: data.kpis.firstOrNull()?.kpiDefinitionId) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
        }
    }
}
