package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class Effect(
    val adjustment: Double,
    val name: String
)