package com.kitchencabinet.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object AppTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun stringListFromString(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun stringListToString(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun ingredientListFromString(value: String): List<Ingredient> {
        if (value.isBlank()) return emptyList()
        val type = object : TypeToken<List<Ingredient>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun ingredientListToString(list: List<Ingredient>): String = gson.toJson(list)

}
