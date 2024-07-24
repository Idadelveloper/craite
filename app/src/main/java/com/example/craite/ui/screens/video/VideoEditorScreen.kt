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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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

    val playbackState = exoPlayer.playbackState
    val latestPlaybackState = rememberUpdatedState(playbackState)
    val currentPosition = exoPlayer.currentPosition
    val latestCurrentPosition = rememberUpdatedState(currentPosition)
    val duration = exoPlayer.duration
    val latestDuration = rememberUpdatedState(duration)

    Log.d("VideoEditScreen", "MediaItemMap: ${project.mediaNames.entries}")
    Log.d("VideoEditScreen", "MediaItems: ${project.media}")
    Log.d("VideoEditScreen", "PromptId: ${project.promptId}")

    LaunchedEffect(latestPlaybackState) { // Log playback state changes
        Log.d("VideoEditorScreen", "Playback State: ${latestPlaybackState.value}")
    }

    Scaffold(
         modifier = Modifier.fillMaxSize(),
    ) {
        innerPadding ->
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
    IconButton(onClick = { /*TODO*/ }) {
        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack , contentDescription = "Back icon" )

        
    }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    onClick = { /*TODO*/ },
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
                    onClick = { /*TODO*/ },
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
                exoPlayer = exoPlayer,
                playbackState = latestPlaybackState.value,
                currentPosition = latestCurrentPosition.value,
                duration = latestDuration.value,
                onPlayPauseClick = {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                },
                onSeekForwardClick = { exoPlayer.seekTo(latestCurrentPosition.value + 10000) }
            )
            Timeline()

        }
    }

    // Prepare ExoPlayer for playback
    LaunchedEffect(mediaItemMap) {
        exoPlayer.setMediaItems(mediaItemMap.values.toList())
        exoPlayer.prepare()
        exoPlayer.play()
    }
}




