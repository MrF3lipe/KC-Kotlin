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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.kitchencabinet.data.MealPlanEntry
import com.kitchencabinet.viewmodel.MealPlanViewModel
import com.kitchencabinet.viewmodel.RecipeViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onRecipeClick: (Int) -> Unit,
    onBack: () -> Unit,
    mealPlanViewModel: MealPlanViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel()
) {
    val weekEntries by mealPlanViewModel.currentWeekEntries.collectAsState()
    val allRecipes by recipeViewModel.recipes.collectAsState()
    val currentWeekStart by mealPlanViewModel.currentWeekStart.collectAsState()

    var selectedDay by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf("") }
    var showRecipePicker by remember { mutableStateOf(false) }

    val dayNames = remember {
        listOf(
            "mon" to "Monday", "tue" to "Tuesday", "wed" to "Wednesday",
            "thu" to "Thursday", "fri" to "Friday", "sat" to "Saturday", "sun" to "Sunday"
        )
    }

    // Build a recipe title map for display
    val recipeTitleMap = remember(allRecipes) {
        allRecipes.associate { it.id to it.title }
    }

    LaunchedEffect(Unit) {
        mealPlanViewModel.loadWeek(currentWeekStart)
        recipeViewModel.setCategory("All")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Previous week
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        cal.time = fmt.parse(currentWeekStart) ?: Calendar.getInstance().time
                        cal.add(Calendar.WEEK_OF_YEAR, -1)
                        mealPlanViewModel.loadWeek(fmt.format(cal.time))
                    }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous week")
                    }
                    // Next week
                    IconButton(onClick = {
                        val cal = Calendar.getInstance()
                        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        cal.time = fmt.parse(currentWeekStart) ?: Calendar.getInstance().time
                        cal.add(Calendar.WEEK_OF_YEAR, 1)
                        mealPlanViewModel.loadWeek(fmt.format(cal.time))
                    }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Next week")
                    }
                    // Add all to shopping
                    IconButton(onClick = {
                        mealPlanViewModel.addCurrentWeekToShopping()
                    }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Add all to shopping")
                    }
                    // Clear week
                    IconButton(onClick = {
                        mealPlanViewModel.clearWeek(currentWeekStart)
                    }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = "Clear week")
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
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Week of $currentWeekStart",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(dayNames) { (dayShort, dayLong) ->
                    val dayEntries = weekEntries.filter { it.day.equals(dayShort, ignoreCase = true) }
                    val breakfast = dayEntries.find { it.slot == "breakfast" }
                    val lunch = dayEntries.find { it.slot == "lunch" }
                    val dinner = dayEntries.find { it.slot == "dinner" }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                dayLong,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))

                            MealSlot("Breakfast", breakfast, recipeTitleMap, onAdd = {
                                selectedDay = dayShort; selectedSlot = "breakfast"; showRecipePicker = true
                            }, onClick = { it.recipeId.let(onRecipeClick) })
                            MealSlot("Lunch", lunch, recipeTitleMap, onAdd = {
                                selectedDay = dayShort; selectedSlot = "lunch"; showRecipePicker = true
                            }, onClick = { it.recipeId.let(onRecipeClick) })
                            MealSlot("Dinner", dinner, recipeTitleMap, onAdd = {
                                selectedDay = dayShort; selectedSlot = "dinner"; showRecipePicker = true
                            }, onClick = { it.recipeId.let(onRecipeClick) })
                        }
                    }
                }
            }
        }
    }

    // Recipe picker dialog
    if (showRecipePicker) {
        AlertDialog(
            onDismissRequest = { showRecipePicker = false },
            title = { Text("Select Recipe for ${selectedSlot.replaceFirstChar { it.uppercase() }}") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(allRecipes.filter { it.id > 0 }) { recipe ->
                        TextButton(
                            onClick = {
                                mealPlanViewModel.addEntry(
                                    weekStart = currentWeekStart,
                                    day = selectedDay,
                                    slot = selectedSlot,
                                    recipeId = recipe.id
                                )
                                showRecipePicker = false
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (!recipe.imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = recipe.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp).clip(MaterialTheme.shapes.small),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(8.dp))
                                }
                                Text(recipe.title)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showRecipePicker = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun MealSlot(
    label: String,
    entry: MealPlanEntry?,
    recipeTitles: Map<Int, String>,
    onAdd: () -> Unit,
    onClick: (MealPlanEntry) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(80.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (entry != null) {
            TextButton(onClick = { onClick(entry) }) {
                Text(
                    recipeTitles[entry.recipeId] ?: "Recipe #${entry.recipeId}",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }
        } else {
            OutlinedButton(
                onClick = onAdd,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
