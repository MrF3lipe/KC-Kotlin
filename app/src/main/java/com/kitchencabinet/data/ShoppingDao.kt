package com.kitchencabinet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY done ASC, name ASC")
    fun getAll(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE id = :id")
    suspend fun getById(id: Int): ShoppingItem?

    @Query("SELECT * FROM shopping_items WHERE fromRecipeId = :recipeId")
    fun getByRecipeId(recipeId: Int): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items")
    suspend fun getAllOnce(): List<ShoppingItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ShoppingItem>)

    @Query("DELETE FROM shopping_items")
    suspend fun deleteAll()

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("UPDATE shopping_items SET done = :done WHERE id = :id")
    suspend fun setDone(id: Int, done: Boolean)

    @Query("DELETE FROM shopping_items WHERE done = 1")
    suspend fun clearDone()

    @Query("SELECT * FROM shopping_items WHERE done = 1")
    suspend fun getDoneItems(): List<ShoppingItem>
}
