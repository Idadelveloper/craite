package com.example.craite

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.example.craite.data.EditSettings
import com.example.craite.data.MediaEffect
import com.example.craite.data.CraiteTextOverlay
import com.example.craite.data.VideoEdit
import com.example.craite.data.models.ProjectDatabase
import com.google.firebase.auth.FirebaseUser
import java.io.File
import java.io.InputStream
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.text.padStart
import kotlin.text.toString


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
    val viewModel = remember { VideoEditViewModel(generateFakeEditSettings(mediaFilePaths.size)) }
    val currentMediaIndex by viewModel.currentMediaItemIndex.collectAsState()
    val mediaUris = mediaFilePaths.map { Uri.fromFile(File(it)) }
    val mediaItemMap = mediaUris.indices.associateWith { MediaItem.fromUri(mediaUris[it]) }
    val uiState by viewModel.uiState.collectAsState()
    val showProgressDialog by viewModel.showProgressDialog.collectAsState()

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
                    MediaItemThumbnail(mediaUris[index], index) { clickedIndex ->
                        viewModel.setCurrentMediaItemIndex(clickedIndex)
                    }
                }
            }

            Button(onClick = {
                val fakeEditSettings = generateFakeEditSettings(mediaFilePaths.size)
                Log.d("Download button", "starting download")
                viewModel.showProgressDialog()
                Toast.makeText(context, "Processing video", Toast.LENGTH_SHORT).show()

                viewModel.launch {
                    val videoEditor = VideoEditor(context, fakeEditSettings)
                    val mergedVideoPath = videoEditor.trimAndMergeToTempFile(mediaFilePaths, fakeEditSettings)

                    mergedVideoPath?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // MediaStore (Android 10 and above)
                            val contentValues = ContentValues().apply {
                                put(MediaStore.MediaColumns.DISPLAY_NAME, "merged_video_${System.currentTimeMillis()}.mp4")
                                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
                            }

                            val resolver = context.contentResolver
                            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

                            uri?.let { videoUri ->
                                resolver.openOutputStream(videoUri)?.use { outputStream ->
                                    File(it).inputStream().use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                    File(it).delete() // Delete temporary file
                                    println("Video saved to MediaStore: $videoUri")
                                    Toast.makeText(context, "Video saved to MediaStore: $videoUri", Toast.LENGTH_SHORT).show()


                                    // Update ExoPlayer with merged video URI (if needed)
                                    val mergedMediaItem = MediaItem.fromUri(videoUri)
                                    exoPlayer.setMediaItem(mergedMediaItem)
                                    exoPlayer.prepare()
                                    exoPlayer.playWhenReady = true
                                }
                            } ?: run {
                                println("Error saving to MediaStore: URI is null")
                            }
                        } else {
                            // Legacy Storage (Older Android versions)
                            // Notify MediaScanner about the new file
                            MediaScannerConnection.scanFile(
                                context,
                                arrayOf(it),
                                arrayOf("video/mp4"),
                                null
                            )
                            println("Video saved to: $it")
                            Toast.makeText(context, "Video saved to: $it", Toast.LENGTH_SHORT).show()

                            // Update ExoPlayer with merged video file path (if needed)
                            val mergedMediaItem = MediaItem.fromUri(Uri.fromFile(File(it)))
                            exoPlayer.setMediaItem(mergedMediaItem)
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                    } ?: run {
                        // Handle the case where trimming or merging failed
                        println("Trimming or merging failed.")
                    }

                    viewModel.hideProgressDialog()
                }
            }) {
                Text("Download Final Video")
            }
            Button(onClick = {
                // Trigger API request to Flask backend
                user?.let {
                    val projectId = 3
                    val promptId = "MnXzLYd8c3IUF4bFeW09"
                        viewModel.fetchEditSettings(
                            it.uid,
                            "I need a cute compilation of my this my small outing where I had ice cream",
                            projectId,
                            promptId
                        )
                }
            }) {
                Text("Process on Server")
            }
        }
    }

    // Release ExoPlayer when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    if (showProgressDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Processing Video") },
            text = { Text("Please wait...") },
            confirmButton = { }
        )
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
    Button(onClick = {}) { }
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

fun generateFakeEditSettings(numVideos: Int): EditSettings {
    val random = Random(System.currentTimeMillis())

    val videoEdits = mutableListOf<VideoEdit>()

    for (i in 1..numVideos) {
        val startTime = random.nextDouble(0.0, 1.0)
        val endTime = random.nextDouble(startTime + 2, 4.0)

        val effects = mutableListOf<MediaEffect>()
        if (random.nextBoolean()) {
            effects.add(MediaEffect(name = "brightness", adjustment = listOf(random.nextFloat() - 0.5f)))
        }
        if (random.nextBoolean()) {
            effects.add(MediaEffect(name = "contrast", adjustment = listOf(random.nextFloat())))
        }
        if (random.nextBoolean()) {
            effects.add(MediaEffect(name = "saturation", adjustment = listOf(random.nextFloat() + 0.5f)))
        }

        val textOverlay = if (random.nextBoolean()) {
            listOf(
                CraiteTextOverlay(
                    text = "Video $i",
                    font_size = random.nextInt(16, 32),
                    text_color = "#${random.nextInt(0xFFFFFF + 1).toString(16).padStart(6, '0')}",
                    background_color = "#80000000"
                )
            )
        } else {
            emptyList()
        }

        val transitions = listOf("fade", "slide", "crossfade")
        val transition = transitions[random.nextInt(transitions.size)]

        videoEdits.add(
            VideoEdit(
                effects = effects,
                end_time = endTime,
                id = i,
                start_time = startTime,
                text = textOverlay,
                transition = transition,
                video_name = "video$i.mp4"
            )
        )
    }

    return EditSettings(video_edits = videoEdits)
}
