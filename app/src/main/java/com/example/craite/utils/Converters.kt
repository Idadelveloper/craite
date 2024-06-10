package com.example.craite.utils

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProjectTypeConverters {

    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        return Gson().toJson(uris)
    }

    @TypeConverter
    fun toUriList(uriString: String): List<Uri> {
        val listType = object : TypeToken<List<Uri>>() {}.type
        return Gson().fromJson(uriString, listType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>): String {
        return Gson().toJson(map)
    }

    @TypeConverter
    fun toMap(mapString: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return Gson().fromJson(mapString, mapType)
    }
}