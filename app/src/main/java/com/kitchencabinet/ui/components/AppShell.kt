package com.kitchencabinet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.ui.theme.NewsreaderFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(
    navController: NavController,
    title: String = "Kitchen Cabinet",
    showHeader: Boolean = true,
    showNav: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    val strings = LocalStrings.current

    Scaffold(
        topBar = {
            if (showHeader) {
                AppHeader(title = title, navController = navController, strings = strings)
            }
        },
        bottomBar = {
            if (showNav) {
                BottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppHeader(
    title: String,
    navController: NavController,
    strings: com.kitchencabinet.ui.i18n.Strings
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("settings") { launchSingleTop = true } }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = strings.appShell.settingsContentDesc,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            actions = {
                IconButton(onClick = { navController.navigate("favorites") { popUpTo("home") { saveState = true }; launchSingleTop = true; restoreState = true } }) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = strings.appShell.favoritesContentDesc,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
