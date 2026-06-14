package com.kitchencabinet.ui.screens

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Ingredient
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Base64

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onBack: () -> Unit,
    onRecipeClick: (Int) -> Unit = {},
    viewModel: RecipeViewModel = viewModel()
) {
    val strings = LocalStrings.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.tools.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = strings.nav.search)
                    }
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unit Converter card
            ToolCard(title = strings.tools.unitConverter, icon = Icons.Filled.SwapHoriz) {
                UnitConverterContent(strings = strings)
            }

            // Scale Calculator card
            ToolCard(title = strings.tools.scaleRecipe, icon = Icons.Filled.Calculate) {
                ScaleCalculatorContent(strings = strings)
            }

            // Barcode Scanner card
            ToolCard(title = strings.tools.barcodeScanner, icon = Icons.Filled.QrCodeScanner) {
                BarcodeScannerContent(strings = strings)
            }

            // Fridge Photo card
            ToolCard(title = strings.tools.fridgePhoto, icon = Icons.Filled.CameraAlt) {
                FridgePhotoContent(strings = strings)
            }

            // Import Recipe card
            ToolCard(title = strings.tools.importRecipe, icon = Icons.Filled.FileDownload) {
                RecipeImportContent(
                    viewModel = viewModel,
                    onRecipeClick = onRecipeClick,
                    strings = strings
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily,
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitConverterContent(strings: com.kitchencabinet.ui.i18n.Strings) {
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf("cup") }
    var toUnit by remember { mutableStateOf("ml") }
    var result by remember { mutableStateOf<String?>(null) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val conversions = mapOf(
        "cup" to 240.0, "tbsp" to 15.0, "tsp" to 5.0,
        "ml" to 1.0, "L" to 1000.0, "oz" to 29.5735, "fl oz" to 29.5735,
        "g" to 1.0, "kg" to 1000.0, "lb" to 453.592
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text(strings.tools.value) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = fromExpanded, onExpandedChange = { fromExpanded = !fromExpanded },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = fromUnit, onValueChange = {}, readOnly = true, label = { Text(strings.tools.from) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                    conversions.keys.sorted().forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { fromUnit = unit; fromExpanded = false })
                    }
                }
            }
            ExposedDropdownMenuBox(expanded = toExpanded, onExpandedChange = { toExpanded = !toExpanded },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = toUnit, onValueChange = {}, readOnly = true, label = { Text(strings.tools.to) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                    conversions.keys.sorted().forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { toUnit = unit; toExpanded = false })
                    }
                }
            }
        }

        Button(
            onClick = {
                val value = inputValue.toDoubleOrNull()
                if (value != null && conversions.containsKey(fromUnit) && conversions.containsKey(toUnit)) {
                    val inMl = value * (conversions[fromUnit] ?: 1.0)
                    val converted = inMl / (conversions[toUnit] ?: 1.0)
                    result = strings.tools.result
                        .replace("{value}", "$value")
                        .replace("{fromUnit}", fromUnit)
                        .replace("{result}", String.format("%.2f", converted))
                        .replace("{toUnit}", toUnit)
                }
            },
            enabled = inputValue.isNotBlank(),
            shape = RoundedCornerShape(50)
        ) { Text(strings.tools.convert) }

        if (result != null) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(result!!, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaleCalculatorContent(strings: com.kitchencabinet.ui.i18n.Strings) {
    var originalServings by remember { mutableStateOf("") }
    var desiredServings by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            strings.tools.scaleDesc,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(value = originalServings, onValueChange = { originalServings = it.filter { c -> c.isDigit() } },
            label = { Text(strings.tools.originalServings) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = desiredServings, onValueChange = { desiredServings = it.filter { c -> c.isDigit() } },
            label = { Text(strings.tools.desiredServings) }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
        Button(
            onClick = {
                val original = originalServings.toIntOrNull()
                val desired = desiredServings.toIntOrNull()
                if (original != null && desired != null && original > 0) {
                    val factor = desired.toDouble() / original.toDouble()
                    result = strings.tools.scaleFactor.replace("{factor}", String.format("%.2f", factor))
                } else result = strings.tools.invalidNumbers
            },
            enabled = originalServings.isNotBlank() && desiredServings.isNotBlank(),
            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(strings.tools.calculate)
        }
        if (result != null) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(result!!, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BarcodeScannerContent(strings: com.kitchencabinet.ui.i18n.Strings) {
    val context = LocalContext.current
    var scannedCode by remember { mutableStateOf<String?>(null) }
    var manualCode by remember { mutableStateOf("") }
    var scanning by remember { mutableStateOf(false) }

    val options = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
    }

    val scanner = remember { GmsBarcodeScanning.getClient(context, options) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            strings.tools.scannerDesc,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (scanning) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else if (scannedCode != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(scannedCode!!, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.QrCodeScanner, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(strings.tools.scannerHint, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Button(
            onClick = {
                scanning = true
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        scannedCode = barcode.rawValue ?: barcode.displayValue
                        scanning = false
                    }
                    .addOnCanceledListener { scanning = false }
                    .addOnFailureListener { scanning = false }
            },
            enabled = !scanning,
            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.QrCodeScanner, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (scannedCode != null) strings.tools.scanAgain else strings.tools.scan)
        }

        HorizontalDivider()
        Text(strings.tools.manualCode, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = manualCode,
                onValueChange = { manualCode = it.filter { c -> c.isDigit() } },
                placeholder = { Text(strings.tools.codePlaceholder) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = { if (manualCode.isNotBlank()) scannedCode = manualCode },
                enabled = manualCode.isNotBlank(),
                shape = RoundedCornerShape(50)
            ) { Text(strings.tools.search) }
        }

        if (scannedCode != null && !scanning) {
            OutlinedButton(onClick = { scannedCode = null; manualCode = "" }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()) {
                Text(strings.tools.clear)
            }
        }
    }
}

@Composable
private fun FridgePhotoContent(strings: com.kitchencabinet.ui.i18n.Strings) {
    var photoUri by remember { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) photoUri = "Foto capturada (${bitmap.width}x${bitmap.height})" }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            strings.tools.fridgeDesc,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (photoUri != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(photoUri!!, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CameraAlt, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(strings.tools.fridgeHint, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Button(
            onClick = { cameraLauncher.launch(null) },
            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.CameraAlt, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (photoUri != null) strings.tools.retake else strings.tools.openCamera)
        }

        if (photoUri != null) {
            OutlinedButton(onClick = { photoUri = null }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()) {
                Text(strings.tools.clear)
            }
        }
    }
}

@Composable
private fun RecipeImportContent(
    viewModel: RecipeViewModel,
    onRecipeClick: (Int) -> Unit,
    strings: com.kitchencabinet.ui.i18n.Strings,
) {
    val scope = rememberCoroutineScope()
    var importUrl by remember { mutableStateOf("") }
    var previewRecipe by remember { mutableStateOf<Recipe?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var importedId by remember { mutableStateOf<Int?>(null) }
    var importing by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            strings.tools.importDesc,
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = importUrl,
            onValueChange = { importUrl = it; previewRecipe = null; errorMsg = null; importedId = null },
            placeholder = { Text(strings.tools.importPlaceholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = { importUrl = ""; previewRecipe = null; errorMsg = null; importedId = null }) {
                    Icon(Icons.Filled.Clear, strings.tools.clean)
                }
            }
        )

        Button(
            onClick = {
                try {
                    val hash = importUrl.substringAfterLast('#')
                    if (hash.isBlank()) throw Exception(strings.tools.noHashError)
                    val json = try {
                        String(Base64.getUrlDecoder().decode(hash))
                    } catch (_: Exception) {
                        String(Base64.getDecoder().decode(hash))
                    }
                    val obj = JSONObject(json)
                    val recipe = Recipe(
                        id = 0,
                        title = obj.optString("title", ""),
                        description = obj.optString("description", ""),
                        image = obj.optString("image", ""),
                        category = obj.optString("category", "General"),
                        difficulty = obj.optString("difficulty", ""),
                        ingredients = run {
                            val arr = obj.optJSONArray("ingredients")
                            if (arr != null) (0 until arr.length()).map { i ->
                                val ing = arr.getJSONObject(i)
                                Ingredient(name = ing.optString("name", ""), quantity = ing.optString("quantity", ""))
                            } else emptyList()
                        },
                        equipment = run {
                            val arr = obj.optJSONArray("equipment")
                            if (arr != null) (0 until arr.length()).map { arr.getString(it) } else emptyList()
                        },
                        steps = run {
                            val arr = obj.optJSONArray("steps")
                            if (arr != null) (0 until arr.length()).map { arr.getString(it) } else emptyList()
                        },
                        timeMinutes = obj.optInt("timeMinutes", 30),
                        servings = obj.optInt("servings", 4),
                        isFavorite = false,
                        featured = false,
                        cookedCount = 0,
                        rating = 0f
                    )
                    if (recipe.title.isBlank()) throw Exception(strings.tools.noTitleError)
                    previewRecipe = recipe
                    errorMsg = null
                } catch (e: Exception) {
                    errorMsg = e.message ?: strings.tools.decodeError
                    previewRecipe = null
                }
            },
            enabled = importUrl.isNotBlank(),
            shape = RoundedCornerShape(50),
            modifier = Modifier.fillMaxWidth()
        ) { Text(strings.tools.decodePreview) }

        if (errorMsg != null) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Text(errorMsg!!, modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        if (previewRecipe != null && importedId == null) {
            val r = previewRecipe!!
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(r.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(strings.tools.previewCount
                        .replace("{ingredients}", "${r.ingredients.size}")
                        .replace("{steps}", "${r.steps.size}"),
                        style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    if (importing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    importing = true
                                    scope.launch {
                                        try {
                                            val newId = viewModel.insertAndGetId(r).toInt()
                                            importedId = newId
                                            onRecipeClick(newId)
                                        } catch (e: Exception) {
                                            errorMsg = "Error al importar: ${e.message}"
                                        } finally {
                                            importing = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(50)
                            ) { Text(strings.tools.importButton) }
                            OutlinedButton(onClick = { previewRecipe = null }, shape = RoundedCornerShape(50)) {
                                Text(strings.tools.cancel)
                            }
                        }
                    }
                }
            }
        }

        if (importedId != null) {
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(strings.tools.importSuccess, modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
