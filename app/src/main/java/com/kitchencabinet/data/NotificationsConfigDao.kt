package com.kitchencabinet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationsConfigDao {
    @Query("SELECT * FROM notifications_config WHERE id = 1")
    fun get(): Flow<NotificationsConfig?>

    @Query("SELECT * FROM notifications_config WHERE id = 1")
    suspend fun getOnce(): NotificationsConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: NotificationsConfig)

    @Update
    suspend fun update(config: NotificationsConfig)

    @Query("UPDATE notifications_config SET expiryEnabled = :enabled WHERE id = 1")
    suspend fun setExpiryEnabled(enabled: Boolean)

    @Query("UPDATE notifications_config SET expiryDaysBefore = :days WHERE id = 1")
    suspend fun setExpiryDaysBefore(days: Int)
}
