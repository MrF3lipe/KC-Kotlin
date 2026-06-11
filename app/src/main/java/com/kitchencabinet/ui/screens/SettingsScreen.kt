package com.kitchencabinet.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.LocalDarkMode
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navController: androidx.navigation.NavController? = null,
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

    if (showExportDialog) {
        var exportedJson by remember { mutableStateOf("") }
        LaunchedEffect(Unit) { viewModel.exportBackup { json -> exportedJson = json } }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exportar respaldo") },
            text = {
                Column {
                    Text("Copi\u00E1 este JSON para guardar tus datos:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    if (exportedJson.isNotBlank()) {
                        OutlinedTextField(value = exportedJson, onValueChange = {}, modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                            textStyle = MaterialTheme.typography.bodySmall)
                    } else CircularProgressIndicator()
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("backup", exportedJson))
                    Toast.makeText(context, "\u00A1Copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) { Text("Copiar") }
            },
            dismissButton = { TextButton(onClick = { showExportDialog = false }) { Text("Cerrar") } }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Importar respaldo") },
            text = {
                Column {
                    Text("Peg\u00E1 tu JSON de respaldo:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = importText, onValueChange = { importText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), textStyle = MaterialTheme.typography.bodySmall,
                        placeholder = { Text("Peg\u00E1 el JSON aqu\u00ED\u2026") })
                    if (importResult != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(importResult!!, color = if (importResult!!.startsWith("Import successful")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (importText.isNotBlank()) viewModel.importBackup(importText) { ok, msg -> importResult = msg } },
                    enabled = importText.isNotBlank()) { Text("Importar") }
            },
            dismissButton = { TextButton(onClick = { showImportDialog = false; importResult = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Appearance card ──────────────────────────────────────────────
            SettingsCard {
                SettingsRow(
                    icon = if (darkMode.value) Icons.Filled.DarkMode else Icons.Filled.LightMode,
                    title = "Apariencia",
                    subtitle = if (darkMode.value) "Oscuro" else "Claro",
                )
                Spacer(Modifier.height(8.dp))
                SegmentedControl(
                    options = listOf(
                        SegOption("\u2600", "light"),
                        SegOption("\uD83C\uDF19", "dark"),
                    ),
                    selectedValue = if (darkMode.value) "dark" else "light",
                    onSelect = { darkMode.value = it == "dark"; viewModel.setTheme(it) }
                )
            }

            // ── Language card ───────────────────────────────────────────────
            SettingsCard {
                SettingsRow(
                    icon = Icons.Filled.Language,
                    title = "Idioma",
                    subtitle = if (currentLocale == "es") "Espa\u00F1ol" else "English",
                )
                Spacer(Modifier.height(8.dp))
                SegmentedControl(
                    options = listOf(
                        SegOption("ES", "es"),
                        SegOption("EN", "en"),
                    ),
                    selectedValue = currentLocale,
                    onSelect = { viewModel.setLocale(it) }
                )
            }

            // ── Notifications card ──────────────────────────────────────────
            SettingsCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Recordatorios", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text(if (expiryEnabled) "Activados" else "Desactivados", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = expiryEnabled, onCheckedChange = { viewModel.setExpiryNotificationsEnabled(it) })
                }
                if (expiryEnabled) {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("D\u00EDas antes de vencer", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        FilledTonalButton(onClick = { if (expiryDays > 1) viewModel.setExpiryDaysBefore(expiryDays - 1) },
                            enabled = expiryDays > 1, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(50)) {
                            Icon(Icons.Filled.Remove, "Decrease", Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("$expiryDays", style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.width(24.dp), textAlign = TextAlign.Center)
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(onClick = { if (expiryDays < 14) viewModel.setExpiryDaysBefore(expiryDays + 1) },
                            enabled = expiryDays < 14, modifier = Modifier.size(32.dp), contentPadding = PaddingValues(0.dp),
                            shape = RoundedCornerShape(50)) {
                            Icon(Icons.Filled.Add, "Increase", Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ── Data card ───────────────────────────────────────────────────
            SettingsCard {
                Text("Datos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(onClick = { showExportDialog = true }, shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary) {
                        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Upload, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            Text("Exportar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                    Surface(onClick = { showImportDialog = true }, shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)) {
                        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Download, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Importar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ── Navigation shortcuts ────────────────────────────────────────
            SettingsCard {
                Text("Accesos r\u00E1pidos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NavShortcut(Icons.Filled.Favorite, "Favoritos", Modifier.weight(1f)) { navController?.navigate("favorites") }
                        NavShortcut(Icons.Filled.ShoppingCart, "Compras", Modifier.weight(1f)) { navController?.navigate("shopping") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NavShortcut(Icons.Filled.CalendarMonth, "Plan semanal", Modifier.weight(1f)) { navController?.navigate("mealplan") }
                        NavShortcut(Icons.Filled.Build, "Herramientas", Modifier.weight(1f)) { navController?.navigate("tools") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class SegOption(val label: String, val value: String)

@Composable
private fun SegmentedControl(options: List<SegOption>, selectedValue: String, onSelect: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEach { option ->
                val isSel = option.value == selectedValue
                Surface(
                    onClick = { onSelect(option.value) },
                    shape = RoundedCornerShape(50),
                    color = if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavShortcut(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}