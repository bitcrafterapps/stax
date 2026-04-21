package com.bitcraftapps.stax.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.ColorMatrix as AndroidColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private data class CropPreset(val label: String, val width: Int, val height: Int) {
    val aspectRatio: Float get() = width.toFloat() / height.toFloat()
}

private data class ResizePreset(val label: String, val maxDimension: Int?)

private val cropPresets = listOf(
    CropPreset("Original", 4, 3),
    CropPreset("Square", 1, 1),
    CropPreset("Portrait", 4, 5),
    CropPreset("Story", 9, 16)
)

private val resizePresets = listOf(
    ResizePreset("Original", null),
    ResizePreset("Large", 2048),
    ResizePreset("Medium", 1600),
    ResizePreset("Small", 1080)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditSheet(
    imagePath: String,
    initialCaption: String,
    onDismiss: () -> Unit,
    onSave: (Bitmap, String) -> Unit
) {
    val bitmap = remember(imagePath) { BitmapFactory.decodeFile(imagePath) }
    var cropPreset by remember { mutableStateOf(cropPresets.first()) }
    var resizePreset by remember { mutableStateOf(resizePresets.first()) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(1f) }
    var hue by remember { mutableFloatStateOf(0f) }
    var caption by remember(initialCaption) { mutableStateOf(initialCaption) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text("Edit Photo", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            if (bitmap == null) {
                Text("Unable to load this photo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(cropPreset.aspectRatio)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black)
                ) {
                    val widthPx = constraints.maxWidth.toFloat()
                    val heightPx = constraints.maxHeight.toFloat()
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.colorMatrix(
                            buildComposeColorMatrix(
                                brightness = brightness,
                                contrast = contrast,
                                hueDegrees = hue
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(cropPreset.aspectRatio)
                            .graphicsLayer {
                                scaleX = zoom
                                scaleY = zoom
                                translationX = offsetX * widthPx * 0.22f
                                translationY = offsetY * heightPx * 0.22f
                            }
                    )
                }

                Spacer(Modifier.height(18.dp))
                Text("Crop", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cropPresets.forEach { preset ->
                        FilterChip(
                            selected = cropPreset == preset,
                            onClick = { cropPreset = preset },
                            label = { Text(preset.label) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Resize", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    resizePresets.forEach { preset ->
                        FilterChip(
                            selected = resizePreset == preset,
                            onClick = { resizePreset = preset },
                            label = { Text(preset.label) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Zoom", style = MaterialTheme.typography.labelLarge)
                Slider(value = zoom, onValueChange = { zoom = it }, valueRange = 1f..3f)
                Text("Horizontal", style = MaterialTheme.typography.labelLarge)
                Slider(value = offsetX, onValueChange = { offsetX = it }, valueRange = -1f..1f)
                Text("Vertical", style = MaterialTheme.typography.labelLarge)
                Slider(value = offsetY, onValueChange = { offsetY = it }, valueRange = -1f..1f)
                Spacer(Modifier.height(16.dp))
                Text("Brightness", style = MaterialTheme.typography.labelLarge)
                Slider(value = brightness, onValueChange = { brightness = it }, valueRange = -0.4f..0.4f)
                Text("Contrast", style = MaterialTheme.typography.labelLarge)
                Slider(value = contrast, onValueChange = { contrast = it }, valueRange = 0.5f..1.8f)
                Text("Hue", style = MaterialTheme.typography.labelLarge)
                Slider(value = hue, onValueChange = { hue = it }, valueRange = -180f..180f)
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Caption") },
                minLines = 2,
                maxLines = 4
            )

            Spacer(Modifier.height(20.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = bitmap != null,
                    onClick = {
                        if (bitmap != null) {
                            onSave(
                                renderEditedBitmap(
                                    source = bitmap,
                                    cropPreset = cropPreset,
                                    zoom = zoom,
                                    offsetX = offsetX,
                                    offsetY = offsetY,
                                    resizePreset = resizePreset,
                                    brightness = brightness,
                                    contrast = contrast,
                                    hueDegrees = hue
                                ),
                                caption
                            )
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}

private fun renderEditedBitmap(
    source: Bitmap,
    cropPreset: CropPreset,
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    resizePreset: ResizePreset,
    brightness: Float,
    contrast: Float,
    hueDegrees: Float
): Bitmap {
    val aspect = cropPreset.aspectRatio
    var cropWidth = source.width.toFloat()
    var cropHeight = cropWidth / aspect
    if (cropHeight > source.height) {
        cropHeight = source.height.toFloat()
        cropWidth = cropHeight * aspect
    }

    val safeZoom = zoom.coerceIn(1f, 3f)
    cropWidth /= safeZoom
    cropHeight /= safeZoom

    val centerX = source.width / 2f
    val centerY = source.height / 2f
    val maxHorizontal = (source.width - cropWidth) / 2f
    val maxVertical = (source.height - cropHeight) / 2f
    val left = (centerX - cropWidth / 2f + offsetX.coerceIn(-1f, 1f) * maxHorizontal)
        .coerceIn(0f, source.width - cropWidth)
    val top = (centerY - cropHeight / 2f + offsetY.coerceIn(-1f, 1f) * maxVertical)
        .coerceIn(0f, source.height - cropHeight)

    val cropped = Bitmap.createBitmap(
        source,
        left.roundToInt(),
        top.roundToInt(),
        cropWidth.roundToInt().coerceAtLeast(1),
        cropHeight.roundToInt().coerceAtLeast(1)
    )

    val filtered = applyAndroidColorAdjustments(cropped, brightness, contrast, hueDegrees)

    val maxDimension = resizePreset.maxDimension ?: return filtered
    val outputWidth: Int
    val outputHeight: Int
    if (filtered.width >= filtered.height) {
        outputWidth = maxDimension
        outputHeight = (maxDimension / aspect).roundToInt().coerceAtLeast(1)
    } else {
        outputHeight = maxDimension
        outputWidth = (maxDimension * aspect).roundToInt().coerceAtLeast(1)
    }
    return Bitmap.createScaledBitmap(filtered, outputWidth, outputHeight, true)
}

private fun applyAndroidColorAdjustments(
    source: Bitmap,
    brightness: Float,
    contrast: Float,
    hueDegrees: Float
): Bitmap {
    val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        colorFilter = ColorMatrixColorFilter(buildAndroidColorMatrix(brightness, contrast, hueDegrees))
    }
    canvas.drawBitmap(source, 0f, 0f, paint)
    return output
}

private fun buildComposeColorMatrix(
    brightness: Float,
    contrast: Float,
    hueDegrees: Float
): ColorMatrix = ColorMatrix(buildColorMatrixValues(brightness, contrast, hueDegrees))

private fun buildAndroidColorMatrix(
    brightness: Float,
    contrast: Float,
    hueDegrees: Float
): AndroidColorMatrix = AndroidColorMatrix(buildColorMatrixValues(brightness, contrast, hueDegrees))

private fun buildColorMatrixValues(
    brightness: Float,
    contrast: Float,
    hueDegrees: Float
): FloatArray {
    val b = brightness * 255f
    val c = contrast
    val translate = (-0.5f * c + 0.5f) * 255f + b

    val contrastMatrix = floatArrayOf(
        c, 0f, 0f, 0f, translate,
        0f, c, 0f, 0f, translate,
        0f, 0f, c, 0f, translate,
        0f, 0f, 0f, 1f, 0f
    )

    val angle = Math.toRadians(hueDegrees.toDouble())
    val cosVal = cos(angle).toFloat()
    val sinVal = sin(angle).toFloat()
    val lumR = 0.213f
    val lumG = 0.715f
    val lumB = 0.072f

    val hueMatrix = floatArrayOf(
        lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
        lumG + cosVal * (-lumG) + sinVal * (-lumG),
        lumB + cosVal * (-lumB) + sinVal * (1 - lumB),
        0f, 0f,
        lumR + cosVal * (-lumR) + sinVal * 0.143f,
        lumG + cosVal * (1 - lumG) + sinVal * 0.140f,
        lumB + cosVal * (-lumB) + sinVal * -0.283f,
        0f, 0f,
        lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
        lumG + cosVal * (-lumG) + sinVal * lumG,
        lumB + cosVal * (1 - lumB) + sinVal * lumB,
        0f, 0f,
        0f, 0f, 0f, 1f, 0f
    )

    return multiplyColorMatrices(contrastMatrix, hueMatrix)
}

private fun multiplyColorMatrices(a: FloatArray, b: FloatArray): FloatArray {
    val result = FloatArray(20)
    for (row in 0 until 4) {
        for (col in 0 until 5) {
            result[row * 5 + col] =
                a[row * 5] * b[col] +
                a[row * 5 + 1] * b[5 + col] +
                a[row * 5 + 2] * b[10 + col] +
                a[row * 5 + 3] * b[15 + col] +
                if (col == 4) a[row * 5 + 4] else 0f
        }
    }
    return result
}
