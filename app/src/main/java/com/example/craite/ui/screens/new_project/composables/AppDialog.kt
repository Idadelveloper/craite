package com.example.craite.ui.screens.new_project.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun AppDialog(modifier: Modifier = Modifier, content: @Composable () -> Unit) {

    var videoDescription by remember { mutableStateOf("") }

    Dialog(

        onDismissRequest = { /*TODO*/ }, properties = DialogProperties(), content = {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(24.dp))
                    .background(
                        MaterialTheme.colorScheme.background
                    )
                    .padding(16.dp)
                    .then(modifier),
            ) {
                content()
            }
        })
}