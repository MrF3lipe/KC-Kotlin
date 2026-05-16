package com.kitchencabinet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.ShoppingItem
import com.kitchencabinet.viewmodel.ShoppingViewModel

private val UNITS = listOf("ud", "g", "kg", "ml", "L", "tsp", "tbsp", "cup")

@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val items by viewModel.shoppingItems.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        ShoppingDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name, qty, unit ->
                viewModel.insert(ShoppingItem(name = name, quantity = qty.toDoubleOrNull() ?: 1.0, unit = unit))
                showDialog = false
            }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.ShoppingCart, null,
                        Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Your list is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to add items from recipes or manually",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (items.any { it.done }) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { viewModel.clearDone() }) {
                                    Icon(Icons.Filled.Clear, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear done", color = MaterialTheme.colorScheme.error)
                                }
                                TextButton(onClick = { viewModel.moveDoneToPantry() }) {
                                    Icon(Icons.Filled.Kitchen, null, Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Move to Pantry", color = MaterialTheme.colorScheme.primary)
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add item")
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    viewModel: ShoppingViewModel
) {
    var unitExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.done,
                onCheckedChange = { viewModel.toggleDone(item.id, it) }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    textDecoration = if (item.done) TextDecoration.LineThrough else TextDecoration.None,
                    style = MaterialTheme.typography.titleSmall,
                )
                if (item.estimatedPrice != null) {
                    Text(
                        "$" + "%.2f".format(item.estimatedPrice),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quantity controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { viewModel.adjustQuantity(item.id, -1.0) },
                    modifier = Modifier.size(28.dp),
                    enabled = item.quantity > 0
                ) {
                    Icon(Icons.Filled.Remove, "Decrease", Modifier.size(16.dp))
                }

                Text(
                    "${item.quantity.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(
                    onClick = { viewModel.adjustQuantity(item.id, 1.0) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.Add, "Increase", Modifier.size(16.dp))
                }
            }

            // Unit dropdown
            Box {
                TextButton(
                    onClick = { unitExpanded = true },
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(item.unit, style = MaterialTheme.typography.labelMedium)
                }
                DropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    UNITS.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                viewModel.updateUnit(item.id, unit)
                                unitExpanded = false
                            }
                        )
                    }
                }
            }

            IconButton(onClick = { viewModel.delete(item) }) {
                Icon(
                    Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ShoppingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(UNITS[0]) }
    var unitExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quantity with +/- controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Quantity") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            val q = quantity.toDoubleOrNull() ?: 1.0
                            quantity = (q - 1.0).coerceAtLeast(0.0).toString()
                        },
                        enabled = (quantity.toDoubleOrNull() ?: 1.0) > 0
                    ) {
                        Icon(Icons.Filled.Remove, "Decrease")
                    }
                    IconButton(onClick = {
                        val q = quantity.toDoubleOrNull() ?: 1.0
                        quantity = (q + 1.0).toString()
                    }) {
                        Icon(Icons.Filled.Add, "Increase")
                    }
                }

                // Unit dropdown
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = !unitExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        UNITS.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    selectedUnit = unit
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), quantity, selectedUnit)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
