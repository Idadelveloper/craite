package com.example.craite

import android.util.Log
import androidx.activity.result.launch
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.EditSettings
import com.example.craite.data.EditSettingsRepository
import com.example.craite.data.EditSettingsRepositoryImpl
import com.example.craite.data.GeminiRequest
import com.example.craite.data.GeminiResult
import com.example.craite.data.MediaEffect
import com.example.craite.data.RetrofitClient
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

    private val repository: EditSettingsRepository = EditSettingsRepositoryImpl(RetrofitClient.geminiResponseApi)

    fun fetchEditSettings(userId: String, prompt: String, projectId: Int) {
        Log.d("VideoEditViewModel", "Fetching edit settings for user: $userId, prompt: $prompt, project: $projectId")
        viewModelScope.launch {
            repository.getEditSettings(GeminiRequest(userId, prompt, projectId))
                .collect { result ->
                    when (result) {
                        is GeminiResult.Success -> {
                            Log.d("VideoEditViewModel", "Edit settings received: ${result.data}")
                            val editSettings = result.data
                            editSettings?.let {
                                updateEditSettings(it) // Update the UI state with received settings
                                // You can also trigger video processing with VideoEditor here if needed
                            }
                            Log.d("VideoEditViewModel", "Edit settings received: $editSettings")
                            // You can store these edit settings in your database or use them immediately
                            // ... (Call your VideoEditor functions to apply edits if needed)
                        }
                        is GeminiResult.Error -> {
                            Log.e("VideoEditViewModel", "Error fetching edit settings: ${result.message}")
                            // Handle the error appropriately (e.g., show a message to the user)
                        }
                    }
                }
        }
    }

}