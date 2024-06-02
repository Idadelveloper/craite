package com.example.craite.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert
    suspend fun insert(project: Project)

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("SELECT * FROM Project")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM Project WHERE id = :id")
    fun getProjectById(id: Int): Flow<Project>
}

@Database(entities = [Project::class], version = 1)
abstract class ProjectDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}

class ProjectRepository(private val database: ProjectDatabase) {
    val allProjects: Flow<List<Project>> = database.projectDao().getAllProjects()

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