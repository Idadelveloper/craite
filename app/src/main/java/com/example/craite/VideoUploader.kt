package com.example.craite

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoUploader {
    companion object {
        suspend fun uploadVideo(
            videoUri: Uri,
            context: Context,
            onProgress: (Float) -> Unit
        ): Boolean = withContext(Dispatchers.IO) {
            TODO()
// Implement your video upload logic here
// Use onProgress(progress
        }
    }
}