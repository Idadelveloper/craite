package com.example.craite.ui.screens.video.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.craite.ui.theme.AppColor

@Preview(showBackground = true)
@Composable
fun Timeline(modifier: Modifier = Modifier) {

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
            .fillMaxWidth()
    ) {


        HorizontalDivider(thickness = 1.dp, color = AppColor().neutral30)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(text = "00:00", style = MaterialTheme.typography.bodySmall)
            Text(text = "00:30", style = MaterialTheme.typography.bodySmall)
            Text(text = "01:00", style = MaterialTheme.typography.bodySmall)
            Text(text = "01:30", style = MaterialTheme.typography.bodySmall)
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
                Spacer(modifier = modifier.width(8.dp))

                Text(
                    "Audio_name.mp3",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.background)
                )
                Spacer(modifier = modifier.width(32.dp))
            }
        }
    }

}