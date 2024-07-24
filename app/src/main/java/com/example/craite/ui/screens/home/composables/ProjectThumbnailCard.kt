package com.example.craite.ui.screens.home.composables

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.craite.R
import com.example.craite.data.models.Project
import com.example.craite.ui.theme.AppColor


@Composable
fun ProjectThumbnailCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    project: Project,
    height: Double
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .clickable { onClick.invoke() }
                .clip(
                    shape = RoundedCornerShape(16.dp)
                )
        ) {

            // Load thumbnail from database path
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(project.thumbnailPath?.let { Uri.parse(it) })
                    .crossfade(true)
                    .build(),
                contentDescription = "Project Thumbnail",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(height.dp)
                    .fillMaxWidth(),
                error = painterResource(id = R.drawable.surfing)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AppColor().black.copy(alpha = 0f),
                                AppColor().black.copy(alpha = .8f),
                            ),
                        ),
                    ),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
                Text("00:00", style = MaterialTheme.typography.bodySmall)
            }
        }
        Text(text = project.name, style = MaterialTheme.typography.bodyMedium)


    }
}