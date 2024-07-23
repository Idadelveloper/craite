package com.example.craite.ui.screens.new_project.composables

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.craite.R

//@Preview()
@Composable
fun FootageThumbnail(modifier: Modifier = Modifier, uri: Uri, videoName: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        // Replace Image with a Composable that can load images from a URI
        AsyncImage(
            model = uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(shape = RoundedCornerShape(16.dp))
                .aspectRatio(11 / 16f)
        )
        Text(text = videoName ?: "Video Title", style = MaterialTheme.typography.bodySmall)
    }
}