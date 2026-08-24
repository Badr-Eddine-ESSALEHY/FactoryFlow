package com.factoryflow.app.di

import com.factoryflow.app.BuildConfig
import com.factoryflow.app.core.network.ApiExecutor
import com.factoryflow.app.core.network.AuthInterceptor
import com.factoryflow.app.core.network.BigDecimalJsonAdapter
import com.factoryflow.app.core.network.FactoryFlowApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun moshi(): Moshi = Moshi.Builder().add(BigDecimalJsonAdapter()).addLast(KotlinJsonAdapterFactory()).build()

    @Provides @Singleton
    fun apiExecutor(moshi: Moshi) = ApiExecutor(moshi)

    @Provides @Singleton
    fun okHttp(auth: AuthInterceptor): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(auth)
        .apply {
            if (BuildConfig.DEBUG) addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
            })
        }
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun api(client: OkHttpClient, moshi: Moshi): FactoryFlowApi {
        check(BuildConfig.API_CONFIGURED) {
            "FACTORYFLOW_RELEASE_API_BASE_URL must be configured with the HTTPS production API URL."
        }
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(FactoryFlowApi::class.java)
    }
}
