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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

                val uploadTasks = filePaths.mapIndexed { index, filePath ->
                    val fileName = "video_${index}_${System.currentTimeMillis()}.mp4"
                    val fileRef = userFolderRef.child(fileName)
                    mediaNames[fileName] = filePath // Store the mapping (file path to itself)

                    // Create a temporary file for compressed video
                    val compressedVideoFile = File.createTempFile("compressed_video_$index", ".mp4", context.cacheDir)

                    // Compress video and save to temporary file (using async to avoid blocking)
                    val transcodeDeferred = async(Dispatchers.IO) {
                        if (File(filePath).exists()) { // Check if file exists before compression
                            Transcoder.into(compressedVideoFile.absolutePath)
                                .addDataSource(filePath)
                                // Add compression configuration options here (e.g., resolution, bitrate)
                                .setListener(object : TranscoderListener {
                                    override fun onTranscodeProgress(progress: Double) {
                                        Log.d("FirebaseStorage", "Compression progress: $progress")
                                        // You can update UI with progress here if needed (using withContext(Dispatchers.Main))
                                    }

                                    override fun onTranscodeCompleted(successCode: Int) {
                                        val compressedVideoUri = Uri.fromFile(compressedVideoFile)
                                        fileRef.putFile(compressedVideoUri)
                                            .addOnSuccessListener {
                                                Log.d("FirebaseStorage", "File uploaded: ${fileRef.path}")
                                                Toast.makeText(context, "File uploaded: ${fileRef.path}", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("FirebaseStorage", "Upload failed: ${exception.message}")
                                                Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                compressedVideoFile.delete() // Delete even if upload fails
                                            }
                                            .addOnCompleteListener {
                                                compressedVideoFile.delete() // Delete after upload attempt (success or failure)
                                            }
                                    }

                                    override fun onTranscodeCanceled() {
                                        Log.w("FirebaseStorage", "Compression canceled")
                                        compressedVideoFile.delete() // Delete if compression is canceled
                                    }

                                    override fun onTranscodeFailed(exception: Throwable) {
                                        Log.e("FirebaseStorage", "Compression failed: ${exception.message}")
                                        Toast.makeText(context, "Compression failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        compressedVideoFile.delete() // Delete even if compression fails
                                    }
                                })
                                .transcode()
                        } else {
                            Log.e("NewProjectViewModel", "File not found for compression: $filePath")
                            null // Return null to indicate skipped upload
                        }
                    }

                    // Wait for transcoding and return the UploadTask (or null if skipped)
                    transcodeDeferred.await()
                    if (File(compressedVideoFile.absolutePath).exists()) {
                        fileRef.putFile(Uri.fromFile(compressedVideoFile))
                    } else {
                        null // Return null if compression failed or file doesn't exist
                    }
                }.filterNotNull() // Filter out null tasks (skipped uploads)

                // Wait for all uploads to complete
                Tasks.whenAllComplete(uploadTasks).addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.all { it.isSuccessful }) {
                        // All uploads successful
                        Toast.makeText(context, "Videos uploaded successfully", Toast.LENGTH_SHORT).show()

                        viewModelScope.launch {
                            // Handle audio saving and upload (if selected)
                            var audioFilePath: String? = null
                            if (audioUri != null) {
                                // Save audio to MediaStore and get its file path
                                audioFilePath = saveAudioToMediaStore(context, audioUri)

                                // Update the project in the Room database with the audio file path
                                if (audioFilePath != null) {
                                    projectDao.updateAudioPath(projectId, audioFilePath)
                                    Log.d("NewProjectViewModel", "Audio path updated in Room database")

                                } else {
                                    Log.e("NewProjectViewModel", "Failed to save audio to MediaStore")
                                    // Handle the error appropriately (e.g., display a message)
                                }

                                // Upload audio to cloud storage (if saving to MediaStore was successful)
                                if (audioFilePath != null) {
                                    val audioFileName = "audio_${System.currentTimeMillis()}.mp3" // Or get the actual file name if possible
                                    // Updated Firebase Storage path for audio
                                    val audioFileRef = storageRef.child("users/${user.uid}/projects/$projectId/audios/$audioFileName")

                                    audioFileRef.putFile(Uri.fromFile(File(audioFilePath)))
                                        .addOnSuccessListener {
                                            Log.d("FirebaseStorage", "Audio file uploaded: ${audioFileRef.path}")
                                            Toast.makeText(context, "Audio file uploaded: ${audioFileRef.path}", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e("FirebaseStorage", "Audio upload failed: ${exception.message}")
                                            Toast.makeText(context, "Audio upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }

                        // Send prompt and video directory to Firestore
                        val promptData = hashMapOf(
                            "prompt" to prompt,
                            "videoDirectory" to "users/${user.uid}/projects/$projectId/videos",
                            "audioDirectory" to "users/${user.uid}/projects/$projectId/audios",
                            "userId" to user.uid,
                            "projectId" to projectId
                        )

                        // Create a Firestore document path mirroring the Storage path
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

                                // Update prompt and promptId in the Room database
                                viewModelScope.launch {
                                    projectDao.updatePromptData(projectId, prompt, promptDocRef.id)
                                    Log.d("NewProjectViewModel", "Prompt data updated in Room database")

                                    // Update the project in the Room database with the mediaNames map
                                    projectDao.updateMediaNames(projectId, mediaNames)
                                    projectDao.updateUploadCompleted(projectId, true)
                                    Log.d("NewProjectViewModel", "Media names and upload status updated in Room database")

                                    _projectCreationInitiated.value = true // Signal project creation completion
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error adding prompt data", e)
                                Toast.makeText(context, "Error saving prompt data", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Handle upload failures
                        Toast.makeText(context, "Error uploading videos", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
                // Handle the error appropriately
            }
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

    private fun triggerVideoProcessing(
        userId: String,
        prompt: String,
        projectId: Int,
        promptId: String
    ) {
        viewModelScope.launch {
            val repository: EditSettingsRepository =
                EditSettingsRepositoryImpl(RetrofitClient.geminiResponseApi)
            repository.getEditSettings(GeminiRequest(userId, prompt, projectId, promptId))
                .collect { result ->
                    when (result) {
                        is GeminiResult.Success -> {
                            val editSettings = result.data
                            Log.d("VideoEditViewModel", "Edit settings received: $editSettings")
                            // You can store these edit settings in your database or use them immediately
                            // ... (Call your VideoEditor functions to apply edits if needed)
                        }

                        is GeminiResult.Error -> {
                            Log.e(
                                "VideoEditViewModel",
                                "Error fetching edit settings: ${result.message}"
                            )
                            // Handle the error appropriately (e.g., show a message to the user)
                        }

                    }
                }
        }
    }
}