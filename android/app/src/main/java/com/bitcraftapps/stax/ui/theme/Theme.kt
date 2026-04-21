package com.bitcraftapps.stax.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private val StaxDarkScheme = darkColorScheme(
    primary = StaxPrimary,
    onPrimary = StaxOnPrimary,
    primaryContainer = StaxPrimaryDim.copy(alpha = 0.35f),
    onPrimaryContainer = StaxOnPrimary,
    secondary = StaxSurfaceHigh,
    onSecondary = StaxOnSurface,
    secondaryContainer = StaxSurfaceHigh,
    onSecondaryContainer = StaxOnSurfaceVariant,
    tertiary = StaxTertiary,
    onTertiary = StaxGraphite,
    background = Color.Transparent,
    onBackground = StaxOnBackground,
    surface = StaxSurface,
    onSurface = StaxOnSurface,
    surfaceVariant = StaxSurfaceHigh,
    onSurfaceVariant = StaxOnSurfaceVariant,
    surfaceContainerLowest = StaxGraphite,
    surfaceContainerLow = StaxGraphiteElevated,
    surfaceContainer = StaxSurface,
    surfaceContainerHigh = StaxSurfaceHigh,
    surfaceContainerHighest = Color(0xFF2E2E36),
    outline = StaxOutline,
    outlineVariant = StaxOutlineMuted,
    error = StaxLoss,
    onError = Color.White
)

private val StaxLightScheme = lightColorScheme(
    primary = StaxPrimaryDim,
    onPrimary = Color.White,
    background = Color.Transparent,
    onBackground = Color(0xFF1C1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFE5E5EA),
    onSurfaceVariant = Color(0xFF636366),
    outline = Color(0xFFC7C7CC),
    error = StaxLoss,
    onError = Color.White
)

@Composable
fun StaxTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) StaxDarkScheme else StaxLightScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StaxShapes
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                content()
            }
        }
    }
}
