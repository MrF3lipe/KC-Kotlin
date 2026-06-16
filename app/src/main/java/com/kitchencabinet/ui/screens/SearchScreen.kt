package com.kitchencabinet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.data.ShoppingItem
import com.kitchencabinet.ui.components.RecipeCard
import com.kitchencabinet.ui.components.RecipeCardVariant
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.viewmodel.PantryViewModel
import com.kitchencabinet.viewmodel.RecipeViewModel
import com.kitchencabinet.viewmodel.ShoppingViewModel

@Composable
fun SearchScreen(
    onRecipeClick: (Int) -> Unit,
    onNavigateToShopping: () -> Unit = {},
    viewModel: RecipeViewModel = viewModel(),
    pantryViewModel: PantryViewModel = viewModel(),
    shoppingViewModel: ShoppingViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.recipes.collectAsState()
    val pantryItems by pantryViewModel.pantryItems.collectAsState()
    val shoppingItems by shoppingViewModel.shoppingItems.collectAsState()
    val focusRequester = remember { FocusRequester() }

    var selectedIngredients by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var showDifficultyFilter by remember { mutableStateOf(false) }
    var showCookableOnly by remember { mutableStateOf(false) }
    var selectedEquipment by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showFilters by remember { mutableStateOf(false) }

    val filterActive = selectedIngredients.isNotEmpty() || selectedDifficulty != null || selectedEquipment.isNotEmpty()

    LaunchedEffect(Unit) { viewModel.setCategory("All") }

    val ingredientOptions = remember(pantryItems) {
        pantryItems.filter { it.available }
            .map { it.name.lowercase().trim() }
            .distinct().sorted()
    }

    val equipmentOptions = remember(results) {
        results.flatMap { it.equipment }
            .filter { it.isNotBlank() }
            .distinct().sorted()
    }

    val availablePantry = remember(pantryItems) {
        pantryItems.filter { it.available }
            .map { it.name.lowercase().trim() }.toSet()
    }

    fun missingIngredients(recipe: Recipe): List<com.kitchencabinet.data.Ingredient> {
        return recipe.ingredients.filter { ing ->
            val name = ing.name.lowercase().trim()
            availablePantry.none { pantryName -> pantryName.contains(name) || name.contains(pantryName) }
        }
    }

    val (cookable, almost, others) = remember(results, selectedIngredients, selectedDifficulty, showCookableOnly, pantryItems, selectedEquipment) {

        data class MatchResult(val recipe: Recipe, val matchedCount: Int, val totalCount: Int)

        val evaluated = results
            .filter { if (selectedDifficulty != null) it.difficulty == selectedDifficulty else true }
            .filter { if (selectedIngredients.isEmpty()) true
                else it.ingredients.any { ing -> selectedIngredients.any { sel -> ing.name.lowercase().contains(sel) || sel.contains(ing.name.lowercase()) } } }
            .filter { if (selectedEquipment.isEmpty()) true
                else selectedEquipment.all { sel -> it.equipment.any { eq -> eq.lowercase().contains(sel.lowercase()) } } }
            .map { recipe ->
                if (recipe.ingredients.isEmpty()) MatchResult(recipe, 0, 0)
                else {
                    val matched = recipe.ingredients.count { ing ->
                        val ingName = ing.name.lowercase().trim()
                        availablePantry.any { pantryName -> pantryName.contains(ingName) || ingName.contains(pantryName) }
                    }
                    MatchResult(recipe, matched, recipe.ingredients.size)
                }
            }

        val cookableList = evaluated.filter { it.totalCount == 0 || it.matchedCount == it.totalCount }.map { it.recipe }
        val almostList = evaluated.filter { it.totalCount > 0 && it.matchedCount in 1 until it.totalCount }.map { it.recipe }
        val othersList = evaluated.filter { it.totalCount > 0 && it.matchedCount == 0 }.map { it.recipe }

        Triple(
            if (showCookableOnly) cookableList else cookableList,
            if (showCookableOnly) emptyList() else almostList,
            if (showCookableOnly) emptyList() else othersList
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header area with padding
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = strings.search.title,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = com.kitchencabinet.ui.theme.NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text(strings.search.placeholder) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) ({
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Filled.Clear, contentDescription = strings.tools.clear)
                    }
                }) else null,
                shape = RoundedCornerShape(50),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                )
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Cookable Only toggle ────────────────────────────────────────────
            item(key = "cookable_toggle") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Kitchen, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                strings.search.cookableToggle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                strings.search.cookableSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = showCookableOnly,
                            onCheckedChange = { showCookableOnly = it }
                        )
                    }
                }
            }

            // ── Filters toggle ────────────────────────────────────────────────────
            item(key = "filters_toggle") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = showFilters,
                        onClick = { showFilters = !showFilters },
                        label = { Text(strings.search.filterBtn) },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = null,
                                Modifier.size(18.dp)
                            )
                        },
                        shape = RoundedCornerShape(50)
                    )
                    if (filterActive) {
                        TextButton(onClick = {
                            selectedIngredients = emptySet()
                            selectedEquipment = emptySet()
                            selectedDifficulty = null
                        }) {
                            Text(strings.tools.clear, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // ── Ingredients section ──────────────────────────────────────────────
            if (showFilters && ingredientOptions.isNotEmpty()) {
                item(key = "ingredients_header") {
                    SectionCard(title = strings.search.ingredientsIHave) {
                        if (selectedIngredients.isNotEmpty()) {
                            Text(
                                strings.search.selectedCount.replace("{count}", "${selectedIngredients.size}"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        ChipGrid(chips = ingredientOptions, selected = selectedIngredients) {
                            selectedIngredients = if (it in selectedIngredients) selectedIngredients - it
                            else selectedIngredients + it
                        }
                    }
                }
            }

            // ── Equipment section ──────────────────────────────────────────────
            if (showFilters && equipmentOptions.isNotEmpty()) {
                item(key = "equipment_header") {
                    SectionCard(title = strings.search.utensils) {
                        ChipGrid(chips = equipmentOptions, selected = selectedEquipment) {
                            selectedEquipment = if (it in selectedEquipment) selectedEquipment - it
                            else selectedEquipment + it
                        }
                    }
                }
            }

            // ── Difficulty section ──────────────────────────────────────────────
            if (showFilters) {
                item(key = "difficulty_header") {
                    SectionCard(title = strings.search.difficulty) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            listOf("easy", "medium", "hard").forEachIndexed { idx, d ->
                                val isSel = selectedDifficulty == d
                                Surface(
                                    onClick = { selectedDifficulty = if (isSel) null else d },
                                    shape = if (idx == 0) RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp)
                                    else if (idx == 2) RoundedCornerShape(topEnd = 50.dp, bottomEnd = 50.dp)
                                    else RoundedCornerShape(0.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = when (d) {
                                            "easy" -> strings.search.easy
                                            "medium" -> strings.search.medium
                                            else -> strings.search.hard
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = if (isSel) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Results ──────────────────────────────────────────────────────────
            val showEmpty = query.isBlank() && selectedIngredients.isEmpty() && selectedDifficulty == null
            val hasResults = cookable.isNotEmpty() || almost.isNotEmpty() || others.isNotEmpty()

            if (showEmpty && !showCookableOnly) {
                item(key = "empty_state") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            strings.search.emptyHint,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!hasResults) {
                item(key = "no_results") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            strings.search.noResults.replace("{query}", query),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (cookable.isNotEmpty()) {
                    item(key = "header_cookable") {
                        GroupHeader(strings.search.groupCookable, Color(0xFF2E7D32), cookable.size)
                    }
                    items(cookable, key = { "cookable_${it.id}" }) { recipe ->
                        RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                            variant = RecipeCardVariant.Compact)
                    }
                }
                if (almost.isNotEmpty()) {
                    item(key = "header_almost") {
                        GroupHeader(strings.search.groupAlmost, Color(0xFFF57F17), almost.size)
                    }
                    items(almost, key = { "almost_${it.id}" }) { recipe ->
                        Column {
                            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                variant = RecipeCardVariant.Compact)
                            val missing = missingIngredients(recipe)
                            if (missing.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        missing.forEach { ing ->
                                            shoppingViewModel.insert(ShoppingItem(name = ing.name, quantity = 1.0, unit = "ud"))
                                        }
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(Icons.Filled.AddShoppingCart, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.search.addMissingCount.replace("{count}", "${missing.size}"), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                if (others.isNotEmpty()) {
                    item(key = "header_others") {
                        GroupHeader(strings.search.groupOthers, MaterialTheme.colorScheme.onSurfaceVariant, others.size)
                    }
                    items(others, key = { "others_${it.id}" }) { recipe ->
                        Column {
                            RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                variant = RecipeCardVariant.Compact)
                            val missing = missingIngredients(recipe)
                            if (missing.isNotEmpty()) {
                                TextButton(
                                    onClick = {
                                        missing.forEach { ing ->
                                            shoppingViewModel.insert(ShoppingItem(name = ing.name, quantity = 1.0, unit = "ud"))
                                        }
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(Icons.Filled.AddShoppingCart, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(strings.search.addMissingCount.replace("{count}", "${missing.size}"), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                }
            }
            } // if showFilters
        }

        // Shopping cart FAB
        if (shoppingItems.isNotEmpty()) {
            FloatingActionButton(
                onClick = onNavigateToShopping,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                BadgedBox(badge = {
                    Badge { Text("${shoppingItems.size}", style = MaterialTheme.typography.labelSmall) }
                }) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                }
            }
        }
    }
}
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontFamily = com.kitchencabinet.ui.theme.NewsreaderFontFamily,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ChipGrid(chips: List<String>, selected: Set<String>, onToggle: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.take(10).forEach { chip ->
            val isSel = chip in selected
            Surface(
                onClick = { onToggle(chip) },
                shape = RoundedCornerShape(50),
                color = if (isSel) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = if (!isSel) androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.outlineVariant
                ) else null,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSel) {
                        Icon(Icons.Filled.Check, null, Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Text(
                        chip.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(label: String, color: Color, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.15f)) {
            Text("$count", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }
    }
}
