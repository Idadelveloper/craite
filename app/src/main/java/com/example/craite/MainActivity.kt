package com.example.craite

import android.content.Context
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.craite.data.Project
import com.example.craite.data.ProjectDatabase
import com.example.craite.ui.theme.CraiteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CraiteTheme {
                val navController = rememberNavController()
                CraiteApp(navController, applicationContext)
            }
        }
    }
}

@Composable
fun CraiteApp(navController: NavHostController, context: Context) {
    val db = Room.databaseBuilder(
        context,
        ProjectDatabase::class.java, "project_database"
    ).fallbackToDestructiveMigration()
        .build()

    NavHost(navController = navController, startDestination = "home_screen", modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        composable("home_screen") {
            HomeScreen(navController = navController, db = db, modifier = Modifier)
        }
        composable("new_project_screen") {
            NewProject(navController = navController, projectDatabase = db, context = context)
        }
        composable(
            route = "video_edit_screen/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: 0
            val project = db.projectDao().getProjectById(projectId).collectAsState(initial = null).value
            val mediaUris = project?.media
            if (mediaUris != null) {
                VideoEditScreen(mediaUris,navController)
            }
        }
    }


}








