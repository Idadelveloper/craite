package com.example.craite.data

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val database: ProjectDatabase) {
//    val allProjects: Flow<List<Project>> = database.projectDao().getAllProjects()

    suspend fun insert(project: Project) {
        database.projectDao().insert(project)
    }

    suspend fun update(project: Project) {
        database.projectDao().update(project)
    }

    suspend fun delete(project: Project) {
        database.projectDao().delete(project)
    }

    fun getProjectById(id: Int): Flow<Project> {
        return database.projectDao().getProjectById(id)
    }

    fun getAllProjects(): Flow<List<Project>> {
        return database.projectDao().getAllProjects()
    }
}