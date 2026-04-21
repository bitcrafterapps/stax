package com.bitcraftapps.stax.data

import android.graphics.RectF

data class ChipCategory(val label: String?, val score: Float)

data class ChipDetection(
    val boundingBox: RectF,
    val categories: List<ChipCategory>
)
