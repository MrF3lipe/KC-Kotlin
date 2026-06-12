package com.kitchencabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitchencabinet.data.AppDatabase
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.data.Repository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = Repository(
            db.recipeDao(), db.pantryDao(), db.shoppingDao(),
            db.settingsDao(), db.pantryCategoryDao(), db.mealPlanDao(), db.notificationsConfigDao()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val categories = listOf("All", "breakfast", "lunch", "dinner", "dessert", "snack", "bakery", "drink", "sauce", "salad", "soup")

    val recipes: StateFlow<List<Recipe>> = combine(
        _searchQuery,
        _selectedCategory
    ) { query, category -> Pair(query, category) }
        .flatMapLatest { (query, category) ->
            when {
                query.isNotBlank() -> repo.searchRecipes(query)
                category == "All" -> repo.allRecipes
                else -> repo.getByCategory(category)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<Recipe>> = repo.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setCategory(category: String) { _selectedCategory.value = category }

    fun insert(recipe: Recipe) = viewModelScope.launch { repo.insertRecipe(recipe) }
    suspend fun insertAndGetId(recipe: Recipe): Long = repo.insertRecipe(recipe)
    fun update(recipe: Recipe) = viewModelScope.launch { repo.updateRecipe(recipe) }
    fun delete(recipe: Recipe) = viewModelScope.launch { repo.deleteRecipe(recipe) }
    fun toggleFavorite(id: Int, isFavorite: Boolean) = viewModelScope.launch {
        repo.toggleFavorite(id, isFavorite)
    }
    fun incrementCookedCount(id: Int) = viewModelScope.launch {
        repo.incrementCookedCount(id)
    }

    suspend fun getById(id: Int): Recipe? = repo.getRecipeById(id)
}
