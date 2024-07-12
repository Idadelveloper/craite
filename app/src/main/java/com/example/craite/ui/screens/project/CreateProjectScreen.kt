package com.example.craite.ui.screens.project


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.craite.R
import com.example.craite.ui.screens.composables.CraiteTextField
import com.example.craite.ui.screens.composables.GradientImageBackground


@Preview(showBackground = true)
@Composable
fun CreateProjectScreen(modifier: Modifier = Modifier) {
    Scaffold {
        innerPadding ->
        Box {
            GradientImageBackground(
                painter = painterResource(id = R.drawable.surfing),
                contentDescription = "Gradient overlay on surfer",
                modifier = Modifier.fillMaxHeight(.3f)
            )
            Column(modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Spacer(modifier = modifier.fillMaxHeight(.2f))
                Text(text = "Create Project", style = MaterialTheme.typography.headlineSmall, onTextLayout = null)
                Spacer(modifier = Modifier.height(24.dp))

                CraiteTextField(label = "Project Name", hintText = " This is a hint Text")
                CraiteTextField(label = "Prompt", hintText = "Prompt", minLines = 6)
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = { /*TODO*/ }, modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Create Project")
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}


