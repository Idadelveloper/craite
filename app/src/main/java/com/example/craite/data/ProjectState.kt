package com.example.craite.data

data class ProjectState(
    val projects: List<Project> = emptyList(),
    val projectName: String = "",
    val selectedProject: Project? = null,
    val isLoading: Boolean = false,
    val isAddingProject: Boolean = false,
    val sortType: SortType = SortType.NAME
)