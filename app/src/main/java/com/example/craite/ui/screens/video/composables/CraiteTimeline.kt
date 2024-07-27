package com.example.craite.ui.screens.video.composables

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.craite.R
import com.example.craite.ui.theme.AppColor
import com.example.craite.utils.Helpers

//@Preview(showBackground = true)
@OptIn(UnstableApi::class)
@Composable
fun CraiteTimeline(timeline: Timeline, exoPlayer: ExoPlayer) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
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

                // Pass ExoPlayer to ClipItem
                ClipItem(index, clipDuration, exoPlayer) {
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
fun ClipItem(index: Int, duration: Double, exoPlayer: ExoPlayer, onClipClick: () -> Unit) { // Add ExoPlayer parameter
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClipClick() }
    ) {
        // Display clip thumbnail or placeholder (you'll need to implement this)
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background), // Replace with actual thumbnail
            contentDescription = "Clip Thumbnail",
            modifier = Modifier.size(64.dp)
        )

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
                Spacer(modifier = Modifier.width(1.dp).height(8.dp))
            }
        }
    }
}