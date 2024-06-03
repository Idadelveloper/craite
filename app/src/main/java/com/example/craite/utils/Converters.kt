package com.example.craite.utils

import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        return uris.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toUriList(value: String): List<Uri> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { Uri.parse(it) }
        }
    }

    @TypeConverter
    fun fromMap(map: Map<String?, Any?>?): String {
        val gson = Gson()
        return gson.toJson(map)
    }

    @TypeConverter
    fun toMap(json: String?): Map<String, Any> {
        val gson = Gson()
        val token: TypeToken<Map<String?, Any?>?> = object : TypeToken<Map<String?, Any?>?>() {}
        return gson.fromJson(json, token.type)
    }

}