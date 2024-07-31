package com.example.craite.data.models

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.craite.data.EditSettings
import com.example.craite.data.GeminiResponse
import com.example.craite.utils.ProjectTypeConverters
import java.util.Date


@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val media: List<String>,
    var mediaNames: Map<String, String> = emptyMap(),
    var audioPath: String? = null,
    var prompt: String? = null,
    var promptId: String? = null,
    var uploadCompleted: Boolean = false,
    var thumbnailPath: String? = null,
    var projectDuration: String? = null,
    val editingSettings: EditSettings? = null,
    val geminiResponse: GeminiResponse? = null,
    @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
    @TypeConverters(ProjectTypeConverters::class)
    val dateCreated: Date = Date()
)
