package com.kitchencabinet.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings WHERE id = 1")
    fun get(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SettingsEntity)

    @Update
    suspend fun update(settings: SettingsEntity)

    @Query("UPDATE settings SET locale = :locale WHERE id = 1")
    suspend fun setLocale(locale: String)

    @Query("UPDATE settings SET theme = :theme WHERE id = 1")
    suspend fun setTheme(theme: String)

    @Query("UPDATE settings SET onboarded = :onboarded WHERE id = 1")
    suspend fun setOnboarded(onboarded: Boolean)

    @Query("UPDATE settings SET currency = :currency WHERE id = 1")
    suspend fun setCurrency(currency: String)
}
