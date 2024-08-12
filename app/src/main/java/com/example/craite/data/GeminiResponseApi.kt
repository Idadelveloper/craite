package com.example.craite.data

import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiResponseApi {
    @POST("process_videos")
    suspend fun getGeminiResponse(@Body request: GeminiRequest): GeminiResponse

    @POST("process_videos")
    suspend fun processVideos(@Body requestBody: GeminiRequest): Response<Unit>

    companion object {
    }
}