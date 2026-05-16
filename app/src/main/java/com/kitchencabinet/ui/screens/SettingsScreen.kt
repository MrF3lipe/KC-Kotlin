package com.kitchencabinet.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.kitchencabinet.LocalDarkMode
import com.kitchencabinet.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navController: NavController? = null,
    viewModel: SettingsViewModel = viewModel()
) {
    val darkMode = LocalDarkMode.current
    val settings by viewModel.settings.collectAsState()
    val notificationsConfig by viewModel.notificationsConfig.collectAsState()
    val context = LocalContext.current

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var importResult by remember { mutableStateOf<String?>(null) }

    val currentLocale = settings?.locale ?: "es"
    val expiryEnabled = notificationsConfig?.expiryEnabled ?: true
    val expiryDays = notificationsConfig?.expiryDaysBefore ?: 2

    // Export dialog
    if (showExportDialog) {
        var exportedJson by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            viewModel.exportBackup { json -> exportedJson = json }
        }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Backup") },
            text = {
                Column {
                    Text("Copy this JSON to save your data:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    if (exportedJson.isNotBlank()) {
                        OutlinedTextField(
                            value = exportedJson,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("backup", exportedJson))
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) { Text("Copy") }
            },
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Close") } }
        )
    }

    // Import dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Backup") },
            text = {
                Column {
                    Text("Paste your backup JSON:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                        placeholder = { Text("Paste JSON here...") }
                    )
                    if (importResult != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            importResult!!,
                            color = if (importResult!!.startsWith("Import successful"))
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (importText.isNotBlank()) {
                            viewModel.importBackup(importText) { ok, msg -> importResult = msg }
                        }
                    },
                    enabled = importText.isNotBlank()
                ) { Text("Import") }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false; importResult = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // === Appearance Section ===
            SectionHeader("Appearance")

            ListItem(
                headlineContent = { Text("Dark Mode") },
                supportingContent = { Text(if (darkMode.value) "Dark theme" else "Light theme") },
                leadingContent = {
                    Icon(if (darkMode.value) Icons.Filled.DarkMode else Icons.Filled.LightMode, contentDescription = null)
                },
                trailingContent = {
                    Switch(checked = darkMode.value, onCheckedChange = {
                        darkMode.value = it
                        viewModel.setTheme(if (it) "dark" else "light")
                    })
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // === Language Section ===
            SectionHeader("Language")

            ListItem(
                headlineContent = { Text("App Language") },
                supportingContent = { Text(if (currentLocale == "es") "Español" else "English") },
                leadingContent = { Icon(Icons.Filled.Language, contentDescription = null) },
                trailingContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(selected = currentLocale == "es", onClick = { viewModel.setLocale("es") }, label = { Text("ES") })
                        FilterChip(selected = currentLocale == "en", onClick = { viewModel.setLocale("en") }, label = { Text("EN") })
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // === Notifications Section ===
            SectionHeader("Notifications")

            ListItem(
                headlineContent = { Text("Expiry Reminders") },
                supportingContent = { Text(if (expiryEnabled) "Enabled" else "Disabled") },
                leadingContent = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                trailingContent = {
                    Switch(checked = expiryEnabled, onCheckedChange = { viewModel.setExpiryNotificationsEnabled(it) })
                }
            )
            if (expiryEnabled) {
                ListItem(
                    headlineContent = {
                        Column {
                            Text("Days before expiry")
                            Text(
                                "$expiryDays days",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    leadingContent = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (expiryDays > 1) viewModel.setExpiryDaysBefore(expiryDays - 1)
                                },
                                enabled = expiryDays > 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Remove, "Decrease", Modifier.size(18.dp))
                            }
                            Text(
                                "$expiryDays",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.width(24.dp),
                                textAlign = TextAlign.Center
                            )
                            IconButton(
                                onClick = {
                                    if (expiryDays < 14) viewModel.setExpiryDaysBefore(expiryDays + 1)
                                },
                                enabled = expiryDays < 14,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Add, "Increase", Modifier.size(18.dp))
                            }
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // === Navigation Shortcuts Section ===
            SectionHeader("Navigation")

            NavShortcutItem(
                label = "Favorites",
                icon = Icons.Filled.Favorite,
                onClick = { navController?.navigate("favorites") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            NavShortcutItem(
                label = "Shopping",
                icon = Icons.Filled.ShoppingCart,
                onClick = { navController?.navigate("shopping") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            NavShortcutItem(
                label = "Meal Plan",
                icon = Icons.Filled.CalendarMonth,
                onClick = { navController?.navigate("mealplan") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            NavShortcutItem(
                label = "Tools",
                icon = Icons.Filled.Build,
                onClick = { navController?.navigate("tools") }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // === Data Section ===
            SectionHeader("Data")

            ListItem(
                headlineContent = { Text("Export Backup") },
                supportingContent = { Text("Save all data as JSON") },
                leadingContent = { Icon(Icons.Filled.Upload, contentDescription = null) },
                trailingContent = {
                    TextButton(onClick = { showExportDialog = true }) {
                        Text("Export")
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Import Backup") },
                supportingContent = { Text("Restore data from JSON") },
                leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                trailingContent = {
                    TextButton(onClick = { showImportDialog = true }) {
                        Text("Import")
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NavShortcutItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            headlineContent = { Text(label) },
            leadingContent = { Icon(icon, contentDescription = null) },
            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}
