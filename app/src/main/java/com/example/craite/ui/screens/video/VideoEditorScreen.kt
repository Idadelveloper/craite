package com.example.craite.ui.screens.video

import android.content.ContentValues
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.Loader.Loadable
import androidx.navigation.NavController
import com.example.craite.data.EditSettings
import com.example.craite.data.models.Project
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.ui.screens.video.composables.CraiteTimeline
import com.example.craite.ui.screens.video.composables.PlaybackControls
import com.example.craite.ui.screens.video.composables.CraiteTimeline
import com.example.craite.ui.screens.video.composables.TimeIntervalDisplay
import com.example.craite.ui.screens.video.composables.VideoPreview
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import kotlin.text.toFloat
import kotlin.text.toLong

//@Preview(showBackground = true, showSystemUi = true)
@OptIn(UnstableApi::class)
@Composable
fun VideoEditorScreen(
    project: Project?,
    navController: NavController,
    user: FirebaseUser?,
    projectDatabase: ProjectDatabase
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val viewModel: VideoEditorViewModel = viewModel(
        factory = VideoEditorViewModelFactory(
            project?.editingSettings ?: EditSettings(emptyList(), null) // Pass EditSettings from project
        )
    )
    val editSettings by viewModel.uiState.collectAsState()
    val showProgressDialog by viewModel.showProgressDialog.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val playbackState = exoPlayer.playbackState
    val latestPlaybackState = rememberUpdatedState(playbackState)
    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }

    var playerReady by remember { mutableStateOf(false) }

    var showResolutionDialog by remember { mutableStateOf(false) }
    var selectedResolution by remember { mutableStateOf("1080p") }

    val totalDuration by viewModel.totalDuration.collectAsState()
    val intervals by viewModel.intervals.collectAsState()
    val timeline by viewModel.timeline.collectAsState()

    var sliderPosition by remember { mutableStateOf(0f) }

    if (project != null) {
        Log.d("VideoEditScreen", "MediaItemMap: ${project.mediaNames}")
        Log.d("VideoEditScreen", "MediaItems: ${project.media}")
        Log.d("VideoEditScreen", "PromptId: ${project.promptId}")
        Log.d("VideoEditScreen", "Prompt: ${project.prompt}")
    }

//    LaunchedEffect(Unit) {
//        viewModel.initializeCache(context)
//    }


    // Combined LaunchedEffect for media sources and preview
    LaunchedEffect(project, editSettings) {
        Log.d("VideoEditorScreen", "Edit settings: $editSettings")
        Log.d("VideoEditorScreen", "Project media names: ${project?.mediaNames}")
        if (project != null) {
            viewModel.previewEditSettings(
                context,
                exoPlayer,
                editSettings,
                project.mediaNames,
                onPlayerReady = { playerReady = true
                    Log.d("VideoEditorScreen", "playerReady set to true")
                }
            )
        }
    }

    // Observe player state and data
    LaunchedEffect(playerReady) {
        if (playerReady) {
            snapshotFlow {
                Triple(exoPlayer.playbackState, exoPlayer.currentPosition, exoPlayer.currentTimeline)
            }.collect { (playbackState, position, timeline) ->
                if (playbackState == Player.STATE_READY) {
                    currentPosition = position
                    duration = exoPlayer.duration
                    viewModel.updateTotalDuration(exoPlayer.duration)
                }
            }
        }
    }

    // Conditionally display content based on playerReady
    if (playerReady) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back icon"
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        onClick = { showResolutionDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "1080p")
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }

                    if (showResolutionDialog) {
                        AlertDialog(
                            onDismissRequest = { showResolutionDialog = false },
                            title = { Text("Select Resolution") },
                            text = {
                                Column {
                                    ResolutionOption("720p") { selectedResolution = "720p" }
                                    ResolutionOption("1080p") { selectedResolution = "1080p" }
                                    // Add more resolution options as needed
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    showResolutionDialog = false
                                    // Call a function in the ViewModel to handle resolution change
                                    viewModel.changeResolution(context, selectedResolution, exoPlayer)
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showResolutionDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    Button(
                        onClick = { viewModel.exportVideo(context, project, editSettings, exoPlayer) }, // Implement export video logic
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

                // Slider for seeking
                Slider(
                    value = sliderPosition,
                    onValueChange = { newValue ->
                        sliderPosition = newValue
                        // Seek to the new position in the video
                        val seekPosition = (newValue * duration).toLong()
                        exoPlayer.seekTo(seekPosition)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                // Update slider position as the video plays
                LaunchedEffect(key1 = playerReady) {
                    if (playerReady) {
                        while (true) {
                            if (duration > 0) {
                                sliderPosition = exoPlayer.currentPosition.toFloat() / duration.toFloat()
                            }
                            delay(100) // Update every 100 milliseconds
                        }
                    }
                }


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

                // Timeline and TimeIntervalDisplay
                Column(modifier = Modifier.fillMaxWidth()) { // Wrap in a Column for vertical arrangement
                    timeline?.let {
                        if (it.windowCount > 0) {
                            CraiteTimeline(timeline = it, exoPlayer = exoPlayer, context = context)
                            TimeIntervalDisplay(it)
                        }
                    }
                }
                Row {
                    Button(
                        onClick = { viewModel.getGeminiEdits(user?.uid ?: "", project, projectDatabase) }, // Get firestore data
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(text = "Get gemini edits")
                    }

                    Button(onClick = { viewModel.applyFirestoreEdits(user?.uid ?: "", project, context) }
                    ) {
                        Text("Apply Firestore Edits")
                    }
                }

            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }

    // Update slider position based on ExoPlayer's current position
    LaunchedEffect(key1 = currentPosition) {
        if (duration > 0) {
            sliderPosition = currentPosition.toFloat() / duration.toFloat()
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
fun ResolutionOption(resolution: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(resolution)
    }
}