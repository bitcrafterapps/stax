package com.bitcraftapps.stax.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bitcraftapps.stax.data.billing.Feature
import com.bitcraftapps.stax.ui.theme.StaxPrimary

@Composable
fun UpgradeBanner(
    message: String,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = StaxPrimary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(
                    containerColor = StaxPrimary
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    "Upgrade",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun UpgradeDialog(
    feature: Feature,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    val (headline, description) = when (feature) {
        Feature.SCAN -> "Daily Scan Limit Reached" to
                "You've used all 5 free scans for today. Upgrade to Premium for unlimited scans every day."
        Feature.AI_SCAN -> "Premium Feature" to
                "AI Stack Counter uses cloud vision to count your chips instantly. Available with STAX Premium."
        Feature.SESSION_CREATE -> "Session Limit Reached" to
                "Free accounts can track up to 3 sessions. Upgrade to Premium for unlimited sessions."
        Feature.PHOTO_ADD -> "Photo Limit Reached" to
                "Free accounts can add up to 10 photos per session. Upgrade to Premium for unlimited photos."
        Feature.CHIP_CONFIG -> "Premium Feature" to
                "Customize chip colors and values for multiple casinos with STAX Premium."
        Feature.FAVORITES -> "Favorites Limit Reached" to
                "Free accounts can save up to 3 favorite card rooms. Upgrade for unlimited favorites."
        Feature.SHARE_CLEAN -> "Premium Feature" to
                "Export clean, watermark-free images with STAX Premium."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                headline,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onUpgrade,
                colors = ButtonDefaults.buttonColors(containerColor = StaxPrimary)
            ) {
                Text("Upgrade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Maybe Later")
            }
        }
    )
}
