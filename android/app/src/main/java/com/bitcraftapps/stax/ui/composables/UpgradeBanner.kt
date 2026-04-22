package com.bitcraftapps.stax.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

@Composable
fun SessionUsageBar(
    used: Int,
    limit: Int,
    onUpgrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = (used.toFloat() / limit).coerceIn(0f, 1f)
    val atLimit = used >= limit
    val barColor = if (atLimit) MaterialTheme.colorScheme.error
                   else MaterialTheme.colorScheme.primary

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (atLimit) "Session limit reached  •  $used / $limit sessions"
                           else "Free plan  •  $used / $limit sessions used",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (atLimit) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onUpgrade,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Upgrade",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(4.dp)
                        .background(
                            color = barColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
