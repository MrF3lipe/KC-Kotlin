package com.kitchencabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitchencabinet.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = Repository(
        db.recipeDao(), db.pantryDao(), db.shoppingDao(),
        db.settingsDao(), db.pantryCategoryDao(), db.mealPlanDao(), db.notificationsConfigDao()
    )

    val settings: StateFlow<SettingsEntity?> = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notificationsConfig: StateFlow<NotificationsConfig?> = repo.notificationsConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setLocale(locale: String) = viewModelScope.launch { repo.setLocale(locale) }
    fun setTheme(theme: String) = viewModelScope.launch { repo.setTheme(theme) }
    fun setOnboarded(onboarded: Boolean) = viewModelScope.launch { repo.setOnboarded(onboarded) }
    fun setCurrency(currency: String) = viewModelScope.launch { repo.setCurrency(currency) }

    fun setExpiryNotificationsEnabled(enabled: Boolean) = viewModelScope.launch {
        repo.setExpiryNotificationsEnabled(enabled)
    }
    fun setExpiryDaysBefore(days: Int) = viewModelScope.launch {
        repo.setExpiryDaysBefore(days)
    }

    fun exportBackup(callback: (String) -> Unit) = viewModelScope.launch {
        val recipes = repo.allRecipes.first()
        val pantryItems = repo.pantryItems.first()
        val shoppingItems = repo.shoppingItems.first()
        val categories = repo.allPantryCategories.first()
        val s = repo.getSettingsOnce()
        val nc = repo.getNotificationsConfigOnce()

        val json = JSONObject().apply {
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())
            put("recipes", JSONArray(recipes.map { it.toJson() }))
            put("pantryItems", JSONArray(pantryItems.map { it.toJson() }))
            put("shoppingItems", JSONArray(shoppingItems.map { it.toJson() }))
            put("pantryCategories", JSONArray(categories.map { it.toJson() }))
            s?.let { put("settings", it.toJson()) }
            nc?.let { put("notificationsConfig", it.toJson()) }
        }
        callback(json.toString(2))
    }

    fun importBackup(jsonString: String, onResult: (Boolean, String) -> Unit) = viewModelScope.launch {
        try {
            val json = JSONObject(jsonString)

            // Clear existing data
            repo.allRecipes.first().forEach { repo.deleteRecipe(it) }
            repo.pantryItems.first().forEach { repo.deletePantryItem(it) }
            repo.shoppingItems.first().forEach { repo.deleteShoppingItem(it) }
            repo.allPantryCategories.first().forEach { repo.deletePantryCategory(it) }

            // Import categories
            val catJson = json.optJSONArray("pantryCategories")
            if (catJson != null) {
                for (i in 0 until catJson.length()) {
                    val obj = catJson.getJSONObject(i)
                    repo.insertPantryCategory(PantryCategory.fromJson(obj))
                }
            }

            // Import recipes
            val recJson = json.optJSONArray("recipes")
            if (recJson != null) {
                for (i in 0 until recJson.length()) {
                    val obj = recJson.getJSONObject(i)
                    repo.insertRecipe(Recipe.fromJson(obj))
                }
            }

            // Import pantry
            val panJson = json.optJSONArray("pantryItems")
            if (panJson != null) {
                for (i in 0 until panJson.length()) {
                    val obj = panJson.getJSONObject(i)
                    repo.insertPantryItem(PantryItem.fromJson(obj))
                }
            }

            // Import shopping
            val shopJson = json.optJSONArray("shoppingItems")
            if (shopJson != null) {
                for (i in 0 until shopJson.length()) {
                    val obj = shopJson.getJSONObject(i)
                    repo.insertShoppingItem(ShoppingItem.fromJson(obj))
                }
            }

            // Import settings
            json.optJSONObject("settings")?.let { repo.saveSettings(SettingsEntity.fromJson(it)) }
            json.optJSONObject("notificationsConfig")?.let { repo.saveNotificationsConfig(NotificationsConfig.fromJson(it)) }

            onResult(true, "Import successful!")
        } catch (e: Exception) {
            onResult(false, "Import failed: ${e.message}")
        }
    }
}

// Extension functions for JSON serialization
private fun Recipe.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("title", title); put("titleEn", titleEn)
    put("description", description); put("descriptionEn", descriptionEn)
    put("image", image); put("category", category); put("difficulty", difficulty)
    put("timeMinutes", timeMinutes); put("servings", servings)
    put("ingredients", JSONArray(ingredients.map { JSONObject().apply { put("name", it.name); put("quantity", it.quantity) } }))
    put("equipment", JSONArray(equipment)); put("steps", JSONArray(steps))
    put("isFavorite", isFavorite); put("featured", featured); put("source", source)
    put("cookedCount", cookedCount); put("rating", rating.toDouble())
    put("tags", JSONArray(tags)); put("createdAt", createdAt); put("updatedAt", updatedAt)
}

private fun PantryItem.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("name", name); put("nameEn", nameEn)
    put("category", category); put("quantity", quantity); put("unit", unit)
    put("available", available); put("expiresAt", expiresAt); put("pricePerUnit", pricePerUnit)
    put("barcode", barcode)
}

private fun ShoppingItem.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("name", name); put("done", done)
    put("quantity", quantity); put("unit", unit); put("fromRecipeId", fromRecipeId)
    put("estimatedPrice", estimatedPrice)
}

private fun PantryCategory.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("name", name); put("nameEn", nameEn)
    put("emoji", emoji); put("order", order)
}

private fun SettingsEntity.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("locale", locale); put("theme", theme)
    put("onboarded", onboarded); put("currency", currency)
}

private fun NotificationsConfig.toJson(): JSONObject = JSONObject().apply {
    put("id", id); put("expiryEnabled", expiryEnabled); put("expiryDaysBefore", expiryDaysBefore)
}
