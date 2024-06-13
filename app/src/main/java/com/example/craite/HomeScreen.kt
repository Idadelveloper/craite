package com.example.craite

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.craite.data.Project
import com.example.craite.data.ProjectDatabase
import kotlinx.coroutines.flow.Flow


@Composable
fun HomeScreen(navController: NavController, modifier: Modifier, db: ProjectDatabase) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProjectList(db.projectDao().getAllProjects(), navController)
        Button(
            modifier = Modifier.padding(16.dp),
            onClick = { navController.navigate("new_project_screen") }
        ) {
            Text(text = "Add Project")
        }
    }

}

@Composable
fun ProjectList(projects: Flow<List<Project>>, navController: NavController) {
    val projectList by projects.collectAsState(initial = emptyList())
    LazyColumn {
        items(projectList) { project ->
            ProjectCard(project, navController)
        }
    }
}


@Composable
fun ProjectCard(project: Project, navController: NavController) {
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),

        ){
        Column(
            modifier = Modifier.padding(16.dp).clickable(onClick = {
                navController.navigate("video_edit_screen/${project.id}")
            })
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}