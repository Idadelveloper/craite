package com.example.craite

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.Project
import com.example.craite.data.ProjectDao
import kotlinx.coroutines.launch

class NewProjectViewModel: ViewModel() {
    fun createProject(projectDao: ProjectDao, projectName: String, uris: List<Uri>) {
        val project = Project(name = projectName, media = uris)
        viewModelScope.launch {
            projectDao.insert(project)
        }
    }

}