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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.craite.ui.theme.AppColor

@Preview(showBackground = true)
@Composable
fun PlaybackControls(modifier: Modifier = Modifier) {
    //Player Controls`
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {

        Row() {
            Spacer(modifier = modifier.width(8.dp))
            Text(text = "00:12", style = MaterialTheme.typography.bodySmall)
            Text(
                text = "/00:48",
                style = MaterialTheme.typography.bodySmall.copy(color = AppColor().neutral50)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Row( horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.wrapContentWidth()) {
            IconButton(onClick = { /*TODO*/ }) {

                Icon(imageVector = Icons.Rounded.FastRewind, contentDescription = "Play Button")

            }
            IconButton(
                onClick = { /*TODO*/ },
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(height = 48.dp, width = 48.dp),

            ) {

                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Play Button")

            }
            IconButton(onClick = { /*TODO*/ }) {

                Icon(imageVector = Icons.Rounded.FastForward, contentDescription = "Play Button")

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
