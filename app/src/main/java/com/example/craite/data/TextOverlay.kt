package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class TextOverlay(
    val color: String,
    val duration: Int,
    val font_size: Int,
    val label: String,
    val position: String
)