package com.example.craite.ui.screens.video.composables

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.example.craite.ui.screens.video.VideoEditorViewModel
import com.example.craite.ui.theme.AppColor
import com.example.craite.utils.Helpers

//@Preview(showBackground = true)
@Composable
fun PlaybackControls(
    modifier: Modifier = Modifier,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeekForwardClick: () -> Unit,
    onSeekBackwardClick: () -> Unit,
    viewModel: VideoEditorViewModel,
    currentPosition: Long, // Receive the current position
    duration: Long,
    exoPlayer: ExoPlayer
) {
    val playIcon by remember(isPlaying) {
        derivedStateOf {
            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow
        }
    }

//    val currentPosition by viewModel.currentPosition.collectAsState()
//    val duration by viewModel.duration.collectAsState()

    //Player Controls`
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        Log.d("PlaybackControls", "Current Position: $currentPosition")
        Log.d("PlaybackControls", "Duration: $duration")
        Text(text = Helpers.formatTime(currentPosition), style = MaterialTheme.typography.bodySmall)
        Text(
            text = "/${Helpers.formatTime(duration)}",
            style = MaterialTheme.typography.bodySmall.copy(color = AppColor().neutral50)
        )

        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            IconButton(onClick = { onSeekBackwardClick() }) {

                Icon(imageVector = Icons.Rounded.FastRewind, contentDescription = "Rewind")

            }
            IconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(height = 48.dp, width = 48.dp),

                ) {

                Icon(imageVector = playIcon, contentDescription = "Play/Pause")

            }
            IconButton(onClick = { onSeekForwardClick() }) {

                Icon(imageVector = Icons.Rounded.FastForward, contentDescription = "Fast Forward")

            }
        }
        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "1x", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = modifier.width(8.dp))
            IconButton(onClick = { /*TODO*/ }) {
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")

            }
        }


    }
}
