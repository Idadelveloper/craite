package com.example.craite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.room.TypeConverter
import com.example.craite.data.EditSettings
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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
    fun fromEditingSettings(settings: EditSettings?): String {
        return gson.toJson(settings)
    }

    @TypeConverter
    fun toEditingSettings(settingsString: String?): EditSettings? {
        return gson.fromJson(settingsString, EditSettings::class.java)
    }

    @TypeConverter
    fun fromMapString(map: Map<String, String>?): String {
        return map?.entries?.joinToString(",") { "${it.key}=${it.value}" } ?: ""
    }

    @TypeConverter
    fun toMapString(value: String?): Map<String, String>? {
        if (value == null) return null
        val map = mutableMapOf<String, String>()
        val entries = value.split(",")
        for (entry in entries) {
            val keyValue = entry.split("=")
            if (keyValue.size == 2) {
                map[keyValue[0]] = keyValue[1]
            }
        }
        return map
    }




}