package com.kitchencabinet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.ui.components.RecipeCard
import com.kitchencabinet.ui.components.RecipeCardVariant
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.viewmodel.RecipeViewModel

@Composable
fun FavoritesScreen(
    onRecipeClick: (Int) -> Unit,
    viewModel: RecipeViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val favorites by viewModel.favorites.collectAsState()

    if (favorites.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    strings.favorites.emptyTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    strings.favorites.emptySubtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites, key = { it.id }) { recipe ->
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
