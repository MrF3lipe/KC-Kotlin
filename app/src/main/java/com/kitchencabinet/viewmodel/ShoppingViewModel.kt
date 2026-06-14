package com.kitchencabinet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kitchencabinet.data.AppDatabase
import com.kitchencabinet.data.PantryItem
import com.kitchencabinet.data.Repository
import com.kitchencabinet.data.ShoppingItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repo = Repository(
            db.recipeDao(), db.pantryDao(), db.shoppingDao(),
            db.settingsDao(), db.pantryCategoryDao(), db.mealPlanDao(), db.notificationsConfigDao()
        )

    val shoppingItems: StateFlow<List<ShoppingItem>> = repo.shoppingItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(item: ShoppingItem) = viewModelScope.launch { repo.insertShoppingItem(item) }
    fun update(item: ShoppingItem) = viewModelScope.launch { repo.updateShoppingItem(item) }
    fun delete(item: ShoppingItem) = viewModelScope.launch { repo.deleteShoppingItem(item) }
    fun toggleDone(id: Int, done: Boolean) = viewModelScope.launch {
        repo.toggleShoppingDone(id, done)
    }
    fun clearDone() = viewModelScope.launch { repo.clearDoneShopping() }

    fun adjustQuantity(id: Int, delta: Double) = viewModelScope.launch {
        val item = repo.getShoppingById(id) ?: return@launch
        val newQty = maxOf(0.0, item.quantity + delta)
        repo.updateShoppingItem(item.copy(quantity = newQty))
    }

    fun updateUnit(id: Int, newUnit: String) = viewModelScope.launch {
        val item = repo.getShoppingById(id) ?: return@launch
        repo.updateShoppingItem(item.copy(unit = newUnit))
    }

    fun updatePrice(id: Int, price: Double?) = viewModelScope.launch {
        val item = repo.getShoppingById(id) ?: return@launch
        repo.updateShoppingItem(item.copy(estimatedPrice = price))
    }

    fun moveDoneToPantry() = viewModelScope.launch {
        val doneItems = repo.getDoneShoppingItems()
        for (item in doneItems) {
            repo.insertPantryItem(
                PantryItem(
                    name = item.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    category = "Otros",
                    available = true
                )
            )
        }
        repo.clearDoneShopping()
    }
}
