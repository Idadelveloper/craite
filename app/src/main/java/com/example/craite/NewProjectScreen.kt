package com.example.craite

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.craite.data.ProjectRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewProjectScreen(
    navController: NavHostController,
    projectRepository: ProjectRepository
) {
    var projectName: TextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val viewModel: NewProjectViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("New Project") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = projectName,
                onValueChange = { projectName = it },
                label = { Text("Project Name") },
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.createProject(projectName.text, projectRepository) {
                        navController.navigate("home")
                    }
                },
                modifier = Modifier.padding(16.dp),
                enabled = projectName.text.isNotBlank()
            ) {
                Text("Create Project")
            }
        }
    }
}


