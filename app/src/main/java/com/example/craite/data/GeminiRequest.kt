package com.example.craite.data

data class GeminiRequest(
    val user_id: String,
    val gemini_prompt: String,
    val project_id: Int
)