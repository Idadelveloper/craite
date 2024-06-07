package com.example.craite

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.craite.data.Project
import com.example.craite.data.ProjectRepository


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    navController: NavHostController,
    projectRepository: ProjectRepository,
    projectId: Int
) {
    val viewModel: ProjectViewModel = viewModel()
    viewModel.getProject(projectId, projectRepository)

    val uiState = viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = {
                when (uiState.value) {
                    is ProjectUiState.Loading -> Text("Loading...")
                    is ProjectUiState.Success -> Text((uiState.value as ProjectUiState.Success).project.name)
                    is ProjectUiState.Error -> Text("Error loading project")
                }
            })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            when (uiState.value) {
                is ProjectUiState.Loading -> {
                    // Show a loading indicator
                    CircularProgressIndicator()
                }
                is ProjectUiState.Success -> {
                    val project = (uiState.value as ProjectUiState.Success).project
                    Text(text = "Project Name: ${project.name}", style = MaterialTheme.typography.bodySmall)

                    // Display selected videos and images (with thumbnails)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        items(project.videos) { videoUri ->
                            VideoItem(videoUri = videoUri)
                        }
                        items(project.images) { imageUri ->
                            ImageItem(imageUri = imageUri)
                        }
                    }
                }
                is ProjectUiState.Error -> {
                    // Show an error message
                    Text("Error loading project")
                }
            }

            // Implement Editing Tools
            Button(onClick = { /* Handle video editing */ }) {
                Text("Edit Video")
            }
            Button(onClick = { /* Handle image editing */ }) {
                Text("Edit Image")
            }

            // Handle Gemini Integration
            // ... (Send data to Gemini, display response)

            // Implement Export Functionality
            Button(onClick = { /* Handle project export */ }) {
                Text("Export Project")
            }

            // Navigation
            Button(onClick = { navController.navigate("home") }) {
                Text("Back to Home")
            }
        }
    }
}

@Composable
fun VideoItem(videoUri: Uri) {
    val context = LocalContext.current
    val bitmap = getBitmapFromUri(context, videoUri)?.asImageBitmap()
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Video Thumbnail",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
fun ImageItem(imageUri: Uri) {
    val context = LocalContext.current
    val bitmap = getBitmapFromUri(context, imageUri)?.asImageBitmap()
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = "Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}


@SuppressLint("Recycle")
fun getBitmapFromUri(context: Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
        parcelFileDescriptor?.let {
            BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
        }
    } catch (e: Exception) {
        // Handle the exception (e.g., log the error)
        null
    }
}