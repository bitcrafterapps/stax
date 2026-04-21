package com.bitcraftapps.stax.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitcraftapps.stax.R
import com.bitcraftapps.stax.ui.theme.StaxHeaderGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val headerAlpha = remember { Animatable(0f) }
    val chipAlpha   = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Header and chip both fade in together
        launch { headerAlpha.animateTo(1f, animationSpec = tween(durationMillis = 700)) }
        launch { chipAlpha.animateTo(1f, animationSpec = tween(durationMillis = 1400)) }
        // Hold, then fade the chip out before navigating away
        delay(2000)
        chipAlpha.animateTo(0f, animationSpec = tween(durationMillis = 1200))
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Header — identical layout to the real screen headers ──────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(StaxHeaderGradient)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .alpha(headerAlpha.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "Stax logo",
                modifier = Modifier.size(88.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "STAX",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Stack it. Snap it. Track it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ── Centered logo chip — fades in then out ────────────────────────
        Image(
            painter = painterResource(id = R.drawable.ic_stax_logo),
            contentDescription = null,
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.Center)
                .alpha(chipAlpha.value)
        )
    }
}
