package com.example.craite

import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.EditSettings
import com.example.craite.data.MediaEffect
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoEditViewModel(initialEditSettings: EditSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(initialEditSettings)
    val uiState: StateFlow<EditSettings> = _uiState.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    private val _showProgressDialog = MutableStateFlow(false)
    val showProgressDialog: StateFlow<Boolean> = _showProgressDialog.asStateFlow()

    fun showProgressDialog() {
        _showProgressDialog.value = true
    }

    fun hideProgressDialog() {
        _showProgressDialog.value = false
    }

    private fun updateEditSettings(newEditSettings: EditSettings) {
        _uiState.value = newEditSettings
    }

    fun setCurrentMediaItemIndex(index: Any) {
        _currentMediaItemIndex.value = index as Int
    }

    fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
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

}