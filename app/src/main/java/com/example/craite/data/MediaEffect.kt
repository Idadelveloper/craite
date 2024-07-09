package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class MediaEffect(
    val name: String,
    val adjustment: List<Float>
)