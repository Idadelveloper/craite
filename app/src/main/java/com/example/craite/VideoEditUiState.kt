package com.example.craite

import com.example.craite.data.EditSettings

sealed class VideoEditUiState {
    data object Loading : VideoEditUiState()
    data class Success(val editSettings: EditSettings?) : VideoEditUiState()
    data class Error(val message: String) : VideoEditUiState()
}