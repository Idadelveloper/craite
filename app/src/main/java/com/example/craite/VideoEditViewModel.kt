package com.example.craite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.EditSettings
import com.example.craite.data.GeminiRequest
import com.example.craite.data.GeminiResult
import com.example.craite.data.MediaEffect
import com.example.craite.data.models.ProjectDao
import com.example.craite.utils.ProjectTypeConverters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class VideoEditViewModel(initialEditSettings: EditSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(initialEditSettings)
    val uiState: StateFlow<EditSettings> = _uiState.asStateFlow()

    private fun updateEditSettings(newEditSettings: EditSettings) {
        _uiState.value = newEditSettings
    }

    // Example editing action functions (add more as needed)
    fun trimVideo(videoIndex: Int, startTime: Double, endTime: Double) {
        val currentEditSettings = _uiState.value
        val updatedVideoEdits = currentEditSettings.video_edits.toMutableList()
        updatedVideoEdits[videoIndex] = updatedVideoEdits[videoIndex].copy(
            start_time = startTime,
            end_time = endTime
        )
        updateEditSettings(currentEditSettings.copy(video_edits = updatedVideoEdits))
    }

    fun addEffect(videoIndex: Int, effect: MediaEffect) {
        val currentEditSettings = _uiState.value
        val updatedVideoEdits = currentEditSettings.video_edits.toMutableList()
        val updatedEffects = updatedVideoEdits[videoIndex].effects.toMutableList()
        updatedEffects.add(effect)
        updatedVideoEdits[videoIndex] = updatedVideoEdits[videoIndex].copy(effects = updatedEffects)
        updateEditSettings(currentEditSettings.copy(video_edits = updatedVideoEdits))
    }

    // ... (Add functions for other editing actions like adding text, transitions, etc.)
}