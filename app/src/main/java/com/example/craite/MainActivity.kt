package com.example.craite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.craite.ui.theme.CraiteTheme
import android.net.Uri
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val context: Context = this


        setContent {
            CraiteTheme {
                MainScreen(this@MainActivity)
            }
        }
    }
}

@Composable
fun MainScreen(context: Context) {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    val coroutineScope = rememberCoroutineScope()

    val pickVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            selectedVideoUri = uri
        }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (selectedVideoUri != null) {
            VideoPlayer(
                selectedVideoUri!!,
                player = remember { ExoPlayer.Builder(context).build() })
        } else {
            Button(
                onClick = {
                    pickVideoLauncher.launch("video/*")
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Select Video")
            }
        }

        if (isUploading) {
            LinearProgressIndicator(progress = uploadProgress)
        }

        if (selectedVideoUri != null) {
            Button(
                onClick = {
                    isUploading = true
                    coroutineScope.launch {
                        // Upload the video using VideoUploader
                        val uploadResult = VideoUploader.uploadVideo(
                            selectedVideoUri!!,
                            context
                        ) { progress ->
                            uploadProgress = progress
                        }

                        isUploading = false
                        // Handle upload result (success or failure)
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Upload Video")
            }
        }
    }
}

@Composable
fun VideoPlayer(videoUri: Uri, player: ExoPlayer) {
    DisposableEffect(player) {
        player.setMediaItem(MediaItem.fromUri(videoUri))
        player.prepare()
        player.play()

        onDispose {
            player.release()
        }
    }

    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                player
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { playerView ->
            playerView.player = player
        }
    )
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(name: String, modifier: Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(text = name)
                }

            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Bottom app bar"
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Upload Video")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(8.dp),
                text =
                """
                    The crAIte app built with Gemini
                """.trimIndent(),
            )

        }
    }
}


private fun uploadVideo(context: Context) {
    val exoPlayer = ExoPlayer.Builder(context).build()
    val looper: Looper = exoPlayer.applicationLooper


}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CraiteTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    CraiteTheme {
        HomeScreen(name = "craite", modifier = Modifier)
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    CraiteTheme {
//        MainScreen(this@)
//    }
//}