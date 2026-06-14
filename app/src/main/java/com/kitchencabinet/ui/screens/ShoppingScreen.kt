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
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.ShoppingViewModel

private val UNITS = listOf("ud", "g", "kg", "ml", "L", "tsp", "tbsp", "cup")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingScreen(
    viewModel: ShoppingViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val strings = LocalStrings.current
    val items by viewModel.shoppingItems.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    var editingItemId by remember { mutableStateOf<Int?>(null) }
    var editingPrice by remember { mutableStateOf("") }

    if (editingItemId != null) {
        AlertDialog(
            onDismissRequest = { editingItemId = null },
            title = { Text(strings.shopping.estimatedPrice) },
            text = {
                OutlinedTextField(value = editingPrice, onValueChange = { editingPrice = it },
                    label = { Text(strings.shopping.estimatedPrice) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true, shape = RoundedCornerShape(12.dp))
            },
            confirmButton = {
                TextButton(onClick = {
                    val price = editingPrice.toDoubleOrNull()
                    viewModel.updatePrice(editingItemId!!, price)
                    editingItemId = null
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.updatePrice(editingItemId!!, null)
                    editingItemId = null
                }) { Text(if (editingPrice.isBlank()) strings.shopping.clear else strings.settings.cancel) }
            }
        )
    }

    if (showDialog) {
        ShoppingBottomSheet(
            onDismiss = { showDialog = false },
            onConfirm = { name, qty, unit, price ->
                viewModel.insert(ShoppingItem(name = name, quantity = qty.toDoubleOrNull() ?: 1.0, unit = unit, estimatedPrice = price))
                showDialog = false
            },
            strings = strings
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ShoppingCart, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text(strings.shopping.emptyTitle, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text(strings.shopping.emptySubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            strings.shopping.title,
                            style = MaterialTheme.typography.displaySmall,
                            fontFamily = NewsreaderFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        val total = items.sumOf { it.estimatedPrice?.times(it.quantity) ?: 0.0 }
                        if (total > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${strings.shopping.total}: $${"%.2f".format(total)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
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
                                        strings.shopping.moveToPantry,
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
                                        strings.shopping.clear,
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
                    ShoppingItemRow(item = item, viewModel = viewModel, onEditPrice = { id ->
                        val curr = items.find { it.id == id }?.estimatedPrice
                        editingPrice = curr?.let { "%.2f".format(it) } ?: ""
                        editingItemId = id
                    })
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ) {
            Icon(Icons.Filled.Add, contentDescription = strings.shopping.addItem)
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItem,
    viewModel: ShoppingViewModel,
    onEditPrice: (Int) -> Unit = {},
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (item.estimatedPrice != null) "$${"%.2f".format(item.estimatedPrice)}" else "—",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = { onEditPrice(item.id) }, modifier = Modifier.size(18.dp)) {
                        Icon(Icons.Filled.Edit, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
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
    onConfirm: (String, String, String, Double?) -> Unit,
    strings: com.kitchencabinet.ui.i18n.Strings,
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(UNITS[0]) }
    var unitExpanded by remember { mutableStateOf(false) }
    var price by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                strings.shopping.addItem,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(strings.shopping.name) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = quantity, onValueChange = { quantity = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(strings.shopping.quantity) },
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
                OutlinedTextField(value = selectedUnit, onValueChange = {}, readOnly = true, label = { Text(strings.shopping.unit) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), singleLine = true, shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    UNITS.forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { selectedUnit = unit; unitExpanded = false })
                    }
                }
            }

            OutlinedTextField(value = price, onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(strings.shopping.estimatedPrice) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp),
                prefix = { Text("$") })

            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim(), quantity, selectedUnit, price.toDoubleOrNull()) },
                enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(50)
            ) { Text(strings.shopping.add) }

            Spacer(Modifier.height(24.dp))
        }
    }
}
