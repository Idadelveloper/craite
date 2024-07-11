package com.example.craite.data

import coil.network.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class EditSettingsRepositoryImpl(
    private val api: GeminiResponseApi
): EditSettingsRepository {
    override suspend fun getEditSettings(request: GeminiRequest): Flow<GeminiResult<EditSettings>> {
        return flow {
            val editSettingsFromApi = try {
                api.getGeminiResponse(request)
            } catch (e: IOException) {
                e.printStackTrace()
                val errorMessage = when (e) {
                    is SocketTimeoutException -> "Connection timed out. Check your network."
                    is UnknownHostException -> "Could not reach the server. Check the server address and your network."
                    else -> "Error loading edit settings - I/O: ${e.message}"
                }
                emit(GeminiResult.Error(message = errorMessage))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(GeminiResult.Error(message = "Error loading edit settings - Http"))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(GeminiResult.Error(message = "Error loading edit settings - Normal exception"))
                return@flow
            }
            emit(GeminiResult.Success(editSettingsFromApi.gemini_response))
        }
    }
}