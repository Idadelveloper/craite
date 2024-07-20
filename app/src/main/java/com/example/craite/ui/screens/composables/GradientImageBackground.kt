package com.example.craite.ui.screens.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import com.example.craite.ui.theme.AppColor


@Composable
fun GradientImageBackground(
    modifier: Modifier = Modifier,
    painter: Painter,
    contentDescription: String?,
    gradientColor: Color = AppColor().black
) {
    Box {
        Image(
            modifier = modifier,
            painter = painter,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            gradientColor.copy(alpha = 0f),
                            gradientColor
                        ),
                    ),
                ),
        )

    }
}