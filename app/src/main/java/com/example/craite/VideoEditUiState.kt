package com.example.craite

import com.example.craite.data.EditSettings
import com.example.craite.data.GeminiResponse

sealed class VideoEditUiState {
    data object Loading : VideoEditUiState()
//    data class Success(val editSettings: EditSettings?) : VideoEditUiState()
//    data class Error(val message: String) : VideoEditUiState()
    data class Success(val geminiResponse: GeminiResponse?) : VideoEditUiState() // Corrected type
    data class Error(val message: String) : VideoEditUiState()
}