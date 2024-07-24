package com.example.craite.ui.screens.video.composables

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
import com.example.craite.ui.theme.AppColor
import com.example.craite.utils.Helpers

//@Preview(showBackground = true)
@Composable
fun PlaybackControls(
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer,
    playbackState: Int,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onSeekForwardClick: () -> Unit,
) {
    var playIcon by remember { mutableStateOf(Icons.Rounded.PlayArrow) }

    //Player Controls`
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {

        Row() {
            Spacer(modifier = modifier.width(8.dp))
            Text(text = Helpers.formatTime(currentPosition), style = MaterialTheme.typography.bodySmall)
            Text(
                text = "/${Helpers.formatTime(duration)}",
                style = MaterialTheme.typography.bodySmall.copy(color = AppColor().neutral50)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.wrapContentWidth()
        ) {
            IconButton(onClick = { /*TODO*/ }) {

                Icon(imageVector = Icons.Rounded.FastRewind, contentDescription = "Rewind")

            }
            IconButton(
                onClick = {
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                        playIcon = Icons.Rounded.Pause // Update to Play when paused
                    } else {
                        if (exoPlayer.currentPosition >= exoPlayer.duration) {
                            exoPlayer.seekTo(0)
                        }
                        exoPlayer.play()
                        playIcon = Icons.Rounded.PlayArrow // Update to Pause when playing
                    }
                },
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(height = 48.dp, width = 48.dp),

                ) {

                Icon(imageVector = playIcon, contentDescription = "Play/Pause")

            }
            IconButton(onClick = { /*TODO*/ }) {

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
                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Play Button")

            }
        }


    }
}
