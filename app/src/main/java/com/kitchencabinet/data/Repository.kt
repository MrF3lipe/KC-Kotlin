package com.kitchencabinet.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val recipeDao: RecipeDao,
    private val pantryDao: PantryDao,
    private val shoppingDao: ShoppingDao,
    private val settingsDao: SettingsDao,
    private val pantryCategoryDao: PantryCategoryDao,
    private val mealPlanDao: MealPlanDao,
    private val notificationsConfigDao: NotificationsConfigDao
) {
    // ── Recipes ──────────────────────────────────────────────
    val allRecipes: Flow<List<Recipe>> = recipeDao.getAll()
    val favorites: Flow<List<Recipe>> = recipeDao.getFavorites()

    fun searchRecipes(query: String): Flow<List<Recipe>> = recipeDao.search(query)
    fun getByCategory(category: String): Flow<List<Recipe>> = recipeDao.getByCategory(category)
    fun getByDifficulty(difficulty: String): Flow<List<Recipe>> = recipeDao.getByDifficulty(difficulty)
    fun getFeatured(): Flow<List<Recipe>> = recipeDao.getFeatured()
    suspend fun getRecipeById(id: Int): Recipe? = recipeDao.getById(id)
    suspend fun insertRecipe(recipe: Recipe): Long = recipeDao.insert(recipe)
    suspend fun updateRecipe(recipe: Recipe) = recipeDao.update(recipe)
    suspend fun deleteRecipe(recipe: Recipe) = recipeDao.delete(recipe)
    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) = recipeDao.setFavorite(id, isFavorite)
    suspend fun incrementCookedCount(id: Int) = recipeDao.incrementCookedCount(id)
    suspend fun setRating(id: Int, rating: Float) = recipeDao.setRating(id, rating)

    // ── Pantry Items ────────────────────────────────────────
    val pantryItems: Flow<List<PantryItem>> = pantryDao.getAll()
    fun getPantryByCategory(category: String): Flow<List<PantryItem>> = pantryDao.getByCategory(category)
    fun getAvailablePantry(): Flow<List<PantryItem>> = pantryDao.getAvailable()
    fun getExpiringPantry(days: Int): Flow<List<PantryItem>> =
        pantryDao.getExpiring(System.currentTimeMillis() + days * 86400000L)
    suspend fun getPantryById(id: Int): PantryItem? = pantryDao.getById(id)
    suspend fun insertPantryItem(item: PantryItem): Long = pantryDao.insert(item)
    suspend fun updatePantryItem(item: PantryItem) = pantryDao.update(item)
    suspend fun deletePantryItem(item: PantryItem) = pantryDao.delete(item)
    suspend fun togglePantryAvailable(id: Int, available: Boolean) = pantryDao.setAvailable(id, available)
    suspend fun setPantryQuantity(id: Int, quantity: Double) = pantryDao.setQuantity(id, quantity)

    // ── Pantry Categories ───────────────────────────────────
    val allPantryCategories: Flow<List<PantryCategory>> = pantryCategoryDao.getAll()
    suspend fun getAllPantryCategoriesOnce(): List<PantryCategory> = pantryCategoryDao.getAllOnce()
    suspend fun getPantryCategoryById(id: Int): PantryCategory? = pantryCategoryDao.getById(id)
    suspend fun insertPantryCategory(category: PantryCategory): Long = pantryCategoryDao.insert(category)
    suspend fun insertPantryCategories(categories: List<PantryCategory>) = pantryCategoryDao.insertAll(categories)
    suspend fun updatePantryCategory(category: PantryCategory) = pantryCategoryDao.update(category)
    suspend fun deletePantryCategory(category: PantryCategory) = pantryCategoryDao.delete(category)

    // ── Shopping ────────────────────────────────────────────
    val shoppingItems: Flow<List<ShoppingItem>> = shoppingDao.getAll()
    fun getShoppingByRecipeId(recipeId: Int): Flow<List<ShoppingItem>> = shoppingDao.getByRecipeId(recipeId)
    suspend fun getShoppingById(id: Int): ShoppingItem? = shoppingDao.getById(id)
    suspend fun insertShoppingItem(item: ShoppingItem): Long = shoppingDao.insert(item)
    suspend fun insertShoppingItems(items: List<ShoppingItem>) = shoppingDao.insertAll(items)
    suspend fun updateShoppingItem(item: ShoppingItem) = shoppingDao.update(item)
    suspend fun deleteShoppingItem(item: ShoppingItem) = shoppingDao.delete(item)
    suspend fun toggleShoppingDone(id: Int, done: Boolean) = shoppingDao.setDone(id, done)
    suspend fun clearDoneShopping() = shoppingDao.clearDone()
    suspend fun getDoneShoppingItems(): List<ShoppingItem> = shoppingDao.getDoneItems()

    // ── Settings ────────────────────────────────────────────
    val settings: Flow<SettingsEntity?> = settingsDao.get()
    suspend fun getSettingsOnce(): SettingsEntity? = settingsDao.getOnce()
    suspend fun saveSettings(settings: SettingsEntity) = settingsDao.insert(settings)
    suspend fun updateSettings(settings: SettingsEntity) = settingsDao.update(settings)
    suspend fun setLocale(locale: String) = settingsDao.setLocale(locale)
    suspend fun setTheme(theme: String) = settingsDao.setTheme(theme)
    suspend fun setOnboarded(onboarded: Boolean) = settingsDao.setOnboarded(onboarded)
    suspend fun setCurrency(currency: String) = settingsDao.setCurrency(currency)

    // ── Meal Plan ───────────────────────────────────────────
    fun getMealPlanWeek(weekStart: String): Flow<List<MealPlanEntry>> = mealPlanDao.getByWeek(weekStart)
    fun getMealPlanDay(weekStart: String, day: String): Flow<List<MealPlanEntry>> = mealPlanDao.getByDay(weekStart, day)
    suspend fun getMealPlanEntry(weekStart: String, day: String, slot: String): MealPlanEntry? =
        mealPlanDao.getEntry(weekStart, day, slot)
    fun getMealPlanByRecipe(recipeId: Int): Flow<List<MealPlanEntry>> = mealPlanDao.getByRecipe(recipeId)
    suspend fun insertMealPlanEntry(entry: MealPlanEntry): Long = mealPlanDao.insert(entry)
    suspend fun insertMealPlanEntries(entries: List<MealPlanEntry>) = mealPlanDao.insertAll(entries)
    suspend fun updateMealPlanEntry(entry: MealPlanEntry) = mealPlanDao.update(entry)
    suspend fun deleteMealPlanEntry(entry: MealPlanEntry) = mealPlanDao.delete(entry)
    suspend fun deleteMealPlanWeek(weekStart: String) = mealPlanDao.deleteWeek(weekStart)

    // ── Notifications Config ────────────────────────────────
    val notificationsConfig: Flow<NotificationsConfig?> = notificationsConfigDao.get()
    suspend fun getNotificationsConfigOnce(): NotificationsConfig? = notificationsConfigDao.getOnce()
    suspend fun saveNotificationsConfig(config: NotificationsConfig) = notificationsConfigDao.insert(config)
    suspend fun updateNotificationsConfig(config: NotificationsConfig) = notificationsConfigDao.update(config)
    suspend fun setExpiryNotificationsEnabled(enabled: Boolean) = notificationsConfigDao.setExpiryEnabled(enabled)
    suspend fun setExpiryDaysBefore(days: Int) = notificationsConfigDao.setExpiryDaysBefore(days)

    // ── Backup & Restore ────────────────────────────────────
    data class BackupData(
        val recipes: List<Recipe>,
        val pantry: List<PantryItem>,
        val pantryCategories: List<PantryCategory>,
        val shopping: List<ShoppingItem>,
        val mealPlan: List<MealPlanEntry>,
        val settings: SettingsEntity?,
        val notificationsConfig: NotificationsConfig?
    )

    suspend fun exportBackup(): BackupData = BackupData(
        recipes = recipeDao.getAllOnce(),
        pantry = pantryDao.getAllOnce(),
        pantryCategories = pantryCategoryDao.getAllOnce(),
        shopping = shoppingDao.getAllOnce(),
        mealPlan = mealPlanDao.getAllOnce(),
        settings = settingsDao.getOnce(),
        notificationsConfig = notificationsConfigDao.getOnce()
    )

    suspend fun importBackup(data: BackupData, clearExisting: Boolean = true) {
        if (clearExisting) {
            recipeDao.deleteAll()
            pantryDao.deleteAll()
            pantryCategoryDao.deleteAllCategories()
            shoppingDao.deleteAll()
            mealPlanDao.deleteAll()
        }
        if (data.recipes.isNotEmpty()) recipeDao.insertAll(data.recipes)
        if (data.pantry.isNotEmpty()) pantryDao.insertAll(data.pantry)
        if (data.pantryCategories.isNotEmpty()) pantryCategoryDao.insertAll(data.pantryCategories)
        if (data.shopping.isNotEmpty()) shoppingDao.insertAll(data.shopping)
        if (data.mealPlan.isNotEmpty()) mealPlanDao.insertAll(data.mealPlan)
        data.settings?.let { settingsDao.insert(it) }
        data.notificationsConfig?.let { notificationsConfigDao.insert(it) }
    }
}
