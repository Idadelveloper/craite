package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class AudioEdit(
    val id: Int,
    val audio_name: String,
    val start_time: Double,
    val end_time: Double,
)