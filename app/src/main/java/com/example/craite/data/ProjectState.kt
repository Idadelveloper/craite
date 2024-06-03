package com.example.craite.data

data class ProjectState(
    val projects: List<Project> = emptyList(),
    val selectedProject: Project? = null,
    val isLoading: Boolean = false,
    val isCreatingProject: Boolean = false
)