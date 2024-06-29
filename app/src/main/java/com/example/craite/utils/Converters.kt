package com.example.craite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.ui.input.key.type
import androidx.room.TypeConverter
import com.example.craite.data.EditSettings
import com.example.craite.data.GeminiResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

class ProjectTypeConverters {
//    val gson = GsonBuilder()
//        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
//        .create()
//    val gson = GsonBuilder().registerTypeAdapter(Uri::class.java, UriJsonAdapter()).create()
    val gson = Gson()

    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        val uriStrings = uris.map { it.toString() }
        return gson.toJson(uriStrings)
    }

    @TypeConverter
    fun toUriList(uriString: String): List<Uri> {
        val listType = object : TypeToken<List<String>>() {}.type
        val uriStrings = gson.fromJson<List<String>>(uriString, listType)
        return uriStrings.map { Uri.parse(it) }
    }

    @TypeConverter
    fun fromMapString(map: Map<String, String>?): String {
        return map?.entries?.joinToString(";") { "${it.key}={it.value}" } ?: ""
    }

    @TypeConverter
    fun toMapString(value: String?): Map<String, String>? {
        if (value == null) return null
        val map = mutableMapOf<String, String>()
        value.split(";").forEach { entry ->
            val parts = entry.split("=")
            if (parts.size == 2) {
                map[parts[0]] = parts[1]
            }
        }
        return map
    }

    @TypeConverter
    fun fromEditSettings(settings: EditSettings?): String {
        return Json.encodeToString(settings)
    }

    @TypeConverter
    fun toEditSettings(settingsString: String?): EditSettings? {
        return settingsString?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromGeminiResponse(response: GeminiResponse?): String {
        return Json.encodeToString(response)
    }

    @TypeConverter
    fun toGeminiResponse(responseString: String?): GeminiResponse? {
        return responseString?.let { Json.decodeFromString(it) }
    }

    @TypeConverter
    fun fromStringList(strings: List<String>): String {
        return gson.toJson(strings)
    }

    @TypeConverter
    fun toStringList(string: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(string, listType)
    }

}