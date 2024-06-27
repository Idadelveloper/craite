package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val gemini_response: EditSettings
)