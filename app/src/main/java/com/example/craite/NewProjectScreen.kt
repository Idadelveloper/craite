package com.example.craite

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController


@Composable
fun NewProject(
    navController: NavController
) {
    var projectName by remember { mutableStateOf("") }
    Column {
        TextField(
            value = projectName,
            onValueChange = { projectName = it },
            label = { Text("Project Name") }
        )
        Button(
            onClick = {
                navController.navigate("project_list")
            }
        ) { }
    }
}