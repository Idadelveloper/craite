package com.example.craite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.craite.data.Project
import com.example.craite.data.ProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewProjectViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<NewProjectUiState>(NewProjectUiState.Idle)
    val uiState: StateFlow<NewProjectUiState> = _uiState.asStateFlow()

    fun createProject(
        projectName: String,
        projectRepository: ProjectRepository,
        function: () -> Unit
    ) {
        if (projectName.isBlank()) {
            _uiState.value = NewProjectUiState.Error("Project name cannot be empty")
            return
        }

        _uiState.value = NewProjectUiState.Loading
        viewModelScope.launch {
            try {
                val newProject: Project = Project(name=projectName)
                projectRepository.insert(newProject)
                _uiState.value = NewProjectUiState.Success
            } catch (e: Exception) {
                _uiState.value = NewProjectUiState.Error(e.message ?: "Error creating project")
            }
        }
    }
}

sealed class NewProjectUiState {
    object Idle : NewProjectUiState()
    object Loading : NewProjectUiState()
    object Success : NewProjectUiState()
    data class Error(val message: String?) : NewProjectUiState()
}