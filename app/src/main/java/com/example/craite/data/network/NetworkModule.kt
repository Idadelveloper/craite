package com.example.craite.data.network

import com.example.craite.data.GeminiResponseApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    fun createGeminiResponseApi(baseUrl: String): GeminiResponseApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(GeminiResponseApi::class.java)
    }
}