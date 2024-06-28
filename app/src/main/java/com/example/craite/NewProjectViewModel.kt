package com.example.craite

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.craite.data.models.Project
import com.example.craite.data.models.ProjectDao
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class NewProjectViewModel: ViewModel() {
    private val storageRef = Firebase.storage.reference
    private val firestoreDb = Firebase.firestore
    private var videoNames  = mutableMapOf<String, String>() // video name: uri string

    private val _projectCreationInitiated = MutableStateFlow(false)
    val projectCreationInitiated: StateFlow<Boolean> = _projectCreationInitiated.asStateFlow()
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
                            videoNames[fileName] = uri.toString()
                            Log.d("FirebaseStorage", "File uploaded: ${fileRef.path}")
                            Toast.makeText(context, "File uploaded: ${fileRef.path}", Toast.LENGTH_SHORT).show()

                            // store key-value pairs of uploaded video names and uris
                            project.mediaNames = videoNames
                            Log.d("VideoNames", "Video names: $videoNames")
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

                        _projectCreationInitiated.value = true
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