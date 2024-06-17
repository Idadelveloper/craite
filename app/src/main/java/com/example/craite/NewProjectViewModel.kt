package com.example.craite

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Transformer
import androidx.navigation.NavController
import com.example.craite.data.Project
import com.example.craite.data.ProjectDao
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.media3.common.util.Util
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportResult
import com.google.common.collect.ImmutableList
import com.google.errorprone.annotations.CanIgnoreReturnValue
import kotlinx.coroutines.tasks.await
import okhttp3.internal.toImmutableList


class NewProjectViewModel: ViewModel() {
    private val storageRef = Firebase.storage.reference
    fun createProject(
        projectDao: ProjectDao,
        projectName: String,
        uris: List<Uri>,
        context: Context,
        navController: NavController,
        user: FirebaseUser
    ) {
        val project = Project(name = projectName, media = uris)
        Log.d("media uri: ", "here are the project $uris")
        viewModelScope.launch {
            try {
                projectDao.insert(project)
                Toast.makeText(context, "Successfully created project", Toast.LENGTH_SHORT).show()

                // upload videos to cloud storage
                val projectId = projectDao.getLastInsertedProject().id
                val userFolderRef = storageRef.child("users/${user.uid}/projects/$projectId")


                val uploadCompletionSource = Tasks.whenAllComplete(uris.mapIndexed { index, uri ->
                    val fileName = "video_${index}_${System.currentTimeMillis()}.mp4"
                    val fileRef = userFolderRef.child(fileName)
                    fileRef.putFile(uri)
                        .addOnSuccessListener {
                            Log.d("FirebaseStorage", "File uploaded: ${fileRef.path}")
                            Toast.makeText(context, "File uploaded: ${fileRef.path}", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener { exception ->
                            Log.e("FirebaseStorage", "Upload failed: ${exception.message}")
                            // Handle the failure (e.g., show an error message)
                            Toast.makeText(context, "Upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                })

                uploadCompletionSource.addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result.all { it.isSuccessful }) {
                        Toast.makeText(context, "Videos uploaded successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate("video_edit_screen/$projectId")
                    } else {
                        Toast.makeText(context, "Error uploading videos", Toast.LENGTH_SHORT).show()
                        // Handle the failure (e.g., retry, delete the project)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
            }
        }
    }


    @OptIn(UnstableApi::class)
    private suspend fun mergeVideos(context: Context, uris: List<Uri>): Pair<Uri?, List<Int>> {
        val timestamps = mutableListOf<Int>()
        var totalDuration = 0

        // Create a temporary file for the merged video
        val mergedVideoFile = File(context.cacheDir, "merged_video_${System.currentTimeMillis()}.mp4")

        // Build a Composition using EditedMediaItem
//        val composition = Composition.Builder()
//            .setRemoveAudio(false) // Keep audio tracks
//            .build()

        // Create EditedMediaItem instances for each video and add to Composition
        val editedMediaItems = uris.map { uri ->
            val mediaItem = MediaItem.fromUri(uri)
            EditedMediaItem.Builder(mediaItem)
                .build()
                .also {
                    val duration = getVideoDuration(context, uri)
                    timestamps.add(totalDuration)
                    totalDuration += duration
                }
        }.toMutableList()

        val editedMediaItemsSequence = EditedMediaItemSequence(editedMediaItems)

        val composition = Composition.Builder(editedMediaItemsSequence)
            .experimentalSetForceAudioTrack(false)
            .build()

        val listener = object : Transformer.Listener {
            override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                super.onCompleted(composition, exportResult)
            }
        }

        // Use Transformer to export the merged video
        val transformer = Transformer.Builder(context)
            .addListener(listener)
            .build()

        // Start the export process and wait for completion
        withContext(Dispatchers.IO) {
            transformer.start(composition, mergedVideoFile.absolutePath)
        }

        // Return the URI of the merged video and the timestamps
        return Pair(Uri.fromFile(mergedVideoFile), timestamps)
    }

    // Helper function to get video duration
    private fun getVideoDuration(context: Context, uri: Uri): Int {
        try {
            var retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            Log.d("VideoDuration", "Duration: $durationString")
            return durationString?.toInt() ?: 0
        }
        catch (e: Exception) {
            e.printStackTrace()
            return 0
        }


    }
}