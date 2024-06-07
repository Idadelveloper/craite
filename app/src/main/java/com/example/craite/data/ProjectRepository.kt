package com.example.craite.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProjectRepository(private val database: ProjectDatabase) {

    fun insert(project: Project): Flow<Unit> = flow {
        database.projectDao().insert(project).collect { emit(Unit) }
    }

    fun update(project: Project): Flow<Unit> = flow {
        database.projectDao().update(project).collect { emit(Unit) }
    }

    fun delete(project: Project): Flow<Unit> = flow {
        database.projectDao().delete(project).collect { emit(Unit) }
    }

    fun getProjectById(id: Int): Flow<Project> = database.projectDao().getProjectById(id)

    fun getAllProjects(): Flow<List<Project>> = database.projectDao().getAllProjects()
}