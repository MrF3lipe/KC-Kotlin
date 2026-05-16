package com.kitchencabinet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY category ASC, name ASC")
    fun getAll(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun getById(id: Int): PantryItem?

    @Query("SELECT * FROM pantry_items WHERE category = :category ORDER BY name ASC")
    fun getByCategory(category: String): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE expiresAt IS NOT NULL AND expiresAt > 0 AND expiresAt <= :expireThreshold")
    fun getExpiring(expireThreshold: Long): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE available = 1 ORDER BY category ASC, name ASC")
    fun getAvailable(): Flow<List<PantryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PantryItem): Long

    @Update
    suspend fun update(item: PantryItem)

    @Delete
    suspend fun delete(item: PantryItem)

    @Query("UPDATE pantry_items SET available = :available WHERE id = :id")
    suspend fun setAvailable(id: Int, available: Boolean)

    @Query("UPDATE pantry_items SET quantity = :quantity WHERE id = :id")
    suspend fun setQuantity(id: Int, quantity: Double)
}
