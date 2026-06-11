package com.kitchencabinet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kitchencabinet.data.Ingredient
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
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
    var cookTime by remember { mutableStateOf("30") }
    var servings by remember { mutableStateOf("4") }
    var stepsText by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var existingRecipe by remember { mutableStateOf<Recipe?>(null) }

    data class IngredientField(val name: String = "", val quantity: String = "", val key: Int = 0)

    var ingredientFields by remember { mutableStateOf(listOf(IngredientField(key = 0))) }
    var equipmentFields by remember { mutableStateOf(listOf("")) }
    var fieldCounter by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()
    val isEditing = recipeId != null
    val difficulties = listOf("easy", "medium", "hard")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.toString()?.let { imageUrl = it }
    }

    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.getById(recipeId)?.let { r ->
                existingRecipe = r
                title = r.title
                description = r.description
                category = r.category
                difficulty = r.difficulty
                imageUrl = r.image
                cookTime = r.timeMinutes.toString()
                servings = r.servings.toString()
                stepsText = r.steps.joinToString("\n")
                ingredientFields = r.ingredients.mapIndexed { i, ing ->
                    IngredientField(name = ing.name, quantity = ing.quantity, key = i)
                }.ifEmpty { listOf(IngredientField(key = 0)) }
                equipmentFields = r.equipment.ifEmpty { listOf("") }
                fieldCounter = r.ingredients.size.coerceAtLeast(1)
            }
        }
    }

    fun collectFields() = ingredientFields
        .filter { it.name.isNotBlank() }
        .map { Ingredient(it.name.trim(), it.quantity.trim()) }

    fun collectEquipment() = equipmentFields
        .filter { it.isNotBlank() }
        .map { it.trim() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar receta" else "Nueva receta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) return@TextButton
                            val recipe = Recipe(
                                id = existingRecipe?.id ?: 0,
                                title = title.trim(),
                                description = description.trim(),
                                image = imageUrl.trim(),
                                category = category,
                                difficulty = difficulty,
                                ingredients = collectFields(),
                                equipment = collectEquipment(),
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
                    ) { Text("Guardar") }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            SectionLabel("Título")
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Ej: Sopa de Tomate") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Description
            SectionLabel("Descripción")
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Breve descripción de la receta") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // Image
            SectionLabel("Imagen")
            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                FilledTonalButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Image, "Galería", Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Galería", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            // Category & Difficulty
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
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

                ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = when (difficulty) { "easy" -> "Fácil"; "medium" -> "Media"; "hard" -> "Difícil"; else -> "" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dificultad") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = difficultyExpanded,
                        onDismissRequest = { difficultyExpanded = false }
                    ) {
                        difficulties.forEach { d ->
                            DropdownMenuItem(
                                text = {
                                    Text(when (d) { "easy" -> "Fácil"; "medium" -> "Media"; else -> "Difícil" })
                                },
                                onClick = { difficulty = d; difficultyExpanded = false }
                            )
                        }
                    }
                }
            }

            // Time & Servings
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cookTime,
                    onValueChange = { cookTime = it.filter { c -> c.isDigit() } },
                    label = { Text("Tiempo (min)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it.filter { c -> c.isDigit() } },
                    label = { Text("Porciones") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Ingredients
            SectionLabel("Ingredientes")
            ingredientFields.forEachIndexed { idx, field ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = field.name,
                        onValueChange = { newName ->
                            ingredientFields = ingredientFields.toMutableList().apply {
                                this[idx] = field.copy(name = newName)
                            }
                        },
                        placeholder = { Text("Nombre") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = field.quantity,
                        onValueChange = { newQty ->
                            ingredientFields = ingredientFields.toMutableList().apply {
                                this[idx] = field.copy(quantity = newQty)
                            }
                        },
                        placeholder = { Text("Cant.") },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (ingredientFields.size > 1) {
                        IconButton(onClick = {
                            ingredientFields = ingredientFields.toMutableList().apply { removeAt(idx) }
                        }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.RemoveCircle, "Quitar", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            TextButton(onClick = {
                ingredientFields = ingredientFields + IngredientField(key = fieldCounter)
                fieldCounter++
            }) {
                Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir ingrediente")
            }

            // Equipment
            SectionLabel("Utensilios")
            equipmentFields.forEachIndexed { idx, eq ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = eq,
                        onValueChange = { newEq ->
                            equipmentFields = equipmentFields.toMutableList().apply { this[idx] = newEq }
                        },
                        placeholder = { Text("Ej: Sartén") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (equipmentFields.size > 1) {
                        IconButton(onClick = {
                            equipmentFields = equipmentFields.toMutableList().apply { removeAt(idx) }
                        }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.RemoveCircle, "Quitar", Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            TextButton(onClick = {
                equipmentFields = equipmentFields + ""
            }) {
                Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Añadir utensilio")
            }

            // Steps
            SectionLabel("Pasos")
            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                placeholder = { Text("Un paso por línea") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        fontFamily = NewsreaderFontFamily,
        color = MaterialTheme.colorScheme.primary,
    )
}
