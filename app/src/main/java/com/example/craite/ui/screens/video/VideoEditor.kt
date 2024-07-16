package com.example.craite.ui.screens.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.example.craite.ui.screens.video.composables.PlaybackControls
import com.example.craite.ui.screens.video.composables.Timeline
import com.example.craite.ui.screens.video.composables.VideoPreview
import com.example.craite.ui.theme.AppColor

import com.microsoft.fluentui.icons.AllIcons
import com.microsoft.fluentui_icons.R as FluentIcons

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VideoEditorScreen(modifier: Modifier = Modifier) {
    Scaffold(
         modifier = Modifier.fillMaxSize(),
    ) {
        innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
            
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)) {
    IconButton(onClick = { /*TODO*/ }) {
        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack , contentDescription = "Back icon" )

        
    }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    contentPadding = PaddingValues(horizontal = 12.dp),
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
                    contentPadding = PaddingValues(horizontal = 16.dp),
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




