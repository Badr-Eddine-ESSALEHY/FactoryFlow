package com.factoryflow.app.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.DashboardRepository
import com.factoryflow.app.core.network.dto.DashboardDto
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class DashboardUiState(val loading: Boolean = true, val refreshing: Boolean = false, val data: DashboardDto? = null, val error: UiError? = null)

@HiltViewModel
class DashboardViewModel @Inject constructor(private val repository: DashboardRepository) : ViewModel() {
    private val _state = MutableStateFlow(DashboardUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null
    init { load() }
    fun load(refresh: Boolean = false) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
        _state.update { it.copy(loading = it.data == null, refreshing = refresh && it.data != null, error = null) }
        runCatching { repository.dashboard() }
            .onSuccess { data -> _state.value = DashboardUiState(data = data, loading = false) }
            .onFailure { error -> _state.update { it.copy(loading = false, refreshing = false, error = error.toUiError()) } }
        }
    }
}
