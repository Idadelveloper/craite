package com.example.craite.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.net.Uri
import androidx.databinding.adapters.Converters
import androidx.room.TypeConverter
import androidx.room.TypeConverters


@Entity
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val videos: List<Uri>,
    val images: List<Uri>,
    val editingSettings: Map<String, Any>,
    val geminiResponse: String? = null
)
