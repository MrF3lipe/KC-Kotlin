package com.kitchencabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitchencabinet.data.AppDatabase
import com.kitchencabinet.data.MealPlanEntry
import com.kitchencabinet.data.Repository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = Repository(
        db.recipeDao(), db.pantryDao(), db.shoppingDao(),
        db.settingsDao(), db.pantryCategoryDao(), db.mealPlanDao(), db.notificationsConfigDao()
    )

    private val _currentWeekStart = MutableStateFlow(getCurrentWeekStart())
    val currentWeekStart: StateFlow<String> = _currentWeekStart.asStateFlow()

    val currentWeekEntries: StateFlow<List<MealPlanEntry>> = _currentWeekStart
        .flatMapLatest { weekStart -> repo.getMealPlanWeek(weekStart) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getCurrentWeekStart(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return fmt.format(cal.time)
    }

    fun loadWeek(weekStart: String) {
        _currentWeekStart.value = weekStart
    }

    fun addEntry(weekStart: String, day: String, slot: String, recipeId: Int) = viewModelScope.launch {
        // First check if there's already an entry for this slot
        val existing = repo.getMealPlanEntry(weekStart, day, slot)
        if (existing != null) {
            repo.updateMealPlanEntry(existing.copy(recipeId = recipeId))
        } else {
            repo.insertMealPlanEntry(
                MealPlanEntry(
                    weekStart = weekStart,
                    day = day,
                    slot = slot,
                    recipeId = recipeId
                )
            )
        }
        // Refresh
        loadWeek(weekStart)
    }

    fun removeEntry(entry: MealPlanEntry) = viewModelScope.launch {
        repo.deleteMealPlanEntry(entry)
    }

    fun clearWeek(weekStart: String) = viewModelScope.launch {
        repo.deleteMealPlanWeek(weekStart)
        loadWeek(weekStart)
    }

    fun addCurrentWeekToShopping() = viewModelScope.launch {
        val entries = currentWeekEntries.value
        val recipeIds = entries.map { it.recipeId }.distinct()
        for (recipeId in recipeIds) {
            val recipe = repo.getRecipeById(recipeId) ?: continue
            val existingItems = repo.getShoppingByRecipeId(recipeId).first()
            val existingNames = existingItems.map { it.name.lowercase() }.toSet()
            val newItems = recipe.ingredients
                .filter { it.name.lowercase() !in existingNames }
                .map { ing ->
                    com.kitchencabinet.data.ShoppingItem(
                        name = ing.name,
                        quantity = ing.quantity.toDoubleOrNull() ?: 1.0,
                        unit = "ud",
                        fromRecipeId = recipeId
                    )
                }
            if (newItems.isNotEmpty()) {
                repo.insertShoppingItems(newItems)
            }
        }
    }
}
