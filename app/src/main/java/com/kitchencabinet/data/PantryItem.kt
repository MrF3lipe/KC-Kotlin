package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val nameEn: String? = null,
    val category: String = "General",
    val quantity: Double = 1.0,
    val unit: String = "ud",
    val available: Boolean = true,
    val expiresAt: Long? = null,
    val pricePerUnit: Double? = null,
    val barcode: String? = null
) {
    companion object {
        fun fromJson(json: JSONObject): PantryItem = PantryItem(
            id = json.optInt("id", 0),
            name = json.optString("name", ""),
            nameEn = json.optString("nameEn", null),
            category = json.optString("category", "General"),
            quantity = json.optDouble("quantity", 1.0),
            unit = json.optString("unit", "ud"),
            available = json.optBoolean("available", true),
            expiresAt = if (json.has("expiresAt") && !json.isNull("expiresAt")) json.optLong("expiresAt") else null,
            pricePerUnit = if (json.has("pricePerUnit") && !json.isNull("pricePerUnit")) json.optDouble("pricePerUnit") else null,
            barcode = json.optString("barcode", null)
        )
    }
}
