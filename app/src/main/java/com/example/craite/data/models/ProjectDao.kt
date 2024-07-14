package com.example.craite.data.models

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.craite.data.GeminiResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(project: Project)

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("SELECT * FROM Project")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM Project WHERE id = :id")
    fun getProjectById(id: Int): Flow<Project>

    @Query("SELECT * FROM Project ORDER BY id DESC LIMIT 1")
    suspend fun getLastInsertedProject(): Project

    @Query("UPDATE Project SET geminiResponse = :geminiResponse WHERE id = :projectId")
    suspend fun updateGeminiResponse(projectId: Int, geminiResponse: String)

    @Query("UPDATE Project SET editingSettings = :editingSettings WHERE id = :projectId")
    suspend fun updateEditingSettings(projectId: Int, editingSettings: String)

    @Query("UPDATE Project SET prompt = :prompt, promptId = :promptId WHERE id = :projectId")
    suspend fun updatePromptData(projectId: Int, prompt: String, promptId: String)

    @Query("UPDATE Project SET geminiResponse = :geminiResponse WHERE id = :projectId")
    suspend fun updateGeminiResponse(projectId: Int, geminiResponse: GeminiResponse)

    @Query("UPDATE Project SET mediaNames = :mediaNames WHERE id = :projectId")
    suspend fun updateMediaNames(projectId: Int, mediaNames: Map<String, String>)
}