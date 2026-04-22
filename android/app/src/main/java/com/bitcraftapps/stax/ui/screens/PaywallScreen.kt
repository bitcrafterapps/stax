package com.bitcraftapps.stax.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bitcraftapps.stax.R
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager
import com.bitcraftapps.stax.data.billing.SubscriptionState
import com.bitcraftapps.stax.ui.theme.StaxAmbientGradient
import com.bitcraftapps.stax.ui.theme.StaxPrimary

private data class FeatureRow(
    val icon: ImageVector,
    val name: String,
    val freeLabel: String,
    val premiumLabel: String,
    val freeIsLocked: Boolean = false
)

private val featureRows = listOf(
    FeatureRow(Icons.Default.Star, "Sessions", "3 max", "Unlimited"),
    FeatureRow(Icons.Default.Star, "Photos per session", "10 max", "Unlimited"),
    FeatureRow(Icons.Default.Star, "Chip Scanner", "5/day", "Unlimited"),
    FeatureRow(Icons.Default.Lock, "AI Stack Counter", "Locked", "Included", freeIsLocked = true),
    FeatureRow(Icons.Default.Star, "Chip Config", "1 casino", "All casinos"),
    FeatureRow(Icons.Default.Star, "Favorites", "3 max", "Unlimited"),
    FeatureRow(Icons.Default.Lock, "Share", "Watermarked", "Clean export", freeIsLocked = true),
    FeatureRow(Icons.Default.Check, "Find Card Rooms", "Free", "Free")
)

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onSubscribe: (productId: String) -> Unit,
    onRestore: () -> Unit
) {
    val entitlementManager = LocalEntitlementManager.current
    val subscriptionState by entitlementManager.subscriptionState.collectAsState()
    var selectedPlan by remember { mutableStateOf("annual") }

    val isInTrial = subscriptionState is SubscriptionState.Premium &&
            (subscriptionState as SubscriptionState.Premium).isInTrial
    val trialDaysRemaining = if (isInTrial) entitlementManager.getTrialDaysRemaining() else 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StaxAmbientGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hero
            Image(
                painter = painterResource(id = R.drawable.ic_stax_logo),
                contentDescription = "Stax logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Unlock the Full Experience",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "All features. No limits.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // Trial banner
            if (isInTrial) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Your trial ends in $trialDaysRemaining days",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Feature comparison table
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.80f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            "Free",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(72.dp),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Premium",
                            style = MaterialTheme.typography.labelMedium,
                            color = StaxPrimary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(80.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    featureRows.forEachIndexed { index, row ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = row.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (row.freeIsLocked) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = row.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = row.freeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (row.freeIsLocked) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(72.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = row.premiumLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = StaxPrimary,
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Pricing cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Monthly card
                PricingCard(
                    selected = selectedPlan == "monthly",
                    modifier = Modifier.weight(1f),
                    onClick = { selectedPlan = "monthly" }
                ) {
                    Text(
                        "$4.99",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "/month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Billed monthly",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Annual card
                Box(modifier = Modifier.weight(1f)) {
                    PricingCard(
                        selected = selectedPlan == "annual",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { selectedPlan = "annual" }
                    ) {
                        Text(
                            "$39",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "/year",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Save 35% · $3.25/mo",
                            style = MaterialTheme.typography.labelSmall,
                            color = StaxPrimary,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                    // BEST VALUE badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StaxPrimary,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .graphicsLayer { translationY = -16f }
                    ) {
                        Text(
                            "BEST VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA button
            Button(
                onClick = {
                    val productId = if (selectedPlan == "annual") "stax_premium_annual"
                    else "stax_premium_monthly"
                    onSubscribe(productId)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StaxPrimary,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    "Start 7-Day Free Trial",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer links
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onRestore) {
                    Text(
                        "Restore Purchase",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "·",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                TextButton(onClick = { /* open terms */ }) {
                    Text(
                        "Terms",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "·",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                TextButton(onClick = { /* open privacy */ }) {
                    Text(
                        "Privacy",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PricingCard(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val borderColor = if (selected) StaxPrimary else Color.Transparent
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(2.dp, borderColor, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.85f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}
