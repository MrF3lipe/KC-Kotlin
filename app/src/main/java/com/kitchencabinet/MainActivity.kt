package com.kitchencabinet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kitchencabinet.ui.components.AppShell
import com.kitchencabinet.ui.screens.*
import com.kitchencabinet.ui.theme.KitchenCabinetTheme

val LocalDarkMode = compositionLocalOf { mutableStateOf(false) }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode = remember { mutableStateOf(false) }

            KitchenCabinetTheme(darkTheme = darkMode.value) {
                CompositionLocalProvider(LocalDarkMode provides darkMode) {
                    KitchenCabinetApp()
                }
            }
        }
    }
}

@Composable
fun KitchenCabinetApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
    ) {
        composable("home") {
            AppShell(navController = navController, title = "Kitchen Cabinet") { padding ->
                HomeScreen(
                    onRecipeClick = { navController.navigate("detail/$it") },
                    onAddRecipe = { navController.navigate("addedit") },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable("favorites") {
            AppShell(navController = navController, title = "Favorites") { padding ->
                FavoritesScreen(
                    onRecipeClick = { navController.navigate("detail/$it") },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable("pantry") {
            AppShell(navController = navController, title = "Pantry", showHeader = false, showNav = true) { padding ->
                PantryScreen(modifier = Modifier.padding(padding))
            }
        }
        composable("shopping") {
            AppShell(navController = navController, title = "Shopping", showHeader = false, showNav = true) { padding ->
                ShoppingScreen(modifier = Modifier.padding(padding))
            }
        }
        composable("search") {
            AppShell(navController = navController, title = "Search") { padding ->
                SearchScreen(
                    onRecipeClick = { navController.navigate("detail/$it") },
                    modifier = Modifier.padding(padding)
                )
            }
        }
        composable("mealplan") {
            MealPlanScreen(
                onRecipeClick = { navController.navigate("detail/$it") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("tools") {
            ToolsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id")
            RecipeDetailScreen(
                recipeId = id,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate("addedit?id=$it") },
                onCook = { navController.navigate("cook/$it") },
                onShare = { navController.navigate("share/$it") }
            )
        }
        composable(
            "addedit?id={id}",
            arguments = listOf(navArgument("id") {
                type = NavType.IntType; defaultValue = -1
            })
        ) { back ->
            val id = back.arguments!!.getInt("id").takeIf { it != -1 }
            AddEditScreen(
                recipeId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable("addedit") {
            AddEditScreen(
                recipeId = null,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "cook/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id")
            CookScreen(
                recipeId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "share/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { back ->
            val id = back.arguments!!.getInt("id")
            ShareScreen(
                recipeId = id,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
