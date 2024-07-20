package com.example.craite.data.models

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
    var prompt: String? = null,
    var promptId: String? = null,
    var isProcessing: Boolean = false,
    val editingSettings: EditSettings? = null,
    val geminiResponse: GeminiResponse? = null,
    val length: String? = null,
) {

    //ToMap
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "name" to name,
            "media" to media,
            "mediaNames" to mediaNames,
            "prompt" to prompt,
            "promptId" to promptId,
            "isProcessing" to isProcessing,
            "editingSettings" to editingSettings?.toMap(), // Assuming EditSettingsalso has a toMap()
            "geminiResponse" to geminiResponse?.toMap(), // Assuming GeminiResponse also has a toMap()
            "length" to length,
        )
    }
}
