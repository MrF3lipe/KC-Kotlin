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

    LaunchedEffect(Unit) { viewModel.setCategory("All") }

    val ingredientOptions = remember(pantryItems) {
        pantryItems.filter { it.available }
            .map { it.name.lowercase().trim() }
            .distinct().sorted()
    }

    val (cookable, almost, others) = remember(results, selectedIngredients, selectedDifficulty, showCookableOnly, pantryItems) {
        val availablePantry = pantryItems.filter { it.available }
            .map { it.name.lowercase().trim() }.toSet()

        data class MatchResult(val recipe: Recipe, val matchedCount: Int, val totalCount: Int)

        val evaluated = results
            .filter { if (selectedDifficulty != null) it.difficulty == selectedDifficulty else true }
            .filter { if (selectedIngredients.isEmpty()) true
                else it.ingredients.any { ing -> selectedIngredients.any { sel -> ing.name.lowercase().contains(sel) || sel.contains(ing.name.lowercase()) } } }
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

    Column(modifier = modifier.fillMaxSize()) {
        // Header area with padding
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = "Buscar recetas",
                style = MaterialTheme.typography.displaySmall,
                fontFamily = com.kitchencabinet.ui.theme.NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Buscar recetas\u2026") },
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
                                "Solo cocinables",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Con lo que ten\u00E9s en tu despensa",
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

            // ── Ingredients section ──────────────────────────────────────────────
            if (ingredientOptions.isNotEmpty()) {
                item(key = "ingredients_header") {
                    SectionCard(title = "Ingredientes que tengo") {
                        if (selectedIngredients.isNotEmpty()) {
                            Text(
                                "${selectedIngredients.size} seleccionados",
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

            // ── Difficulty section ──────────────────────────────────────────────
            item(key = "difficulty_header") {
                SectionCard(title = "Dificultad") {
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
                                        "easy" -> "F\u00E1cil"
                                        "medium" -> "Media"
                                        else -> "Dif\u00EDcil"
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

            // ── Results ──────────────────────────────────────────────────────────
            val showEmpty = query.isBlank() && selectedIngredients.isEmpty() && selectedDifficulty == null
            val hasResults = cookable.isNotEmpty() || almost.isNotEmpty() || others.isNotEmpty()

            if (showEmpty && !showCookableOnly) {
                item(key = "empty_state") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Escrib\u00ED para buscar recetas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (!hasResults) {
                item(key = "no_results") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Sin resultados para \"$query\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (cookable.isNotEmpty()) {
                    item(key = "header_cookable") {
                        GroupHeader("\u2705 Puedes cocinar", Color(0xFF2E7D32), cookable.size)
                    }
                    items(cookable, key = { "cookable_${it.id}" }) { recipe ->
                        RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                            variant = RecipeCardVariant.Compact)
                    }
                }
                if (almost.isNotEmpty()) {
                    item(key = "header_almost") {
                        GroupHeader("\uD83D\uDFE1 Casi listas", Color(0xFFF57F17), almost.size)
                    }
                    items(almost, key = { "almost_${it.id}" }) { recipe ->
                        RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                            variant = RecipeCardVariant.Compact)
                    }
                }
                if (others.isNotEmpty()) {
                    item(key = "header_others") {
                        GroupHeader("\uD83D\uDCDA Otras", MaterialTheme.colorScheme.onSurfaceVariant, others.size)
                    }
                    items(others, key = { "others_${it.id}" }) { recipe ->
                        RecipeCard(recipe = recipe, onClick = { onRecipeClick(recipe.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(recipe.id, !recipe.isFavorite) },
                            variant = RecipeCardVariant.Compact)
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