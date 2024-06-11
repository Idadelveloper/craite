package com.example.craite.utils

import android.net.Uri
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Type

class UriTypeAdapter : TypeAdapter<Uri>() {
    override fun write(out: JsonWriter, value: Uri) {
        out.value(value.toString())
    }

    override fun read(`in`: JsonReader): Uri? {
        return Uri.parse(`in`.nextString())
    }
}

class UriJsonAdapter: JsonSerializer<Uri>, JsonDeserializer<Uri> {
    override fun serialize(src: Uri, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(src: JsonElement, srcType: Type, context: JsonDeserializationContext): Uri {
        return try {
            val url = src.asString
            if (url.isNullOrEmpty()) {
                Uri.EMPTY
            } else {
                Uri.parse(url)
            }
        } catch (e: UnsupportedOperationException) {
            Uri.EMPTY
        }
    }
}