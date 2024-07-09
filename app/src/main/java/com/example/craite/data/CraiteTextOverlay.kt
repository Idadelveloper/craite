package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class CraiteTextOverlay(
    val text: String,
    val font_size: Int,
    val text_color: String,
    val background_color: String
)