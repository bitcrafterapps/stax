package com.bitcraftapps.stax.data.billing

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

val LocalEntitlementManager = compositionLocalOf<EntitlementManager> {
    error("No EntitlementManager provided")
}

class EntitlementManager(context: Context) {

    private val prefs = context.getSharedPreferences("stax_subscription", Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(readStateFromPrefs())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _isPremium = MutableStateFlow(_subscriptionState.value is SubscriptionState.Premium)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private fun readStateFromPrefs(): SubscriptionState {
        val stateStr = prefs.getString("subscription_state", "free") ?: "free"
        return when (stateStr) {
            "premium" -> {
                val isInTrial = prefs.getBoolean("is_in_trial", false)
                val expiryMs = prefs.getLong("expiry_ms", Long.MAX_VALUE)
                SubscriptionState.Premium(isInTrial, expiryMs)
            }
            "expired" -> {
                val gracePeriodEndsMs = prefs.getLong("grace_period_ends_ms", -1L)
                SubscriptionState.Expired(if (gracePeriodEndsMs == -1L) null else gracePeriodEndsMs)
            }
            else -> SubscriptionState.Free
        }
    }

    private fun emitState(state: SubscriptionState) {
        _subscriptionState.value = state
        _isPremium.value = state is SubscriptionState.Premium
    }

    fun setPremium(isInTrial: Boolean, expiryMs: Long) {
        prefs.edit()
            .putString("subscription_state", "premium")
            .putBoolean("is_in_trial", isInTrial)
            .putLong("expiry_ms", expiryMs)
            .apply()
        emitState(SubscriptionState.Premium(isInTrial, expiryMs))
    }

    fun setExpired(gracePeriodEndsMs: Long?) {
        prefs.edit()
            .putString("subscription_state", "expired")
            .putLong("grace_period_ends_ms", gracePeriodEndsMs ?: -1L)
            .apply()
        emitState(SubscriptionState.Expired(gracePeriodEndsMs))
    }

    fun setFree() {
        prefs.edit()
            .putString("subscription_state", "free")
            .remove("is_in_trial")
            .remove("expiry_ms")
            .remove("grace_period_ends_ms")
            .apply()
        emitState(SubscriptionState.Free)
    }

    fun recordScan() {
        val key = scanKeyForToday()
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun getDailyScans(): Int = prefs.getInt(scanKeyForToday(), 0)

    private fun scanKeyForToday(): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "scans_$dateStr"
    }

    fun checkLimit(
        feature: Feature,
        sessionPhotoCount: Int? = null,
        totalSessions: Int? = null,
        favoritesCount: Int? = null,
        casinoIndex: Int? = null
    ): LimitResult {
        if (_isPremium.value) return LimitResult.Allowed

        return when (feature) {
            Feature.SCAN -> {
                val scans = getDailyScans()
                if (scans < MAX_FREE_SCANS_PER_DAY) LimitResult.Allowed
                else LimitResult.Blocked(Feature.SCAN)
            }
            Feature.AI_SCAN -> LimitResult.Blocked(Feature.AI_SCAN)
            Feature.SESSION_CREATE -> {
                val count = totalSessions ?: 0
                if (count < MAX_FREE_SESSIONS) LimitResult.Allowed
                else LimitResult.Blocked(Feature.SESSION_CREATE)
            }
            Feature.PHOTO_ADD -> {
                val count = sessionPhotoCount ?: 0
                if (count < MAX_FREE_PHOTOS_PER_SESSION) LimitResult.Allowed
                else LimitResult.Blocked(Feature.PHOTO_ADD)
            }
            Feature.CHIP_CONFIG -> {
                val idx = casinoIndex ?: 0
                if (idx < MAX_FREE_CHIP_CONFIG_CASINOS) LimitResult.Allowed
                else LimitResult.Blocked(Feature.CHIP_CONFIG)
            }
            Feature.FAVORITES -> {
                val count = favoritesCount ?: 0
                if (count < MAX_FREE_FAVORITES) LimitResult.Allowed
                else LimitResult.Blocked(Feature.FAVORITES)
            }
            Feature.SHARE_CLEAN -> LimitResult.Blocked(Feature.SHARE_CLEAN)
        }
    }

    fun getTrialDaysRemaining(): Int {
        val state = _subscriptionState.value
        if (state !is SubscriptionState.Premium || !state.isInTrial) return 0
        val remainingMs = state.expiryMs - System.currentTimeMillis()
        return TimeUnit.MILLISECONDS.toDays(remainingMs).toInt().coerceAtLeast(0)
    }
}
