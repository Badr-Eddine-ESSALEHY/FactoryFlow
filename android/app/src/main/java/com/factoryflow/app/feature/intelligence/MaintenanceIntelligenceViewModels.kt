package com.factoryflow.app.feature.intelligence

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.MaintenanceIntelligenceRepository
import com.factoryflow.app.core.model.IntelligenceAlert
import com.factoryflow.app.core.model.IntelligenceAnalysis
import com.factoryflow.app.core.model.IntelligenceOverview
import com.factoryflow.app.core.network.AppError
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface IntelligenceOverviewUiState {
    data object Loading : IntelligenceOverviewUiState
    data class Content(val overview: IntelligenceOverview, val refreshing: Boolean = false) : IntelligenceOverviewUiState
    data class Error(val error: UiError) : IntelligenceOverviewUiState
}

@HiltViewModel
class IntelligenceOverviewViewModel @Inject constructor(
    private val repository: MaintenanceIntelligenceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<IntelligenceOverviewUiState>(IntelligenceOverviewUiState.Loading)
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    fun load() {
        loadJob?.cancel()
        val retained = (_state.value as? IntelligenceOverviewUiState.Content)?.overview
        _state.value = retained?.let { IntelligenceOverviewUiState.Content(it, refreshing = true) }
            ?: IntelligenceOverviewUiState.Loading
        loadJob = viewModelScope.launch {
            runCatchingCancellable { repository.overview() }
                .onSuccess { _state.value = IntelligenceOverviewUiState.Content(it) }
                .onFailure { failure ->
                    _state.value = retained?.let { IntelligenceOverviewUiState.Content(it) }
                        ?: IntelligenceOverviewUiState.Error(failure.toUiError())
                }
        }
    }
}

enum class IntelligenceWorkspacePage { OVERVIEW, ANOMALIES, FORECAST, TREND, QUALITY }

data class KpiIntelligenceUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val analysis: IntelligenceAnalysis? = null,
    val alerts: List<IntelligenceAlert> = emptyList(),
    val page: IntelligenceWorkspacePage = IntelligenceWorkspacePage.OVERVIEW,
    val error: UiError? = null,
    val noAnalysis: Boolean = false,
    val alertsUnavailable: Boolean = false,
)

@HiltViewModel
class KpiIntelligenceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MaintenanceIntelligenceRepository,
) : ViewModel() {
    private val kpiId: Long = checkNotNull(savedStateHandle["kpiId"])
    private val _state = MutableStateFlow(KpiIntelligenceUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init { load() }

    fun selectPage(page: IntelligenceWorkspacePage) = _state.update { it.copy(page = page) }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.update { it.copy(loading = it.analysis == null, error = null, noAnalysis = false) }
            val analysisRequest = async { runCatchingCancellable { repository.detail(kpiId) } }
            val alertsRequest = async { runCatchingCancellable { repository.alerts(kpiId = kpiId).content } }
            val analysis = analysisRequest.await()
            val alerts = alertsRequest.await()
            analysis.onSuccess { value ->
                _state.update {
                    it.copy(
                        loading = false,
                        analysis = value,
                        alerts = alerts.getOrDefault(it.alerts),
                        alertsUnavailable = alerts.isFailure,
                    )
                }
            }.onFailure { failure ->
                val noAnalysis = failure is AppError.Server && failure.code == "INTELLIGENCE_ANALYSIS_NOT_FOUND"
                _state.update { it.copy(loading = false, error = if (noAnalysis) null else failure.toUiError(), noAnalysis = noAnalysis) }
            }
        }
    }

    fun refresh() = viewModelScope.launch {
        if (_state.value.refreshing) return@launch
        loadJob?.cancel()
        _state.update { it.copy(refreshing = true, error = null) }
        runCatchingCancellable { repository.refresh(kpiId) }
            .onSuccess { analysis ->
                val alerts = runCatchingCancellable { repository.alerts(kpiId = kpiId).content }
                _state.update {
                    it.copy(
                        refreshing = false,
                        analysis = analysis,
                        alerts = alerts.getOrDefault(it.alerts),
                        alertsUnavailable = alerts.isFailure,
                        noAnalysis = false,
                    )
                }
            }
            .onFailure { failure -> _state.update { it.copy(refreshing = false, error = failure.toUiError()) } }
    }
}

data class IntelligenceAlertsUiState(
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val items: List<IntelligenceAlert> = emptyList(),
    val attentionLevel: String? = null,
    val page: Int = 0,
    val hasMore: Boolean = false,
    val error: UiError? = null,
)

@HiltViewModel
class IntelligenceAlertsViewModel @Inject constructor(
    private val repository: MaintenanceIntelligenceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(IntelligenceAlertsUiState())
    val state = _state.asStateFlow()
    private var loadJob: Job? = null
    init { load() }

    fun filter(level: String?) {
        if (_state.value.attentionLevel == level) return
        _state.update { it.copy(attentionLevel = level, items = emptyList(), page = 0, hasMore = false) }
        load(reset = true)
    }

    fun load(reset: Boolean = true) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val page = if (reset) 0 else _state.value.page + 1
            _state.update { it.copy(loading = reset, loadingMore = !reset, error = null) }
            runCatchingCancellable { repository.alerts(attentionLevel = _state.value.attentionLevel, page = page) }
                .onSuccess { response ->
                    _state.update {
                        it.copy(
                            loading = false,
                            loadingMore = false,
                            items = if (reset) response.content else it.items + response.content,
                            page = response.page,
                            hasMore = !response.last,
                        )
                    }
                }
                .onFailure { failure -> _state.update { it.copy(loading = false, loadingMore = false, error = failure.toUiError()) } }
        }
    }

    fun loadMore() {
        if (_state.value.hasMore && !_state.value.loadingMore) load(reset = false)
    }
}

sealed interface IntelligenceAlertDetailUiState {
    data object Loading : IntelligenceAlertDetailUiState
    data class Content(val alert: IntelligenceAlert) : IntelligenceAlertDetailUiState
    data class Error(val error: UiError) : IntelligenceAlertDetailUiState
}

@HiltViewModel
class IntelligenceAlertDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MaintenanceIntelligenceRepository,
) : ViewModel() {
    private val alertId: Long = checkNotNull(savedStateHandle["alertId"])
    private val _state = MutableStateFlow<IntelligenceAlertDetailUiState>(IntelligenceAlertDetailUiState.Loading)
    val state = _state.asStateFlow()
    private var loadJob: Job? = null
    init { load() }

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = IntelligenceAlertDetailUiState.Loading
            runCatchingCancellable { repository.alert(alertId) }
                .onSuccess { _state.value = IntelligenceAlertDetailUiState.Content(it) }
                .onFailure { _state.value = IntelligenceAlertDetailUiState.Error(it.toUiError()) }
        }
    }
}

private suspend inline fun <T> runCatchingCancellable(crossinline block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: Throwable) {
    Result.failure(failure)
}
