package com.example.craite.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.room.Entity

@Entity
data class Media(
    val uri: Uri,
    val byteArray: ByteArray,
    val isVideo: Boolean,
    val duration: Int = 0,
    val bitmap: Bitmap
)