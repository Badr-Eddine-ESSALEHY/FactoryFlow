package com.factoryflow.app.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.factoryflow.app.core.data.AuthRepository
import com.factoryflow.app.core.network.AppError
import com.factoryflow.app.core.network.dto.UserDto
import com.factoryflow.app.core.util.UiError
import com.factoryflow.app.core.util.toUiError
import com.factoryflow.app.core.util.isValidEmail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

data class LoginUiState(
    val email: String = "", val password: String = "", val emailError: Boolean = false,
    val passwordError: Boolean = false, val submitting: Boolean = false, val error: UiError? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()
    private var loginJob: Job? = null

    fun email(value: String) = _state.update { it.copy(email = value, emailError = false, error = null) }
    fun password(value: String) = _state.update { it.copy(password = value, passwordError = false, error = null) }

    fun login(onSuccess: () -> Unit) {
        if (loginJob?.isActive == true) return
        val current = _state.value
        if (current.submitting) return
        val emailInvalid = !current.email.isValidEmail()
        val passwordInvalid = current.password.isBlank()
        if (emailInvalid || passwordInvalid) {
            _state.update { it.copy(emailError = emailInvalid, passwordError = passwordInvalid) }
            return
        }
        loginJob = viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            runCatching { repository.login(current.email, current.password) }
                .onSuccess {
                    _state.update { state -> state.copy(password = "", submitting = false) }
                    onSuccess()
                }
                .onFailure { error -> _state.update { it.copy(submitting = false, error = error.toUiError()) } }
        }
    }
}

sealed interface SessionUiState {
    data object Loading : SessionUiState
    data class SignedOut(val expired: Boolean = false) : SessionUiState
    data class RestoreFailed(val error: UiError) : SessionUiState
    data class SignedIn(val user: UserDto) : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state =
        MutableStateFlow<SessionUiState>(SessionUiState.Loading)

    val state = _state.asStateFlow()

    private var restoreJob: Job? = null

    init {
        restore()

        viewModelScope.launch {
            repository.authenticated
                .drop(1)
                .collect { authenticated ->
                    if (!authenticated) {
                        _state.value = SessionUiState.SignedOut(repository.sessionExpired.value)
                    }
                }
        }
    }

    fun restore() {
        if (restoreJob?.isActive == true) return

        restoreJob = viewModelScope.launch {
            _state.value = SessionUiState.Loading

            // Startup must never remain blocked forever.
            val hasSession = withTimeoutOrNull(1500) {
                repository.hasSession()
            } ?: false

            if (!hasSession) {
                _state.value = SessionUiState.SignedOut()
                return@launch
            }

            val restored = withTimeoutOrNull(5000) { runCatching { repository.currentUser() } }
            when {
                restored == null -> _state.value = SessionUiState.RestoreFailed(AppError.NetworkUnavailable.toUiError())
                restored.isSuccess -> _state.value = SessionUiState.SignedIn(restored.getOrThrow())
                restored.exceptionOrNull() == AppError.Unauthorized ->
                    _state.value = SessionUiState.SignedOut(expired = true)
                else -> _state.value = SessionUiState.RestoreFailed(checkNotNull(restored.exceptionOrNull()).toUiError())
            }
        }
    }

    fun logout() {
        repository.logout()
        _state.value = SessionUiState.SignedOut()
    }
}
