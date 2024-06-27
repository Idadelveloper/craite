package com.example.craite.data

import kotlinx.coroutines.flow.Flow

interface EditSettingsRepository {
    suspend fun getEditSettings(): Flow<GeminiResult<EditSettings>>
}