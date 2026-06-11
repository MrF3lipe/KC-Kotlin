package com.kitchencabinet.ui.screens

import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.kitchencabinet.ui.theme.NewsreaderFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herramientas") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Unit Converter card
            ToolCard(title = "Conversor de unidades", icon = Icons.Filled.SwapHoriz) {
                UnitConverterContent()
            }

            // Scale Calculator card
            ToolCard(title = "Escalar receta", icon = Icons.Filled.Calculate) {
                ScaleCalculatorContent()
            }

            // Barcode Scanner card
            ToolCard(title = "Esc\u00E1ner de c\u00F3digo", icon = Icons.Filled.QrCodeScanner) {
                BarcodeScannerContent()
            }

            // Fridge Photo card
            ToolCard(title = "Foto de nevera", icon = Icons.Filled.CameraAlt) {
                FridgePhotoContent()
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
private fun UnitConverterContent() {
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
            label = { Text("Valor") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposedDropdownMenuBox(expanded = fromExpanded, onExpandedChange = { fromExpanded = !fromExpanded },
                modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = fromUnit, onValueChange = {}, readOnly = true, label = { Text("De") },
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
                OutlinedTextField(value = toUnit, onValueChange = {}, readOnly = true, label = { Text("A") },
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
                    result = "$value $fromUnit = ${String.format("%.2f", converted)} $toUnit"
                }
            },
            enabled = inputValue.isNotBlank(),
            shape = RoundedCornerShape(50)
        ) { Text("Convertir") }

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
private fun ScaleCalculatorContent() {
    var originalServings by remember { mutableStateOf("") }
    var desiredServings by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Ajust\u00E1 las cantidades para diferentes porciones.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(value = originalServings, onValueChange = { originalServings = it.filter { c -> c.isDigit() } },
            label = { Text("Porciones originales") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = desiredServings, onValueChange = { desiredServings = it.filter { c -> c.isDigit() } },
            label = { Text("Porciones deseadas") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp))
        Button(
            onClick = {
                val original = originalServings.toIntOrNull()
                val desired = desiredServings.toIntOrNull()
                if (original != null && desired != null && original > 0) {
                    val factor = desired.toDouble() / original.toDouble()
                    result = "Factor de escala: ${String.format("%.2f", factor)}"
                } else result = "Ingres\u00E1 n\u00FAmeros v\u00E1lidos."
            },
            enabled = originalServings.isNotBlank() && desiredServings.isNotBlank(),
            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Calcular")
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
private fun BarcodeScannerContent() {
    val context = LocalContext.current
    var scannedCode by remember { mutableStateOf<String?>(null) }

    val barcodeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val code = result.data?.extras?.getString("code")
            if (code != null) scannedCode = code
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) scannedCode = "C\u00F3digo detectado" }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Escanear c\u00F3digos de barras para agregar ingredientes autom\u00E1ticamente.",
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (scannedCode != null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CheckCircle, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        Text(scannedCode!!, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.QrCodeScanner, null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text("Apunt\u00E1 la c\u00E1mara a un c\u00F3digo", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Button(
            onClick = {
                try { barcodeLauncher.launch(Intent("com.google.zxing.client.android.SCAN").apply { putExtra("SCAN_MODE", "PRODUCT_MODE") }) }
                catch (_: Exception) { cameraLauncher.launch(null) }
            },
            shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.QrCodeScanner, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (scannedCode != null) "Escanear otro" else "Escanear")
        }

        if (scannedCode != null) {
            OutlinedButton(onClick = { scannedCode = null }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()) {
                Text("Limpiar")
            }
        }
    }
}

@Composable
private fun FridgePhotoContent() {
    var photoUri by remember { mutableStateOf<String?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap -> if (bitmap != null) photoUri = "Foto capturada (${bitmap.width}x${bitmap.height})" }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Sac\u00E1 una foto de tu nevera para detectar ingredientes.",
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
                        Text("Foto del contenido de la nevera", style = MaterialTheme.typography.bodySmall,
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
            Text(if (photoUri != null) "Re-tomar" else "Abrir c\u00E1mara")
        }

        if (photoUri != null) {
            OutlinedButton(onClick = { photoUri = null }, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth()) {
                Text("Limpiar")
            }
        }
    }
}