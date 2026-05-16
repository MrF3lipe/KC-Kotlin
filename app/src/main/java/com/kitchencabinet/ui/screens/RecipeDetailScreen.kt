package com.kitchencabinet.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Ingredient
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Hardcoded ingredient substitutes map.
 * Used to suggest alternatives for common ingredients.
 */
private val substitutesMap: Map<String, String> = mapOf(
    "butter" to "olive oil",
    "milk" to "oat milk",
    "heavy cream" to "coconut cream",
    "sour cream" to "Greek yogurt",
    "cream cheese" to "mashed avocado",
    "egg" to "flax egg",
    "eggs" to "flax eggs",
    "flour" to "almond flour",
    "all-purpose flour" to "whole wheat flour",
    "sugar" to "honey or maple syrup",
    "granulated sugar" to "coconut sugar",
    "brown sugar" to "coconut sugar",
    "honey" to "maple syrup",
    "salt" to "sea salt",
    "black pepper" to "white pepper",
    "soy sauce" to "coconut aminos",
    "chicken broth" to "vegetable broth",
    "beef broth" to "mushroom broth",
    "cheese" to "nutritional yeast",
    "mozzarella" to "vegan mozzarella",
    "cheddar" to "vegan cheddar",
    "parmesan" to "nutritional yeast",
    "rice" to "cauliflower rice",
    "white rice" to "brown rice",
    "pasta" to "zucchini noodles or chickpea pasta",
    "spaghetti" to "zucchini noodles",
    "bread" to "gluten-free bread",
    "breadcrumbs" to "almond flour or crushed pork rinds",
    "mayonnaise" to "hummus or Greek yogurt",
    "buttermilk" to "almond milk + lemon juice",
    "yogurt" to "coconut yogurt",
    "cream" to "coconut cream",
    "tomato sauce" to "crushed tomatoes",
    "tomato paste" to "ketchup (in a pinch)",
    "olive oil" to "avocado oil",
    "vegetable oil" to "coconut oil",
    "sesame oil" to "olive oil",
    "vinegar" to "lemon juice",
    "balsamic vinegar" to "red wine vinegar + honey",
    "lemon juice" to "lime juice",
    "garlic" to "garlic powder",
    "onion" to "onion powder or shallots",
    "ginger" to "ground ginger",
    "cilantro" to "parsley",
    "basil" to "oregano or thyme",
    "parsley" to "cilantro or basil",
    "rosemary" to "thyme",
    "thyme" to "oregano",
    "oregano" to "marjoram",
    "cinnamon" to "nutmeg",
    "paprika" to "smoked paprika or cayenne",
    "cumin" to "chili powder",
    "chili powder" to "cumin + paprika",
    "nutmeg" to "cinnamon",
    "vanilla extract" to "vanilla bean paste or almond extract",
    "chocolate" to "carob powder",
    "dark chocolate" to "cacao powder + coconut oil",
    "peanut butter" to "almond butter or sunflower seed butter",
    "almond butter" to "peanut butter",
    "maple syrup" to "honey or agave",
    "agave" to "maple syrup",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Int,
    onBack: () -> Unit,
    onEdit: (Int) -> Unit,
    onCook: (Int) -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var currentServings by remember { mutableStateOf<Int?>(null) }
    var expandedSubstitutes by remember { mutableStateOf(setOf<String>()) }
    val scope = rememberCoroutineScope()

    // Load recipe on initial composition or when recipeId changes
    LaunchedEffect(recipeId) {
        val r = viewModel.getById(recipeId)
        recipe = r
        currentServings = r?.servings ?: 4
    }

    // Loading state
    if (recipe == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val r = recipe!!
    val servings = currentServings ?: r.servings
    val factor = if (r.servings > 0) servings.toFloat() / r.servings.toFloat() else 1f

    // ── Delete confirmation dialog ───────────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete recipe?") },
            text = { Text("\u201C${r.title}\u201D will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        viewModel.delete(r)
                        showDeleteDialog = false
                        onBack()
                    }
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Main scaffold ────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        r.title,
                        maxLines = 1,
                        fontFamily = NewsreaderFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Favorite toggle
                    IconButton(onClick = {
                        scope.launch {
                            viewModel.toggleFavorite(r.id, !r.isFavorite)
                            // Refresh recipe to get updated state
                            recipe = viewModel.getById(r.id)
                        }
                    }) {
                        Icon(
                            imageVector = if (r.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (r.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (r.isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Edit button
                    IconButton(onClick = { onEdit(r.id) }) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit recipe"
                        )
                    }
                    // Delete button
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete recipe"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onCook(r.id) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        "Cook",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Image header placeholder ──────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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

            // ── Info chips row ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category chip
                if (r.category.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                r.category,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    )
                }

                // Difficulty chip
                if (r.difficulty.isNotBlank()) {
                    AssistChip(
                        onClick = {},
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        label = {
                            Text(
                                r.difficulty,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    )
                }

                // Time chip
                AssistChip(
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = {
                        Text(
                            "${r.timeMinutes} min",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )

                // Servings chip
                AssistChip(
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            Icons.Filled.People,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = {
                        Text(
                            "${r.servings} servings",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }

            // ── Servings adjuster ─────────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Servings:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    FilledTonalIconButton(
                        onClick = {
                            if (servings > 1) currentServings = servings - 1
                        },
                        enabled = servings > 1,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Decrease servings",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = "$servings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    FilledTonalIconButton(
                        onClick = {
                            currentServings = servings + 1
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Increase servings",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Rating section (interactive) ─────────────────────────────────────
            Column {
                Text(
                    text = "Rating",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    val currentRating = r.rating.roundToInt()
                    for (starIndex in 1..5) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    // Update rating using existing update method
                                    viewModel.update(r.copy(rating = starIndex.toFloat()))
                                    recipe = viewModel.getById(r.id)
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Rate $starIndex stars",
                                tint = if (starIndex <= currentRating)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    if (r.rating > 0f) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(r.rating),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Description ───────────────────────────────────────────────────────
            if (r.description.isNotBlank()) {
                Text(
                    text = r.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Cooked count ──────────────────────────────────────────────────────
            if (r.cookedCount > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Cooked ${r.cookedCount} times",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Equipment section ─────────────────────────────────────────────────
            if (r.equipment.isNotEmpty()) {
                Text(
                    text = "Equipment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    r.equipment.forEach { equipmentItem ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    equipmentItem,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            shape = RoundedCornerShape(50)
                        )
                    }
                }
            }

            // ── Ingredients section ───────────────────────────────────────────────
            if (r.ingredients.isNotEmpty()) {
                Text(
                    text = "Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        r.ingredients.forEach { ingredient ->
                            val scaledQuantity = scaleQuantity(ingredient.quantity, factor)
                            val substitute = substitutesMap[ingredient.name.lowercase().trim()]
                            val isExpanded = expandedSubstitutes.contains(ingredient.name)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Bullet point
                                Text(
                                    text = "\u2022 ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(end = 4.dp)
                                )

                                // Ingredient name and quantity
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${ingredient.name} ($scaledQuantity)",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    // Show substitute suggestion when expanded
                                    if (isExpanded && substitute != null) {
                                        Spacer(Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = "\u2192 Substitute: $substitute",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // Substitute toggle button (only if substitute exists)
                                if (substitute != null) {
                                    TextButton(
                                        onClick = {
                                            expandedSubstitutes = if (isExpanded) {
                                                expandedSubstitutes - ingredient.name
                                            } else {
                                                expandedSubstitutes + ingredient.name
                                            }
                                        },
                                        modifier = Modifier.sizeIn(minWidth = 32.dp, minHeight = 32.dp),
                                        contentPadding = PaddingValues(4.dp)
                                    ) {
                                        Text(
                                            text = if (isExpanded) "\u2716" else "\u21BB",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Steps section ─────────────────────────────────────────────────────
            if (r.steps.isNotEmpty()) {
                Text(
                    text = "Steps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    shape = MaterialTheme.shapes.medium
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
                                // Step number circle
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Step text
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacer to account for the FAB
            Spacer(Modifier.height(80.dp))
        }
    }
}

// ── Quantity scaling helpers ───────────────────────────────────────────────────────

/**
 * Scales an ingredient quantity string by the given factor.
 *
 * Examples:
 *  "2 cups" * 1.5  -> "3 cups"
 *  "1/2 tsp" * 2   -> "1 tsp"
 *  "1 1/2 cups" * 2 -> "3 cups"
 *  "to taste" * 2  -> "to taste" (non-numeric)
 */
private fun scaleQuantity(quantity: String, factor: Float): String {
    if (quantity.isBlank()) return ""
    if (factor == 1f) return quantity

    val trimmed = quantity.trim()

    // Try extracting a leading numeric value from a range first (e.g., "2-3 cloves")
    val rangeRegex = Regex("""^(\d+)\s*[-–]\s*(\d+)\s*(.*)$""")
    val rangeMatch = rangeRegex.find(trimmed)
    if (rangeMatch != null) {
        val low = rangeMatch.groupValues[1].toFloatOrNull()
        val high = rangeMatch.groupValues[2].toFloatOrNull()
        val rest = rangeMatch.groupValues[3].trim()
        if (low != null && high != null) {
            val avg = (low + high) / 2f
            val scaled = avg * factor
            val formatted = formatQuantity(scaled, "")
            return "$formatted $rest".trim()
        }
    }

    // Parse leading numeric portion: digits, spaces, dots, slashes (for fractions)
    val numericRegex = Regex("""^([\d\s./]+)\s*(.*)$""")
    val match = numericRegex.find(trimmed)

    if (match != null) {
        val numStr = match.groupValues[1].trim()
        val rest = match.groupValues[2].trim()

        val numericValue = parseNumericQuantity(numStr)
        if (numericValue != null) {
            val scaled = numericValue * factor
            return formatQuantity(scaled, rest)
        }
    }

    // If we can't parse it, return as-is
    return quantity
}

/**
 * Parses a string that may contain a decimal, integer, fraction, or mixed fraction into a Float.
 * Returns null if the string cannot be parsed.
 */
private fun parseNumericQuantity(s: String): Float? {
    // Direct float/decimal (e.g., "2", "1.5", "0.75")
    s.toFloatOrNull()?.let { return it }

    // Simple fraction (e.g., "1/2", "3/4")
    val simpleFractionRegex = Regex("""^(\d+)\s*/\s*(\d+)$""")
    val simpleMatch = simpleFractionRegex.find(s)
    if (simpleMatch != null) {
        val numerator = simpleMatch.groupValues[1].toFloatOrNull()
        val denominator = simpleMatch.groupValues[2].toFloatOrNull()
        if (numerator != null && denominator != null && denominator != 0f) {
            return numerator / denominator
        }
    }

    // Mixed fraction (e.g., "1 1/2", "2 3/4")
    val mixedFractionRegex = Regex("""^(\d+)\s+(\d+)\s*/\s*(\d+)$""")
    val mixedMatch = mixedFractionRegex.find(s)
    if (mixedMatch != null) {
        val whole = mixedMatch.groupValues[1].toFloatOrNull()
        val numerator = mixedMatch.groupValues[2].toFloatOrNull()
        val denominator = mixedMatch.groupValues[3].toFloatOrNull()
        if (whole != null && numerator != null && denominator != null && denominator != 0f) {
            return whole + numerator / denominator
        }
    }

    return null
}

/**
 * Formats a numeric value with optional unit for display.
 * Rounds to whole numbers, halves, quarters, or common fractions.
 */
private fun formatQuantity(value: Float, unit: String): String {
    // Round to nearest quarter for display
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
