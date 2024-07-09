package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class VideoEdit(
    val id: Int,
    val video_name: String,
    val start_time: Double,
    val end_time: Double,
    val effects: List<MediaEffect>,
    val text: List<CraiteTextOverlay>,
    val transition: String
)