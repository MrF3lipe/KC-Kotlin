package com.kitchencabinet.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.ui.components.RecipeCard
import com.kitchencabinet.ui.components.RecipeCardVariant
import com.kitchencabinet.viewmodel.PantryViewModel
import com.kitchencabinet.viewmodel.RecipeViewModel

@Composable
fun SearchScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: RecipeViewModel = viewModel(),
    pantryViewModel: PantryViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.recipes.collectAsState()
    val pantryItems by pantryViewModel.pantryItems.collectAsState()
    val focusRequester = remember { FocusRequester() }

    var selectedIngredients by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var showDifficultyFilter by remember { mutableStateOf(false) }
    var showCookableOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setCategory("All")
    }

    val ingredientOptions = remember(pantryItems) {
        pantryItems.filter { it.available }
            .map { it.name.lowercase().trim() }
            .distinct()
            .sorted()
    }

    val (cookable, almost, others) = remember(results, selectedIngredients, selectedDifficulty, showCookableOnly, pantryItems) {
        val availablePantry = pantryItems.filter { it.available }
            .map { it.name.lowercase().trim() }
            .toSet()

        data class MatchResult(val recipe: Recipe, val matchedCount: Int, val totalCount: Int)

        val evaluated = results
            .filter { recipe ->
                if (selectedDifficulty != null) recipe.difficulty == selectedDifficulty else true
            }
            .filter { recipe ->
                if (selectedIngredients.isEmpty()) true
                else recipe.ingredients.any { ing ->
                    selectedIngredients.any { sel ->
                        ing.name.lowercase().contains(sel) || sel.contains(ing.name.lowercase())
                    }
                }
            }
            .map { recipe ->
                if (recipe.ingredients.isEmpty()) {
                    MatchResult(recipe, 0, 0)
                } else {
                    val matched = recipe.ingredients.count { ing ->
                        val ingName = ing.name.lowercase().trim()
                        availablePantry.any { pantryName ->
                            pantryName.contains(ingName) || ingName.contains(pantryName)
                        }
                    }
                    MatchResult(recipe, matched, recipe.ingredients.size)
                }
            }

        val cookableList = evaluated.filter {
            it.totalCount == 0 || it.matchedCount == it.totalCount
        }.map { it.recipe }

        val almostList = evaluated.filter {
            it.totalCount > 0 && it.matchedCount in 1 until it.totalCount
        }.map { it.recipe }

        val othersList = evaluated.filter {
            it.totalCount > 0 && it.matchedCount == 0
        }.map { it.recipe }

        val finalCookable = if (showCookableOnly) cookableList else cookableList
        val finalAlmost = if (showCookableOnly) emptyList<Recipe>() else almostList
        val finalOthers = if (showCookableOnly) emptyList<Recipe>() else othersList

        Triple(finalCookable, finalAlmost, finalOthers)
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {

        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search recipes\u2026") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) ({
                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                }
            }) else null,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
        )

        Spacer(Modifier.height(8.dp))

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedDifficulty != null,
                onClick = { showDifficultyFilter = !showDifficultyFilter },
                label = { Text(selectedDifficulty ?: "Difficulty") },
                leadingIcon = { Icon(Icons.Filled.Speed, null, Modifier.size(16.dp)) },
                trailingIcon = if (selectedDifficulty != null) ({
                    IconButton(onClick = { selectedDifficulty = null }) {
                        Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                    }
                }) else null
            )

            FilterChip(
                selected = showCookableOnly,
                onClick = { showCookableOnly = !showCookableOnly },
                label = { Text("Cookable") },
                leadingIcon = { Icon(Icons.Filled.Kitchen, null, Modifier.size(16.dp)) }
            )

            if (ingredientOptions.isNotEmpty()) {
                FilterChip(
                    selected = selectedIngredients.isNotEmpty(),
                    onClick = {
                        selectedIngredients = if (selectedIngredients.isNotEmpty()) emptySet() else ingredientOptions.take(5).toSet()
                    },
                    label = {
                        Text(
                            if (selectedIngredients.isEmpty()) "Ingredients"
                            else "${selectedIngredients.size} selected"
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.ShoppingBasket, null, Modifier.size(16.dp)) },
                    trailingIcon = if (selectedIngredients.isNotEmpty()) ({
                        IconButton(onClick = { selectedIngredients = emptySet() }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                        }
                    }) else null
                )
            }
        }

        // Difficulty picker
        if (showDifficultyFilter) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("easy", "medium", "hard").forEach { d ->
                    FilterChip(
                        selected = selectedDifficulty == d,
                        onClick = { selectedDifficulty = if (selectedDifficulty == d) null else d },
                        label = { Text(d) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        // Ingredient chips
        if (selectedIngredients.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ingredientOptions.forEach { ing ->
                    val isSelected = ing in selectedIngredients
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedIngredients = if (isSelected) selectedIngredients - ing
                            else selectedIngredients + ing
                        },
                        label = { Text(ing.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                if (isSelected) Icons.Filled.Check else Icons.Filled.Add,
                                null,
                                Modifier.size(14.dp)
                            )
                        }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(8.dp))

        // Results
        val showEmpty = query.isBlank() && selectedIngredients.isEmpty() && selectedDifficulty == null
        val hasResults = cookable.isNotEmpty() || almost.isNotEmpty() || others.isNotEmpty()

        when {
            showEmpty && !showCookableOnly -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Type to search recipes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            !hasResults -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No results for \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (cookable.isNotEmpty()) {
                        item(key = "header_cookable") {
                            GroupHeader("Cookable", Color(0xFF2E7D32), cookable.size)
                        }
                        items(cookable, key = { "cookable_${it.id}" }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                variant = RecipeCardVariant.Compact
                            )
                        }
                    }
                    if (almost.isNotEmpty()) {
                        item(key = "header_almost") {
                            GroupHeader("Almost", Color(0xFFF57F17), almost.size)
                        }
                        items(almost, key = { "almost_${it.id}" }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                variant = RecipeCardVariant.Compact
                            )
                        }
                    }
                    if (others.isNotEmpty()) {
                        item(key = "header_others") {
                            GroupHeader("Others", MaterialTheme.colorScheme.onSurfaceVariant, others.size)
                        }
                        items(others, key = { "others_${it.id}" }) { recipe ->
                            RecipeCard(
                                recipe = recipe,
                                onClick = { onRecipeClick(recipe.id) },
                                onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                                variant = RecipeCardVariant.Compact
                            )
                        }
                    }
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
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = color.copy(alpha = 0.15f),
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
