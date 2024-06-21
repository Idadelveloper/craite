package com.example.craite

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.core.os.bundleOf
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
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import androidx.media3.common.util.Util
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.ExportResult
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.example.craite.utils.CommonUtils.Companion.bundleToString
import com.google.common.collect.ImmutableList
import com.google.errorprone.annotations.CanIgnoreReturnValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import okhttp3.internal.toImmutableList


class NewProjectViewModel: ViewModel() {
    private val storageRef = Firebase.storage.reference
    private val firestoreDb = Firebase.firestore
    fun createProject(
        projectDao: ProjectDao,
        projectName: String,
        uris: List<Uri>,
        context: Context,
        navController: NavController,
        user: FirebaseUser,
        prompt: String
    ) {
        val project = Project(name = projectName, media = uris)
        Log.d("media uri: ", "here are the project $uris")
        viewModelScope.launch {
            try {
                projectDao.insert(project)
                Toast.makeText(context, "Successfully created project", Toast.LENGTH_SHORT).show()

                // upload videos to cloud storage
                val projectId = projectDao.getLastInsertedProject().id
                val userFolderRef = storageRef.child("users/${user.uid}/projects/$projectId/videos")


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

                        // Send prompt and video directory to Firestore with similar path structure
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
                                Log.d("Firestore", "Prompt data added with ID: ${promptDocRef.id}")
                                Toast.makeText(context, "Prompt data added with ID: ${promptDocRef.id}", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("Firestore", "Error adding prompt data", e)
                                Toast.makeText(context, "Error saving prompt data", Toast.LENGTH_SHORT).show()
                            }

                        navController.navigate("video_edit_screen/$projectId")
                    } else {
                        Toast.makeText(context, "Error uploading videos", Toast.LENGTH_SHORT).show()
                        // Handle the failure
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
            }
        }
    }

}