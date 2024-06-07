package com.example.craite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import com.example.craite.data.Project
import com.example.craite.data.ProjectDao
import com.example.craite.data.ProjectEvent
import com.example.craite.data.ProjectRepository
import com.example.craite.data.ProjectState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProjectViewModel (
    private val dao: ProjectDao
): ViewModel() {
    private val _state = MutableStateFlow(ProjectState())
    private val _projects = MutableStateFlow(emptyList<Project>()))

    fun onEvent(event: ProjectEvent) {
        when(event) {
            is ProjectEvent.DeleteProject -> {
                viewModelScope.launch {
                    dao.delete(event.project)
                }
            }
            ProjectEvent.HideDialog -> {
                _state.update { it.copy(
                    isAddingProject = false
                ) }
            }
            is ProjectEvent.SetProjectName -> {


            }

            is ProjectEvent.AddImage -> {

            }

            is ProjectEvent.AddVideo -> {

            }

            ProjectEvent.SaveProject -> TODO()

            ProjectEvent.ShowDialog -> {
                _state.update { it.copy(
                    isAddingProject = true
                ) }
            }
        }
    }
}


//class ProjectViewModel : ViewModel() {
//    private val _project = MutableStateFlow<Project?>(null)
//    val project: StateFlow<Project?> = _project.asStateFlow()
//
//    fun getProject(projectId: Int, projectRepository: ProjectRepository) {
//        viewModelScope.launch {
//            projectRepository.getProjectById(projectId).collect { project ->
//                _project.value = project
//            }
//        }
//    }
//}

