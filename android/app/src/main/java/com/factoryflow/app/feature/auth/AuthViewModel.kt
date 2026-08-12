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
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "", val password: String = "", val emailError: Boolean = false,
    val passwordError: Boolean = false, val submitting: Boolean = false, val error: UiError? = null,
)

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun email(value: String) = _state.update { it.copy(email = value, emailError = false, error = null) }
    fun password(value: String) = _state.update { it.copy(password = value, passwordError = false, error = null) }

    fun login(onSuccess: () -> Unit) {
        val current = _state.value
        val emailInvalid = !current.email.isValidEmail()
        val passwordInvalid = current.password.isBlank()
        if (emailInvalid || passwordInvalid) {
            _state.update { it.copy(emailError = emailInvalid, passwordError = passwordInvalid) }
            return
        }
        viewModelScope.launch {
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
    data object SignedOut : SessionUiState
    data class SignedIn(val user: UserDto) : SessionUiState
}

@HiltViewModel
class SessionViewModel @Inject constructor(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow<SessionUiState>(SessionUiState.Loading)
    val state = _state.asStateFlow()

    init {
        restore()
        viewModelScope.launch {
            repository.authenticated.drop(1).collect { authenticated ->
                if (!authenticated) _state.value = SessionUiState.SignedOut
            }
        }
    }

    fun restore() {
        if (!repository.hasSession()) { _state.value = SessionUiState.SignedOut; return }
        viewModelScope.launch {
            _state.value = SessionUiState.Loading
            runCatching { repository.currentUser() }
                .onSuccess { _state.value = SessionUiState.SignedIn(it) }
                .onFailure {
                    if (it is AppError.Unauthorized) repository.logout()
                    _state.value = SessionUiState.SignedOut
                }
        }
    }

    fun logout() { repository.logout(); _state.value = SessionUiState.SignedOut }
}
