package com.factoryflow.app.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.NotificationsRepository
import com.factoryflow.app.core.network.dto.NotificationDto
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(val loading: Boolean = true, val items: List<NotificationDto> = emptyList(), val error: UiError? = null)

@HiltViewModel
class NotificationsViewModel @Inject constructor(private val repository: NotificationsRepository) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsUiState())
    val state = _state.asStateFlow()
    init { load() }
    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { repository.list() }.onSuccess { items -> _state.value = NotificationsUiState(false, items) }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }
    fun read(id: Long) = viewModelScope.launch {
        runCatching { repository.markRead(id) }.onSuccess { updated ->
            _state.update { state -> state.copy(items = state.items.map { if (it.id == id) updated else it }) }
        }
    }
}
