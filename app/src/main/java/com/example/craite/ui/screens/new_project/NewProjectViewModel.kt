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
        uris: List<Uri>,
        context: Context,
        user: FirebaseUser,
        prompt: String
    ) {
        viewModelScope.launch {
            try {
                // Copy videos to internal storage and get file paths
                val filePaths = uris.mapNotNull { copyVideoToInternalStorage(context, it) }

                // Create project with file paths
                val project = Project(name = projectName, media = filePaths)
                projectDao.insert(project)
                Toast.makeText(context, "Successfully created project", Toast.LENGTH_SHORT).show()

                // Generate and save thumbnail for the first video
                if (uris.isNotEmpty()) {
                    val firstVideoUri = uris[0]
                    val thumbnailUri = getThumbnailUriFromVideo(context, firstVideoUri)
                    val thumbnailPath = thumbnailUri.first?.toString() // Get the path from Uri

                    // Update the project in the Room database with the thumbnail path
                    if (thumbnailPath != null) {
                        val projectId = projectDao.getLastInsertedProject().id
                        projectDao.updateThumbnailPath(projectId, thumbnailPath)
                        Log.d("NewProjectViewModel", "Thumbnail path updated in Room database")
                    }
                }

                // Calculate total duration
                val totalDurationMillis = calculateTotalDuration(context, uris)
                val formattedDuration = formatDuration(totalDurationMillis)

                // Update the project in the Room database with the total duration
                val projectId = projectDao.getLastInsertedProject().id
                projectDao.updateProjectDuration(projectId, formattedDuration)
                Log.d("NewProjectViewModel", "Project duration updated in Room database")

                // Upload videos to cloud storage (using file paths)
                val userFolderRef = storageRef.child("users/${user.uid}/projects/$projectId/videos")

                val uploadTasks = filePaths.mapIndexed { index, filePath ->
                    val fileName = "video_${index}_${System.currentTimeMillis()}.mp4"
                    val fileRef = userFolderRef.child(fileName)

                    // Create a temporary file for compressed video
                    val compressedVideoFile =
                        File.createTempFile("compressed_video_$index", ".mp4", context.cacheDir)

                    // Compress video and save to temporary file (using async to avoid blocking)
                    val transcodeDeferred = async(Dispatchers.IO) {
                        Transcoder.into(compressedVideoFile.absolutePath)
                            .addDataSource(filePath)
                            .setListener(object : TranscoderListener {
                                override fun onTranscodeProgress(progress: Double) {
                                    Log.d("FirebaseStorage", "Compression progress: $progress")
                                }

                                override fun onTranscodeCompleted(successCode: Int) {
                                    // Compression successful, initiate upload
                                    val compressedVideoUri = Uri.fromFile(compressedVideoFile)
                                    fileRef.putFile(compressedVideoUri)
                                        .addOnSuccessListener {
                                            // *** YOUR SUCCESS LOGIC HERE ***
                                            videoNames[fileName] = filePaths[index]
                                            Log.d(
                                                "FirebaseStorage",
                                                "File uploaded: ${fileRef.path}"
                                            )
                                            Toast.makeText(
                                                context,
                                                "File uploaded: ${fileRef.path}",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // ... (Rest of your success logic) ...

                                            // Delete the temporary file after successful upload
                                            compressedVideoFile.delete()
                                        }
                                        .addOnFailureListener { exception ->
                                            // *** YOUR EXISTING FAILURE LOGIC HERE ***
                                            Log.e(
                                                "FirebaseStorage",
                                                "Upload failed: ${exception.message}"
                                            )
                                            Toast.makeText(
                                                context,
                                                "Upload failed: ${exception.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            // Delete the temporary file even if upload fails
                                            compressedVideoFile.delete()
                                        }
                                }

                                override fun onTranscodeCanceled() {
                                    // Handle cancellation if needed
                                }

                                override fun onTranscodeFailed(exception: Throwable) {
                                    // *** YOUR EXISTING FAILURE LOGIC (if different) HERE ***
                                    Log.e(
                                        "FirebaseStorage",
                                        "Compression failed: ${exception.message}"
                                    )
                                    compressedVideoFile.delete()
                                }
                            })
                            .transcode() // Start transcoding
                    }

                    // Wait for transcoding to complete and return the UploadTask
                    transcodeDeferred.await() // Wait for transcoding
                    fileRef.putFile(Uri.fromFile(compressedVideoFile)) // Return the UploadTask
                }

                // Wait for all uploads to complete
                Tasks.whenAllComplete(uploadTasks).addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.all { it.isSuccessful }) {
                        // All uploads successful
                        Toast.makeText(context, "Videos uploaded successfully", Toast.LENGTH_SHORT)
                            .show()

                        // Send prompt and video directory to Firestore
                        val promptData = hashMapOf(
                            "prompt" to prompt,
                            "videoDirectory" to "users/${user.uid}/projects/$projectId/videos",
                            "userId" to user.uid,
                            "projectId" to projectId
                        )

                        // Create a Firestore document path mirroring the Storage path
                        val promptDocRef = firestoreDb.collection("users")
                            .document(user.uid)
                            .collection("projects")
                            .document(projectId.toString())
                            .collection("prompts")
                            .document() // Generate a unique ID for the prompt document

                        promptDocRef.set(promptData)
                            .addOnSuccessListener {
                                Log.d(
                                    "Firestore",
                                    "Prompt data added with ID: ${promptDocRef.id}"
                                )
                                Toast.makeText(
                                    context,
                                    "Prompt data added with ID: ${promptDocRef.id}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                // Update prompt and promptId in the room database
                                viewModelScope.launch {
                                    projectDao.updatePromptData(
                                        projectId,
                                        prompt,
                                        promptDocRef.id
                                    )
                                    Log.d(
                                        "NewProjectViewModel",
                                        "Prompt data updated in room database"
                                    )
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error adding prompt data", e)
                                Toast.makeText(
                                    context,
                                    "Error saving prompt data",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        _projectCreationInitiated.value = true
                    } else {
                        // Handle upload failures
                        Toast.makeText(context, "Error uploading videos", Toast.LENGTH_SHORT).show()
                        // ... (Your failure handling logic) ...
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
                // ... (Your error handling logic) ...
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

    private fun calculateTotalDuration(context: Context, uris: List<Uri>): Long {
        var totalDurationMillis = 0L
        for (uri in uris) {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationStr =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
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