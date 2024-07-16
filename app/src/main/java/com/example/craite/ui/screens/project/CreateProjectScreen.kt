package com.example.craite.ui.screens.project


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.craite.R
import com.example.craite.ui.screens.composables.CraiteTextField
import com.example.craite.ui.screens.composables.GradientImageBackground
import com.example.craite.ui.screens.project.composables.FootageThumbnail


@Preview(showBackground = true)
@Composable
fun CreateProjectScreen(modifier: Modifier = Modifier) {

    val projectNameValue = remember {
        mutableStateOf(TextFieldValue() )
    }

    val promptValue = remember { mutableStateOf(TextFieldValue())}
    Scaffold(
         modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box {
            GradientImageBackground(
                painter = painterResource(id = R.drawable.surfing),
                contentDescription = "Gradient overlay on surfer",
                modifier = Modifier.fillMaxHeight(.3f)
            )
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = modifier.fillMaxHeight(.2f))
                Text(
                    text = "Create Project",
                    style = MaterialTheme.typography.headlineSmall,
                    onTextLayout = null
                )
                Spacer(modifier = Modifier.height(24.dp))

                CraiteTextField(
                    label = "Project Name",
                    hintText = " This is a hint Text",
                    onValueChanged = {projectNameValue.value = it}
                    )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Footage", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FootageThumbnail()
                    FootageThumbnail()
                    FootageThumbnail()
                }

                Button(
                    onClick = { /*TODO*/ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import Footage",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                CraiteTextField(
                    label = "Prompt",
                    hintText = "Prompt",
                    minLines = 6,
                    onValueChanged = {
                        promptValue.value = it
                    }
                )


                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { /*TODO*/ }, modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {

                    Text(text = "Create Project")

                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}


