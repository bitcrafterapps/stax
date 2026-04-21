package com.bitcraftapps.stax.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val StaxHeaderGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0A0A0A),
        Color(0xFF1A1A1E)
    )
)

val StaxAmbientGradient = Brush.verticalGradient(
    colors = listOf(
        StaxGraphite,
        Color(0xFF0D0D12),
        Color(0xFF12101A),
        StaxPrimaryDim.copy(alpha = 0.35f)
    )
)

@Deprecated("Use StaxAmbientGradient", ReplaceWith("StaxAmbientGradient"))
val PurpleGradient = StaxAmbientGradient
