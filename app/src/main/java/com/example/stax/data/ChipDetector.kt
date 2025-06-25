package com.example.stax.data

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

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

    private fun setupObjectDetector() {
        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(0.0f)
            .setMaxResults(5)
        val baseOptionsBuilder = BaseOptions.builder().useNnapi()
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(context, modelPath, optionsBuilder.build())
        } catch (e: Exception) {
            listener.onError(e.message ?: "Unknown error setting up chip detector")
        }
    }

    fun detect(image: Bitmap, imageRotation: Int) {
        if (objectDetector == null) {
            setupObjectDetector()
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(Rot90Op(-imageRotation / 90))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        val allResults = objectDetector?.detect(tensorImage)
        val maxScore = allResults?.maxOfOrNull { it.categories.firstOrNull()?.score ?: 0f }

        val filteredResults = allResults?.filter {
            (it.categories.firstOrNull()?.score ?: 0f) >= displayThreshold
        }

        listener.onResults(
            filteredResults,
            tensorImage.height,
            tensorImage.width,
            if (filteredResults.isNullOrEmpty()) maxScore else null
        )
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: List<Detection>?,
            imageHeight: Int,
            imageWidth: Int,
            maxScoreBelowThreshold: Float?
        )
    }
} 