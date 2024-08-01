package com.example.craite.ui.screens.video.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun Thumb(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    onDrag: (Float) -> Unit
) {
    Box(
        modifier = modifier
            .size(24.dp)
            .background(if (isActive) Color.DarkGray else Color.Gray, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    onDrag(dragAmount.x)
                    change.consume()
                }
            }
    )
}