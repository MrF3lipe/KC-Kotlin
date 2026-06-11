package com.kitchencabinet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.ShoppingItem
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.ShoppingViewModel

private val UNITS = listOf("ud", "g", "kg", "ml", "L", "tsp", "tbsp", "cup")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val items by viewModel.shoppingItems.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ShoppingBottomSheet(
            onDismiss = { showDialog = false },
            onConfirm = { name, qty, unit ->
                viewModel.insert(ShoppingItem(name = name, quantity = qty.toDoubleOrNull() ?: 1.0, unit = unit))
                showDialog = false
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ShoppingCart, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Tu lista est\u00E1 vac\u00EDa", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("Agreg\u00E1 items desde recetas o manualmente", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header
                item {
                    Column(modifier = Modifier.padding(bottom = 4.dp)) {
                        Text(
                            "Lista de compras",
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = NewsreaderFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                }

                // Action buttons
                if (items.any { it.done }) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { viewModel.moveDoneToPantry() },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.Kitchen, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                    Text(
                                        "Pasar a despensa",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                                }
                            }
                            Surface(
                                onClick = { viewModel.clearDone() },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.errorContainer,
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Filled.Clear, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onErrorContainer)
                                    Text(
                                        "Limpiar",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                    )
                                }
                            }
                        }
                    }
                }

                items(items, key = { it.id }) { item ->
                    ShoppingItemRow(item = item, viewModel = viewModel)
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add item")
        }
    }
}

@Composable
private fun ShoppingItemRow(item: ShoppingItem, viewModel: ShoppingViewModel) {
    var unitExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.done, onCheckedChange = { viewModel.toggleDone(item.id, it) })

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                    style = MaterialTheme.typography.titleSmall)
                if (item.estimatedPrice != null) {
                    Text("$" + "%.2f".format(item.estimatedPrice), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalButton(onClick = { viewModel.adjustQuantity(item.id, -1.0) },
                    modifier = Modifier.size(28.dp), contentPadding = PaddingValues(0.dp),
                    enabled = item.quantity > 0, shape = RoundedCornerShape(50)) {
                    Icon(Icons.Filled.Remove, "Decrease", Modifier.size(14.dp))
                }
                Text("${item.quantity.toInt()}", style = MaterialTheme.typography.bodyMedium)
                FilledTonalButton(onClick = { viewModel.adjustQuantity(item.id, 1.0) },
                    modifier = Modifier.size(28.dp), contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(50)) {
                    Icon(Icons.Filled.Add, "Increase", Modifier.size(14.dp))
                }
            }

            Box {
                TextButton(onClick = { unitExpanded = true }, modifier = Modifier.height(32.dp)) {
                    Text(item.unit, style = MaterialTheme.typography.labelMedium)
                }
                DropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    UNITS.forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { viewModel.updateUnit(item.id, unit); unitExpanded = false })
                    }
                }
            }

            IconButton(onClick = { viewModel.delete(item) }) {
                Icon(Icons.Filled.Close, "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShoppingBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(UNITS[0]) }
    var unitExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Agregar item",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Cantidad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(12.dp))
                FilledTonalButton(onClick = {
                    val q = quantity.toDoubleOrNull() ?: 1.0
                    quantity = (q - 1.0).coerceAtLeast(0.0).toString()
                }, enabled = (quantity.toDoubleOrNull() ?: 1.0) > 0, modifier = Modifier.size(40.dp),
                    contentPadding = PaddingValues(0.dp), shape = RoundedCornerShape(50)) {
                    Icon(Icons.Filled.Remove, "Decrease")
                }
                FilledTonalButton(onClick = {
                    val q = quantity.toDoubleOrNull() ?: 1.0
                    quantity = (q + 1.0).toString()
                }, modifier = Modifier.size(40.dp), contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(50)) {
                    Icon(Icons.Filled.Add, "Increase")
                }
            }

            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = !unitExpanded }) {
                OutlinedTextField(value = selectedUnit, onValueChange = {}, readOnly = true, label = { Text("Unidad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), singleLine = true, shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    UNITS.forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { selectedUnit = unit; unitExpanded = false })
                    }
                }
            }

            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), quantity, selectedUnit) },
                enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)
            ) { Text("Agregar") }

            Spacer(Modifier.height(24.dp))
        }
    }
}