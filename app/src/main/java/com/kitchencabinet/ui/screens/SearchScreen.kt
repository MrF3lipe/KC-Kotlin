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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.ui.components.RecipeCard
import com.kitchencabinet.ui.components.RecipeCardVariant
import com.kitchencabinet.viewmodel.RecipeViewModel

@Composable
fun SearchScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: RecipeViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.recipes.collectAsState()
    val focusRequester = remember { FocusRequester() }

    var showDifficultyFilter by remember { mutableStateOf(false) }
    var selectedDifficulty by remember { mutableStateOf<String?>(null) }
    var showCookableOnly by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setCategory("All")
    }

    val filteredResults = remember(results, selectedDifficulty, showCookableOnly) {
        var list = results
        if (selectedDifficulty != null) {
            list = list.filter { it.difficulty == selectedDifficulty }
        }
        list
    }

    val groupedResults = remember(filteredResults) {
        filteredResults.groupBy { it.difficulty.ifEmpty { "other" } }.toSortedMap()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search recipes…") },
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

        Row(
            modifier = Modifier.fillMaxWidth(),
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
        }

        if (showDifficultyFilter) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("easy", "medium", "hard").forEach { d ->
                    FilterChip(
                        selected = selectedDifficulty == d,
                        onClick = { selectedDifficulty = if (selectedDifficulty == d) null else d },
                        label = { Text(d) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(8.dp))

        when {
            query.isBlank() && !showCookableOnly -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Type to search recipes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            filteredResults.isEmpty() -> {
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
                    groupedResults.forEach { (difficulty, recipes) ->
                        item(key = "header_$difficulty") {
                            Text(
                                difficulty.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        items(recipes, key = { it.id }) { recipe ->
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
