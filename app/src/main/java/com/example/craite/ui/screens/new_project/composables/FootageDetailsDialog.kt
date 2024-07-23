package com.example.craite.ui.screens.new_project.composables

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.craite.R
import com.example.craite.ui.screens.composables.CraiteTextField

@Preview(showBackground = true)
@Composable
fun FootageDetailsDialog(modifier: Modifier = Modifier) {

    var videoDescription by remember { mutableStateOf("") }

    Dialog(

        onDismissRequest = { /*TODO*/ }, properties = DialogProperties(), content = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(24.dp))
                    .background(
                        MaterialTheme.colorScheme.background
                    )
                    .padding(16.dp)
                    .then(modifier),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FootageThumbnail(modifier = Modifier.width(100.dp), uri = Uri.parse("android.resource://com.example.craite/${R.drawable.surfing}"))
                CraiteTextField(
                    label = "Video Content",
                    hintText = "Video Description",
                    minLines = 3,
                    onValueChanged = { newVideoDescription ->
                        videoDescription = newVideoDescription
                    }
                )
            }
        })
}