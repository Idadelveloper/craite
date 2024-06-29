package com.example.craite.data.models

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.craite.data.EditSettings
import com.example.craite.data.GeminiResponse


@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val media: List<String>,
    var mediaNames: Map<String, String> = emptyMap(),
    val editingSettings: EditSettings? = null,
    val geminiResponse: GeminiResponse? = null
)
