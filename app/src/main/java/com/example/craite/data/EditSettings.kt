package com.example.craite.data

import kotlinx.serialization.Serializable

@Serializable
data class EditSettings(
    val video_edits: List<VideoEdit>,
    val audio_edits: AudioEdit

)