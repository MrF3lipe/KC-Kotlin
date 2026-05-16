package com.kitchencabinet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    recipeId: Int?,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var difficulty by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var ingredientsText by remember { mutableStateOf("") }
    var equipmentText by remember { mutableStateOf("") }
    var stepsText by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("30") }
    var servings by remember { mutableStateOf("4") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var existingRecipe by remember { mutableStateOf<Recipe?>(null) }

    val scope = rememberCoroutineScope()
    val isEditing = recipeId != null

    val difficulties = listOf("easy", "medium", "hard")

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.getById(recipeId)?.let { r ->
                existingRecipe = r
                title = r.title
                description = r.description
                category = r.category
                difficulty = r.difficulty
                imageUrl = r.image
                ingredientsText = r.ingredients.joinToString("\n") { "${it.name}|${it.quantity}" }
                equipmentText = r.equipment.joinToString("\n")
                stepsText = r.steps.joinToString("\n")
                cookTime = r.timeMinutes.toString()
                servings = r.servings.toString()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Recipe" else "New Recipe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) return@TextButton
                            val parsedIngredients = ingredientsText.lines()
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                                .map { line ->
                                    val parts = line.split("|", limit = 2)
                                    if (parts.size == 2) com.kitchencabinet.data.Ingredient(parts[0].trim(), parts[1].trim())
                                    else com.kitchencabinet.data.Ingredient(line)
                                }
                            val parsedEquipment = equipmentText.lines()
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            val recipe = Recipe(
                                id = existingRecipe?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                image = imageUrl.trim(),
                                category = category,
                                difficulty = difficulty,
                                ingredients = parsedIngredients,
                                equipment = parsedEquipment,
                                steps = stepsText.lines().map { it.trim() }.filter { it.isNotBlank() },
                                timeMinutes = cookTime.toIntOrNull() ?: 30,
                                servings = servings.toIntOrNull() ?: 4,
                                isFavorite = existingRecipe?.isFavorite ?: false,
                                featured = existingRecipe?.featured ?: false,
                                cookedCount = existingRecipe?.cookedCount ?: 0,
                                rating = existingRecipe?.rating ?: 0f
                            )
                            scope.launch {
                                if (isEditing) viewModel.update(recipe) else viewModel.insert(recipe)
                                onBack()
                            }
                        }
                    ) { Text("Save") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Recipe Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Image URL") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("https://...") }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        viewModel.categories.filter { it != "All" }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = { category = cat; categoryExpanded = false }
                            )
                        }
                    }
                }

                // Difficulty dropdown
                ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Difficulty") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = difficultyExpanded,
                        onDismissRequest = { difficultyExpanded = false }
                    ) {
                        difficulties.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { difficulty = d; difficultyExpanded = false }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cookTime,
                    onValueChange = { cookTime = it.filter { c -> c.isDigit() } },
                    label = { Text("Cook Time (min)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it.filter { c -> c.isDigit() } },
                    label = { Text("Servings") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = ingredientsText,
                onValueChange = { ingredientsText = it },
                label = { Text("Ingredients (name|quantity per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            OutlinedTextField(
                value = equipmentText,
                onValueChange = { equipmentText = it },
                label = { Text("Equipment (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                label = { Text("Steps (one per line)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8
            )
        }
    }
}
