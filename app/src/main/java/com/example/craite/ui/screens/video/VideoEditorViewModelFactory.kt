package com.example.craite.ui.screens.video

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.example.craite.data.EditSettings
import com.example.craite.data.models.ProjectDatabase

class VideoEditorViewModelFactory(
    private val editSettings: EditSettings
) : ViewModelProvider.Factory {
    @OptIn(UnstableApi::class)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideoEditorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VideoEditorViewModel(editSettings) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}