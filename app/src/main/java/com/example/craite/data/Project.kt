package com.example.craite.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.net.Uri

@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val videos: List<Uri> = emptyList(),
    val images: List<Uri> = emptyList(),
    val editingSettings: Map<String, Any> = emptyMap(),
    val geminiResponse: String? = null
)