package com.example.craite

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VideoEditViewModel(initialEditSettings: EditSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(initialEditSettings)
    val uiState: StateFlow<EditSettings> = _uiState.asStateFlow()

    private val _currentMediaItemIndex = MutableStateFlow(0)
    val currentMediaItemIndex: StateFlow<Int> = _currentMediaItemIndex.asStateFlow()

    private val _showProgressDialog = MutableStateFlow(false)
    val showProgressDialog: StateFlow<Boolean> = _showProgressDialog.asStateFlow()

    private val _downloadButtonEnabled = MutableStateFlow(false)
    val downloadButtonEnabled: StateFlow<Boolean> = _downloadButtonEnabled.asStateFlow()

    fun showProgressDialog() {
        _showProgressDialog.value = true
    }

    fun hideProgressDialog() {
        _showProgressDialog.value = false
    }

    private fun updateEditSettings(newEditSettings: EditSettings) {
        _uiState.value = newEditSettings
        _downloadButtonEnabled.value = true // Enable download button when edits are available
    }

    fun setCurrentMediaItemIndex(index: Any) {
        _currentMediaItemIndex.value = index as Int
    }

    fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            block()
        }
    }

    private val repository: EditSettingsRepository =
        EditSettingsRepositoryImpl(RetrofitClient.geminiResponseApi)

    fun fetchEditSettings(userId: String, prompt: String, projectId: Int, promptId: String) {
        Log.d(
            "VideoEditViewModel",
            "Fetching edit settings for user: $userId, prompt: $prompt, project: $projectId"
        )
        viewModelScope.launch {
            repository.getEditSettings(GeminiRequest(userId, prompt, projectId, promptId))
                .collect { result ->
                    when (result) {
                        is GeminiResult.Success -> {
                            Log.d("VideoEditViewModel", "Edit settings received: ${result.data}")
                            result.data?.let { updateEditSettings(it) }
                        }
                        is GeminiResult.Error -> { // Handle the Error case
                            Log.e(
                                "VideoEditViewModel",
                                "Error fetching edit settings: ${result.message}"
                            )
                            // Handle the error appropriately (e.g., show a Snackbar)
                            // Example: showSnackbar { Text("Error fetching edit settings: ${result.message}") }
                        }
                    }
                }
        }
    }

    fun fetchAndSaveGeminiResponse(
        userId: String,
        projectId: Int,
        promptId: String?,
        projectDatabase: ProjectDatabase
    ) {
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
                        val geminiResponseJson =
                            documentSnapshot.get("geminiResponse") as? Map<*, *>
                        if (geminiResponseJson != null) {
                            Log.d(
                                "VideoEditViewModel",
                                "Gemini response found: $geminiResponseJson"
                            )
                            val editSettings = parseEditSettingsFromJson(geminiResponseJson)
                            if (editSettings != null) { // Check if editSettings is not null
                                editSettings?.let { settings ->
                                    val geminiResponse = GeminiResponse(settings)
                                    viewModelScope.launch {
                                        projectDatabase.projectDao()
                                            .updateGeminiResponse(projectId, geminiResponse)
                                        Log.d(
                                            "VideoEditViewModel",
                                            "Gemini response saved to Room database"
                                        )
                                    }
                                }
                            } else {
                                // Handle the case where parsing failed (e.g., show an error message)
                                Log.e("VideoEditViewModel", "Error parsing edit settings from JSON")
//                                showSnackbar { Text("Error parsing edit settings") }
                            }

                        } else {
                            Log.e(
                                "VideoEditViewModel",
                                "Gemini response not found in Firestore document"
                            )
                            // Handle error: Gemini response field not found
                            // Example: showSnackbar { Text("Gemini response not found") }
                        }
                    } else {
                        Log.e("VideoEditViewModel", "Firestore document not found")
                        // Handle error: Firestore document not found
                        // Example: showSnackbar { Text("Firestore document not found") }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("VideoEditViewModel", "Error fetching/saving edit settings: ${e.message}")
                // Handle error fetching/saving edit settings
                // Example: showSnackbar { Text("Error fetching/saving edit settings") }
            }
        }
    }

    fun fetchAndApplyFirestoreEdits(
        userId: String,
        projectId: Int,
        promptId: String,
        mediaNames: Map<String, String>,
        context: Context
    ) {
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
                    val geminiResponseJson =
                        documentSnapshot.get("geminiResponse") as? Map<String, Any>
                    if (geminiResponseJson != null) {
                        val editSettings = parseEditSettingsFromJson(geminiResponseJson)
                        editSettings?.let { settings ->
                            updateEditSettings(settings)

                        }
                    } else {
                        Log.e(
                            "VideoEditViewModel",
                            "Gemini response not found in Firestore document"
                        )
                        // Handle error: Gemini response field not found
                        // Example: showSnackbar { Text("Gemini response not found") }
                    }
                } else {
                    Log.e("VideoEditViewModel", "Firestore document not found")
                    // Handle error: Firestore document not found
                    // Example: showSnackbar { Text("Firestore document not found") }
                }
            } catch (e: Exception) {
                Log.e("VideoEditViewModel", "Error fetching edit settings: ${e.message}")
                // Handle error fetching edit settings
                // Example: showSnackbar { Text("Error fetching edit settings") }
            } finally {
                hideProgressDialog()
            }
        }
    }

    // Helper function to parse JSON into EditSettings
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
                            adjustment = (effectJson["adjustment"] as? List<Double>)?.map { it.toFloat() }
                                ?: emptyList()
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

    // Helper function to show a Snackbar (remind lewis to design it)
    // Example usage: showSnackbar { Text("Error message") }
    private fun showSnackbar(message: @Composable () -> Unit) {
        // Implement  Snackbar logic here
    }
}