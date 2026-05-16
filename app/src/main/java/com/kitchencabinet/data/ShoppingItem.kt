package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val done: Boolean = false,
    val quantity: Double = 1.0,
    val unit: String = "ud",
    val fromRecipeId: Int? = null,
    val estimatedPrice: Double? = null
) {
    companion object {
        fun fromJson(json: JSONObject): ShoppingItem = ShoppingItem(
            id = json.optInt("id", 0),
            name = json.optString("name", ""),
            done = json.optBoolean("done", false),
            quantity = json.optDouble("quantity", 1.0),
            unit = json.optString("unit", "ud"),
            fromRecipeId = if (json.has("fromRecipeId") && !json.isNull("fromRecipeId")) json.optInt("fromRecipeId") else null,
            estimatedPrice = if (json.has("estimatedPrice") && !json.isNull("estimatedPrice")) json.optDouble("estimatedPrice") else null
        )
    }
}
