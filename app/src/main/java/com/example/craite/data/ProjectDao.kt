package com.example.craite.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
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