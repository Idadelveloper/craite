package com.example.craite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.craite.data.EditSettings
import com.example.craite.data.MediaEffect
import com.example.craite.data.TextOverlay
import com.example.craite.data.VideoEdit
import com.example.craite.data.models.ProjectDatabase
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditScreen(
    mediaFilePaths: List<String>,
    navController: NavController,
    user: FirebaseUser?,
    projectDatabase: ProjectDatabase
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    // Use ViewModel to manage selected media item index
    val viewModel = remember { VideoEditViewModel(generateFakeEditSettings()) }
    val currentMediaIndex by viewModel.currentMediaItemIndex.collectAsState()

    // Convert file paths to URIs
    val mediaUris = mediaFilePaths.map { Uri.fromFile(File(it)) }
    val mediaItemMap = mediaUris.indices.associateWith { MediaItem.fromUri(mediaUris[it]) }

    val uiState by viewModel.uiState.collectAsState()


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        topBar = {
            TopAppBar(title = { Text("Craite - Edit Media") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Media Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AndroidView(
                    factory = { context ->
                        PlayerView(context).apply {
                            player = exoPlayer
                            // Set media item based on ViewModel state
                            mediaItemMap[currentMediaIndex]?.let {
                                exoPlayer.setMediaItem(it)
                                exoPlayer.prepare()
                                exoPlayer.playWhenReady = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Media List (Horizontally Scrollable)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaItemMap.entries.toList()) { entry ->
                    val index = entry.key
                    val mediaItem = entry.value
                    MediaItemThumbnail(mediaUris[index], index) { clickedIndex ->
                        viewModel.setCurrentMediaItemIndex(clickedIndex) // Update ViewModel state
                        }
                    }
                }
            }
            Button(onClick = {

            }) { Text("Upload Videos") }

            // Editing Controls (Add your desired controls here)
            // ... (Same as before, but now operate on the selected media item)
    }

    // Release ExoPlayer when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun MediaItemThumbnail(uri: Uri, index: Int, onClick: (Int) -> Unit) {
    // Use Coil or another image loading library to display thumbnail
//    AsyncImage(
//        model = uri,
//        contentDescription = "Video Thumbnail",
//        modifier = Modifier
//            .size(100.dp)
//            .clickable { onClick(index) }, // Call onClick with index when clicked
//        contentScale = ContentScale.Crop
//    )
    Button(onClick={}) { }
}


fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun generateFakeEditSettings(): EditSettings {
    val videoEdits = listOf(
        VideoEdit(
            edit = "trim",
            effects = listOf(
                MediaEffect(adjustment = 0.5, name = "brightness"),
                MediaEffect(adjustment = -0.2, name = "contrast")
            ),
            end_time = 15.0,
            id = 1,
            start_time = 5.0,
            text = listOf(
                TextOverlay(color = "#FFFFFF", duration = 3, font_size = 24, label = "Hello", position = "top-left")
            ),
            transition = "fade",
            video_name = "video1.mp4"
        ),
        VideoEdit(
            edit = "crop",
            effects = listOf(
                MediaEffect(adjustment = 1.2, name = "saturation")
            ),
            end_time = 25.0,
            id = 2,
            start_time = 10.0,
            text = emptyList(),
            transition = "slide",
            video_name = "video2.mp4"
        )
    )

    return EditSettings(video_edits = videoEdits)
}
