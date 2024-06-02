package com.example.craite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.craite.data.ProjectDatabase
import com.example.craite.data.ProjectRepository
import com.example.craite.ui.theme.CraiteTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CraiteTheme {
                val database = ProjectDatabase.getInstance(this)
                val projectRepository = ProjectRepository(database)
                CraiteApp(projectRepository)
            }
        }
    }
}


@Composable
fun CraiteApp(projectRepository: ProjectRepository) {
    val navController = rememberNavController()

    Surface(color = MaterialTheme.colorScheme.background) {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") { HomeScreen(navController, projectRepository) }
            composable("newProject") { NewProjectScreen(navController, projectRepository) }
            composable("project/{projectId}") { backStackEntry ->
                val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
                ProjectScreen(navController, projectRepository, projectId)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CraiteApp(projectRepository = ProjectRepository(ProjectDatabase.getInstance(null)))
}



//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    CraiteTheme {
//        Greeting("Android")
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun HomeScreenPreview() {
//    CraiteTheme {
//        HomeScreen(name = "craite", modifier = Modifier)
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun MainScreenPreview() {
//    CraiteTheme {
//        MainScreen(this@)
//    }
//}