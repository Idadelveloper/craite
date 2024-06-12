package com.example.craite

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.Project
import com.example.craite.data.ProjectDao
import kotlinx.coroutines.launch

class NewProjectViewModel: ViewModel() {
    fun createProject(projectDao: ProjectDao, projectName: String, uris: List<Uri>, context: Context) {
        val project = Project(name = projectName, media = uris)
        viewModelScope.launch {
            try {
                projectDao.insert(project)
                Toast.makeText(context, "Successfully created project", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating project", Toast.LENGTH_SHORT).show()
            }
        }
    }

}