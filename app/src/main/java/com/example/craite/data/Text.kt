package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class Text(
    val color: String,
    val duration: Int,
    val font_size: Int,
    val label: String,
    val position: String
)