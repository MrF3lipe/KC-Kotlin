package com.kitchencabinet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kitchencabinet.ui.i18n.LocalStrings

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

@Composable
fun BottomNavBar(navController: NavController) {
    val strings = LocalStrings.current
    val items = listOf(
        BottomNavItem("home", strings.nav.explore, Icons.Filled.Explore, Icons.Outlined.Explore),
        BottomNavItem("search", strings.nav.search, Icons.Filled.Search, Icons.Outlined.Search),
        BottomNavItem("addedit", strings.nav.new, Icons.Filled.AddCircle, Icons.Outlined.AddCircle),
        BottomNavItem("pantry", strings.nav.pantry, Icons.Filled.Kitchen, Icons.Outlined.Kitchen),
    )

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        items.forEach { item ->
            val selected = when (item.route) {
                "home" -> currentRoute == "home"
                "search" -> currentRoute == "search"
                "addedit" -> currentRoute?.startsWith("addedit") == true
                "pantry" -> currentRoute == "pantry"
                else -> currentRoute == item.route
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                )
            )
        }
    }
}
