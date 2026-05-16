package com.kitchencabinet.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "notifications_config")
data class NotificationsConfig(
    @PrimaryKey val id: Int = 1,
    val expiryEnabled: Boolean = true,
    val expiryDaysBefore: Int = 2
) {
    companion object {
        fun fromJson(json: JSONObject): NotificationsConfig = NotificationsConfig(
            id = json.optInt("id", 1),
            expiryEnabled = json.optBoolean("expiryEnabled", true),
            expiryDaysBefore = json.optInt("expiryDaysBefore", 2)
        )
    }
}
