package com.example.craite.ui.screens.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun CraiteTextField(modifier: Modifier = Modifier, label: String, hintText: String, maxLines: Int = 1 ) {
    var value = remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)

        TextField(
            value = value,
            hintText = hintText,
            onValueChange = { newValue: TextFieldValue -> value = newValue},
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),

            shape = RoundedCornerShape(24.dp),
            maxLines = maxLines,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ))
    }
}