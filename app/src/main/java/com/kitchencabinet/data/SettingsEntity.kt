package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val locale: String = "es",
    val theme: String = "light",
    val onboarded: Boolean = false,
    val currency: String? = "€"
) {
    companion object {
        fun fromJson(json: JSONObject): SettingsEntity = SettingsEntity(
            id = json.optInt("id", 1),
            locale = json.optString("locale", "es"),
            theme = json.optString("theme", "light"),
            onboarded = json.optBoolean("onboarded", false),
            currency = json.optString("currency", null)
        )
    }
}
