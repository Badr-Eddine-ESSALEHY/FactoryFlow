package com.factoryflow.app.core.network

import com.factoryflow.app.core.auth.SecureTokenStore
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(private val tokens: SecureTokenStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (request.url.encodedPath == "/api/auth/login") return chain.proceed(request)
        val token = tokens.accessToken() ?: return chain.proceed(request)
        return chain.proceed(request.newBuilder().header("Authorization", "Bearer $token").build()).also { response ->
            if (response.code == 401) tokens.clear()
        }
    }
}
