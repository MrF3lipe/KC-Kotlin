package com.kitchencabinet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kitchencabinet.data.Ingredient
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val substitutesMap: Map<String, String> = mapOf(
    "butter" to "olive oil", "milk" to "oat milk", "heavy cream" to "coconut cream",
    "sour cream" to "Greek yogurt", "cream cheese" to "mashed avocado", "egg" to "flax egg",
    "eggs" to "flax eggs", "flour" to "almond flour", "all-purpose flour" to "whole wheat flour",
    "sugar" to "honey or maple syrup", "granulated sugar" to "coconut sugar",
    "brown sugar" to "coconut sugar", "honey" to "maple syrup", "salt" to "sea salt",
    "black pepper" to "white pepper", "soy sauce" to "coconut aminos",
    "chicken broth" to "vegetable broth", "beef broth" to "mushroom broth",
    "cheese" to "nutritional yeast", "mozzarella" to "vegan mozzarella",
    "cheddar" to "vegan cheddar", "parmesan" to "nutritional yeast",
    "rice" to "cauliflower rice", "white rice" to "brown rice",
    "pasta" to "zucchini noodles or chickpea pasta", "spaghetti" to "zucchini noodles",
    "bread" to "gluten-free bread", "breadcrumbs" to "almond flour or crushed pork rinds",
    "mayonnaise" to "hummus or Greek yogurt", "buttermilk" to "almond milk + lemon juice",
    "yogurt" to "coconut yogurt", "cream" to "coconut cream",
    "tomato sauce" to "crushed tomatoes", "tomato paste" to "ketchup (in a pinch)",
    "olive oil" to "avocado oil", "vegetable oil" to "coconut oil", "sesame oil" to "olive oil",
    "vinegar" to "lemon juice", "balsamic vinegar" to "red wine vinegar + honey",
    "lemon juice" to "lime juice", "garlic" to "garlic powder", "onion" to "onion powder or shallots",
    "ginger" to "ground ginger", "cilantro" to "parsley", "basil" to "oregano or thyme",
    "parsley" to "cilantro or basil", "rosemary" to "thyme", "thyme" to "oregano",
    "oregano" to "marjoram", "cinnamon" to "nutmeg", "paprika" to "smoked paprika or cayenne",
    "cumin" to "chili powder", "chili powder" to "cumin + paprika", "nutmeg" to "cinnamon",
    "vanilla extract" to "vanilla bean paste or almond extract",
    "chocolate" to "carob powder", "dark chocolate" to "cacao powder + coconut oil",
    "peanut butter" to "almond butter or sunflower seed butter",
    "almond butter" to "peanut butter", "maple syrup" to "honey or agave", "agave" to "maple syrup",
)

@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onCook: (Int) -> Unit,
    onShare: (Int) -> Unit = {},
    viewModel: RecipeViewModel = viewModel()
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentServings by remember { mutableStateOf<Int?>(null) }
    var expandedSubstitutes by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(recipeId) {
        val r = viewModel.getById(recipeId)
        recipe = r
        currentServings = r?.servings ?: 4
    }

    if (recipe == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val r = recipe!!
    val servings = currentServings ?: r.servings
    val factor = if (r.servings > 0) servings.toFloat() / r.servings.toFloat() else 1f

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete recipe?") },
            text = { Text("\u201C${r.title}\u201D will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { viewModel.delete(r); showDeleteDialog = false; onBack() }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Image hero (aspect 4:3) ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
            ) {
                if (r.image.isNotBlank()) {
                    AsyncImage(
                        model = r.image,
                        contentDescription = r.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = r.title.take(2).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            fontFamily = NewsreaderFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Gradient overlay at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.95f))
                            )
                        )
                )

                // Overlay buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    Surface(
                        onClick = onBack,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
                        shadowElevation = 2.dp,
                    ) {
                        Box(modifier = Modifier.padding(8.dp)) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Share
                        Surface(
                            onClick = { onShare(r.id) },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
                            shadowElevation = 2.dp,
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(Icons.Filled.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        // Favorite
                        Surface(
                            onClick = {
                                scope.launch {
                                    viewModel.toggleFavorite(r.id, !r.isFavorite)
                                    recipe = viewModel.getById(r.id)
                                }
                            },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
                            shadowElevation = 2.dp,
                        ) {
                            Box(modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    imageVector = if (r.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (r.isFavorite) "Remove from favorites" else "Add to favorites",
                                    tint = if (r.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // ── Overlapping content panel ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (r.category.isNotBlank()) {
                        Badge(label = r.category, variant = "primary")
                    }
                    if (r.difficulty.isNotBlank()) {
                        Badge(label = r.difficulty, variant = "muted")
                    }
                    Badge(label = "${r.timeMinutes} min", variant = "muted")
                }

                // Title
                Text(
                    text = r.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                // Description
                if (r.description.isNotBlank()) {
                    Text(
                        text = r.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Cooked count
                if (r.cookedCount > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle, contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Cooked ${r.cookedCount} times",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // ── Servings adjuster ───────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shadowElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.People, contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Servings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilledTonalIconButton(
                                onClick = { if (servings > 1) currentServings = servings - 1 },
                                enabled = servings > 1,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Remove, "Decrease", Modifier.size(18.dp))
                            }
                            Text(
                                "$servings",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = NewsreaderFontFamily,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FilledTonalIconButton(
                                onClick = { currentServings = servings + 1 },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Add, "Increase", Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // ── Rating ─────────────────────────────────────────────────────
                Column {
                    Text(
                        "Rating",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = NewsreaderFontFamily
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val currentRating = r.rating.roundToInt()
                        for (starIndex in 1..5) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        viewModel.update(r.copy(rating = starIndex.toFloat()))
                                        recipe = viewModel.getById(r.id)
                                    }
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = "Rate $starIndex stars",
                                    tint = if (starIndex <= currentRating)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (r.rating > 0f) {
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "%.1f".format(r.rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // ── Equipment ───────────────────────────────────────────────────
                if (r.equipment.isNotEmpty()) {
                    Column {
                        Text(
                            "Equipment",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            r.equipment.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    pair.forEach { item ->
                                        Surface(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainerLow
                                        ) {
                                            Text(
                                                item,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Ingredients ─────────────────────────────────────────────────
                if (r.ingredients.isNotEmpty()) {
                    Column {
                        Text(
                            "Ingredients",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                r.ingredients.forEach { ingredient ->
                                    val scaledQuantity = scaleQuantity(ingredient.quantity, factor)
                                    val substitute = substitutesMap[ingredient.name.lowercase().trim()]
                                    val isExpanded = expandedSubstitutes.contains(ingredient.name)

                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = scaledQuantity,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.widthIn(max = 80.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = ingredient.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (substitute != null) {
                                                TextButton(
                                                    onClick = {
                                                        expandedSubstitutes = if (isExpanded)
                                                            expandedSubstitutes - ingredient.name
                                                        else expandedSubstitutes + ingredient.name
                                                    },
                                                    modifier = Modifier.sizeIn(minWidth = 32.dp, minHeight = 32.dp),
                                                    contentPadding = PaddingValues(4.dp)
                                                ) {
                                                    Text(
                                                        if (isExpanded) "\u2716" else "\u21BB",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                        if (isExpanded && substitute != null) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                            ) {
                                                Text(
                                                    text = "\u2192 Substitute: $substitute",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Steps ──────────────────────────────────────────────────────
                if (r.steps.isNotEmpty()) {
                    Column {
                        Text(
                            "Steps",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily
                        )
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                r.steps.forEachIndexed { index, step ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                                Text(
                                                    "${index + 1}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        Text(
                                            step,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Edit/Delete FABs ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { showDeleteDialog = true },
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete recipe", modifier = Modifier.size(20.dp))
            }
            SmallFloatingActionButton(
                onClick = { onEdit(r.id) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit recipe", modifier = Modifier.size(20.dp))
            }
            ExtendedFloatingActionButton(
                onClick = { onCook(r.id) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                text = { Text("Cook", fontWeight = FontWeight.Bold) }
            )
        }
    }
}

@Composable
private fun Badge(label: String, variant: String = "muted") {
    val (bg, fg) = when (variant) {
        "primary" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
        "secondary" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

private fun scaleQuantity(quantity: String, factor: Float): String {
    if (quantity.isBlank()) return ""
    if (factor == 1f) return quantity
    val trimmed = quantity.trim()
    val rangeRegex = Regex("""^(\d+)\s*[-–]\s*(\d+)\s*(.*)$""")
    val rangeMatch = rangeRegex.find(trimmed)
    if (rangeMatch != null) {
        val low = rangeMatch.groupValues[1].toFloatOrNull()
        val high = rangeMatch.groupValues[2].toFloatOrNull()
        val rest = rangeMatch.groupValues[3].trim()
        if (low != null && high != null) {
            val avg = (low + high) / 2f
            return "${formatQuantity(avg * factor, "")} $rest".trim()
        }
    }
    val numericRegex = Regex("""^([\d\s./]+)\s*(.*)$""")
    val match = numericRegex.find(trimmed)
    if (match != null) {
        val numStr = match.groupValues[1].trim()
        val rest = match.groupValues[2].trim()
        val numericValue = parseNumericQuantity(numStr)
        if (numericValue != null) return formatQuantity(numericValue * factor, rest)
    }
    return quantity
}

private fun parseNumericQuantity(s: String): Float? {
    s.toFloatOrNull()?.let { return it }
    val simpleFractionRegex = Regex("""^(\d+)\s*/\s*(\d+)$""")
    val simpleMatch = simpleFractionRegex.find(s)
    if (simpleMatch != null) {
        val num = simpleMatch.groupValues[1].toFloatOrNull()
        val den = simpleMatch.groupValues[2].toFloatOrNull()
        if (num != null && den != null && den != 0f) return num / den
    }
    val mixedFractionRegex = Regex("""^(\d+)\s+(\d+)\s*/\s*(\d+)$""")
    val mixedMatch = mixedFractionRegex.find(s)
    if (mixedMatch != null) {
        val whole = mixedMatch.groupValues[1].toFloatOrNull()
        val num = mixedMatch.groupValues[2].toFloatOrNull()
        val den = mixedMatch.groupValues[3].toFloatOrNull()
        if (whole != null && num != null && den != null && den != 0f) return whole + num / den
    }
    return null
}

private fun formatQuantity(value: Float, unit: String): String {
    val rounded = (value * 4).roundToInt() / 4f
    val wholePart = rounded.toInt()
    val fracPart = rounded - wholePart
    val fracSymbol = when {
        fracPart == 0f -> ""
        kotlin.math.abs(fracPart - 0.25f) < 0.01f -> "\u00BC"
        kotlin.math.abs(fracPart - 0.5f) < 0.01f -> "\u00BD"
        kotlin.math.abs(fracPart - 0.75f) < 0.01f -> "\u00BE"
        kotlin.math.abs(fracPart - 0.33f) < 0.02f -> "\u2153"
        kotlin.math.abs(fracPart - 0.67f) < 0.02f -> "\u2154"
        kotlin.math.abs(fracPart - 0.125f) < 0.01f -> "\u215B"
        kotlin.math.abs(fracPart - 0.375f) < 0.01f -> "\u215C"
        kotlin.math.abs(fracPart - 0.625f) < 0.01f -> "\u215D"
        kotlin.math.abs(fracPart - 0.875f) < 0.01f -> "\u215E"
        else -> String.format("%.1f", rounded)
    }
    val numberPart = when {
        wholePart > 0 && fracSymbol.isNotEmpty() -> "$wholePart $fracSymbol"
        wholePart > 0 -> "$wholePart"
        fracSymbol.isNotEmpty() -> fracSymbol
        else -> "0"
    }
    return if (unit.isBlank()) numberPart else "$numberPart $unit"
}