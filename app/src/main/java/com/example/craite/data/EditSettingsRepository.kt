package com.example.craite.data

import kotlinx.coroutines.flow.Flow

interface EditSettingsRepository {
    suspend fun getEditSettings(request: GeminiRequest): Flow<GeminiResult<EditSettings>>
}