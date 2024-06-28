package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class MediaEffect(
    val adjustment: Double,
    val name: String
)