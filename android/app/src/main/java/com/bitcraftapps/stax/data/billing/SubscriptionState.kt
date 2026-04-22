package com.bitcraftapps.stax.data.billing

sealed interface SubscriptionState {
    data object Free : SubscriptionState
    data class Premium(val isInTrial: Boolean, val expiryMs: Long) : SubscriptionState
    data class Expired(val gracePeriodEndsMs: Long?) : SubscriptionState
}

enum class Feature {
    SCAN,
    AI_SCAN,
    SESSION_CREATE,
    PHOTO_ADD,
    CHIP_CONFIG,
    FAVORITES,
    SHARE_CLEAN
}

sealed interface LimitResult {
    data object Allowed : LimitResult
    data class SoftCap(val message: String) : LimitResult
    data class Blocked(val feature: Feature) : LimitResult
}

const val MAX_FREE_SCANS_PER_DAY = 5
const val MAX_FREE_SESSIONS = 3
const val MAX_FREE_PHOTOS_PER_SESSION = 10
const val MAX_FREE_FAVORITES = 3
const val MAX_FREE_CHIP_CONFIG_CASINOS = 1
