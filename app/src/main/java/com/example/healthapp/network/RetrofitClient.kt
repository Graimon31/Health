package com.example.healthapp.network

import com.example.healthapp.BuildConfig
import com.example.healthapp.data.prefs.UserPreferences
import okhttp3.ConnectionSpec
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds Retrofit clients based on dynamic user preferences.
 */
class RetrofitClient {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun create(prefs: UserPreferences): ApiService {
        val baseUrl = normalizeUrl(prefs.baseUrl)
        if (!prefs.allowHttp && baseUrl.startsWith("http://", ignoreCase = true)) {
            throw IllegalStateException("HTTP is disabled in settings")
        }

        val clientBuilder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)

        val token = prefs.token?.takeIf { it.isNotBlank() }
        if (token != null) {
            clientBuilder.addInterceptor(Interceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${token.trim()}")
                    .build()
                chain.proceed(request)
            })
        }

        if (BuildConfig.ENABLE_LOGGING) {
            val logger = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            clientBuilder.addInterceptor(logger)
        }

        if (baseUrl.startsWith("http://", ignoreCase = true)) {
            clientBuilder.connectionSpecs(listOf(ConnectionSpec.CLEARTEXT, ConnectionSpec.MODERN_TLS))
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        return retrofit.create(ApiService::class.java)
    }

    private fun normalizeUrl(raw: String): String {
        if (raw.isBlank()) return BuildConfig.DEFAULT_BASE_URL
        return if (raw.endsWith('/')) raw else "$raw/"
    }
}
