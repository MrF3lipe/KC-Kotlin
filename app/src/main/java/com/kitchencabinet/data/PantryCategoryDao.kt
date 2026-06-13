package com.kitchencabinet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryCategoryDao {
    @Query("SELECT * FROM pantry_categories ORDER BY `order` ASC")
    fun getAll(): Flow<List<PantryCategory>>

    @Query("SELECT * FROM pantry_categories ORDER BY `order` ASC")
    suspend fun getAllOnce(): List<PantryCategory>

    @Query("SELECT * FROM pantry_categories WHERE id = :id")
    suspend fun getById(id: Int): PantryCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: PantryCategory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<PantryCategory>)

    @Update
    suspend fun update(category: PantryCategory)

    @Delete
    suspend fun delete(category: PantryCategory)

    @Query("DELETE FROM pantry_categories WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM pantry_categories")
    suspend fun deleteAllCategories()
}
