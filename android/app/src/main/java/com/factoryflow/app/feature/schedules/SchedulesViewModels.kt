package com.factoryflow.app.feature.schedules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.SchedulesRepository
import com.factoryflow.app.core.network.dto.*
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import com.factoryflow.app.core.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchedulesUiState(val loading: Boolean = true, val schedules: List<ReportScheduleDto> = emptyList(), val togglingId: Long? = null, val error: UiError? = null)
@HiltViewModel
class SchedulesViewModel @Inject constructor(private val repository: SchedulesRepository) : ViewModel() {
    private val _state = MutableStateFlow(SchedulesUiState()); val state = _state.asStateFlow()
    init { load() }
    fun load() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { repository.list() }.onSuccess { _state.value = SchedulesUiState(false, it) }
            .onFailure { _state.value = SchedulesUiState(false, error = it.toUiError()) }
    }
    fun toggle(schedule: ReportScheduleDto) = viewModelScope.launch {
        _state.update { it.copy(togglingId = schedule.id) }
        runCatching { repository.setEnabled(schedule.id, !schedule.enabled) }
            .onSuccess { updated -> _state.update { state -> state.copy(togglingId = null, schedules = state.schedules.map { if (it.id == updated.id) updated else it }) } }
            .onFailure { error -> _state.update { it.copy(togglingId = null, error = error.toUiError()) } }
    }
}

data class ScheduleFormUiState(
    val loading: Boolean = true, val id: Long? = null, val type: String = "DAILY", val time: String = "08:00",
    val dayOfWeek: String? = "MONDAY", val excel: Boolean = true, val pdf: Boolean = false,
    val emailEnabled: Boolean = false, val recipients: String = "", val enabled: Boolean = true,
    val runs: List<ScheduleRunDto> = emptyList(), val saving: Boolean = false, val formatError: Boolean = false,
    val recipientError: Boolean = false, val error: UiError? = null,
)

@HiltViewModel
class ScheduleFormViewModel @Inject constructor(savedState: SavedStateHandle, private val repository: SchedulesRepository) : ViewModel() {
    private val routeId = savedState.get<String>("scheduleId")?.toLongOrNull()?.takeIf { it > 0 }
    private val _state = MutableStateFlow(ScheduleFormUiState(loading = routeId != null, id = routeId)); val state = _state.asStateFlow()
    init { if (routeId != null) load(routeId) }
    private fun load(id: Long) = viewModelScope.launch {
        runCatching { val detail = async { repository.detail(id) }; val runs = async { repository.runs(id) }; detail.await() to runs.await() }
            .onSuccess { (schedule, runs) -> _state.value = ScheduleFormUiState(false, schedule.id, schedule.type, schedule.time.take(5), schedule.dayOfWeek, schedule.generateExcel, schedule.generatePdf, schedule.emailEnabled, schedule.recipients.joinToString(", "), schedule.enabled, runs.content) }
            .onFailure { error -> _state.update { it.copy(loading = false, error = error.toUiError()) } }
    }
    fun type(value: String) = _state.update { it.copy(type = value, dayOfWeek = if (value == "WEEKLY") it.dayOfWeek ?: "MONDAY" else null) }
    fun time(value: String) = _state.update { it.copy(time = value) }
    fun day(value: String) = _state.update { it.copy(dayOfWeek = value) }
    fun excel(value: Boolean) = _state.update { it.copy(excel = value, formatError = false) }
    fun pdf(value: Boolean) = _state.update { it.copy(pdf = value, formatError = false) }
    fun email(value: Boolean) = _state.update { it.copy(emailEnabled = value, recipientError = false) }
    fun recipients(value: String) = _state.update { it.copy(recipients = value, recipientError = false) }
    fun enabled(value: Boolean) = _state.update { it.copy(enabled = value) }
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        val recipients = current.recipients.split(',').map(String::trim).filter(String::isNotBlank)
        val formatError = !current.excel && !current.pdf
        val recipientError = current.emailEnabled && (recipients.isEmpty() || recipients.any { !it.isValidEmail() })
        if (formatError || recipientError) { _state.update { it.copy(formatError = formatError, recipientError = recipientError) }; return }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            val request = ReportScheduleRequest(current.type, "${current.time}:00", if (current.type == "WEEKLY") current.dayOfWeek else null, generateExcel = current.excel, generatePdf = current.pdf, emailEnabled = current.emailEnabled, recipients = if (current.emailEnabled) recipients else emptyList(), enabled = current.enabled)
            runCatching { current.id?.let { repository.update(it, request) } ?: repository.create(request) }
                .onSuccess { _state.update { it.copy(saving = false) }; onSaved() }
                .onFailure { error -> _state.update { it.copy(saving = false, error = error.toUiError()) } }
        }
    }
}
