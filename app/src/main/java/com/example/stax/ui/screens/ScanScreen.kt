package com.example.stax.ui.screens

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.stax.data.ChipDetection
import com.example.stax.data.ChipDetector
import com.example.stax.data.OpenAiService
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen() {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)

    LaunchedEffect(key1 = true) {
        cameraPermissionState.launchPermissionRequest()
    }

    if (cameraPermissionState.status.isGranted) {
        CameraView()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Camera access",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Allow camera access to scan chip stacks and capture training photos.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Continue")
            }
        }
    }
}

@Composable
fun CameraView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var detections by remember { mutableStateOf<List<ChipDetection>>(emptyList()) }
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    var showTrainDialog by remember { mutableStateOf(false) }
    var chipValue by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    val lastBitmap = remember { mutableStateOf<Bitmap?>(null) }
    var trainingSummary by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }
    var openAiEnabled by remember { mutableStateOf(getOpenAiEnabled(context)) }
    var chipTotalFromOpenAi by remember { mutableStateOf<String?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(trainingSummary) {
        if (trainingSummary.isNotEmpty()) {
            infoMessage = trainingSummary
            delay(3000)
            infoMessage = ""
            trainingSummary = ""
        }
    }

    LaunchedEffect(infoMessage) {
        if (infoMessage.isNotEmpty() && trainingSummary.isEmpty()) {
            delay(3000)
            infoMessage = ""
        }
    }

    fun updateTrainingSummary() {
        trainingSummary = getTrainingSummary(context)
    }

    val chipDetector by remember {
        mutableStateOf(
            ChipDetector(
                context = context,
                listener = object : ChipDetector.DetectorListener {
                    override fun onError(error: String) {
                        Log.e("CameraView", "Error: $error")
                        isScanning = false
                    }

                    override fun onResults(results: List<ChipDetection>?, height: Int, width: Int, maxScoreBelowThreshold: Float?) {
                        if (results.isNullOrEmpty()) {
                            val scorePercent = maxScoreBelowThreshold?.let { (it * 100).toInt() }
                            val scoreMessage = scorePercent?.let { " (max confidence: $it%)" } ?: ""
                            infoMessage = "Could not recognize chips. Try a different angle.$scoreMessage"
                        }
                        detections = results ?: emptyList()
                        imageHeight = height
                        imageWidth = width
                        isScanning = false
                    }
                }
            )
        )
    }

    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(cameraExecutor) { imageProxy ->
                                val bitmap = imageProxy.toBitmap()
                                lastBitmap.value = bitmap
                                if (isScanning && !openAiEnabled) {
                                    chipDetector.detect(bitmap, imageProxy.imageInfo.rotationDegrees)
                                }
                                imageProxy.close()
                            }
                        }

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        try {
                            cameraProvider.unbindAll()
                            camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraView", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView.setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) {
                            val factory = previewView.meteringPointFactory
                            val point = factory.createPoint(event.x, event.y)
                            val action = FocusMeteringAction.Builder(point, FocusMeteringAction.FLAG_AF).build()
                            camera?.cameraControl?.startFocusAndMetering(action)
                            return@setOnTouchListener true
                        }
                        return@setOnTouchListener false
                    }

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!openAiEnabled) {
                DetectionOverlay(detections, imageHeight, imageWidth)
            }

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 18.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ChipTotal(detections, openAiEnabled, chipTotalFromOpenAi)
                    Spacer(modifier = Modifier.height(12.dp))
                    if (infoMessage.isNotEmpty()) {
                        InfoMessage(message = infoMessage)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            "Cloud estimate (OpenAI)",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = openAiEnabled,
                            onCheckedChange = {
                                openAiEnabled = it
                                setOpenAiEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            )
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                isScanning = true
                                if (openAiEnabled) {
                                    val apiKey = getApiKey(context)
                                    if (apiKey.isNullOrEmpty()) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Add an API key in About → OpenAI settings.")
                                        }
                                        isScanning = false
                                        return@Button
                                    }
                                    lastBitmap.value?.let { bitmap ->
                                        coroutineScope.launch {
                                            try {
                                                val service = OpenAiService(apiKey)
                                                val result = service.getChipCount(bitmap)
                                                chipTotalFromOpenAi = result
                                            } catch (e: Exception) {
                                                Log.e("CameraView", "OpenAI call failed", e)
                                                chipTotalFromOpenAi = "Error: ${e.message}"
                                            } finally {
                                                isScanning = false
                                            }
                                        }
                                    } ?: run { isScanning = false }
                                }
                            },
                            enabled = !isScanning
                        ) {
                            Text("Scan")
                        }
                        OutlinedButton(
                            onClick = { showTrainDialog = true },
                            enabled = !openAiEnabled
                        ) {
                            Text("Train")
                        }
                    }
                }
            }

            if (showTrainDialog) {
                TrainDialog(
                    onDismiss = { showTrainDialog = false },
                    onConfirm = {
                        val bitmapToSave = lastBitmap.value
                        if (bitmapToSave != null && chipValue.isNotBlank()) {
                            val success = saveTrainingImage(context, bitmapToSave, chipValue)
                            val message = if (success) "Training image saved for value: $chipValue" else "Failed to save image."
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                            updateTrainingSummary()
                        }
                        showTrainDialog = false
                        chipValue = ""
                    },
                    chipValue = chipValue,
                    onChipValueChange = { chipValue = it.filter { c -> c.isDigit() } }
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun getApiKey(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("openai_api_key", null)
}

private fun getOpenAiEnabled(context: Context): Boolean {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getBoolean("openai_enabled", false)
}

private fun setOpenAiEnabled(context: Context, isEnabled: Boolean) {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putBoolean("openai_enabled", isEnabled)
        apply()
    }
}

fun saveTrainingImage(context: Context, bitmap: Bitmap, label: String): Boolean {
    return try {
        val trainingDir = File(context.cacheDir, "training_data")
        if (!trainingDir.exists()) {
            trainingDir.mkdirs()
        }

        val labelDir = File(trainingDir, label)
        if (!labelDir.exists()) {
            labelDir.mkdirs()
        }

        val fileName = "chip_${System.currentTimeMillis()}.jpg"
        val file = File(labelDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        }
        Log.d("saveTrainingImage", "Saved to ${file.absolutePath}")
        true
    } catch (e: Exception) {
        Log.e("saveTrainingImage", "Error saving training image", e)
        false
    }
}

fun getTrainingSummary(context: Context): String {
    val trainingDir = File(context.cacheDir, "training_data")
    if (!trainingDir.exists()) return "No training data yet."

    val summary = trainingDir.listFiles { file -> file.isDirectory }
        ?.joinToString(" | ") { labelDir ->
            val count = labelDir.listFiles()?.size ?: 0
            "${labelDir.name}s: $count"
        }
    return summary ?: "No training data yet."
}

@Composable
fun InfoMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        )
    }
}

@Composable
fun DetectionOverlay(detections: List<ChipDetection>, imageHeight: Int, imageWidth: Int) {
    val accent = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        detections.forEach { detection ->
            val boundingBox = detection.boundingBox
            if (imageHeight > 0 && imageWidth > 0) {
                val denormBoundingBox = RectF(
                    boundingBox.left * size.width / imageWidth,
                    boundingBox.top * size.height / imageHeight,
                    boundingBox.right * size.width / imageWidth,
                    boundingBox.bottom * size.height / imageHeight
                )
                drawRect(
                    color = accent,
                    topLeft = Offset(denormBoundingBox.left, denormBoundingBox.top),
                    size = Size(denormBoundingBox.width(), denormBoundingBox.height()),
                    style = Stroke(width = 2.dp.toPx())
                )

                detection.categories.firstOrNull()?.let { category ->
                    val scorePercent = (category.score * 100).toInt()
                    val textToShow = "$${category.label} ($scorePercent%)"

                    drawContext.canvas.nativeCanvas.drawText(
                        textToShow,
                        denormBoundingBox.left,
                        denormBoundingBox.top - 10,
                        android.graphics.Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 50f
                            setShadowLayer(10f, 0f, 0f, Color.Black.toArgb())
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChipTotal(detections: List<ChipDetection>, openAiEnabled: Boolean, openAiResult: String?) {
    val modelName = if (openAiEnabled) "OpenAI" else "On-device"

    val totalValue = if (openAiEnabled) {
        openAiResult
    } else {
        val sum = detections.sumOf {
            it.categories.firstOrNull()?.label?.toIntOrNull() ?: 0
        }
        NumberFormat.getCurrencyInstance(Locale.US).format(sum)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "Total: ${totalValue ?: "…"}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Source · $modelName",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TrainDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    chipValue: String,
    onChipValueChange: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Training label",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            OutlinedTextField(
                value = chipValue,
                onValueChange = onChipValueChange,
                label = { Text("Chip value") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
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