package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meal_plan")
data class MealPlanEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val weekStart: String, // YYYY-MM-DD del lunes
    val day: String, // mon, tue, wed, thu, fri, sat, sun
    val slot: String, // breakfast, lunch, dinner
    val recipeId: Int
)
