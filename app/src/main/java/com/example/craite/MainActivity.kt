package com.example.craite

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.craite.data.models.ProjectDatabase
import com.example.craite.ui.screens.home.HomeScreenNew
import com.example.craite.ui.screens.project.CreateProjectScreen
import com.example.craite.ui.screens.video.VideoEditorScreen
import com.example.craite.ui.theme.CraiteTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
        )
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        if (auth.currentUser == null) {
            signInAnonymously()
        }
        setContent {
            CraiteTheme(
                darkTheme = true
            ) {
                val navController = rememberNavController()

                val user = auth.currentUser
                Log.d("user", "$user")
                Log.d("user id", "${user?.uid}")
                CraiteApp(navController, applicationContext, user)
            }
        }
    }

    private fun signInAnonymously() {
        auth.signInAnonymously().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    Toast.makeText(applicationContext, "Successfully signed in", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // sign-in failure
                    val exception = task.exception
                    Toast.makeText(
                        applicationContext, "Error signing in: $exception", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun CraiteApp(navController: NavHostController, context: Context, currentUser: FirebaseUser?) {
    val db = Room.databaseBuilder(
        context, ProjectDatabase::class.java, "project_database"
    ).fallbackToDestructiveMigration().build()

    val user by remember { mutableStateOf(currentUser) }

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        composable("home_screen") {
            HomeScreen(navController = navController, db = db, modifier = Modifier, user = user)
        }
        composable("home") {
            HomeScreenNew()
        }
        composable("project") {
            CreateProjectScreen()
        }
        composable("video_editor") {
            VideoEditorScreen()
        }
        composable("new_project_screen") {
            NewProject(
                navController = navController, projectDatabase = db, context = context, user = user
            )
        }
        composable(
            route = "video_edit_screen/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.IntType })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getInt("projectId") ?: -1

//            val bundle = backStackEntry.arguments?.getString("bundle")?.let { stringToBundle(it) }
//            val timestamps = bundle?.getIntegerArrayList("timestamps") ?: arrayListOf()

            val project =
                db.projectDao().getProjectById(projectId).collectAsState(initial = null).value
            val mediaUris = project?.media
            if (mediaUris != null) {
                VideoEditScreen(mediaUris, navController, user, db)
            }

        }
    }


}










