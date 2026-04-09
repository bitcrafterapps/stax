package com.example.stax.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromPhotoList(photos: List<Photo>?): String? {
        return gson.toJson(photos)
    }

    @TypeConverter
    fun toPhotoList(photosString: String?): List<Photo> {
        if (photosString.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<Photo>>() {}.type
        return gson.fromJson(photosString, listType) ?: emptyList()
    }
} 