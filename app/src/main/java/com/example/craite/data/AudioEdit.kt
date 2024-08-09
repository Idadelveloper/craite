package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class AudioEdit(
    val start_time: Double,
    val end_time: Double,
)