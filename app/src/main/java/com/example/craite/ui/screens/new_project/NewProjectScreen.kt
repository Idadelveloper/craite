package com.example.craite.ui.screens.new_project


import android.Manifest
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.craite.R
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.ui.screens.composables.CraiteTextField
import com.example.craite.ui.screens.composables.GradientImageBackground
import com.example.craite.ui.screens.new_project.composables.FootageThumbnail
import com.example.craite.ui.theme.AppColor
import com.google.firebase.auth.FirebaseUser
import java.io.ByteArrayOutputStream


//@Preview(showBackground = true)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun NewProjectScreen(
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
    var prompt by remember { mutableStateOf("") }

    val projectCreationInitiated by newProjectViewModel.projectCreationInitiated.collectAsState()

    var thumbnailData by remember { mutableStateOf<List<Pair<Uri?, String?>>>(emptyList()) }
    var footagesNotSelected by remember { mutableStateOf(true) }

    val pickMultipleMedia = rememberLauncherForActivityResult(PickMultipleVisualMedia(10)) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            selectedMedia = uris
            footagesNotSelected = false
            thumbnailData = uris.map { uri ->
                getThumbnailUriFromVideo(context, uri)
                }
            }
        }

    val localConfiguration: Configuration = LocalConfiguration.current

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
            // Permission denied
            Log.d("Permission", "READ_MEDIA_VIDEO permission denied")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
            GradientImageBackground(
                modifier = Modifier.height((localConfiguration.screenHeightDp * .8).dp),
                painter = painterResource(R.drawable.surfing),
                contentDescription = "Surfer surfing",
                gradientColor = AppColor().black
            )
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(.2f))
                Text(
                    text = "Create Project",
                    style = MaterialTheme.typography.headlineSmall,
//                    onTextLayout = null
                )
                Spacer(modifier = Modifier.height(24.dp))

                CraiteTextField(
                    label = "Project Name",
                    hintText = "Give your project a suitable name",
                    onValueChanged = { newProjectName ->
                        projectName = newProjectName
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Footages", style = MaterialTheme.typography.titleMedium)
                // Conditional Thumbnail Display
                if (footagesNotSelected) {
                    // Show default surfing footage
                    Text(
                        text = "No selected media",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    )
                } else if (thumbnailData.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(thumbnailData) { item ->
                            item.first?.let {
                                FootageThumbnail(
                                    modifier = Modifier.width(100.dp),
                                    uri = it,
                                    videoName = item.second
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        // Request permission before launching picker
                        requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import Footages",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CraiteTextField(
                    label = "Prompt",
                    hintText = "What's happening in your videos and what do you expect in the final video?",
                    minLines = 6,
                    onValueChanged = { newPrompt ->
                        prompt = newPrompt
                    }
                )


                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        if (user != null) {
                            newProjectViewModel.createProject(
                                projectDatabase.projectDao(),
                                projectName,
                                selectedMedia,
                                context,
                                user,
                                prompt
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {

                    Text(text = "Create Project")

                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

fun getThumbnailUriFromVideo(context: Context, videoUri: Uri): Pair<Uri?, String?> {
    return try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, videoUri)
        val bitmap = retriever.frameAtTime
        val videoName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?: getFileNameFromUri(context, videoUri)
        retriever.release()
        Log.d("Thumbnail", "Video name: $videoName")

        val thumbnailUri = bitmap?.let {
            val bytes = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
            val path = MediaStore.Images.Media.insertImage(
                context.contentResolver, it, "Title", null
            )
            Uri.parse(path)
        }

        Pair(thumbnailUri, videoName) // Return both thumbnail URI and video name
    } catch (e: Exception) {
        Log.e("ThumbnailError", "Error getting thumbnail: ${e.message}")
        Pair(null, null) // Return nulls if extraction fails
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String? {
    return try {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                it.getString(nameIndex)
            } else {
                null
            }
        }
    } catch (e: Exception) {
        Log.e("FileNameError", "Error getting file name: ${e.message}")
        null
    }
}


