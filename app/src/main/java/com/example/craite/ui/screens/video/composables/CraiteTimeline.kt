package com.example.craite.ui.screens.video.composables

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.craite.R
import com.example.craite.ui.theme.AppColor
import com.example.craite.utils.Helpers
import kotlin.text.clear

//@Preview(showBackground = true)
@OptIn(UnstableApi::class)
@Composable
fun CraiteTimeline(timeline: Timeline, exoPlayer: ExoPlayer, context: Context) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {

        val frames = remember { mutableStateMapOf<Int, List<ImageBitmap?>>() }
        LaunchedEffect(timeline) {
            frames.clear()
            for (i in 0 until timeline.windowCount) {
                val clipUri = getClipUriFromExoPlayer(exoPlayer, i)
                if (clipUri != null) {
                    frames[i] = loadFramesForClip(context, clipUri, 10) // Load frames for clip i
                } else {
                    Log.e("CraiteTimeline", "Clip URI not found for index $i")
                }
            }
        }
        // Time Intervals Display
        TimeIntervalDisplay(timeline)

        HorizontalDivider(thickness = 1.dp, color = AppColor().neutral30)

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(timeline.periodCount) { index ->
                val period = Timeline.Period()
                timeline.getPeriod(index, period)
                val clipDuration = period.durationUs / 1000000.0

                val clipFrames = frames[index] // Get frames for the current clip
                ClipItem(index, clipDuration, exoPlayer, clipFrames) {
                    // Handle clip interaction (e.g., seeking, trimming)
                    val window = Timeline.Window()
                    timeline.getWindow(index, window)
                    exoPlayer.seekTo(window.defaultPositionMs)
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun ClipItem(index: Int, duration: Double, exoPlayer: ExoPlayer, frames: List<ImageBitmap?>?, onClipClick: () -> Unit) {
    // Calculate width based on number of frames and spacing
    val itemWidth = (frames?.size ?: 0) * (64.dp + 4.dp) // 64.dp per frame + 4.dp spacing

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(itemWidth)
            .clickable { onClipClick() }
            .border(1.dp, Color.Gray) // Add a border to the ClipItem
            .padding(4.dp) // Optional: Add padding around the content within the border
    ) {
        LazyRow(
            modifier = Modifier.height(64.dp)
            // Removed spacing between frames
        ) {
            frames?.let {
                items(it.size) { frameIndex ->
                    val frame = it[frameIndex]
                    if (frame != null) {
                        Image(
                            bitmap = frame,
                            contentDescription = "Clip Thumbnail",
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }
        }

        Text(text = "Clip $index (${String.format("%.2f", duration)}s)")
    }
}


@OptIn(UnstableApi::class)
@Composable
fun TimeIntervalDisplay(timeline: Timeline) {
    val lastWindow = Timeline.Window()
    timeline.getWindow(timeline.windowCount - 1, lastWindow)
    val totalDurationUs = lastWindow.windowStartTimeMs + lastWindow.durationUs // Remove the unnecessary multiplication
    val totalDurationInSeconds = (totalDurationUs / 1000000).toInt()
    Log.d("TimeIntervalDisplay", "Total Duration in Seconds: $totalDurationInSeconds")

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(totalDurationInSeconds + 1) { second ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = Helpers.formatTime(second * 1000L), style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier
                    .width(1.dp)
                    .height(8.dp))
            }
        }
    }
}

fun loadFramesForClip(context: Context, clipUri: Uri, numFrames: Int): List<ImageBitmap?> {
    val frames = mutableListOf<ImageBitmap?>()
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, clipUri)

        // Get video duration and calculate interval
        val durationUs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        val intervalUs = if (numFrames > 0) durationUs / numFrames else 0L

        // Extract frames at intervals
        for (i in 0 until numFrames) {
            val timeUs = i * intervalUs
            val frameBitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            frames.add(frameBitmap?.asImageBitmap())
        }
    } catch (e: Exception) {
        Log.e("FrameLoading", "Error loading frames: ${e.message}")
        // Handle exceptions, e.g., by adding null frames or showing an error message
    } finally {
        retriever.release()
    }
    return frames
}

fun getClipUriFromExoPlayer(exoPlayer: ExoPlayer, index: Int): Uri? {
    val timeline = exoPlayer.currentTimeline
    if (index in 0 until timeline.windowCount) {
        val window = Timeline.Window()
        timeline.getWindow(index, window)
        val mediaItem = window.mediaItem
        return mediaItem.localConfiguration?.uri
    }
    return null
}