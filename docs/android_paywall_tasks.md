# STAX Android Paywall — Implementation Tasklist

Every checkbox below is a discrete unit of work that can be handed to a code-generation pass.
Complete them in order — each phase builds on the previous one.

---

## Phase 1 — Entitlement Foundation

### 1.1 Dependencies & Manifest

- [ ] Add `com.android.billingclient:billing-ktx:8.3.0` to `app/build.gradle.kts`
- [ ] Add `<uses-permission android:name="com.android.vending.BILLING" />` to `AndroidManifest.xml`
- [ ] Sync Gradle and confirm build succeeds

### 1.2 SubscriptionState.kt

> **New file:** `app/src/main/java/com/bitcraftapps/stax/data/billing/SubscriptionState.kt`

- [ ] Create `sealed interface SubscriptionState` with three cases:
  - `data object Free`
  - `data class Premium(val isInTrial: Boolean, val expiryMs: Long)`
  - `data class Expired(val gracePeriodEndsMs: Long?)`
- [ ] Create `enum class Feature` with entries: `SCAN`, `AI_SCAN`, `SESSION_CREATE`, `PHOTO_ADD`, `CHIP_CONFIG`, `FAVORITES`, `SHARE_CLEAN`
- [ ] Create `sealed interface LimitResult` with cases: `Allowed`, `SoftCap(message: String)`, `Blocked(feature: Feature)`
- [ ] Define free-tier limit constants:
  - `MAX_FREE_SCANS_PER_DAY = 5`
  - `MAX_FREE_SESSIONS = 3`
  - `MAX_FREE_PHOTOS_PER_SESSION = 10`
  - `MAX_FREE_FAVORITES = 3`
  - `MAX_FREE_CHIP_CONFIG_CASINOS = 1`

### 1.3 EntitlementManager.kt

> **New file:** `app/src/main/java/com/bitcraftapps/stax/data/billing/EntitlementManager.kt`

- [ ] Create `EntitlementManager(context: Context)` class
- [ ] Initialize `SharedPreferences` named `stax_subscription` — used as a cache only, always verify against Play on launch
- [ ] Store and read `subscription_state` (string: `free`, `premium`, `expired`) and `is_in_trial` (boolean)
- [ ] Store and read `expiry_ms` (long) — epoch millis of subscription expiry
- [ ] Expose `val subscriptionState: StateFlow<SubscriptionState>` — computed from prefs, updated by BillingRepository
- [ ] Expose `val isPremium: StateFlow<Boolean>` — `true` when `Premium` regardless of `isInTrial`
- [ ] Implement `fun recordScan()` — increments daily scan counter (reset key: `scans_YYYY-MM-DD`)
- [ ] Implement `fun getDailyScans(): Int` — reads today's scan count
- [ ] Implement `fun checkLimit(feature: Feature, sessionPhotoCount: Int? = null, totalSessions: Int? = null, favoritesCount: Int? = null, casinoIndex: Int? = null): LimitResult`
  - Returns `Allowed` immediately if `isPremium.value == true`
  - `SCAN` → check `getDailyScans() < MAX_FREE_SCANS_PER_DAY`
  - `AI_SCAN` → always `Blocked`
  - `SESSION_CREATE` → check `totalSessions < MAX_FREE_SESSIONS`
  - `PHOTO_ADD` → check `sessionPhotoCount < MAX_FREE_PHOTOS_PER_SESSION`
  - `CHIP_CONFIG` → check `casinoIndex < MAX_FREE_CHIP_CONFIG_CASINOS`
  - `FAVORITES` → check `favoritesCount < MAX_FREE_FAVORITES`
  - `SHARE_CLEAN` → always `Blocked`
- [ ] Implement `fun setPremium(isInTrial: Boolean, expiryMs: Long)` — update persisted state + emit to flow
- [ ] Implement `fun setExpired(gracePeriodEndsMs: Long?)` — update persisted state + emit to flow
- [ ] Implement `fun setFree()` — reset to free state
- [ ] Implement `fun getTrialDaysRemaining(): Int` — computed from `expiryMs - System.currentTimeMillis()`, only meaningful when `isInTrial == true`

### 1.4 CompositionLocal + MainActivity wiring

- [ ] Create `val LocalEntitlementManager = staticCompositionLocalOf<EntitlementManager> { error("No EntitlementManager provided") }` (in `EntitlementManager.kt` or a new `CompositionLocals.kt`)
- [ ] In `MainActivity.kt`, instantiate `EntitlementManager(applicationContext)` with `remember`
- [ ] Wrap `AppNavigation` in `CompositionLocalProvider(LocalEntitlementManager provides entitlementManager)`
- [ ] Confirm build succeeds

### 1.5 Debug toggle (temporary)

- [ ] In `AboutScreen.kt`, add a temporary "Debug: Toggle Premium" switch that calls `entitlementManager.setPremium(isInTrial = false, expiryMs = Long.MAX_VALUE)` / `setFree()`
- [ ] Verify toggling updates `isPremium` StateFlow reactively across screens

---

## Phase 2 — Paywall Screen

### 2.1 PaywallScreen.kt

> **New file:** `app/src/main/java/com/bitcraftapps/stax/ui/screens/PaywallScreen.kt`

- [ ] Create `@Composable fun PaywallScreen(onDismiss: () -> Unit, onSubscribe: (productId: String) -> Unit, onRestore: () -> Unit)`
- [ ] Background: `StaxAmbientGradient` filling the screen
- [ ] Hero section:
  - [ ] Stax logo (`R.drawable.ic_stax_logo`) at 80.dp
  - [ ] "Unlock the Full Experience" headline (`headlineMedium`, bold)
  - [ ] "All features. No limits." subhead (`bodyLarge`, `onSurfaceVariant`)
- [ ] Feature comparison list (`LazyColumn` or `Column`):
  - [ ] Each row: icon (check or lock) + feature name + Free label + Premium label
  - [ ] Rows to include: Sessions (3 vs Unlimited), Photos (10/session vs Unlimited), Chip Scanner (5/day vs Unlimited), AI Stack Counter (Locked vs Included), Chip Config (1 casino vs All casinos), Favorites (3 max vs Unlimited), Share (Watermarked vs Clean export), Find Card Rooms (Free vs Free)
- [ ] Pricing cards row (`Row` with two `Card`s):
  - [ ] Monthly card: "$4.99" + "/month" + "Billed monthly"
  - [ ] Annual card: "$39" + "/year" + "BEST VALUE" badge + "Save 35%" + "Just $3.25/mo"
  - [ ] Selected state toggles between cards; default: annual selected
- [ ] Primary CTA button: "Start 7-Day Free Trial" (full width, primary color) → calls `onSubscribe(selectedProductId)`
- [ ] Trial countdown banner: shown only when `subscriptionState is Premium && isInTrial == true` → "Your trial ends in X days" using `entitlementManager.getTrialDaysRemaining()`
- [ ] Footer links: "Restore Purchase" → `onRestore()` · "Terms" · "Privacy"
- [ ] Close/dismiss: top-right `IconButton` with X icon → `onDismiss()`

### 2.2 Navigation route

- [ ] In `AppNavigation.kt`, add `object Paywall : Screen("paywall")` to sealed class
- [ ] Add `composable(Screen.Paywall.route)` block rendering `PaywallScreen`
  - `onDismiss = { navController.popBackStack() }`
  - `onSubscribe = { /* no-op — wired in Phase 5 */ }`
  - `onRestore = { /* no-op — wired in Phase 5 */ }`
- [ ] Do NOT add Paywall to `navItems` (not a tab)
- [ ] Confirm paywall is reachable via `navController.navigate(Screen.Paywall.route)` from the About screen debug toggle
- [ ] Confirm build succeeds and paywall renders correctly

---

## Phase 3 — Gate Composables

### 3.1 PremiumGate.kt

> **New file:** `app/src/main/java/com/bitcraftapps/stax/ui/composables/PremiumGate.kt`

- [ ] Create `@Composable fun PremiumGate(feature: Feature, onShowPaywall: () -> Unit, extraParams: Map<String, Any> = emptyMap(), content: @Composable () -> Unit)`
- [ ] Read `LocalEntitlementManager.current`
- [ ] Collect `isPremium` as state
- [ ] Call `entitlementManager.checkLimit(feature, ...)` using `extraParams` values
  - `Allowed` → render `content()`
  - `SoftCap` → render `content()` + show `UpgradeBanner` below with the message
  - `Blocked` → render `UpgradeBanner` only; call `onShowPaywall` on tap

### 3.2 UpgradeBanner.kt

> **New file:** `app/src/main/java/com/bitcraftapps/stax/ui/composables/UpgradeBanner.kt`

- [ ] Create `@Composable fun UpgradeBanner(message: String, onUpgrade: () -> Unit)`
  - [ ] `Card` with `RoundedCornerShape(16.dp)`, `surfaceContainerHigh` background
  - [ ] Row: lock icon + message text (weight 1f) + "Upgrade" filled button
- [ ] Create `@Composable fun UpgradeDialog(feature: Feature, onUpgrade: () -> Unit, onDismiss: () -> Unit)`
  - [ ] `AlertDialog` with feature-specific headline, description, and "Upgrade" / "Maybe Later" buttons
- [ ] Confirm build succeeds

---

## Phase 4 — Wire Gates into Screens

### 4.1 ScanScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] On each scan action, call `entitlementManager.recordScan()`
- [ ] Before scan executes, call `entitlementManager.checkLimit(Feature.SCAN)`:
  - `Blocked` → show `UpgradeDialog` instead of scanning
- [ ] Gate the OpenAI toggle: if not premium, disable toggle and show lock icon with "Premium feature" tooltip
- [ ] Confirm build succeeds

### 4.2 DashboardScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] In `AddSessionDialog` onConfirm, call `entitlementManager.checkLimit(Feature.SESSION_CREATE, totalSessions = currentSessionCount)`:
  - `Blocked` → navigate to `PaywallScreen` instead of creating session
- [ ] Confirm build succeeds

### 4.3 SessionsScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] In "Add New Session" dialog onConfirm, call `entitlementManager.checkLimit(Feature.SESSION_CREATE, totalSessions = sessions.size)`:
  - `Blocked` → navigate to `PaywallScreen`
- [ ] Confirm build succeeds

### 4.4 PhotoGalleryScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] On "Add Photo" (camera button or gallery import), call `entitlementManager.checkLimit(Feature.PHOTO_ADD, sessionPhotoCount = photos.size)`:
  - `Blocked` → show `UpgradeDialog` instead of opening camera/gallery
- [ ] Confirm build succeeds

### 4.5 ChipConfigurationScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] Collect `isPremium` as state
- [ ] If not premium and selected casino index ≥ `MAX_FREE_CHIP_CONFIG_CASINOS`:
  - [ ] Show lock overlay on chip grid
  - [ ] Show `UpgradeBanner`: "Upgrade to configure all casinos"
  - [ ] Disable "Edit Chips" button
- [ ] Confirm build succeeds

### 4.6 FindScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] In `onToggleFavorite`, before adding, call `entitlementManager.checkLimit(Feature.FAVORITES, favoritesCount = favorites.size)`:
  - `Blocked` → show Toast/Snackbar: "Upgrade to Premium for unlimited favorites"
  - Do NOT add the favorite
- [ ] Confirm build succeeds

### 4.7 AboutScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] Collect `subscriptionState` as state
- [ ] Add "STAX Premium" section after the version number:
  - `Free` → "Free Plan" label + "Upgrade to Premium" button → navigates to PaywallScreen
  - `Premium(isInTrial = true)` → "Trial — X days remaining" + "Upgrade Now" button → PaywallScreen
  - `Premium(isInTrial = false)` → "Premium ✓" badge + "Manage Subscription" button (deep-links to Play Store)
  - `Expired` → "Subscription Expired" + "Resubscribe" button → PaywallScreen
- [ ] Manage Subscription deep-link:
  ```kotlin
  val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=com.bitcraftapps.stax")
  startActivity(Intent(Intent.ACTION_VIEW, uri))
  ```
- [ ] Remove debug toggle (from Phase 1.5) once all gates are verified
- [ ] Confirm build succeeds

---

## Phase 5 — Google Play Billing Integration

### 5.1 BillingRepository.kt

> **New file:** `app/src/main/java/com/bitcraftapps/stax/data/billing/BillingRepository.kt`

- [ ] Create `BillingRepository(context: Context, entitlementManager: EntitlementManager)`
- [ ] Initialize `BillingClient` with `PurchasesUpdatedListener`
- [ ] Implement `fun startConnection()` — connect to Play, retry exponentially on `SERVICE_DISCONNECTED`
- [ ] Implement `fun queryProducts()` — query `stax_premium_monthly` and `stax_premium_annual` (type `SUBS`)
- [ ] Expose `val products: StateFlow<List<ProductDetails>>` — updated after successful query
- [ ] Implement `fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String, oldPurchaseToken: String? = null, replacementMode: Int? = null)`:
  - If `oldPurchaseToken != null`, build `SubscriptionUpdateParams` with given `replacementMode`
  - Call `BillingClient.launchBillingFlow`
- [ ] In `PurchasesUpdatedListener.onPurchasesUpdated`:
  - `OK` → call `handlePurchase(purchase)` for each purchase in the list
  - `USER_CANCELED` → no-op
  - `ITEM_ALREADY_OWNED` → call `queryExistingPurchases()` (user may already be subscribed)
  - Other errors → log and surface Toast
- [ ] Implement `fun handlePurchase(purchase: Purchase)`:
  - Verify `purchase.purchaseState == PURCHASED`
  - If not acknowledged → call `acknowledgePurchase`
  - Inspect first `PricingPhase.priceAmountMicros` to determine `isInTrial`
  - Call `entitlementManager.setPremium(isInTrial, expiryMs)` with expiry from purchase token or Long.MAX_VALUE
- [ ] Implement `fun queryExistingPurchases()` — called on app start and on `startConnection` success:
  - Query active subscriptions via `queryPurchasesAsync(QueryPurchasesParams SUBS)`
  - If active → `entitlementManager.setPremium(...)`
  - If `SUBSCRIPTION_STATE_IN_GRACE_PERIOD` → `entitlementManager.setPremium(...)` (keep access)
  - If `SUBSCRIPTION_STATE_ON_HOLD` or none → `entitlementManager.setExpired(...)`
- [ ] Implement `fun restorePurchases()` — same as `queryExistingPurchases` with user-facing feedback
- [ ] Implement `fun endConnection()` — call in `MainActivity.onDestroy`

### 5.2 Wire BillingRepository into MainActivity

- [ ] Instantiate `BillingRepository(applicationContext, entitlementManager)` in `MainActivity`
- [ ] Provide via `CompositionLocal` or pass through navigation callbacks
- [ ] Call `billingRepository.startConnection()` in `onCreate`
- [ ] Call `billingRepository.queryExistingPurchases()` in `onResume` (catches external subscription changes)
- [ ] Call `billingRepository.endConnection()` in `onDestroy`

### 5.3 Wire PaywallScreen to real billing

- [ ] Update `PaywallScreen` to read `billingRepository.products` via CompositionLocal or parameter
- [ ] Display real prices from Play Store; fall back to hardcoded strings if products not yet loaded
- [ ] On subscribe button tap:
  - [ ] Get selected `ProductDetails` (monthly or annual)
  - [ ] Get the offer token for the free trial offer (if user is trial-eligible) or the base plan offer
  - [ ] Call `billingRepository.launchPurchaseFlow(activity, productDetails, offerToken)`
- [ ] On "Restore Purchase" → call `billingRepository.restorePurchases()`
- [ ] After successful purchase, pop paywall and show confirmation Toast/Snackbar

### 5.4 Handle lifecycle edge cases

- [ ] Handle `BILLING_UNAVAILABLE` — do not lock features; default to last-known cached state
- [ ] Handle network errors during `queryProducts` gracefully (show hardcoded prices as fallback)
- [ ] Test `ITEM_ALREADY_OWNED` path on a device that reinstalled after subscribing
- [ ] Grace period: map `SUBSCRIPTION_STATE_IN_GRACE_PERIOD` → `Premium` (access retained; prompt to fix payment)
- [ ] Account hold: map `SUBSCRIPTION_STATE_ON_HOLD` → `Expired` (access blocked)

---

## Phase 6 — Soft Upsells & Polish

### 6.1 Contextual upsell banners

- [ ] After 3rd session created (free user): show inline banner at top of DashboardScreen — "You've used 3 of 3 free sessions. Unlock unlimited."
- [ ] After 5th scan in a day (free user): show banner in ScanScreen — "You've used all 5 free scans today. Upgrade for unlimited."
- [ ] On About screen: show persistent plan badge ("Free Plan" / "Trial — X days" / "Premium ✓")

### 6.2 Trial countdown

- [ ] When `isInTrial == true` and ≤ 2 days remaining: show banner on DashboardScreen — "Your free trial ends in X days"
- [ ] On first launch after trial expires: show one-time dialog — "Your trial has ended. Upgrade to keep all features."

### 6.3 Share watermark

- [ ] When free user shares a stack image: overlay small "Tracked with STAX" text in bottom corner
- [ ] Premium users get clean export with no watermark

### 6.4 Visual polish

- [ ] Add lock/crown icon next to premium-only casino entries in ChipConfigurationScreen
- [ ] Add small "PRO" badge on locked feature tiles
- [ ] Animate paywall entry (slide up from bottom)
- [ ] Add success animation (confetti or checkmark) on purchase completion

---

## Phase 7 — Play Store Setup (non-code)

- [ ] Create subscription `stax_premium_monthly` in Google Play Console ($4.99/month)
- [ ] Add 7-day free trial **offer** to `stax_premium_monthly` base plan
- [ ] Create subscription `stax_premium_annual` in Google Play Console ($39/year)
- [ ] Add 7-day free trial **offer** to `stax_premium_annual` base plan
- [ ] Add license tester email addresses for internal testing
- [ ] Test full purchase flow on a physical device with a license tester account
- [ ] Test restore flow on a second device / after reinstall
- [ ] Test subscription cancellation — verify app reverts to free after grace period
- [ ] Test grace period — verify access is retained during grace window
- [ ] Test account hold — verify premium features are blocked
- [ ] Verify no crashes when Play Store is unavailable (sideloaded APK / no Google Play)

---

## Verification Checklist (end-to-end)

- [ ] Fresh install → user is `Free` → free-tier limits enforced
- [ ] Subscribe monthly → `isPremium = true`, `isInTrial = true` → all features unlocked
- [ ] Trial period expires → free-tier limits reactivate
- [ ] Subscribe annual → `isPremium = true`, `isInTrial = true` → all features unlocked
- [ ] Annual trial expires → `Premium(isInTrial = false)` → features remain unlocked
- [ ] Free user hits scan limit → paywall shown → can subscribe
- [ ] Free user hits session limit → paywall shown → can subscribe
- [ ] Free user hits photo limit → paywall shown → can subscribe
- [ ] Free user hits favorites limit → toast shown, no favorite added
- [ ] Free user taps AI toggle → locked with premium label
- [ ] Free user on chip config for 2nd casino → lock overlay shown
- [ ] Paywall displays correct prices fetched from Play Store
- [ ] Monthly purchase succeeds → gates unlock immediately
- [ ] Annual purchase succeeds → gates unlock immediately
- [ ] Restore purchases works after reinstall
- [ ] Subscription cancelled → reverts to free after grace period ends
- [ ] About screen shows correct plan status in all states (Free / Trial / Premium / Expired)
- [ ] Find Card Rooms works fully on free tier — no gates
- [ ] App does not crash when billing service is unavailable
- [ ] Manage Subscription deep-link opens Play Store subscription page correctly
