package com.example.craite.ui.screens.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun CraiteTextField(
    modifier: Modifier = Modifier,
    label: String,
    hintText: String,
    minLines: Int = 1,
    onValueChanged: (TextFieldValue) -> Unit
) {
    val value by remember {
        mutableStateOf(TextFieldValue())
    }


   val borderRadius = if (minLines == 1) {
        28.0
    } else {
        20.0
    }
///Todo: Flesh out the implementation of this text field
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)

        TextField(
            value = value,
            onValueChange = {
                onValueChanged(it)
            },

            placeholder = { Text(text = hintText) },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(0.dp),
            shape = RoundedCornerShape(borderRadius.dp),
            singleLine = minLines == 1,
            minLines = minLines,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,

                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor = MaterialTheme.colorScheme.errorContainer,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        )
    }
}


