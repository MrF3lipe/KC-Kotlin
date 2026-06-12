package com.kitchencabinet.ui.screens

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookScreen(
    recipeId: Int,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel()
) {
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var currentStep by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }
    var timerSeconds by remember { mutableIntStateOf(0) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var timerInput by remember { mutableStateOf("") }
    var showFinishDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Load recipe
    LaunchedEffect(recipeId) {
        recipe = viewModel.getById(recipeId)
    }

    // Keep screen on while cooking
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Timer countdown effect — uses while loop so it doesn't restart on each tick
    LaunchedEffect(timerRunning) {
        if (!timerRunning) return@LaunchedEffect
        while (timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
        // Timer reached zero
        timerRunning = false
        onTimerExpired(context)
    }

    val r = recipe ?: return

    // Auto-detect minutes in current step text
    val detectedMinutes = remember(r, currentStep) {
        if (currentStep < r.steps.size) {
            extractMinutes(r.steps[currentStep])
        } else null
    }

    // Timer dialog
    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Set Timer") },
            text = {
                OutlinedTextField(
                    value = timerInput,
                    onValueChange = { timerInput = it.filter { c -> c.isDigit() } },
                    label = { Text("Minutes") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val mins = timerInput.toIntOrNull() ?: 0
                    if (mins > 0) {
                        timerSeconds = mins * 60
                        timerRunning = true
                    }
                    showTimerDialog = false
                    timerInput = ""
                }) {
                    Text("Start")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimerDialog = false
                    timerInput = ""
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Finish confirmation dialog
    if (showFinishDialog) {
        AlertDialog(
            onDismissRequest = { showFinishDialog = false },
            title = { Text("Finish Cooking?") },
            text = {
                if (currentStep < r.steps.size - 1) {
                    Text("You haven't reached the last step yet. Are you sure you want to finish?")
                } else {
                    Text("Mark this recipe as cooked and go back?")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showFinishDialog = false
                    viewModel.incrementCookedCount(recipeId)
                    onBack()
                }) {
                    Text("Finish", color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Cooking: ${r.title}",
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(Icons.Filled.Timer, contentDescription = "Timer")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            if (r.steps.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Previous button
                        OutlinedButton(
                            onClick = {
                                if (currentStep > 0) currentStep--
                            },
                            enabled = currentStep > 0,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.ChevronLeft,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("Previous")
                        }

                        // Next / Finish button
                        if (currentStep < r.steps.size - 1) {
                            Button(
                                onClick = { currentStep++ },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Next")
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = { showFinishDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Finish")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Empty state
            if (r.steps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No steps for this recipe.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@Column
            }

            // Timer display card
            if (timerSeconds > 0) {
                TimerDisplayCard(
                    timerSeconds = timerSeconds,
                    timerRunning = timerRunning,
                    onPause = { timerRunning = false },
                    onResume = { timerRunning = true },
                    onStop = {
                        timerRunning = false
                        timerSeconds = 0
                    }
                )
            }

            // Auto-detect timer chip
            if (detectedMinutes != null && !timerRunning && timerSeconds == 0) {
                SuggestionChip(
                    onClick = {
                        timerSeconds = detectedMinutes * 60
                        timerRunning = true
                    },
                    icon = {
                        Icon(
                            Icons.Filled.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text("Timer: $detectedMinutes min") }
                )
            }

            // Progress indicator
            LinearProgressIndicator(
                progress = { (currentStep + 1).toFloat() / r.steps.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Step label
            Text(
                "Step ${currentStep + 1} of ${r.steps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Current step card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Step number badge
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.wrapContentSize()
                    ) {
                        Text(
                            "Step ${currentStep + 1}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Step description
                    Text(
                        text = r.steps[currentStep],
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun TimerDisplayCard(
    timerSeconds: Int,
    timerRunning: Boolean,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (timerRunning)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timer display
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (timerRunning) Icons.Filled.Timer else Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = if (timerRunning) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                val minutes = timerSeconds / 60
                val seconds = timerSeconds % 60
                Text(
                    text = "%d:%02d".format(minutes, seconds),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (timerRunning) MaterialTheme.colorScheme.onTertiaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }

            // Controls
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (timerRunning) {
                    IconButton(onClick = onPause) {
                        Icon(
                            Icons.Filled.Pause,
                            contentDescription = "Pause timer",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                } else {
                    IconButton(onClick = onResume) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Resume timer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = onStop) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = "Stop timer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Vibrate and play a short alarm sound when the timer expires.
 */
private fun onTimerExpired(context: Context) {
    // Vibrate
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vm.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(1500L, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1500L)
        }
    } catch (_: Exception) {
        // Device may not support vibration
    }

    // Play alarm sound if available
    try {
        val resId = context.resources.getIdentifier(
            "alarm_timer",
            "raw",
            context.packageName
        )
        if (resId != 0) {
            val mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnCompletionListener { release() }
                setOnErrorListener { _, _, _ -> release(); true }
                start()
            }
        }
    } catch (_: Exception) {
        // Audio playback not available
    }
}

/**
 * Extract minutes from step text using a regex pattern.
 * Matches patterns like "5 minutes", "10 min", "2 minutos", "3m", etc.
 * Returns the number of minutes, or null if no match found.
 */
private fun extractMinutes(text: String): Int? {
    val regex = Regex("""(\d+)\s*(minutos?|minutes?|min|m)\b""", RegexOption.IGNORE_CASE)
    val match = regex.find(text)
    return match?.groupValues?.get(1)?.toIntOrNull()
}
