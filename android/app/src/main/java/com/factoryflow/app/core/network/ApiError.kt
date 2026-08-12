package com.factoryflow.app.core.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = false)
data class ApiErrorResponse(
    val timestamp: String? = null,
    val status: Int = 0,
    val code: String? = null,
    val message: String? = null,
    val path: String? = null,
    val details: List<ApiErrorDetail> = emptyList(),
)

@JsonClass(generateAdapter = false)
data class ApiErrorDetail(val field: String? = null, val message: String? = null)

sealed class AppError(cause: Throwable? = null) : RuntimeException(cause) {
    data object InvalidCredentials : AppError()
    data object Unauthorized : AppError()
    data object NetworkUnavailable : AppError()
    data class Validation(val code: String?, val detail: String?) : AppError()
    data class Conflict(val code: String?, val detail: String?) : AppError()
    data class Server(val code: String?, val detail: String?) : AppError()
    data class Unknown(val original: Throwable) : AppError(original)
}
