package com.example.craite.ui.screens.video.composables

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.craite.ui.theme.AppColor
import com.example.craite.utils.Helpers

//@Preview(showBackground = true)
@Composable
fun Timeline(
    intervals: List<Long> // Pass the total duration to Timeline
) {
    val videoFrames = remember { mutableStateListOf<ImageBitmap?>() }

    Log.d("Timeline", "Intervals: $intervals")

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalDivider(thickness = 1.dp, color = AppColor().neutral30)

        // Placeholder for future frame-based timeline
        // (You'll need to implement frame loading and display here)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Adjust spacing as needed
        ) {
            items(intervals) { interval ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Display time label only for whole seconds
                    if (interval % 1000 == 0L) { // 1000 milliseconds = 1 second
                        Text(text = Helpers.formatTime(interval), style = MaterialTheme.typography.bodySmall)
                    }

                    // Vertical line to represent each interval
                    Spacer(modifier = Modifier.width(1.dp).height(8.dp)) // Adjust size as needed
                }
            }
        }

        Box(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .background(AppColor().purple70)
                .padding(4.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                //  horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.wrapContentWidth()

            ) {

                Box(
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = AppColor().purple80)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.background
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Audio_name.mp3",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.background)
                )
                Spacer(modifier = Modifier.width(32.dp))
            }
        }
    }

}