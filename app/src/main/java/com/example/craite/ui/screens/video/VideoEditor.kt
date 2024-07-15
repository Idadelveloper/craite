package com.example.craite.ui.screens.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Timeline
import com.example.craite.R
import com.example.craite.ui.theme.AppColor

import com.microsoft.fluentui.icons.AllIcons
import com.microsoft.fluentui_icons.R as FluentIcons

@Preview(showBackground = true)
@Composable
fun VideoEditorScreen(modifier: Modifier = Modifier) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
            
        ) {
            Spacer(modifier = Modifier.height(38.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
    IconButton(onClick = { /*TODO*/ }) {
        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack , contentDescription = "Back icon" )

        
    }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "1080p")
                        Spacer(modifier.width(8.dp))
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                }
                Button(
                    onClick = { /*TODO*/ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {


                        Text(text = "Export")

                }
}
            VideoPreview()
            PlaybackControls()
            Timeline()


        }
    }
}


@Composable
fun VideoPreview(modifier: Modifier = Modifier) {
    Box (       modifier = Modifier
        .fillMaxWidth(.5f)
        .aspectRatio(9 / 16f)
        .clip(shape = RoundedCornerShape(24.dp))
        .then(modifier)){
        Image(
            painter = painterResource(id = R.drawable.surfing),
            contentDescription = "Video Play",
     contentScale = ContentScale.Crop
        )
    }

}

@Composable
fun PlaybackControls(modifier: Modifier = Modifier) {
    //Player Controls`
    Row(horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {

        Row {
            Text(text = "00:12", style = MaterialTheme.typography.bodySmall)
            Text(text = "/00:48", style =MaterialTheme.typography.bodySmall.copy(color = AppColor().neutral50))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = { /*TODO*/ }, ) {

                Icon(imageVector = Icons.Filled.FastRewind, contentDescription = "Play Button", )

            }
            IconButton(onClick = { /*TODO*/ }, modifier = Modifier
                .clip(shape = RoundedCornerShape(12.dp))
                .size(height = 48.dp, width = 48.dp), colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ) ) {

                Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Play Button", )

            }
            IconButton(onClick = { /*TODO*/ }) {

                Icon(imageVector = Icons.Filled.FastForward, contentDescription = "Play Button", )

            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = "1x", style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = { /*TODO*/ }) {

                Icon(imageVector = Icons.Filled.Settings, contentDescription = "Play Button", )

            } }


    }
}

@Preview(showBackground = true)
@Composable
fun Timeline(modifier: Modifier = Modifier) {

    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {


        HorizontalDivider(thickness = 1.dp, color = AppColor().neutral50)

        Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            Text(text = "00:00")
            Text(text = "00:10")
            Text(text = "00:15")
            Text(text = "00:20")
        }

        Box(
modifier = Modifier
    .clip(shape = RoundedCornerShape(12.dp))
    .background(AppColor().purple70)
    .padding(4.dp)
        ){
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()

                ){
                    Box(modifier = Modifier
                        .clip(shape = RoundedCornerShape(8.dp))
                        .background(color = AppColor().purple90)
                        .padding(4.dp)) {
                       // Icon(painter = painterResource( ), contentDescription = null)
                    }


                    Text("Audio_name.mp3")
                }
        }
    }

}