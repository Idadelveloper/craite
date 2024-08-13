package com.example.craite.ui.screens.new_project

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.vector.path
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.EditSettingsRepository
import com.example.craite.data.EditSettingsRepositoryImpl
import com.example.craite.data.GeminiRequest
import com.example.craite.data.GeminiResult
import com.example.craite.data.RetrofitClient
import com.example.craite.data.models.Project
import com.example.craite.data.models.ProjectDao
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.otaliastudios.transcoder.Transcoder
import com.otaliastudios.transcoder.TranscoderListener
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.collections.toTypedArray

class NewProjectViewModel : ViewModel() {
    private val storageRef = Firebase.storage.reference
    private val firestoreDb = Firebase.firestore
    private var videoNames = mutableMapOf<String, String>()

    private val _projectCreationInitiated = MutableStateFlow(false)
    val projectCreationInitiated: StateFlow<Boolean> = _projectCreationInitiated.asStateFlow()

    // Copy video to internal storage and return file path
    private fun copyVideoToInternalStorage(context: Context, contentUri: Uri): String? {
        val fileName = getFileName(context, contentUri) ?: return null
        val file = File(context.filesDir, fileName)

        try {
            val inputStream = context.contentResolver.openInputStream(contentUri)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            return file.absolutePath
        } catch (e: IOException) {
            Log.e("NewProjectViewModel", "Error copying video: ${e.message}")
            Toast.makeText(context, "Error copying video", Toast.LENGTH_SHORT).show()
            return null
        }
    }

    // Helper function to get file name from content URI
    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    return it.getString(nameIndex)
                }
            }
        }
        return uri.path?.lastIndexOf('/')?.plus(1)?.let { uri.path?.substring(it) }
    }

    fun createProject(
        projectDao: ProjectDao,
        projectName: String,
        videoUris: List<Uri>,
        audioUri: Uri?,
        context: Context,
        user: FirebaseUser,
        prompt: String
    ) {
        viewModelScope.launch {
            try {
                // Copy videos to internal storage and get file paths
                val filePaths = videoUris.mapNotNull { copyVideoToInternalStorage(context, it) }

                // Create project with file paths
                val project = Project(name = projectName, media = filePaths)
                projectDao.insert(project)
                val projectId = projectDao.getLastInsertedProject().id
                Toast.makeText(context, "Successfully created project", Toast.LENGTH_SHORT).show()

                // Generate and save thumbnail for the first video (if available)
                if (filePaths.isNotEmpty()) {
                    val firstVideoPath = filePaths[0]
                    val thumbnailResult = getThumbnailUriFromVideo(context, Uri.fromFile(File(firstVideoPath)))
                    val thumbnailPath = thumbnailResult.first?.toString()

                    if (thumbnailPath != null) {
                        projectDao.updateThumbnailPath(projectId, thumbnailPath)
                        Log.d("NewProjectViewModel", "Thumbnail path updated in Room database")
                    } else {
                        Log.e("NewProjectViewModel", "Failed to generate thumbnail")
                        // Handle the error appropriately (e.g., display a message)
                    }
                }

                // Calculate total duration (using file paths)
                val totalDurationMillis = calculateTotalDurationFromFiles(context, filePaths)
                val formattedDuration = formatDuration(totalDurationMillis)

                // Update the project in the Room database with the total duration
                projectDao.updateProjectDuration(projectId, formattedDuration)
                Log.d("NewProjectViewModel", "Project duration updated in Room database")

                // Initialize an empty mediaNames map (using file paths as keys)
                val mediaNames = mutableMapOf<String, String>()

                // Upload videos to cloud storage (using file paths)
                val userFolderRef = storageRef.child("users/${user.uid}/projects/$projectId/videos")

                // Use async to handle video uploads concurrently
                val videoUploadDeferreds = filePaths.mapIndexed { index, filePath ->
                    async(Dispatchers.IO) {
                        val fileName = "video_${index}_${System.currentTimeMillis()}.mp4"
                        val fileRef = userFolderRef.child(fileName)
                        mediaNames[fileName] = filePath

                        // Create a temporary file for compressed video
                        val compressedVideoFile = File.createTempFile("compressed_video_$index", ".mp4", context.cacheDir)

                        // Compress video and save to temporary file
                        if (File(filePath).exists()) {
                            Transcoder.into(compressedVideoFile.absolutePath)
                                .addDataSource(filePath)
                                // Add compression configuration options here
                                .setListener(object : TranscoderListener {
                                    override fun onTranscodeProgress(progress: Double) {
                                        Log.d("NewProjectViewModel", "Video compression progress: $progress")
                                    }

                                    override fun onTranscodeCompleted(successCode: Int) {
                                        // No specific action needed here since upload happens after compression
                                    }

                                    override fun onTranscodeCanceled() {
                                        Log.w("FirebaseStorage", "Compression canceled")
                                        compressedVideoFile.delete() // Delete the temporary file if compression is canceled
                                    }

                                    override fun onTranscodeFailed(exception: Throwable) {
                                        Log.e("FirebaseStorage", "Compression failed: ${exception.message}")
                                        // Use viewModelScope.launch to switch to the main thread
                                        viewModelScope.launch {
                                            Toast.makeText(context, "Compression failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                        compressedVideoFile.delete()
                                    }
                                })
                                .transcode()
                        } else {
                            Log.e("NewProjectViewModel", "File not found for compression: $filePath")
                            return@async null // Indicate skipped upload
                        }

                        // Upload compressed video if it exists
                        if (File(compressedVideoFile.absolutePath).exists()) {
                            try {
                                fileRef.putFile(Uri.fromFile(compressedVideoFile)).await() // Use await() for coroutines
                                // Switch to the main thread to show the Toast
                                withContext(Dispatchers.Main) {
                                    Log.d("FirebaseStorage", "File uploaded: ${fileRef.path}")
                                    Toast.makeText(context, "File uploaded: ${fileRef.path}", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                // Handle upload failure
                                withContext(Dispatchers.Main) {
                                    Log.e("FirebaseStorage", "Upload failed: ${e.message}")
                                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            null // Indicate failed compression or missing file
                        }
                    }
                }

                // Wait for all video uploads to complete
                val videoUploadResults = videoUploadDeferreds.awaitAll()
                if (videoUploadResults.all { it != null }) {
                    Toast.makeText(context, "Videos uploaded successfully", Toast.LENGTH_SHORT).show()

                    // Handle audio saving and upload (if selected)
                    var audioUploadDeferred: Deferred<Unit?>? = null
                    if (audioUri != null) {
                        val audioFilePath = saveAudioToMediaStore(context, audioUri)

                        if (audioFilePath != null) {
                            projectDao.updateAudioPath(projectId, audioFilePath)
                            Log.d("NewProjectViewModel", "Audio path updated in Room database")

                            val audioFileName = "audio_${System.currentTimeMillis()}.mp3"
                            val audioFileRef = storageRef.child("users/${user.uid}/projects/$projectId/audios/$audioFileName")

                            audioUploadDeferred = async(Dispatchers.IO) {
                                audioFileRef.putFile(Uri.fromFile(File(audioFilePath))).await() // Use await() for coroutines
                                Log.d("FirebaseStorage", "Audio file uploaded: ${audioFileRef.path}")

                                // Switch to the main thread to show the Toast
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Audio file uploaded: ${audioFileRef.path}", Toast.LENGTH_SHORT).show()
                                }

                                null // Indicate success
                            }
                        } else {
                            Log.e("NewProjectViewModel", "Failed to save audio to MediaStore")
                        }
                    }

                    // Wait for audio upload (if any) and then send prompt data to Firestore
                    audioUploadDeferred?.await()

                    // Send prompt and video directory to Firestore
                    sendPromptDataToFirestore(context, user, projectId, prompt, mediaNames, projectDao)

                } else {
                    // Handle video upload failures
                    Toast.makeText(context, "Error uploading videos", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
                // Handle the error appropriately
            }
        }
    }

    // Function to send prompt data to Firestore after all uploads are complete
    private fun sendPromptDataToFirestore(
        context: Context,
        user: FirebaseUser,
        projectId: Int,
        prompt: String,
        mediaNames: MutableMap<String, String>,
        projectDao: ProjectDao
    ) {
        val promptData = hashMapOf(
            "prompt" to prompt,
            "videoDirectory" to "users/${user.uid}/projects/$projectId/videos",
            "audioDirectory" to "users/${user.uid}/projects/$projectId/audios",
            "userId" to user.uid,
            "projectId" to projectId
        )

        val promptDocRef = firestoreDb.collection("users")
            .document(user.uid)
            .collection("projects")
            .document(projectId.toString())
            .collection("prompts")
            .document()

        promptDocRef.set(promptData)
            .addOnSuccessListener {
                Log.d("Firestore", "Prompt data added with ID: ${promptDocRef.id}")
                Toast.makeText(context, "Prompt data added with ID: ${promptDocRef.id}", Toast.LENGTH_SHORT).show()

                // Update Room database and trigger video processing in a coroutine
                viewModelScope.launch {
                    projectDao.updatePromptData(projectId, prompt, promptDocRef.id)
                    Log.d("NewProjectViewModel", "Prompt data updated in Room database")

                    projectDao.updateMediaNames(projectId, mediaNames)
                    projectDao.updateUploadCompleted(projectId, true)
                    Log.d("NewProjectViewModel", "Media names and upload status updated in Room database")

                    // Trigger video processing (fire-and-forget)
                    launch(Dispatchers.IO) {
                        try {
                            val baseUrl = "http://192.168.1.30:5000"
                            triggerVideoProcessing(user.uid, prompt, projectId, promptDocRef.id, baseUrl)
                        } catch (e: Exception) {
                            Log.e("NewProjectViewModel", "Error triggering video processing: ${e.message}")
                            // Handle the error appropriately (e.g., log it or show a message to the user)
                        }
                    }

                    // Set project creation initiated flag
                    _projectCreationInitiated.value = true
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding prompt data", e)
                Toast.makeText(context, "Error saving prompt data", Toast.LENGTH_SHORT).show()
            }
    }

    // Helper function to save audio to MediaStore and return its file path
    private fun saveAudioToMediaStore(context: Context, audioUri: Uri): String? {
        val fileName = getFileName(context, audioUri) ?: return null
        val contentValues = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg") // Adjust MIME type if needed
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC)
                put(MediaStore.Audio.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        var audioFileUri: Uri? = null
        var outputStream: OutputStream? = null

        try {
            audioFileUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            outputStream = audioFileUri?.let { resolver.openOutputStream(it) }
            val inputStream = resolver.openInputStream(audioUri)
            inputStream?.use { input ->
                outputStream?.use { output ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            // Return the file path of the saved audio
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android Q and above, copy the file to a temporary file and return its path
                val tempFile = File.createTempFile("temp_audio", ".mp3", context.cacheDir)
                val tempOutputStream = FileOutputStream(tempFile)
                if (audioFileUri != null) {
                    resolver.openInputStream(audioFileUri)?.use { inputStream ->
                        inputStream.copyTo(tempOutputStream)
                    }
                }
                tempFile.absolutePath
            } else {
                // For older Android versions, get the file path directly from the content URI
                val audioFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), fileName)
                audioFile.absolutePath
            }
        } catch (e: Exception) {
            Log.e("NewProjectViewModel", "Failed to save audio to MediaStore: ${e.message}")
            return null
        } finally {
            outputStream?.close()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                audioFileUri?.let { resolver.update(it, contentValues, null, null) }
            }
        }
    }

    private suspend fun getThumbnailUriFromVideo(
        context: Context,
        videoUri: Uri
    ): Pair<Uri?, String?> {
        return try {
            withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(context, videoUri)
                val bitmap = retriever.frameAtTime
                val videoName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                    ?: getFileNameFromUri(context, videoUri)
                retriever.release()

                val thumbnailUri = bitmap?.let {
                    val imageName = "thumbnail_${System.currentTimeMillis()}.jpg"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, imageName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(
                                MediaStore.MediaColumns.RELATIVE_PATH,
                                Environment.DIRECTORY_PICTURES
                            )
                            put(MediaStore.MediaColumns.IS_PENDING, 1) // Mark as pending
                        }
                    }

                    val resolver = context.contentResolver
                    var imageUri: Uri? = null
                    var outputStream: OutputStream? = null

                    try {
                        imageUri = resolver.insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                        )
                        outputStream = imageUri?.let { resolver.openOutputStream(it) }
                        if (outputStream != null) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        }
                    } catch (e: Exception) {
                        Log.e("ThumbnailError", "Failed to create thumbnail: ${e.message}", e)
                    } finally {
                        outputStream?.close()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                            imageUri?.let { resolver.update(it, contentValues, null, null) }
                        }
                    }

                    imageUri
                }

                Pair(thumbnailUri, videoName)
            }
        } catch (e: Exception) {
            Log.e("ThumbnailError", "Error getting thumbnail: ${e.message}", e)
            Pair(null, null)
        }
    }

    // Helper function to calculate total duration from file paths
    private fun calculateTotalDurationFromFiles(context: Context, filePaths: List<String>): Long {
        var totalDurationMillis = 0L
        for (filePath in filePaths) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(filePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull()?.let {
                totalDurationMillis += it
            }
            retriever.release()
        }
        return totalDurationMillis
    }

    @SuppressLint("DefaultLocale")
    private fun formatDuration(durationMillis: Long): String {
        val totalSeconds = durationMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private suspend fun triggerVideoProcessing(
        userId: String,
        prompt: String,
        projectId: Int,
        promptId: String,
        baseUrl: String
    ) {

        val geminiRequest = GeminiRequest(userId, prompt, projectId, promptId)

        // Create GeminiResponseApi instance with baseUrl
        val geminiResponseApi = RetrofitClient.createGeminiResponseApi(baseUrl)

        // Make the network request to the Flask backend
        val response = geminiResponseApi.processVideos(geminiRequest)

        // Log the response code for basic error handling
        Log.d("NewProjectViewModel", "Video processing triggered, response code: ${response.code()}")
    }

}