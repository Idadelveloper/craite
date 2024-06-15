package com.example.craite

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.craite.data.Project
import com.example.craite.data.ProjectDao
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.launch
import com.google.android.gms.tasks.Tasks

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


}