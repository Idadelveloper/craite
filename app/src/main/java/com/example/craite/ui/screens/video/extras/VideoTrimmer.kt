package com.example.craite.ui.screens.video.extras

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ClippingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.craite.ui.screens.video.composables.VideoPreview


@OptIn(UnstableApi::class)
@Composable
fun VideoTrimmer(
    videoUri: Uri,
    onTrimComplete: (startTimeMs: Long, endTimeMs: Long) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var videoDuration by remember { mutableStateOf(0L) }
    var startTimeMs by remember { mutableStateOf(0L) }
    var endTimeMs by remember { mutableStateOf(0L) }

    // Load the video
    LaunchedEffect(videoUri) {
        val mediaItem = MediaItem.fromUri(videoUri)
        val mediaSource = ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
            .createMediaSource(mediaItem)
        exoPlayer.setMediaSource(mediaSource)
        exoPlayer.prepare()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    videoDuration = exoPlayer.duration
                    endTimeMs = videoDuration // Initial end time is the full video duration
                }
            }
        })
    }

    // UI
    Column(modifier = Modifier.fillMaxSize()) {
        // Video preview
        VideoPreview(exoPlayer = exoPlayer, modifier = Modifier.weight(1f))

        // Trimming controls
        Column(modifier = Modifier.padding(16.dp)) {
            // RangeSeekBar for trimming
            RangeSeekBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                videoUri = videoUri,
                onTrimComplete = { start, end ->
                    startTimeMs = start
                    endTimeMs = end

                    // Apply trimming to ExoPlayer (using ClippingMediaSource)
                    val trimmedMediaSource = exoPlayer.currentMediaItem?.mediaId?.let {
                        ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context))
                            .createMediaSource(MediaItem.Builder().setMediaId(it).build())
                    }?.let {
                        ClippingMediaSource(
                            it,
                            startTimeMs,
                            endTimeMs
                        )
                    }
                    if (trimmedMediaSource != null) {
                        exoPlayer.setMediaSource(trimmedMediaSource)
                    }
                    exoPlayer.prepare()
                    exoPlayer.seekTo(startTimeMs)
                }
            )

            // Start and end time indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Start: ${formatTime(startTimeMs)}")
                Text("End: ${formatTime(endTimeMs)}")
            }
        }
    }

    // Release ExoPlayer on disposal
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}

// Helper function to format time in milliseconds to a readable string
private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}