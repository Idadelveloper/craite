package com.example.craite.ui.screens.video.extras

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlin.text.toFloat
import kotlin.text.toLong

@OptIn(UnstableApi::class)
@Composable
fun RangeSeekBar(
    modifier: Modifier = Modifier,
    videoUri: Uri,
    onTrimComplete: (startTimeMs: Long, endTimeMs: Long) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var videoDuration by remember { mutableStateOf(0L) }
    var startTimeMs by remember { mutableStateOf(0L) }
    var endTimeMs by remember { mutableStateOf(0L) }
    var startValue by remember { mutableStateOf(0f) }
    var endValue by remember { mutableStateOf(1f) }
    var activeThumb by remember { mutableStateOf<Int?>(null) }

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

    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()

        // Calculate thumb positions based on values (corrected)
        val startPosition = startValue * width
        val endPosition = endValue * width

        // Draw the seek bar
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw the base line
            drawLine(
                color = Color.Gray,
                start = Offset(0f, center.y),
                end = Offset(width, center.y),
                strokeWidth = 4f
            )

            // Draw the selected range
            drawLine(
                color = Color.Blue,
                start = Offset(startPosition, center.y),
                end = Offset(endPosition, center.y),
                strokeWidth = 8f
            )
        }

        // Thumb composables (with interaction checks)
        Thumb(
            modifier = Modifier.offset(x = (startPosition - 12).dp),
            isActive = activeThumb == 0,
            onDrag = { delta ->
                val newValue = (startPosition + delta).coerceIn(0f, (endPosition - 24f).coerceAtLeast(0f))
                startValue = newValue / width
                startTimeMs = (startValue * videoDuration).toLong()
                activeThumb = 0
            }
        )

        Thumb(
            modifier = Modifier.offset(x = (endPosition - 12).dp),
            isActive = activeThumb == 1,
            onDrag = { delta ->
                val newValue = (endPosition + delta).coerceIn((startPosition + 24f).coerceAtMost(width), width)
                endValue = newValue / width
                endTimeMs = (endValue * videoDuration).toLong()
                activeThumb = 1
            }
        )

        // Trim button
        Button(
            onClick = { onTrimComplete(startTimeMs, endTimeMs) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text("Trim")
        }
    }

    // Release ExoPlayer on disposal
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}