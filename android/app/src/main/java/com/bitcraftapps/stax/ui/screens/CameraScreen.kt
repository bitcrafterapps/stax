package com.bitcraftapps.stax.ui.screens

import android.content.Context
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val rectHeight = canvasHeight / 5f
            val rectWidth = canvasWidth * 0.9f
            val rectTopLeft = androidx.compose.ui.geometry.Offset(
                (canvasWidth - rectWidth) / 2,
                (canvasHeight - rectHeight) / 2
            )
            val rectBottomRight = androidx.compose.ui.geometry.Offset(
                rectTopLeft.x + rectWidth,
                rectTopLeft.y + rectHeight
            )

            // Draw the rectangle
            drawRect(
                color = Color.White.copy(alpha = 0.7f),
                topLeft = rectTopLeft,
                size = androidx.compose.ui.geometry.Size(rectWidth, rectHeight),
                style = Stroke(width = 6f)
            )

            // Draw the plus sign in the middle
            val crosshairSize = 20.dp.toPx()
            val center = center
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = androidx.compose.ui.geometry.Offset(center.x - crosshairSize / 2, center.y),
                end = androidx.compose.ui.geometry.Offset(center.x + crosshairSize / 2, center.y),
                strokeWidth = 6f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.7f),
                start = androidx.compose.ui.geometry.Offset(center.x, center.y - crosshairSize / 2),
                end = androidx.compose.ui.geometry.Offset(center.x, center.y + crosshairSize / 2),
                strokeWidth = 6f
            )
        }

        IconButton(
            onClick = {
                takePhoto(
                    context = context,
                    imageCapture = imageCapture,
                    onImageCaptured = onImageCaptured,
                    onError = onError
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .size(80.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = "Take photo",
                tint = Color.White,
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, Color.White, CircleShape)
                    .padding(8.dp)
            )
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    val photoFile = context.createImageFile()
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
            }
            override fun onError(exc: ImageCaptureException) {
                onError(exc)
            }
        }
    )
}

fun Context.createImageFile(): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
    val imageFileName = "JPEG_" + timeStamp + "_"
    return File.createTempFile(
        imageFileName,
        ".jpg",
        externalCacheDir
    )
} 