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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.PantryCategory
import com.kitchencabinet.data.PantryItem
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.CategoryWithItems
import com.kitchencabinet.viewmodel.PantryViewModel

private val UNITS = listOf("ud", "g", "kg", "ml", "L", "tsp", "tbsp", "cup")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val groupedItems by viewModel.groupedItems.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showCategoryManager by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<PantryItem?>(null) }

    val allItems = groupedItems.flatMap { it.items }
    val totalItems = categories.sumOf { cat -> groupedItems.find { it.category.name == cat.name }?.items?.size ?: 0 }

    // Add/Edit Dialog
    if (showDialog) {
        PantryItemBottomSheet(
            item = editItem,
            categories = categories,
            onDismiss = { showDialog = false; editItem = null },
            onConfirm = { name, category, qty, unit ->
                val item = editItem
                if (item == null) {
                    viewModel.insert(PantryItem(name = name, category = category, quantity = qty.toDoubleOrNull() ?: 1.0, unit = unit))
                } else {
                    viewModel.update(item.copy(name = name, category = category, quantity = qty.toDoubleOrNull() ?: 1.0, unit = unit))
                }
                showDialog = false; editItem = null
            }
        )
    }

    // Category Manager Bottom Sheet
    if (showCategoryManager) {
        CategoryManagerBottomSheet(
            categories = categories,
            onDismiss = { showCategoryManager = false },
            onInsert = { viewModel.insertCategory(it) },
            onUpdate = { viewModel.updateCategory(it) },
            onDelete = { viewModel.deleteCategory(it) }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (allItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Kitchen, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Tu despensa est\u00E1 vac\u00EDa", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Header
                item(key = "header") {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            "\u00BFQu\u00E9 hay en la despensa?",
                            style = MaterialTheme.typography.displayMedium,
                            fontFamily = NewsreaderFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$totalItems / $totalItems items",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Action buttons
                item(key = "actions") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            onClick = { showDialog = true },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                Text(
                                    "A\u00F1adir ingrediente",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                        Surface(
                            onClick = { showCategoryManager = true },
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Text(
                                "Gestionar categor\u00EDas",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                groupedItems.forEach { group ->
                    val emoji = group.category.emoji ?: "\uD83D\uDCE6"
                    val catName = group.category.name

                    item(key = "cat_$catName") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(catName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.weight(1f))
                            Text("${group.items.size} items", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    items(group.items, key = { "pantry_${it.id}" }) { item ->
                        PantryItemRow(
                            item = item,
                            onToggleAvailable = { viewModel.toggleAvailable(item) },
                            onAdjustQuantity = { delta -> viewModel.adjustQuantity(item, delta) },
                            onEdit = { editItem = item; showDialog = true },
                            onDelete = { viewModel.delete(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PantryItemRow(
    item: PantryItem,
    onToggleAvailable: (PantryItem) -> Unit,
    onAdjustQuantity: (Double) -> Unit,
    onEdit: (PantryItem) -> Unit,
    onDelete: (PantryItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.available) MaterialTheme.colorScheme.surfaceContainerLow
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = item.available, onCheckedChange = { onToggleAvailable(item) })

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name, style = MaterialTheme.typography.titleSmall, maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (!item.available) TextDecoration.LineThrough else TextDecoration.None
                )
                if (item.expiresAt != null && item.expiresAt > 0) {
                    val daysLeft = (item.expiresAt - System.currentTimeMillis()) / 86400000
                    val expiryText = when {
                        daysLeft < 0 -> "\u00A1Vencido!"
                        daysLeft == 0L -> "Hoy"
                        daysLeft == 1L -> "Ma\u00F1ana"
                        daysLeft <= 3L -> "${daysLeft} d\u00EDas"
                        else -> "${daysLeft} d\u00EDas"
                    }
                    val expiryColor = when {
                        daysLeft <= 1L -> MaterialTheme.colorScheme.error
                        daysLeft <= 3L -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(expiryText, style = MaterialTheme.typography.bodySmall, color = expiryColor)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(onClick = {
                    val step = if (item.unit == "ud") 1.0 else if (item.quantity >= 100) 100.0 else if (item.quantity >= 10) 10.0 else 1.0
                    onAdjustQuantity(-step)
                }, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(50)) {
                    Icon(Icons.Filled.Remove, "Decrease", Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(if (item.quantity == item.quantity.toLong().toDouble()) item.quantity.toLong().toString() else String.format("%.1f", item.quantity),
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(6.dp))
                FilledTonalButton(onClick = {
                    val step = if (item.unit == "ud") 1.0 else if (item.quantity >= 100) 100.0 else if (item.quantity >= 10) 10.0 else 1.0
                    onAdjustQuantity(step)
                }, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(50)) {
                    Icon(Icons.Filled.Add, "Increase", Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(item.unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
            IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Edit, "Edit", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Delete, "Delete", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryItemBottomSheet(
    item: PantryItem?,
    categories: List<PantryCategory>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, quantity: String, unit: String) -> Unit,
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(item?.category ?: categories.firstOrNull()?.name ?: "General") }
    var quantity by remember { mutableStateOf(if (item != null) item.quantity.toString() else "1") }
    var selectedUnit by remember { mutableStateOf(item?.unit ?: "ud") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var unitExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                if (item == null) "A\u00F1adir ingrediente" else "Editar ingrediente",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
            )

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") },
                singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(value = selectedCategory, onValueChange = {}, readOnly = true,
                    label = { Text("Categor\u00EDa") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(text = { Text("${cat.emoji ?: "\uD83D\uDCE6"}  ${cat.name}") },
                            onClick = { selectedCategory = cat.name; categoryExpanded = false })
                    }
                }
            }

            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantidad") },
                singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                OutlinedTextField(value = selectedUnit, onValueChange = {}, readOnly = true,
                    label = { Text("Unidad") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp))
                ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                    UNITS.forEach { unit ->
                        DropdownMenuItem(text = { Text(unit) }, onClick = { selectedUnit = unit; unitExpanded = false })
                    }
                }
            }

            Button(
                onClick = { onConfirm(name, selectedCategory, quantity, selectedUnit) },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50)
            ) { Text("Guardar") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagerBottomSheet(
    categories: List<PantryCategory>,
    onDismiss: () -> Unit,
    onInsert: (PantryCategory) -> Unit,
    onUpdate: (PantryCategory) -> Unit,
    onDelete: (PantryCategory) -> Unit,
) {
    var newName by remember { mutableStateOf("") }
    var newEmoji by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Int?>(null) }
    var editName by remember { mutableStateOf("") }
    var editEmoji by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .heightIn(max = 500.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Gestionar categor\u00EDas",
                style = MaterialTheme.typography.titleLarge,
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.SemiBold,
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(categories, key = { it.id }) { cat ->
                    if (editingId == cat.id) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = editEmoji, onValueChange = { editEmoji = it },
                                modifier = Modifier.width(56.dp), singleLine = true, placeholder = { Text("\uD83D\uDCE6") })
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(value = editName, onValueChange = { editName = it },
                                modifier = Modifier.weight(1f), singleLine = true)
                            IconButton(onClick = {
                                if (editName.isNotBlank()) { onUpdate(cat.copy(name = editName, emoji = editEmoji.ifBlank { null })) }
                                editingId = null
                            }) { Icon(Icons.Filled.Check, "Save") }
                            IconButton(onClick = { editingId = null }) { Icon(Icons.Filled.Close, "Cancel") }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(cat.emoji ?: "\uD83D\uDCE6", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(8.dp))
                            Text(cat.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            if (categories.size > 1) {
                                IconButton(onClick = { editingId = cat.id; editName = cat.name; editEmoji = cat.emoji ?: "" },
                                    modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.Edit, "Edit", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onDelete(cat) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Filled.Delete, "Delete", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("A\u00F1adir categor\u00EDa", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = newEmoji, onValueChange = { newEmoji = it },
                            modifier = Modifier.width(56.dp), singleLine = true, placeholder = { Text("\uD83D\uDCE6") })
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(value = newName, onValueChange = { newName = it },
                            modifier = Modifier.weight(1f), singleLine = true, placeholder = { Text("Nombre") })
                        IconButton(onClick = {
                            if (newName.isNotBlank()) {
                                onInsert(PantryCategory(name = newName, emoji = newEmoji.ifBlank { null }))
                                newName = ""; newEmoji = ""
                            }
                        }, enabled = newName.isNotBlank()) {
                            Icon(Icons.Filled.Add, "Add", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}