package com.example.craite.ui.screens.video

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.example.craite.data.models.Project
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.generateFakeEditSettings
import com.example.craite.ui.screens.video.composables.PlaybackControls
import com.example.craite.ui.screens.video.composables.Timeline
import com.example.craite.ui.screens.video.composables.VideoPreview
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import java.io.File

//@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VideoEditorScreen(
    project: Project,
    navController: NavController,
    user: FirebaseUser?,
    projectDatabase: ProjectDatabase
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val viewModel = remember { VideoEditorViewModel(generateFakeEditSettings(project.media.size)) }
    val currentMediaIndex by viewModel.currentMediaItemIndex.collectAsState()
    val mediaUris = project.media.map { Uri.fromFile(File(it)) }
    val mediaItemMap = mediaUris.indices.associateWith { MediaItem.fromUri(mediaUris[it]) }
    val editSettings by viewModel.uiState.collectAsState()
    val showProgressDialog by viewModel.showProgressDialog.collectAsState()
    val downloadButtonEnabled by viewModel.downloadButtonEnabled.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val playbackState = exoPlayer.playbackState
    val latestPlaybackState = rememberUpdatedState(playbackState)
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    // Observe total duration from ViewModel
    val totalDuration by viewModel.totalDuration.collectAsState()

    // Observe intervals from ViewModel
    val intervals by viewModel.intervals.collectAsState()

    // Observe currentPosition and duration
    LaunchedEffect(exoPlayer) {
        while (true) {
            currentPosition = exoPlayer.currentPosition
            duration = exoPlayer.duration

            // Update total duration in ViewModel
            viewModel.updateTotalDuration(duration)
            Log.d("VideoEditorScreen", "Total Duration: $duration")
            Log.d("VideoEditorScreen", "Intervals: $intervals")
            delay(1000)
        }
    }

    Log.d("VideoEditScreen", "MediaItemMap: ${project.mediaNames.entries}")
    Log.d("VideoEditScreen", "MediaItems: ${project.media}")
    Log.d("VideoEditScreen", "PromptId: ${project.promptId}")

    LaunchedEffect(latestPlaybackState) { // Log playback state changes
        Log.d("VideoEditorScreen", "Playback State: ${latestPlaybackState.value}")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
                IconButton(onClick = { navController.popBackStack() }) { // Navigate back
                    Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack , contentDescription = "Back icon" )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    onClick = { /* Change video resolution logic */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "1080p")
                        Spacer(Modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
                Button(
                    onClick = { TODO() }, // Implement export video logic
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = "Export")
                }
            }
            VideoPreview(exoPlayer)
            PlaybackControls(
                isPlaying = isPlaying,
                exoPlayer = exoPlayer,
                playbackState = latestPlaybackState.value,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPauseClick = {
                    if (isPlaying) {
                        viewModel.pauseVideo()
                    } else {
                        viewModel.playVideo()
                    }
                },
                onSeekForwardClick = {
                    val newPosition = exoPlayer.currentPosition + 10000
                    exoPlayer.seekTo(newPosition.coerceAtMost(duration))
                },
                onSeekBackwardClick = {
                    val newPosition = exoPlayer.currentPosition - 10000
                    exoPlayer.seekTo(newPosition.coerceAtLeast(0))
                }
            )
            // Trigger ExoPlayer actions based on isPlaying
            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    if (exoPlayer.currentPosition >= exoPlayer.duration) {
                        exoPlayer.seekTo(0)
                    }
                    exoPlayer.play()
                } else {
                    exoPlayer.pause()
                }
            }

            Timeline(intervals)

        }
    }

    // Prepare ExoPlayer for playback
    LaunchedEffect(mediaItemMap) {
        exoPlayer.setMediaItems(mediaItemMap.values.toList())
        exoPlayer.prepare()
        exoPlayer.play()
    }
}




