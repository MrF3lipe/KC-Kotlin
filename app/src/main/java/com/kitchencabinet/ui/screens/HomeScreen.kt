package com.kitchencabinet.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.ui.components.RecipeCard
import com.kitchencabinet.ui.components.RecipeCardVariant
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.RecipeViewModel

@Composable
fun HomeScreen(
    onRecipeClick: (Int) -> Unit,
    onAddRecipe: () -> Unit,
    viewModel: RecipeViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val recipes by viewModel.recipes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipes = recipes.filter { recipe ->
        val matchesCategory = selectedCategory == "All" || recipe.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() ||
                recipe.title.contains(searchQuery, ignoreCase = true) ||
                recipe.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val categoryChips = listOf("All") + viewModel.categories.filter { it != "All" }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = strings.home.title,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = strings.nav.search,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                strings.home.placeholder,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryChips.forEach { category ->
                    val isSelected = selectedCategory == category
                    val chipTone = when (category) {
                        "Breakfast" -> "secondary"
                        "Lunch" -> "primary"
                        else -> "muted"
                    }

                    Surface(
                        onClick = { viewModel.setCategory(category) },
                        shape = RoundedCornerShape(50),
                        color = when {
                            isSelected && chipTone == "primary" -> MaterialTheme.colorScheme.primary
                            isSelected && chipTone == "secondary" -> MaterialTheme.colorScheme.secondaryContainer
                            isSelected -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = when {
                                isSelected && chipTone == "primary" -> MaterialTheme.colorScheme.onPrimary
                                isSelected && chipTone == "secondary" -> MaterialTheme.colorScheme.onSecondaryContainer
                                isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        if (filteredRecipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank()) strings.home.emptyTitle
                        else strings.home.emptySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(filteredRecipes, key = { it.id }) { recipe ->
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
