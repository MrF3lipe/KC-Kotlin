package com.kitchencabinet.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
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
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.PantryViewModel
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    recipeId: Int?,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel(),
    pantryViewModel: PantryViewModel = viewModel()
) {
    val strings = LocalStrings.current
    val pantryItems by pantryViewModel.pantryItems.collectAsState()
    var showPantrySheet by remember { mutableStateOf(false) }
    var pantryTargetIndex by remember { mutableIntStateOf(-1) }
    var pantryIsEquipment by remember { mutableStateOf(false) }
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
    var tagsInput by remember { mutableStateOf<List<String>>(emptyList()) }

    data class IngredientField(val name: String = "", val quantity: String = "", val key: Int = 0)

    var ingredientFields by remember { mutableStateOf(listOf(IngredientField(key = 0))) }
    var equipmentFields by remember { mutableStateOf(listOf("")) }
    var fieldCounter by remember { mutableStateOf(1) }

    val scope = rememberCoroutineScope()
    val isEditing = recipeId != null
    val difficulties = listOf("easy", "medium", "hard")

    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showTitleError by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val path = copyToInternalStorage(context, it)
            if (path != null) imageUrl = path
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = createCameraUri()
            cameraImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            cameraImageUri?.let { uri ->
                val path = copyToInternalStorage(context, uri)
                if (path != null) imageUrl = path
            }
        }
    }

    fun createCameraUri(): Uri {
        val file = java.io.File(context.cacheDir, "recipe_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
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
                tagsInput = r.tags
            }
        }
    }

    fun collectFields() = ingredientFields
        .filter { it.name.isNotBlank() }
        .map { Ingredient(it.name.trim(), it.quantity.trim()) }

    fun collectEquipment() = equipmentFields
        .filter { it.isNotBlank() }
        .map { it.trim() }

    fun onPantryPick(name: String, quantity: String?) {
        if (pantryIsEquipment) {
            if (pantryTargetIndex in equipmentFields.indices) {
                equipmentFields = equipmentFields.toMutableList().apply {
                    this[pantryTargetIndex] = name
                }
            }
        } else {
            if (pantryTargetIndex in ingredientFields.indices) {
                ingredientFields = ingredientFields.toMutableList().apply {
                    this[pantryTargetIndex] = this[pantryTargetIndex].copy(name = name, quantity = quantity ?: "")
                }
            }
        }
        showPantrySheet = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) strings.addEdit.editTitle else strings.addEdit.newTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = strings.nav.search)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) { showTitleError = true; return@TextButton }
                            showTitleError = false
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
                                tags = tagsInput,
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
                    ) { Text(strings.addEdit.save) }
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
            SectionLabel(strings.addEdit.fieldTitle)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; showTitleError = false },
                placeholder = { Text(strings.addEdit.fieldTitlePlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                isError = showTitleError,
                supportingText = if (showTitleError) {{ Text(strings.addEdit.titleRequired) }} else null
            )

            // Description
            SectionLabel(strings.addEdit.fieldDescription)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text(strings.addEdit.fieldDescriptionPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            // Image
            SectionLabel(strings.addEdit.fieldImage)
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
                    placeholder = { Text(strings.addEdit.fieldImagePlaceholder) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                FilledTonalButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Image, strings.addEdit.gallery, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(strings.addEdit.gallery, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            val uri = createCameraUri()
                            cameraImageUri = uri
                            cameraLauncher.launch(uri)
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, strings.addEdit.camera, Modifier.size(18.dp))
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
                        label = { Text(strings.addEdit.category) },
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
                        value = when (difficulty) { "easy" -> strings.addEdit.easy; "medium" -> strings.addEdit.medium; "hard" -> strings.addEdit.hard; else -> "" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(strings.addEdit.difficulty) },
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
                                    Text(when (d) { "easy" -> strings.addEdit.easy; "medium" -> strings.addEdit.medium; else -> strings.addEdit.hard })
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
                    label = { Text(strings.addEdit.time) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it.filter { c -> c.isDigit() } },
                    label = { Text(strings.addEdit.servings) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Ingredients
            SectionLabel(strings.addEdit.ingredients)
            ingredientFields.forEachIndexed { idx, field ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { pantryTargetIndex = idx; pantryIsEquipment = false; showPantrySheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.List, strings.addEdit.pickFromPantry, Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = field.name,
                        onValueChange = { newName ->
                            ingredientFields = ingredientFields.toMutableList().apply {
                                this[idx] = field.copy(name = newName)
                            }
                        },
                        placeholder = { Text(strings.addEdit.ingredientName) },
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
                        placeholder = { Text(strings.addEdit.ingredientQuantity) },
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (ingredientFields.size > 1) {
                        IconButton(onClick = {
                            ingredientFields = ingredientFields.toMutableList().apply { removeAt(idx) }
                        }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.RemoveCircle, strings.addEdit.remove, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
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
                Text(strings.addEdit.addIngredient)
            }

            // Equipment
            SectionLabel(strings.addEdit.utensils)
            equipmentFields.forEachIndexed { idx, eq ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { pantryTargetIndex = idx; pantryIsEquipment = true; showPantrySheet = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Filled.List, strings.addEdit.pickFromPantry, Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedTextField(
                        value = eq,
                        onValueChange = { newEq ->
                            equipmentFields = equipmentFields.toMutableList().apply { this[idx] = newEq }
                        },
                        placeholder = { Text(strings.addEdit.utensilPlaceholder) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (equipmentFields.size > 1) {
                        IconButton(onClick = {
                            equipmentFields = equipmentFields.toMutableList().apply { removeAt(idx) }
                        }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Filled.RemoveCircle, strings.addEdit.remove, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            TextButton(onClick = {
                equipmentFields = equipmentFields + ""
            }) {
                Icon(Icons.Filled.Add, null, Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(strings.addEdit.addUtensil)
            }

            // Steps
            SectionLabel(strings.addEdit.steps)
            OutlinedTextField(
                value = stepsText,
                onValueChange = { stepsText = it },
                placeholder = { Text(strings.addEdit.stepsPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8,
                shape = RoundedCornerShape(12.dp)
            )

            // ── Tags ──────────────────────────────────────────────────
            Text(strings.addEdit.tags, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                fontFamily = NewsreaderFontFamily)
            OutlinedTextField(
                value = tagsInput.joinToString(", "),
                onValueChange = { input ->
                    tagsInput = input.split(",").map { it.trim() }.filter { it.isNotBlank() }
                },
                placeholder = { Text(strings.addEdit.tagsPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    // Pantry picker bottom sheet
    if (showPantrySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPantrySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(strings.addEdit.pantryItems, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                if (pantryItems.isEmpty()) {
                    Text(strings.addEdit.noPantryItems, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    pantryItems.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPantryPick(item.name, null) }.padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilledTonalButton(onClick = {
                                onPantryPick(item.name, "${item.quantity.toIntOrNull() ?: item.quantity} ${item.unit}")
                            }, shape = RoundedCornerShape(50)) {
                                Text(strings.addEdit.pickFromPantry, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
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

private fun copyToInternalStorage(context: Context, uri: Uri): String? {
    return try {
        val dir = java.io.File(context.filesDir, "images")
        dir.mkdirs()
        val file = java.io.File(dir, "recipe_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}
