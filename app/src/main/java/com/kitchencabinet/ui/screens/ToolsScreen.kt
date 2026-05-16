package com.kitchencabinet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
            // Tool tabs
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
            }

            when (selectedTool) {
                0 -> UnitConverter()
                1 -> ScaleCalculator()
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
