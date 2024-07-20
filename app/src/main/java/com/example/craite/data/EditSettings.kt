package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class EditSettings(
    val videoEdits: List<VideoEdit>
){
    fun toMap(): Map<String, Any?> {
        ///Todo: Implement toMap
        return emptyMap()
    }

    fun fromMap(map: Map<String, Any?>): VideoEdit?{
        ///Todo: Implement fromMap
        return null
    }
}