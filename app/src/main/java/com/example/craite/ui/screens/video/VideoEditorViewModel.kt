package com.example.craite.ui.screens.video

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.transformer.Composition
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.example.craite.data.AudioEdit
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
import com.example.craite.data.models.Project
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.utils.Helpers
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.internal.Contexts.getApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

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

    private val videoEditor = VideoEditor()

    private lateinit var cache: Cache

    fun initializeCache(context: Context) {
        cache = SimpleCache(context.cacheDir, LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024))
    }

    fun createMediaSources(context: Context, mediaUris: List<Uri>) {
        viewModelScope.launch {
            val sources = mediaUris.map { uri ->
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
                val cacheDataSourceFactory = CacheDataSource.Factory()
                    .setCache(cache)
                    .setUpstreamDataSourceFactory(dataSourceFactory)

                ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
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
    private fun fetchAndSaveGeminiResponse(
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
                        val geminiResponseJson = documentSnapshot.get("geminiResponse") as? Map<*, *>
                        val editSettings = geminiResponseJson?.let { parseEditSettingsFromJson(it) }

                        if (editSettings != null) {
                            Log.d("VideoEditViewModel", "Edit settings parsed: $editSettings")
                            val geminiResponse = GeminiResponse(editSettings)

                            // Update both GeminiResponse and EditSettings in the database
                            projectDatabase.projectDao().updateGeminiResponse(projectId, geminiResponse)
                            projectDatabase.projectDao().updateEditingSettings(projectId, editSettings)
                            Log.d("VideoEditViewModel", "Gemini response and EditSettings updated in Room database")
                            val project = projectDatabase.projectDao().getProjectById(projectId)
                            Log.d("VideoEditViewModel", "Edit settings: ${project.first().editingSettings}")

                            // Update the UI state with the fetched EditSettings
                            _uiState.value = editSettings
                        } else {
                            Log.e("VideoEditViewModel", "Error parsing edit settings from JSON")
                            // Handle parsing error (e.g., show a Snackbar)
                        }
                    } else {
                        Log.e("VideoEditViewModel", "Firestore document not found")
                        // Handle document not found error (e.g., show a Snackbar)
                    }
                }
            } catch (e: Exception) {
                Log.e("VideoEditViewModel", "Error fetching/saving edit settings: ${e.message}")
                // Handle general error (e.g., show a Snackbar)
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
                    video_name = (videoEditJson["video_name"] as? String)?.let { name ->
                        if (name.contains('/')) {
                            name.split('/').lastOrNull() ?: ""
                        } else {
                            name
                        }
                    } ?: "",
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

            // Parse audio edits
            val audioEditsJson = json?.get("audio_edits") as? Map<String, Any>
            val audioEdits = audioEditsJson?.let {
                (it["end_time"] as? Double)?.let { it1 ->
                    AudioEdit(
                        start_time = (it["start_time"] as? Double)!!,
                        end_time = it1
                    )
                }
            }
            Log.d("VideoEditViewModel", "Audio Edits: $audioEdits")
            Log.d("VideoEditViewModel", "Video Edits: $videoEdits")

            EditSettings(video_edits = videoEdits, audio_edits = audioEdits)
        } catch (e: Exception) {
            Log.e("VideoEditViewModel", "Error parsing JSON: ${e.message}")
            null
        }
    }

    // Export video
    fun exportVideo(context: Context, project: Project?, editSettings: EditSettings, exoPlayer: ExoPlayer) {
        Toast.makeText(context, "Processing video", Toast.LENGTH_SHORT).show()
        showProgressDialog()
        viewModelScope.launch {
            Log.d("VideoEditorViewModel", "Edit settings for export: $editSettings")
            val videoEditor = VideoEditor()
            val mergeResult = project?.let {
                videoEditor.trimAndMergeToTempFile(
                    context,
                    editSettings,
                    it.mediaNames,
                    it.audioPath
                )
            }

            if (mergeResult != null) {
                mergeResult.onSuccess { mergedVideoPath ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        // MediaStore (Android 10 and above)
                        val contentValues = ContentValues().apply {
                            put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                "merged_video_${System.currentTimeMillis()}.mp4"
                            )
                            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_MOVIES
                            )
                        }

                        val resolver = context.contentResolver
                        val uri = resolver.insert(
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )

                        uri?.let { videoUri ->
                            resolver.openOutputStream(videoUri)?.use { outputStream ->
                                File(mergedVideoPath).inputStream().use { inputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                File(mergedVideoPath).delete() // Delete temporary file
                                Log.d("VideoEditorViewModel", "Video saved to MediaStore: $videoUri")
                                Toast.makeText(
                                    context,
                                    "Video saved to MediaStore: $videoUri",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Update ExoPlayer with merged video URI (if needed)
                                val mergedMediaItem = MediaItem.fromUri(videoUri)
                                exoPlayer.setMediaItem(mergedMediaItem)
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            }
                        } ?: run {
                            Log.e("VideoEditorViewModel", "Error saving to MediaStore: URI is null")
                            Toast.makeText(
                                context,
                                "Error saving to MediaStore",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Legacy Storage (Older Android versions)
                        MediaScannerConnection.scanFile(
                            context,
                            arrayOf(mergedVideoPath), // Pass the file path directly
                            arrayOf("video/mp4"),
                            null
                        )
                        Log.d("VideoEditorViewModel", "Video saved to: $mergedVideoPath")
                        Toast.makeText(
                            context,
                            "Video saved to: $mergedVideoPath",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Update ExoPlayer with merged video file path (if needed)
                        val mergedMediaItem = MediaItem.fromUri(Uri.fromFile(File(mergedVideoPath)))
                        exoPlayer.setMediaItem(mergedMediaItem)
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                    }
                }.onFailure { exception ->
                    Log.e("VideoEditorViewModel", "Error merging videos: ${exception.message}")
                    Toast.makeText(context, "Error merging videos", Toast.LENGTH_SHORT).show()
                }
            }

            hideProgressDialog()
        }
    }

    // Function to handle Get Gemini Edits button click
    fun getGeminiEdits(userId: String, project: Project?, projectDatabase: ProjectDatabase) {
        if (project != null) {
            fetchAndSaveGeminiResponse(
                userId,
                project.id,
                project.promptId,
                projectDatabase
            )
        }
    }

    // Function to handle Apply Firestore Edits button click
    fun applyFirestoreEdits(userId: String, project: Project?, context: Context) {
        Log.d("VideoEditorViewModel", project?.mediaNames.toString())
        Log.d("VideoEditorViewModel", project?.media.toString())

        if (project != null) {
            fetchAndApplyFirestoreEdits(
                userId,
                project.id,
                project.promptId ?: "",
                project.mediaNames,
                context
            )
        }

    }

    fun previewEditSettings(
        context: Context,
        exoPlayer: ExoPlayer,
        editSettings: EditSettings,
        mediaNameMap: Map<String, String>,
        onPlayerReady: () -> Unit // Add the callback parameter
    ) {
        viewModelScope.launch {
            videoEditor.previewEditSettings(
                context,
                exoPlayer,
                editSettings,
                mediaNameMap,
                onPlayerReady = onPlayerReady
            )
        }
    }

    // Show Snackbar (Placeholder for Snackbar implementation)
    private fun showSnackbar(message: @Composable () -> Unit) {
        // Implement Snackbar logic here
    }

    fun changeResolution(context: Context, resolution: String, exoPlayer: ExoPlayer) {
        viewModelScope.launch {
            // 1. Get the current video file path
            val currentVideoPath = "" // ... (Get the path of the video being played) ...

            // 2. Create a temporary output file path
            val outputFile = File.createTempFile("transcoded_video", ".mp4", context.cacheDir)
            val outputFilePath = outputFile.absolutePath

            // 3. Build the FFmpeg command
            val command = "-i $currentVideoPath -s $resolution -c:v libx264 -crf 23 -preset ultrafast -c:a copy $outputFilePath"

            // 4. Execute the FFmpeg command using FFmpegKit
            FFmpegKit.executeAsync(command) { session ->
                val returnCode = session.returnCode
                if (ReturnCode.isSuccess(returnCode)) {
                    // 5. Transcoding successful, update ExoPlayer
                    val newMediaItem = MediaItem.fromUri(Uri.fromFile(outputFile))
                    exoPlayer.setMediaItem(newMediaItem)
                    exoPlayer.prepare()
                } else {
                    // 6. Handle transcoding error
                    Log.e("VideoEditorViewModel", "FFmpegKit error: ${session.failStackTrace}")
                    // ... (Display error message to the user) ...
                }
            }
        }
    }
}