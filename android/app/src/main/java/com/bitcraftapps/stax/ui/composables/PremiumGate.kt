package com.bitcraftapps.stax.ui.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bitcraftapps.stax.data.billing.Feature
import com.bitcraftapps.stax.data.billing.LimitResult
import com.bitcraftapps.stax.data.billing.LocalEntitlementManager

@Composable
fun PremiumGate(
    feature: Feature,
    onShowPaywall: () -> Unit,
    extraParams: Map<String, Any> = emptyMap(),
    content: @Composable () -> Unit
) {
    val entitlementManager = LocalEntitlementManager.current
    val isPremium by entitlementManager.isPremium.collectAsState()

    val sessionPhotoCount = (extraParams["sessionPhotoCount"] as? Int)
    val totalSessions = (extraParams["totalSessions"] as? Int)
    val favoritesCount = (extraParams["favoritesCount"] as? Int)
    val casinoIndex = (extraParams["casinoIndex"] as? Int)

    val limitResult = entitlementManager.checkLimit(
        feature = feature,
        sessionPhotoCount = sessionPhotoCount,
        totalSessions = totalSessions,
        favoritesCount = favoritesCount,
        casinoIndex = casinoIndex
    )

    when (limitResult) {
        is LimitResult.Allowed -> content()
        is LimitResult.SoftCap -> {
            Column {
                content()
                UpgradeBanner(
                    message = limitResult.message,
                    onUpgrade = onShowPaywall
                )
            }
        }
        is LimitResult.Blocked -> {
            UpgradeBanner(
                message = featureLockMessage(feature),
                onUpgrade = onShowPaywall
            )
        }
    }
}

private fun featureLockMessage(feature: Feature): String = when (feature) {
    Feature.SCAN -> "You've used all 5 free scans today. Upgrade for unlimited."
    Feature.AI_SCAN -> "AI Stack Counter is a Premium feature. Upgrade to unlock."
    Feature.SESSION_CREATE -> "You've reached the 3-session limit. Upgrade for unlimited."
    Feature.PHOTO_ADD -> "You've reached 10 photos for this session. Upgrade for unlimited."
    Feature.CHIP_CONFIG -> "Upgrade to configure all casinos."
    Feature.FAVORITES -> "You've reached 3 favorites. Upgrade for unlimited."
    Feature.SHARE_CLEAN -> "Clean export is a Premium feature. Upgrade to unlock."
}
