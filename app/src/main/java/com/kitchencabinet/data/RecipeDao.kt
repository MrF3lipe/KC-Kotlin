package com.kitchencabinet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY title ASC")
    fun getAll(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavorites(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getById(id: Int): Recipe?

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY title ASC")
    fun getByCategory(category: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE difficulty = :difficulty ORDER BY title ASC")
    fun getByDifficulty(difficulty: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE featured = 1 ORDER BY title ASC")
    fun getFeatured(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: Recipe): Long

    @Update
    suspend fun update(recipe: Recipe)

    @Delete
    suspend fun delete(recipe: Recipe)

    @Query("UPDATE recipes SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Int, isFavorite: Boolean)

    @Query("UPDATE recipes SET cookedCount = cookedCount + 1 WHERE id = :id")
    suspend fun incrementCookedCount(id: Int)

    @Query("UPDATE recipes SET rating = :rating WHERE id = :id")
    suspend fun setRating(id: Int, rating: Float)
}
