package com.example.craite.ui.screens.video

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.transformer.Composition
import com.example.craite.data.CraiteTextOverlay
import com.example.craite.data.EditSettings
import com.example.craite.data.EditSettingsRepository
import com.example.craite.data.EditSettingsRepositoryImpl
import com.example.craite.data.GeminiRequest
import com.example.craite.data.GeminiResponse
import com.example.craite.data.GeminiResult
import com.example.craite.data.MediaEffect
import com.example.craite.data.RetrofitClient
import com.example.craite.data.VideoEdit
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.utils.Helpers
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@UnstableApi
class VideoEditorViewModel(initialEditSettings: EditSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(initialEditSettings)
    val uiState: StateFlow<EditSettings> = _uiState.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    private val _showProgressDialog = MutableStateFlow(false)
    val showProgressDialog: StateFlow<Boolean> = _showProgressDialog.asStateFlow()

    private val _downloadButtonEnabled = MutableStateFlow(false)
    val downloadButtonEnabled: StateFlow<Boolean> = _downloadButtonEnabled.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _intervals = MutableStateFlow<List<Long>>(emptyList())
    val intervals: StateFlow<List<Long>> = _intervals.asStateFlow()

    private val repository: EditSettingsRepository = EditSettingsRepositoryImpl(RetrofitClient.geminiResponseApi)

    private val _composition = MutableStateFlow<Composition?>(null)
    val composition: StateFlow<Composition?> = _composition.asStateFlow()

    private val _mediaSources = MutableStateFlow<List<MediaSource>>(emptyList())
    val mediaSources: StateFlow<List<MediaSource>> = _mediaSources.asStateFlow()

    private val _timeline = MutableStateFlow<Timeline?>(null)
    val timeline: StateFlow<Timeline?> = _timeline.asStateFlow()

    fun createMediaSources(context: Context, mediaUris: List<Uri>) {
        viewModelScope.launch {
            val sources = mediaUris.map { uri ->
                try {
                    ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                        .createMediaSource(MediaItem.fromUri(uri))
                } catch (e: Exception) {
                    Log.e("VideoEditorViewModel", "Error creating MediaSource for $uri: ${e.message}")
                    null
                }
            }.filterNotNull()
            _mediaSources.value = sources
        }
    }

    fun updateTimeline(exoPlayer: ExoPlayer) {
        viewModelScope.launch {
            _timeline.value = exoPlayer.currentTimeline
        }
    }



    fun updateEditSettings(newEditSettings: EditSettings, context: Context, mediaMap: Map<String, String>) {
        _uiState.value = newEditSettings
        _downloadButtonEnabled.value = true


    }

    // Playback controls
    fun playVideo() {
        _isPlaying.value = true
    }

    fun pauseVideo() {
        _isPlaying.value = false
    }

    // Dialog controls
    fun showProgressDialog() {
        _showProgressDialog.value = true
    }

    fun hideProgressDialog() {
        _showProgressDialog.value = false
    }

    // Update settings
    private fun updateEditSettings(newEditSettings: EditSettings) {
        _uiState.value = newEditSettings
        _downloadButtonEnabled.value = true // Enable download button when edits are available
    }

    // Update total duration
    fun updateTotalDuration(duration: Long) {
        _totalDuration.value = duration
        // Recalculate intervals when totalDuration changes
        _intervals.value = Helpers.calculateIntervals(duration)
    }

    // Set current media item index
    fun setCurrentMediaItemIndex(index: Int) {
        _currentMediaItemIndex.value = index
    }

    // Fetch edit settings
    fun fetchEditSettings(userId: String, prompt: String, projectId: Int, promptId: String) {
        Log.d("VideoEditViewModel", "Fetching edit settings for user: $userId, prompt: $prompt, project: $projectId")
        viewModelScope.launch {
            repository.getEditSettings(GeminiRequest(userId, prompt, projectId, promptId)).collect { result ->
                when (result) {
                    is GeminiResult.Success -> {
                        Log.d("VideoEditViewModel", "Edit settings received: ${result.data}")
                        result.data?.let { updateEditSettings(it) }
                    }
                    is GeminiResult.Error -> {
                        Log.e("VideoEditViewModel", "Error fetching edit settings: ${result.message}")
                    }
                }
            }
        }
    }

    // Fetch and save Gemini response
    fun fetchAndSaveGeminiResponse(userId: String, projectId: Int, promptId: String?, projectDatabase: ProjectDatabase) {
        viewModelScope.launch {
            try {
                promptId?.let {
                    Log.d("VideoEditViewModel", "Fetching Gemini response for prompt ID: $it")
                    val docRef = Firebase.firestore.collection("users")
                        .document(userId)
                        .collection("projects")
                        .document(projectId.toString())
                        .collection("prompts")
                        .document(it)

                    val documentSnapshot = docRef.get().await()
                    if (documentSnapshot.exists()) {
                        val geminiResponseJson = documentSnapshot.get("geminiResponse") as? Map<*, *>
                        if (geminiResponseJson != null) {
                            Log.d("VideoEditViewModel", "Gemini response found: $geminiResponseJson")
                            val editSettings = parseEditSettingsFromJson(geminiResponseJson)
                            if (editSettings != null) {
                                val geminiResponse = GeminiResponse(editSettings)
                                projectDatabase.projectDao().updateGeminiResponse(projectId, geminiResponse)
                                Log.d("VideoEditViewModel", "Gemini response saved to Room database")
                            } else {
                                Log.e("VideoEditViewModel", "Error parsing edit settings from JSON")
                            }
                        } else {
                            Log.e("VideoEditViewModel", "Gemini response not found in Firestore document")
                        }
                    } else {
                        Log.e("VideoEditViewModel", "Firestore document not found")
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoEditViewModel", "Error fetching/saving edit settings: ${e.message}")
            }
        }
    }

    // Fetch and apply Firestore edits
    fun fetchAndApplyFirestoreEdits(userId: String, projectId: Int, promptId: String, mediaNames: Map<String, String>, context: Context) {
        viewModelScope.launch {
            showProgressDialog()
            try {
                val docRef = Firebase.firestore.collection("users")
                    .document(userId)
                    .collection("projects")
                    .document(projectId.toString())
                    .collection("prompts")
                    .document(promptId)

                val documentSnapshot = docRef.get().await()
                if (documentSnapshot.exists()) {
                    val geminiResponseJson = documentSnapshot.get("geminiResponse") as? Map<*, *>
                    if (geminiResponseJson != null) {
                        val editSettings = parseEditSettingsFromJson(geminiResponseJson)
                        editSettings?.let { settings ->
                            updateEditSettings(settings)
                        }
                    } else {
                        Log.e("VideoEditViewModel", "Gemini response not found in Firestore document")
                    }
                } else {
                    Log.e("VideoEditViewModel", "Firestore document not found")
                }
            } catch (e: Exception) {
                Log.e("VideoEditViewModel", "Error fetching edit settings: ${e.message}")
            } finally {
                hideProgressDialog()
            }
        }
    }

    // Parse JSON into EditSettings
    private fun parseEditSettingsFromJson(json: Map<*, *>?): EditSettings? {
        return try {
            val videoEditsJson = json?.get("video_edits") as? List<Map<String, Any>>
            val videoEdits = videoEditsJson?.map { videoEditJson ->
                VideoEdit(
                    video_name = (videoEditJson["video_name"] as? String)?.split("/")?.lastOrNull() ?: "",
                    start_time = videoEditJson["start_time"] as? Double ?: 0.0,
                    effects = (videoEditJson["effects"] as? List<Map<String, Any>>)?.map { effectJson ->
                        MediaEffect(
                            name = effectJson["name"] as? String ?: "",
                            adjustment = (effectJson["adjustment"] as? List<Double>)?.map { it.toFloat() } ?: emptyList()
                        )
                    } ?: emptyList(),
                    end_time = videoEditJson["end_time"] as? Double ?: 0.0,
                    id = videoEditJson["id"] as? Int ?: 0,
                    text = (videoEditJson["text"] as? List<Map<String, Any>>)?.map { textJson ->
                        CraiteTextOverlay(
                            background_color = textJson["background_color"] as? String ?: "",
                            font_size = textJson["font_size"] as? Int ?: 0,
                            text = textJson["text"] as? String ?: "",
                            text_color = textJson["text_color"] as? String ?: ""
                        )
                    } ?: emptyList(),
                    transition = videoEditJson["transition"] as? String ?: ""
                )
            } ?: emptyList()

            EditSettings(video_edits = videoEdits)
        } catch (e: Exception) {
            Log.e("VideoEditViewModel", "Error parsing JSON: ${e.message}")
            null
        }
    }

    // Show Snackbar (Placeholder for Snackbar implementation)
    private fun showSnackbar(message: @Composable () -> Unit) {
        // Implement Snackbar logic here
    }
}