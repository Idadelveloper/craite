package com.example.craite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseUser
import java.io.InputStream
import java.util.ArrayList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditScreen(
    mediaUris: List<Uri>,
    navController: NavController,
    user: FirebaseUser?,
//    timestamps: ArrayList<Int>
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var currentMediaIndex by remember { mutableIntStateOf(0) }
    var mediaItemMap = mediaUris.indices.associateWith {MediaItem.fromUri(Uri.parse(mediaUris[it].toString()))}


    Scaffold(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        topBar = {
            TopAppBar(title = { Text("Craite - Edit Media") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text("Editing screen")
            // Media Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                val currentUri = mediaUris.getOrNull(currentMediaIndex)
                if (currentUri != null) {
                    AndroidView(
                        factory = { context ->
                            PlayerView(context).apply {
                                player = exoPlayer.apply {
                                    mediaItemMap[currentMediaIndex]?.let { setMediaItem(it) }
                                    prepare()
                                    playWhenReady = true // Autoplay video
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Media List (Horizontally Scrollable)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mediaUris) { uri ->
                    MediaItemThumbnail(uri) { index ->
                        currentMediaIndex = index
                        if (uri.toString().endsWith(".mp4")) {
                            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                    }
                }
            }
            Button(onClick = {

            }) { Text("Upload Videos") }

            // Editing Controls (Add your desired controls here)
            // ... (Same as before, but now operate on the selected media item)
        }
    }

    // Release ExoPlayer when the screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

@Composable
fun MediaItemThumbnail(uri: Uri, onClick: (Int) -> Unit) {
    // ... (Implement a thumbnail view for each media item)
    // You can use Coil or Glide to load images efficiently
    // For videos, you might display a static thumbnail or a short animated preview
    Button(onClick = { /* Calculate and pass the index to onClick */ }) {
        // ... Display thumbnail content ...
    }
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


//@Preview(showBackground = true)
//@Composable
//fun VideoEditScreenPreview() {
//    val context = LocalContext.current
//    val sampleVideoUri = Uri.parse("content://media/picker/0/com.android.providers.media.photopicker/media/1000008360") // Replace with your sample video resource
//    val mediaUris = listOf(sampleVideoUri)
////    "content://media/picker/0/com.android.providers.media.photopicker/media/1000008368",
////    "content://media/picker/0/com.android.providers.media.photopicker/media/1000008342",
////    "content://media/picker/0/com.android.providers.media.photopicker/media/1000008332"
//
//    VideoEditScreen(mediaUris)
//}