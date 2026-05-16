package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "pantry_categories")
data class PantryCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val nameEn: String? = null,
    val emoji: String? = null,
    val order: Int = 0
) {
    companion object {
        fun fromJson(json: JSONObject): PantryCategory = PantryCategory(
            id = json.optInt("id", 0),
            name = json.optString("name", ""),
            nameEn = json.optString("nameEn", null),
            emoji = json.optString("emoji", null),
            order = json.optInt("order", 0)
        )
    }
}
