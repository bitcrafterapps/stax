package com.example.stax.data

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Category
import com.google.mediapipe.tasks.components.containers.Detection
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector

class ChipDetector(
    val context: Context,
    val modelPath: String = "model.tflite",
    val listener: DetectorListener
) {
    private var objectDetector: ObjectDetector? = null
    private val displayThreshold = 0.5f

    init {
        setupObjectDetector()
    }

    private fun buildOptions(delegate: Delegate?): ObjectDetector.ObjectDetectorOptions {
        val baseBuilder = BaseOptions.builder().setModelAssetPath(modelPath)
        if (delegate != null) {
            baseBuilder.setDelegate(delegate)
        }
        val baseOptions = baseBuilder.build()
        return ObjectDetector.ObjectDetectorOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.IMAGE)
            .setMaxResults(5)
            .setScoreThreshold(0f)
            .build()
    }

    private fun setupObjectDetector() {
        objectDetector = try {
            ObjectDetector.createFromOptions(context, buildOptions(Delegate.NPU))
        } catch (_: Exception) {
            try {
                ObjectDetector.createFromOptions(context, buildOptions(null))
            } catch (e: Exception) {
                listener.onError(e.message ?: "Unknown error setting up chip detector")
                null
            }
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (objectDetector == null) {
            setupObjectDetector()
        }
        val detector = objectDetector ?: return

        BitmapImageBuilder(image).build().use { mpImage ->
            val opts = ImageProcessingOptions.builder()
                .setRotationDegrees(imageRotation)
                .build()
            val result = detector.detect(mpImage, opts)
            val allResults = result.detections()
            val maxScore = allResults.maxOfOrNull { d ->
                d.categories().firstOrNull()?.score() ?: 0f
            }
            val filtered = allResults.filter { d ->
                (d.categories().firstOrNull()?.score() ?: 0f) >= displayThreshold
            }
            listener.onResults(
                filtered.map { it.toChipDetection() },
                mpImage.height,
                mpImage.width,
                if (filtered.isEmpty()) maxScore else null
            )
        }
    }

    private fun Detection.toChipDetection(): ChipDetection {
        val cats = categories().map { c: Category ->
            ChipCategory(label = c.categoryName(), score = c.score())
        }
        return ChipDetection(boundingBox = boundingBox(), categories = cats)
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: List<ChipDetection>?,
            imageHeight: Int,
            imageWidth: Int,
            maxScoreBelowThreshold: Float?
        )
    }
}
