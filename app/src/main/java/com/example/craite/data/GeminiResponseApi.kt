package com.example.craite.data

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiResponseApi {

    @POST("process_videos")
    suspend fun getGeminiResponse(

    ) {

    }

    companion object {
        const val BASE_URL = ""
    }
}