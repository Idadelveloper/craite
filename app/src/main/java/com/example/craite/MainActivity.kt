package com.example.craite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.example.craite.data.Project
import com.example.craite.data.ProjectDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            ProjectDatabase::class.java, "project_database"
        ).build()
        enableEdgeToEdge()

        setContent {
            Column {
                ProjectList(db.projectDao().getAllProjects(), rememberNavController())
                Button(
                    modifier = Modifier.padding(16.dp),
                    onClick = {TODO()}
                ) {
                    Text(text = "Add Project")
                }
            }

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
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}





