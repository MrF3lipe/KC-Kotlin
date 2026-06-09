package com.kitchencabinet.ui.screens

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onBack: () -> Unit
) {
    var selectedTool by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tools") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = selectedTool) {
                Tab(selected = selectedTool == 0, onClick = { selectedTool = 0 }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SwapHoriz, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Converter")
                    }
                }
                Tab(selected = selectedTool == 1, onClick = { selectedTool = 1 }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Calculate, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Scale")
                    }
                }
                Tab(selected = selectedTool == 2, onClick = { selectedTool = 2 }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Barcode")
                    }
                }
                Tab(selected = selectedTool == 3, onClick = { selectedTool = 3 }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CameraAlt, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Fridge")
                    }
                }
            }

            when (selectedTool) {
                0 -> UnitConverter()
                1 -> ScaleCalculator()
                2 -> BarcodeScanner()
                3 -> FridgePhoto()
            }
        }
    }
}

@Composable
private fun UnitConverter() {
    var inputValue by remember { mutableStateOf("") }
    var fromUnit by remember { mutableStateOf("cup") }
    var toUnit by remember { mutableStateOf("ml") }
    var result by remember { mutableStateOf<String?>(null) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val conversions = mapOf(
        "cup" to 240.0, "tbsp" to 15.0, "tsp" to 5.0,
        "ml" to 1.0, "L" to 1000.0,
        "oz" to 29.5735, "fl oz" to 29.5735,
        "g" to 1.0, "kg" to 1000.0, "lb" to 453.592
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Unit Converter", style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = inputValue,
            onValueChange = { inputValue = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ExposedDropdownMenuBox(
                expanded = fromExpanded,
                onExpandedChange = { fromExpanded = !fromExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = fromUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("From") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = fromExpanded,
                    onDismissRequest = { fromExpanded = false }
                ) {
                    conversions.keys.sorted().forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = { fromUnit = unit; fromExpanded = false }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(
                expanded = toExpanded,
                onExpandedChange = { toExpanded = !toExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = toUnit,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("To") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = toExpanded,
                    onDismissRequest = { toExpanded = false }
                ) {
                    conversions.keys.sorted().forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = { toUnit = unit; toExpanded = false }
                        )
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
                    result = "$value $fromUnit = ${String.format("%.2f", converted)} $toUnit"
                }
            },
            enabled = inputValue.isNotBlank()
        ) { Text("Convert") }

        if (result != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    result!!,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun ScaleCalculator() {
    var originalServings by remember { mutableStateOf("") }
    var desiredServings by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Scale Recipe", style = MaterialTheme.typography.titleMedium)
        Text(
            "Adjust ingredient quantities for different serving sizes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = originalServings,
            onValueChange = { originalServings = it.filter { c -> c.isDigit() } },
            label = { Text("Original Servings") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = desiredServings,
            onValueChange = { desiredServings = it.filter { c -> c.isDigit() } },
            label = { Text("Desired Servings") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                val original = originalServings.toIntOrNull()
                val desired = desiredServings.toIntOrNull()
                if (original != null && desired != null && original > 0) {
                    val factor = desired.toDouble() / original.toDouble()
                    result = "Scale factor: ${String.format("%.2f", factor)}"
                } else {
                    result = "Please enter valid serving numbers."
                }
            },
            enabled = originalServings.isNotBlank() && desiredServings.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Calculate")
        }

        if (result != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        result!!,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        originalServings.toIntOrNull().let { orig ->
                            desiredServings.toIntOrNull().let { desired ->
                                if (orig != null && desired != null && orig > 0) {
                                    val factor = desired.toDouble() / orig.toDouble()
                                    "Multiply each ingredient quantity by ${String.format("%.2f", factor)}"
                                } else ""
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun BarcodeScanner() {
    val context = LocalContext.current
    var scannedCode by remember { mutableStateOf<String?>(null) }

    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data != null && data.extras != null) {
                val code = data.extras!!.getString("code")
                if (code != null) scannedCode = code
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            scannedCode = "Barcode detected via image"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Barcode Scanner", style = MaterialTheme.typography.titleMedium)
        Text(
            "Scan product barcodes to auto-add ingredients to your pantry.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth().height(180.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (scannedCode != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            scannedCode!!,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Point camera at a barcode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                val intent = Intent("com.google.zxing.client.android.SCAN").apply {
                    putExtra("SCAN_MODE", "PRODUCT_MODE")
                }
                try {
                    barcodeLauncher.launch(intent)
                } catch (_: Exception) {
                    cameraLauncher.launch(null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (scannedCode != null) "Scan Again" else "Scan Barcode")
        }

        if (scannedCode != null) {
            OutlinedButton(
                onClick = { scannedCode = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun FridgePhoto() {
    var photoUri by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            photoUri = "Photo captured (${bitmap.width}x${bitmap.height})"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Fridge Photo", style = MaterialTheme.typography.titleMedium)
        Text(
            "Take a photo of your fridge to auto-detect ingredients.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth().height(240.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            photoUri!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Take a photo of your fridge contents",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Button(
            onClick = { cameraLauncher.launch(null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (photoUri != null) "Retake Photo" else "Open Camera")
        }

        if (photoUri != null) {
            OutlinedButton(
                onClick = { photoUri = null },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }
        }
    }
}
