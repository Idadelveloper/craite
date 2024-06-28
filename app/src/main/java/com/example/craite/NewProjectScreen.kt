package com.example.craite

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.craite.data.models.ProjectDatabase
import com.google.firebase.auth.FirebaseUser


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NewProject(
    navController: NavController,
    projectDatabase: ProjectDatabase,
    context: Context,
    user: FirebaseUser?
) {
    val newProjectViewModel: NewProjectViewModel = viewModel()
    var projectName by remember { mutableStateOf("") }
    var selectedMedia by remember {
        mutableStateOf(emptyList<Uri>())
    }
    val prompt = "What do you think is going on in these videos"

    val projectCreationInitiated by newProjectViewModel.projectCreationInitiated.collectAsState()

    val pickMultipleMedia = rememberLauncherForActivityResult(PickMultipleVisualMedia(10)) {uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            Log.d("PhotoPicker", "Number of items selected: ${uris.size}")
            selectedMedia = uris
            Log.d("PhotoPicker", "Selected media: $selectedMedia")
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    // Trigger navigation when project creation is initiated
    LaunchedEffect(key1 = projectCreationInitiated) {
        if (projectCreationInitiated) {
            val projectId = projectDatabase.projectDao().getLastInsertedProject().id
            navController.navigate("video_edit_screen/$projectId")
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch the media picker
            pickMultipleMedia.launch(PickVisualMediaRequest(PickVisualMedia.VideoOnly))
        } else {
            // Permission denied, handle accordingly (e.g., show a message)
            Log.d("Permission", "READ_MEDIA_VIDEO permission denied")
            // You might want to display a Snackbar or a Dialog here
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Project Name") },

        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Button(
                        onClick = {
                            // Request permission before launching picker
                            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
                        }
                    ) {
                        Text("Add Videos")
                    }
                    Button(
                        onClick = {
                            if (user != null) {
                                newProjectViewModel.createProject(
                                    projectDatabase.projectDao(),
                                    projectName,
                                    selectedMedia,
                                    context,
                                    navController, // unnecessary but whatever
                                    user,
                                    prompt
                                )
                            }
                        }
                    ) {
                        Text("Create Project")
                    }
                }
            }
            items(selectedMedia.size) { index ->
                Text("Selected video $index")
            }
        }

    }
}

