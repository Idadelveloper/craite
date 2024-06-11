package com.example.craite.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream

class ProjectTypeConverters {
//    val gson = GsonBuilder()
//        .registerTypeAdapter(Uri::class.java, UriTypeAdapter())
//        .create()
    val gson = GsonBuilder().registerTypeAdapter(Uri::class.java, UriJsonAdapter()).create()

    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        return gson.toJson(uris)
    }

    @TypeConverter
    fun toUriList(uriString: String): List<Uri> {
        val listType = object : TypeToken<List<Uri>>() {}.type
        return gson.fromJson(uriString, listType)
    }

    @TypeConverter
    fun fromMap(map: Map<String, Any>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toMap(mapString: String): Map<String, Any> {
        val mapType = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(mapString, mapType)
    }

    @TypeConverter
    fun fromBitmap(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}