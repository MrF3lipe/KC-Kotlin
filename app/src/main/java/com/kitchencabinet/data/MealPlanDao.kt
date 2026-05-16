package com.kitchencabinet.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    @Query("SELECT * FROM meal_plan WHERE weekStart = :weekStart ORDER BY day, CASE slot WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    fun getByWeek(weekStart: String): Flow<List<MealPlanEntry>>

    @Query("SELECT * FROM meal_plan WHERE weekStart = :weekStart AND day = :day ORDER BY CASE slot WHEN 'breakfast' THEN 1 WHEN 'lunch' THEN 2 WHEN 'dinner' THEN 3 END")
    fun getByDay(weekStart: String, day: String): Flow<List<MealPlanEntry>>

    @Query("SELECT * FROM meal_plan WHERE weekStart = :weekStart AND day = :day AND slot = :slot")
    suspend fun getEntry(weekStart: String, day: String, slot: String): MealPlanEntry?

    @Query("SELECT * FROM meal_plan WHERE recipeId = :recipeId")
    fun getByRecipe(recipeId: Int): Flow<List<MealPlanEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MealPlanEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MealPlanEntry>)

    @Update
    suspend fun update(entry: MealPlanEntry)

    @Delete
    suspend fun delete(entry: MealPlanEntry)

    @Query("DELETE FROM meal_plan WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM meal_plan WHERE weekStart = :weekStart")
    suspend fun deleteWeek(weekStart: String)
}
