package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class GeminiResponse(
    val gemini_response: EditSettings
){
    fun toMap(): Map<String, Any?> {
        ///Todo: Implement toMap
        return emptyMap();
    }

    fun fromMap(map: Map<String, Any?>): GeminiResponse?{
        //Todo: Implement fromMap
        return null
    }
}