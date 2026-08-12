package com.factoryflow.app.core.util

import androidx.annotation.StringRes
import com.factoryflow.app.R
import com.factoryflow.app.core.network.AppError

data class UiError(@param:StringRes val title: Int, @param:StringRes val detail: Int, val sessionExpired: Boolean = false)

fun Throwable.toUiError(): UiError = when (this) {
    AppError.InvalidCredentials -> UiError(R.string.invalid_credentials, R.string.invalid_credentials)
    AppError.Unauthorized -> UiError(R.string.session_expired, R.string.session_expired, true)
    AppError.NetworkUnavailable -> UiError(R.string.network_unavailable, R.string.network_unavailable)
    is AppError.Validation -> UiError(R.string.invalid_data, R.string.invalid_data)
    is AppError.Conflict -> UiError(R.string.invalid_data, R.string.confirm_failed)
    is AppError.Server -> UiError(R.string.server_error, R.string.server_error)
    else -> UiError(R.string.unknown_error, R.string.unknown_error)
}
