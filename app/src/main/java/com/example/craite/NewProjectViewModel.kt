package com.example.craite

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.vector.path
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
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class NewProjectViewModel: ViewModel() {
    private val storageRef = Firebase.storage.reference
    private val firestoreDb = Firebase.firestore
    private var videoNames  = mutableMapOf<String, String>()

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
            // Handle exceptions
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

                // Upload videos to cloud storage (using file paths)
                val projectId = projectDao.getLastInsertedProject().id
                val userFolderRef = storageRef.child("users/${user.uid}/projects/$projectId/videos")

                val uploadCompletionSource = Tasks.whenAllComplete(filePaths.mapIndexed { index, filePath ->
                    val fileName = "video_${index}_${System.currentTimeMillis()}.mp4"
                    val fileRef = userFolderRef.child(fileName)
                    val fileUri = Uri.fromFile(File(filePath))
                    fileRef.putFile(fileUri)
                        .addOnSuccessListener {
                            videoNames[fileName] = filePath
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