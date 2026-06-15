package com.kitchencabinet.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchencabinet.data.Recipe
import com.kitchencabinet.ui.i18n.LocalStrings
import com.kitchencabinet.ui.theme.NewsreaderFontFamily
import com.kitchencabinet.viewmodel.RecipeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    recipeId: Int,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = viewModel(),
) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var recipe by remember { mutableStateOf<Recipe?>(null) }
    var copied by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var shareImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var sharingImage by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        recipe = viewModel.getById(recipeId)
    }

    val r = recipe

    LaunchedEffect(r) {
        if (r != null) {
            withContext(Dispatchers.Default) {
                qrBitmap = generateQrCode("https://kitchencabinet.app/recipe/${r.id}", 400)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.share.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = strings.nav.search)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (r == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else {
            val deepLink = "https://kitchencabinet.app/recipe/${r.id}"

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    r.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NewsreaderFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── QR Code ────────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(strings.share.qrCode, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        if (qrBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = qrBitmap!!.asImageBitmap(),
                                contentDescription = strings.share.shareImageDesc,
                                modifier = Modifier.size(180.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val path = File(context.cacheDir, "qr_${r.id}.png")
                                    FileOutputStream(path).use { qrBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", path)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, strings.share.shareQrChooser))
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Filled.Share, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(strings.share.shareQr)
                            }
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                }

                // ── Share as Image ──────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.share.shareImage, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            strings.share.shareImageDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        if (shareImageBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = shareImageBitmap!!.asImageBitmap(),
                                contentDescription = strings.share.shareImageDesc,
                                modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        Button(
                            onClick = {
                                sharingImage = true
                                scope.launch(Dispatchers.Default) {
                                    val bitmap = generateRecipeImage(context, r)
                                    shareImageBitmap = bitmap
                                    val file = File(context.cacheDir, "recipe_${r.id}.png")
                                    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/png"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    withContext(Dispatchers.Main) {
                                        sharingImage = false
                                        context.startActivity(Intent.createChooser(intent, strings.share.shareImageChooser))
                                    }
                                }
                            },
                            enabled = !sharingImage,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (sharingImage) {
                                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.Image, null, Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(if (sharingImage) strings.share.generating else strings.share.generateShare)
                        }
                    }
                }

                // ── Share as Text ───────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.share.shareText, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        val shareText = buildString {
                            appendLine(r.title)
                            appendLine()
                            appendLine("${strings.share.category} ${r.category}")
                            if (r.difficulty.isNotBlank()) appendLine("${strings.share.difficulty} ${r.difficulty}")
                            appendLine("${strings.share.time} ${r.timeMinutes} min")
                            appendLine("${strings.share.servings} ${r.servings}")
                            appendLine()
                            appendLine(strings.share.ingredients)
                            r.ingredients.forEach { appendLine("- ${it.quantity} ${it.name}".trimStart()) }
                            appendLine()
                            appendLine(strings.share.steps)
                            r.steps.forEachIndexed { i, step -> appendLine("${i + 1}. $step") }
                        }
                        Text(
                            shareText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, r.title)
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, strings.share.shareRecipeChooser))
                            },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Share, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(strings.share.shareVia)
                        }
                    }
                }

                // ── Share as Link ───────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(strings.share.shareLink, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                            fontFamily = NewsreaderFontFamily, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deepLink,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Recipe Link", deepLink))
                                    copied = true
                                }) {
                                    Icon(
                                        if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                                        contentDescription = strings.share.copied
                                    )
                                }
                            }
                        )
                        if (copied) {
                            Text(strings.share.copied, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun generateQrCode(content: String, size: Int): Bitmap? {
    return try {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (_: Exception) { null }
}

private fun generateRecipeImage(context: Context, recipe: Recipe): Bitmap {
    val width = 1080
    val pad = 80f
    val titleSize = 72f
    val sectionSize = 48f
    val bodySize = 36f
    val lineSpace = 52f
    val sectionSpace = 40f
    val imageHeight = 540f

    val titlePaint = Paint().apply {
        color = android.graphics.Color.parseColor("#9A4028")
        textSize = titleSize
        isFakeBoldText = true
        isAntiAlias = true
        typeface = Typeface.DEFAULT
    }
    val sectionPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#56642B")
        textSize = sectionSize
        isFakeBoldText = true
        isAntiAlias = true
    }
    val bodyPaint = Paint().apply {
        color = android.graphics.Color.DKGRAY
        textSize = bodySize
        isAntiAlias = true
    }

    // Try to load recipe image
    var headerImage: Bitmap? = null
    if (recipe.image.isNotBlank()) {
        try {
            val url = URL(recipe.image)
            val connection = url.openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            val input = connection.getInputStream()
            val raw = BitmapFactory.decodeStream(input)
            input.close()
            if (raw != null) {
                val cropW = raw.width
                val cropH = (cropW * 9f / 16f).toInt().coerceAtMost(raw.height)
                val offsetY = ((raw.height - cropH) / 2).coerceAtLeast(0)
                headerImage = Bitmap.createBitmap(raw, 0, offsetY, cropW, cropH)
                raw.recycle()
            }
        } catch (_: Exception) { }
    }

    val topOffset = if (headerImage != null) imageHeight + sectionSpace else 0f

    // Measure total height
    var y = topOffset + pad + titleSize + sectionSpace
    y += sectionSize + sectionSpace // Ingredients
    for (ing in recipe.ingredients) { y += lineSpace }
    y += sectionSize + sectionSpace // Steps
    for (step in recipe.steps) { y += lineSpace * 2 }
    y += sectionSize
    y += pad

    val height = y.toInt().coerceAtLeast(800)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    // Draw header image if available
    var py = pad
    if (headerImage != null) {
        val scaled = Bitmap.createScaledBitmap(headerImage, width, imageHeight.toInt(), true)
        canvas.drawBitmap(scaled, 0f, 0f, null)
        scaled.recycle()
        headerImage.recycle()
        py = imageHeight + sectionSpace + pad
    }

    // Title
    canvas.drawText(recipe.title, pad, py + titleSize, titlePaint)
    py += titleSize + sectionSpace

    // Ingredients
    canvas.drawText("INGREDIENTES", pad, py + sectionSize, sectionPaint)
    py += sectionSize + sectionSpace
    for (ing in recipe.ingredients) {
        val text = "${ing.quantity}  ${ing.name}"
        canvas.drawText(text, pad + 40f, py + bodySize, bodyPaint)
        py += lineSpace
    }

    py += sectionSpace

    // Steps
    canvas.drawText("PASOS", pad, py + sectionSize, sectionPaint)
    py += sectionSize + sectionSpace
    for ((i, step) in recipe.steps.withIndex()) {
        val text = "${i + 1}.  $step"
        // Word wrap long lines
        val words = text.split(" ")
        var line = ""
        for (word in words) {
            val test = if (line.isEmpty()) word else "$line $word"
            if (bodyPaint.measureText(test) > width - pad * 2) {
                canvas.drawText(line, pad + 40f, py + bodySize, bodyPaint)
                py += lineSpace
                line = word
            } else {
                line = test
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, pad + 40f, py + bodySize, bodyPaint)
            py += lineSpace
        }
        py += 8f
    }

    return bitmap
}
