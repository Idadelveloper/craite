package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class VideoEdit(
    val id: Int,
    val videoName: String,
    val startTime: Double,
    val endTime: Double,
    val effects: List<MediaEffect>,
    val text: List<CraiteTextOverlay>,
    val transition: String
) {
    fun toMap(): Map<String, Any?> {
        ///Todo: Implement toMap
        return emptyMap();
    }

    fun fromMap(map: Map<String, Any?>): VideoEdit? {
        //Todo: Implement fromMap
        return null
    }
}