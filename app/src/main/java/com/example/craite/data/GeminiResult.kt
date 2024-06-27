package com.example.craite.data

sealed class GeminiResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T> (data: T?): GeminiResult<T>(data)
    class Error<T> (data: T? = null, message: String): GeminiResult<T>(data, message)
}