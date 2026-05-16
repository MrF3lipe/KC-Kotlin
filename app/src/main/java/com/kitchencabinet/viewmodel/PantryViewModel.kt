package com.kitchencabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitchencabinet.data.AppDatabase
import com.kitchencabinet.data.PantryCategory
import com.kitchencabinet.data.PantryItem
import com.kitchencabinet.data.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryWithItems(
    val category: PantryCategory,
    val items: List<PantryItem>
)

class PantryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = Repository(
            db.recipeDao(), db.pantryDao(), db.shoppingDao(),
            db.settingsDao(), db.pantryCategoryDao(), db.mealPlanDao(), db.notificationsConfigDao()
        )

    val pantryItems: StateFlow<List<PantryItem>> = repo.pantryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<PantryCategory>> = repo.allPantryCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val groupedItems: StateFlow<List<CategoryWithItems>> = combine(
        pantryItems, categories
    ) { items, cats ->
        val catMap = cats.associateBy { it.name }
        items.groupBy { it.category }
            .map { (catName, catItems) ->
                val category = catMap[catName] ?: PantryCategory(name = catName, order = 999)
                CategoryWithItems(category, catItems)
            }
            .sortedBy { it.category.order }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(item: PantryItem) = viewModelScope.launch { repo.insertPantryItem(item) }
    fun update(item: PantryItem) = viewModelScope.launch { repo.updatePantryItem(item) }
    fun delete(item: PantryItem) = viewModelScope.launch { repo.deletePantryItem(item) }
    fun toggleAvailable(item: PantryItem) = viewModelScope.launch {
        repo.togglePantryAvailable(item.id, !item.available)
    }
    fun adjustQuantity(item: PantryItem, delta: Double) = viewModelScope.launch {
        val newQty = (item.quantity + delta).coerceAtLeast(0.0)
        repo.setPantryQuantity(item.id, newQty)
    }

    // Category management
    fun insertCategory(category: PantryCategory) = viewModelScope.launch {
        repo.insertPantryCategory(category)
    }
    fun updateCategory(category: PantryCategory) = viewModelScope.launch {
        repo.updatePantryCategory(category)
    }
    fun deleteCategory(category: PantryCategory) = viewModelScope.launch {
        repo.deletePantryCategory(category)
    }
}
