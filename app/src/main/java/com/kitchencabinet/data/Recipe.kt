package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "recipes")
@TypeConverters(TypeConverters::class)
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val titleEn: String? = null,
    val description: String = "",
    val descriptionEn: String? = null,
    val image: String = "",
    val category: String = "General",
    val difficulty: String = "",
    val timeMinutes: Int = 30,
    val servings: Int = 4,
    val ingredients: List<Ingredient> = emptyList(),
    val ingredientsEn: List<Ingredient>? = null,
    val equipment: List<String> = emptyList(),
    val equipmentEn: List<String>? = null,
    val steps: List<String> = emptyList(),
    val stepsEn: List<String>? = null,
    val isFavorite: Boolean = false,
    val featured: Boolean = false,
    val source: String = "",
    val cookedCount: Int = 0,
    val rating: Float = 0f,
    val estimatedCost: Double? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromJson(json: JSONObject): Recipe = Recipe(
            id = json.optInt("id", 0),
            title = json.optString("title", ""),
            titleEn = json.optString("titleEn", null),
            description = json.optString("description", ""),
            descriptionEn = json.optString("descriptionEn", null),
            image = json.optString("image", ""),
            category = json.optString("category", "General"),
            difficulty = json.optString("difficulty", ""),
            timeMinutes = json.optInt("timeMinutes", 30),
            servings = json.optInt("servings", 4),
            ingredients = jsonArrayToIngredients(json.optJSONArray("ingredients")),
            equipment = jsonArrayToStringList(json.optJSONArray("equipment")),
            steps = jsonArrayToStringList(json.optJSONArray("steps")),
            isFavorite = json.optBoolean("isFavorite", false),
            featured = json.optBoolean("featured", false),
            source = json.optString("source", ""),
            cookedCount = json.optInt("cookedCount", 0),
            rating = json.optDouble("rating", 0.0).toFloat(),
            estimatedCost = if (json.has("estimatedCost") && !json.isNull("estimatedCost")) json.optDouble("estimatedCost") else null,
            tags = jsonArrayToStringList(json.optJSONArray("tags")),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
        )

        private fun jsonArrayToStringList(arr: JSONArray?): List<String> {
            if (arr == null) return emptyList()
            return (0 until arr.length()).map { arr.optString(it, "") }
        }

        private fun jsonArrayToIngredients(arr: JSONArray?): List<Ingredient> {
            if (arr == null) return emptyList()
            return (0 until arr.length()).map { i ->
                val obj = arr.optJSONObject(i)
                if (obj != null) Ingredient(obj.optString("name", ""), obj.optString("quantity", ""))
                else Ingredient("")
            }
        }
    }
}
