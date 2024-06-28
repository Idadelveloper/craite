package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class VideoEdit(
    val edit: String,
    val effects: List<MediaEffect>,
    val end_time: Double,
    val id: Int,
    val start_time: Double,
    val text: List<TextOverlay>,
    val transition: String,
    val video_name: String
)