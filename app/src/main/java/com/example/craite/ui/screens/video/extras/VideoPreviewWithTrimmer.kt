package com.example.craite.ui.screens.video.extras

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import java.io.File


import kotlin.io.path.exists

@OptIn(UnstableApi::class)
@Composable
fun VideoPreviewWithTrimmer(
    videoUri: Uri, // This is now unused, kept for compatibility
    modifier: Modifier = Modifier,
    mediaNameMap: Map<String, String>,
    onTrimComplete: (startTimeMs: Long, endTimeMs: Long) -> Unit
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    var videoDuration by remember { mutableStateOf(0L) }
    var startTimeMs by remember { mutableStateOf(0L) }
    var endTimeMs by remember { mutableStateOf(0L) }

    // Load videos from mediaNameMap
    LaunchedEffect(mediaNameMap) {
        if (mediaNameMap.isNotEmpty()) {
            val mediaItems = mediaNameMap.values.mapNotNull { filePath ->
                val file = File(filePath)
                if (file.exists()) {
                    MediaItem.fromUri(Uri.fromFile(file))
                } else {
                    null
                }
            }
            val concatenatingMediaSource = ConcatenatingMediaSource()
            mediaItems.forEach {
                concatenatingMediaSource.addMediaSource(
                    ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context)).createMediaSource(it)
                )
            }
            exoPlayer.setMediaSource(concatenatingMediaSource)
            exoPlayer.prepare()
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        videoDuration = exoPlayer.duration
                        endTimeMs = videoDuration
                    }
                }
            })
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Video preview with AndroidView
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(9 / 16f)
        )

        // RangeSeekBar for trimming
        RangeSeekBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            videoUri = Uri.EMPTY, // Pass empty URI as we're using mediaNameMap
            onTrimComplete = onTrimComplete
        )
    }

    // Release ExoPlayer on disposal
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }
}