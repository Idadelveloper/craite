package com.example.craite.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiResponseApi {
    @POST("process_videos")
    suspend fun getGeminiResponse(@Body request: GeminiRequest): GeminiResponse

    companion object {
        const val BASE_URL = "http://192.168.1.30:5000"
    }
}