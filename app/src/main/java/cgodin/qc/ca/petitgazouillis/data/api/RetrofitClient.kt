package cgodin.qc.ca.petitgazouillis.data.api

import android.content.Context
import cgodin.qc.ca.petitgazouillis.data.utils.NetworkUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8000"
    private val logger = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun create(
        appContext: Context,
        tokenProvider: () -> String?
    ): ApiService {

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                if (!NetworkUtils.isOnline(appContext)) {
                    throw IOException("no_internet")
                }
                chain.proceed(chain.request())
            }
            .addInterceptor(logger)
            .addInterceptor { chain ->
                val token = tokenProvider()
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
