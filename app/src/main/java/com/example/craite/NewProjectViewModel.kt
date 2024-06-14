package com.example.craite

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.craite.data.Project
import com.example.craite.data.ProjectDao
import kotlinx.coroutines.launch

class NewProjectViewModel: ViewModel() {
    fun createProject(projectDao: ProjectDao, projectName: String, uris: List<Uri>, context: Context, navController: NavController) {
        val project = Project(name = projectName, media = uris)
        Log.d("media uri: ", "here are the project $uris")
        viewModelScope.launch {
            try {
                Log.d("project name before saving: ", "$projectName")
                Log.d("project media before saving: ", "$uris")
                projectDao.insert(project)
                Toast.makeText(context, "Successfully created project", Toast.LENGTH_SHORT).show()
                val projectId = projectDao.getLastInsertedProject().id
                Log.d("current project id: ", "$projectId")
                navController.navigate("video_edit_screen/$projectId")
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
            }
        }
    }

}