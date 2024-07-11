package com.example.craite.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(GeminiResponseApi.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client( // Add OkHttpClient with timeouts
            OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        )
        .build()

    val geminiResponseApi: GeminiResponseApi = retrofit.create(GeminiResponseApi::class.java)
}