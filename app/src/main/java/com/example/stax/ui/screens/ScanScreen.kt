package com.example.stax.ui.screens

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.stax.data.ChipDetector
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.tensorflow.lite.task.vision.detector.Detection
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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to use this feature.")
        }
    }
}

@Composable
fun CameraView() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var detections by remember { mutableStateOf<List<Detection>>(emptyList()) }
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    var showTrainDialog by remember { mutableStateOf(false) }
    var chipValue by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    val lastBitmap = remember { mutableStateOf<Bitmap?>(null) }
    var trainingSummary by remember { mutableStateOf("") }
    var infoMessage by remember { mutableStateOf("") }
    var openAiEnabled by remember { mutableStateOf(getOpenAiEnabled(context)) }

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

                    override fun onResults(results: List<Detection>?, height: Int, width: Int, maxScoreBelowThreshold: Float?) {
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
                                if (isScanning && bitmap != null) {
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
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (exc: Exception) {
                            Log.e("CameraView", "Use case binding failed", exc)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            DetectionOverlay(detections, imageHeight, imageWidth)
            ChipTotal(detections)

            if (isScanning) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(infoMessage.isNotEmpty()){
                    InfoMessage(message = infoMessage)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Use OpenAI", color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = openAiEnabled,
                        onCheckedChange = {
                            openAiEnabled = it
                            setOpenAiEnabled(context, it)
                        }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = {
                        isScanning = true
                    }) {
                        Text("Scan")
                    }
                    Button(onClick = { showTrainDialog = true }) {
                        Text("Train")
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(8.dp)
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun DetectionOverlay(detections: List<Detection>, imageHeight: Int, imageWidth: Int) {
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
                    color = Color.Red,
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
fun ChipTotal(detections: List<Detection>) {
    val totalValue = detections.sumOf {
        it.categories.firstOrNull()?.label?.toIntOrNull() ?: 0
    }
    val formattedTotal = NumberFormat.getCurrencyInstance(Locale.US).format(totalValue)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = "Total: $formattedTotal",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
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
        title = { Text("Enter Chip Value") },
        text = {
            OutlinedTextField(
                value = chipValue,
                onValueChange = onChipValueChange,
                label = { Text("Value") }
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 