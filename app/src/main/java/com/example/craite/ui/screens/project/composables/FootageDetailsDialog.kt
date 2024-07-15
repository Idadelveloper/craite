package com.example.craite.ui.screens.project.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.craite.ui.screens.composables.CraiteTextField

@Preview(showBackground = true)
@Composable
fun FootageDetailsDialog(modifier: Modifier = Modifier) {

    Dialog(

        onDismissRequest = { /*TODO*/ }, properties = DialogProperties(), content = {

            Column(
                modifier = Modifier.fillMaxWidth().clip(shape = RoundedCornerShape(24.dp)).background(
                    MaterialTheme.colorScheme.background).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FootageThumbnail()
                CraiteTextField(
                    label = "Video Content",
                    hintText = "Video Description",
                    minLines = 3
                )
            }
        })
}