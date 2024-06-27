package com.example.craite.data

import coil.network.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException

class EditSettingsRepositoryImpl(
    private val api: GeminiResponseApi
): EditSettingsRepository {
    override suspend fun getEditSettings(request: GeminiRequest): Flow<GeminiResult<EditSettings>> {
        return flow {
            val editSettingsFromApi = try {
                api.getGeminiResponse(request)
            } catch (e: IOException) {
                e.printStackTrace()
                emit(GeminiResult.Error(message = "Error loading edit settings"))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(GeminiResult.Error(message = "Error loading edit settings"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(GeminiResult.Error(message = "Error loading edit settings"))
                return@flow
            }
            emit(GeminiResult.Success(editSettingsFromApi.gemini_response))
        }
    }
}