package com.factoryflow.app.core.network

import com.squareup.moshi.Moshi
import java.io.IOException
import retrofit2.HttpException

class ApiExecutor(private val moshi: Moshi) {
    suspend fun <T> execute(block: suspend () -> T): T = try {
        block()
    } catch (error: HttpException) {
        val envelope = runCatching {
            error.response()?.errorBody()?.string()?.let {
                moshi.adapter(ApiErrorResponse::class.java).fromJson(it)
            }
        }.getOrNull()
        when (error.code()) {
            401 -> if (envelope?.code == "AUTH_INVALID_CREDENTIALS") AppError.InvalidCredentials else AppError.Unauthorized
            400, 422 -> AppError.Validation(envelope?.code, envelope?.message)
            409 -> AppError.Conflict(envelope?.code, envelope?.message)
            else -> AppError.Server(envelope?.code, envelope?.message)
        }.let { throw it }
    } catch (error: IOException) {
        throw AppError.NetworkUnavailable
    } catch (error: AppError) {
        throw error
    } catch (error: Throwable) {
        throw AppError.Unknown(error)
    }
}
