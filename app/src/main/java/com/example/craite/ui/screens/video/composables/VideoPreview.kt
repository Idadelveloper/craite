package com.example.craite.ui.screens.video.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.craite.R

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
