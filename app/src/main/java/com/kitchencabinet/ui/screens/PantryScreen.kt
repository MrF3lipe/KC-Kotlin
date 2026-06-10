package com.kitchencabinet.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.PantryCategory
import com.kitchencabinet.data.PantryItem
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

    // Add/Edit Dialog
    if (showDialog) {
        PantryItemDialog(
            item = editItem,
            categories = categories,
            onDismiss = { showDialog = false; editItem = null },
            onConfirm = { name, category, qty, unit ->
                val item = editItem
                if (item == null) {
                    viewModel.insert(
                        PantryItem(
                            name = name,
                            category = category,
                            quantity = qty.toDoubleOrNull() ?: 1.0,
                            unit = unit
                        )
                    )
                } else {
                    viewModel.update(
                        item.copy(
                            name = name,
                            category = category,
                            quantity = qty.toDoubleOrNull() ?: 1.0,
                            unit = unit
                        )
                    )
                }
                showDialog = false
                editItem = null
            }
        )
    }

    // Category Manager
    if (showCategoryManager) {
        CategoryManagerDialog(
            categories = categories,
            onDismiss = { showCategoryManager = false },
            onInsert = { viewModel.insertCategory(it) },
            onUpdate = { viewModel.updateCategory(it) },
            onDelete = { viewModel.deleteCategory(it) }
        )
    }

    val allItems = groupedItems.flatMap { it.items }

    Box(modifier = modifier.fillMaxSize()) {
        if (allItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Kitchen, null,
                        Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Your pantry is empty",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                groupedItems.forEach { group ->
                    val emoji = group.category.emoji ?: "📦"
                    val catName = group.category.name

                    // Category header
                    item(key = "cat_$catName") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(emoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                catName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "${group.items.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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

        // Bottom FABs
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = { showCategoryManager = true },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Manage categories",
                    modifier = Modifier.size(24.dp)
                )
            }
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add item")
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────
// PantryItemRow
// ──────────────────────────────────────────────────────────────────

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
            containerColor = if (item.available)
                MaterialTheme.colorScheme.surfaceContainerLow
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Checkbox ──
            Checkbox(
                checked = item.available,
                onCheckedChange = { onToggleAvailable(item) }
            )

            // ── Name + expiry column ──
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (!item.available)
                        TextDecoration.LineThrough
                    else
                        TextDecoration.None
                )

                // Expiry info
                if (item.expiresAt != null && item.expiresAt > 0) {
                    val daysLeft = (item.expiresAt - System.currentTimeMillis()) / 86400000
                    val expiryText = when {
                        daysLeft < 0 -> "Expired!"
                        daysLeft == 0L -> "Today"
                        daysLeft == 1L -> "Tomorrow"
                        daysLeft <= 3L -> "${daysLeft} days left"
                        else -> "${daysLeft} days left"
                    }
                    val expiryColor = when {
                        daysLeft <= 1L -> MaterialTheme.colorScheme.error
                        daysLeft <= 3L -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        expiryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = expiryColor
                    )
                }
            }

            // ── Quantity controls ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Decrement
                FilledTonalButton(
                    onClick = {
                        val step = if (item.unit == "ud") 1.0
                        else if (item.quantity > 100) 100.0
                        else if (item.quantity > 10) 10.0
                        else 1.0
                        onAdjustQuantity(-step)
                    },
                    modifier = Modifier.size(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                }

                Spacer(Modifier.width(6.dp))

                // Quantity text
                Text(
                    text = if (item.quantity == item.quantity.toLong().toDouble())
                        item.quantity.toLong().toString()
                    else
                        String.format("%.1f", item.quantity),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(Modifier.width(6.dp))

                // Increment
                FilledTonalButton(
                    onClick = {
                        val step = if (item.unit == "ud") 1.0
                        else if (item.quantity >= 100) 100.0
                        else if (item.quantity >= 10) 10.0
                        else 1.0
                        onAdjustQuantity(step)
                    },
                    modifier = Modifier.size(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.width(6.dp))

            // ── Unit ──
            Text(
                item.unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(28.dp)
            )

            // ── Edit button ──
            IconButton(onClick = { onEdit(item) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Edit item",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // ── Delete button ──
            IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete item",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────
// PantryItemDialog
// ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryItemDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (item == null) "Add Item" else "Edit Item")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    val emoji = cat.emoji ?: "📦"
                                    Text("$emoji  ${cat.name}")
                                },
                                onClick = {
                                    selectedCategory = cat.name
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Unit dropdown
                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedUnit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                onClick = { onConfirm(name, selectedCategory, quantity, selectedUnit) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ──────────────────────────────────────────────────────────────────
// CategoryManagerDialog
// ──────────────────────────────────────────────────────────────────

@Composable
private fun CategoryManagerDialog(
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Categories") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Existing categories
                categories.forEach { cat ->
                    if (editingId == cat.id) {
                        // Inline edit mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = editEmoji,
                                onValueChange = { editEmoji = it },
                                modifier = Modifier.width(56.dp),
                                singleLine = true,
                                placeholder = { Text("📦") }
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = editName,
                                onValueChange = { editName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(onClick = {
                                if (editName.isNotBlank()) {
                                    onUpdate(cat.copy(name = editName, emoji = editEmoji.ifBlank { null }))
                                }
                                editingId = null
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "Save")
                            }
                            IconButton(onClick = { editingId = null }) {
                                Icon(Icons.Filled.Close, contentDescription = "Cancel")
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingId = cat.id
                                    editName = cat.name
                                    editEmoji = cat.emoji ?: ""
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(cat.emoji ?: "📦", style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (categories.size > 1) {
                                IconButton(
                                    onClick = { onDelete(cat) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete ${cat.name}",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Add new category
                Text(
                    "Add new category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newEmoji,
                        onValueChange = { newEmoji = it },
                        modifier = Modifier.width(56.dp),
                        singleLine = true,
                        placeholder = { Text("📦") }
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Category name") }
                    )
                    IconButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                onInsert(
                                    PantryCategory(
                                        name = newName,
                                        emoji = newEmoji.ifBlank { null }
                                    )
                                )
                                newName = ""
                                newEmoji = ""
                            }
                        },
                        enabled = newName.isNotBlank()
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add category",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
