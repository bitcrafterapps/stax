# STAX Paywall — Implementation Tasklist

Every checkbox below is a discrete unit of work that can be handed to a code-generation pass.
Complete them in order — each phase builds on the previous one.

---

## Phase 1 — Entitlement Foundation

### 1.1 Dependencies & Manifest

- [ ] Add `com.android.billingclient:billing-ktx:7.1.1` to `app/build.gradle.kts`
- [ ] Add `<uses-permission android:name="com.android.vending.BILLING" />` to `AndroidManifest.xml`
- [ ] Sync Gradle and confirm build succeeds

### 1.2 SubscriptionState.kt

> **New file:** `app/src/main/java/com/example/stax/data/billing/SubscriptionState.kt`

- [ ] Create `sealed interface SubscriptionState` with cases: `Free`, `Trial(expiresAt: Long)`, `Premium`, `Expired`
- [ ] Create `enum class Feature` with entries: `SCAN`, `AI_SCAN`, `SESSION_CREATE`, `PHOTO_ADD`, `CHIP_CONFIG`, `FAVORITES`, `SHARE_CLEAN`
- [ ] Create `sealed interface LimitResult` with cases: `Allowed`, `SoftCap(message: String)`, `Blocked(feature: Feature)`
- [ ] Define free-tier limit constants:
  - `MAX_FREE_SCANS_PER_DAY = 5`
  - `MAX_FREE_SESSIONS = 3`
  - `MAX_FREE_PHOTOS_PER_SESSION = 10`
  - `MAX_FREE_FAVORITES = 3`
  - `MAX_FREE_CHIP_CONFIG_CASINOS = 1`
  - `TRIAL_DURATION_MS = 7 * 24 * 60 * 60 * 1000L`

### 1.3 EntitlementManager.kt

> **New file:** `app/src/main/java/com/example/stax/data/billing/EntitlementManager.kt`

- [ ] Create `EntitlementManager(context: Context)` class
- [ ] Initialize `SharedPreferences` named `stax_subscription`
- [ ] Store and read `first_launch_timestamp` (set once on first construction)
- [ ] Store and read `subscription_state` (string: `free`, `trial`, `premium`, `expired`)
- [ ] Expose `val subscriptionState: StateFlow<SubscriptionState>` — computed from prefs
- [ ] Expose `val isPremium: StateFlow<Boolean>` — `true` when `Premium` or `Trial` (not expired)
- [ ] Implement `fun recordScan()` — increments daily scan counter (reset key: `scans_YYYY-MM-DD`)
- [ ] Implement `fun getDailyScans(): Int` — reads today's scan count
- [ ] Implement `fun checkLimit(feature: Feature, sessionId: Long?, sessionPhotoCount: Int?, totalSessions: Int?, favoritesCount: Int?): LimitResult`
  - `SCAN` → check `getDailyScans() < MAX_FREE_SCANS_PER_DAY`
  - `AI_SCAN` → always blocked for free
  - `SESSION_CREATE` → check `totalSessions < MAX_FREE_SESSIONS`
  - `PHOTO_ADD` → check `sessionPhotoCount < MAX_FREE_PHOTOS_PER_SESSION`
  - `CHIP_CONFIG` → check casino index against `MAX_FREE_CHIP_CONFIG_CASINOS`
  - `FAVORITES` → check `favoritesCount < MAX_FREE_FAVORITES`
  - `SHARE_CLEAN` → always blocked for free
  - All return `Allowed` when `isPremium` is `true`
- [ ] Implement `fun setPremium()` and `fun setExpired()` — update persisted state + flow
- [ ] Implement `fun getTrialDaysRemaining(): Int` — computes from `first_launch_timestamp`

### 1.4 CompositionLocal + MainActivity wiring

- [ ] Create `val LocalEntitlementManager = staticCompositionLocalOf<EntitlementManager> { error("...") }` (in `EntitlementManager.kt` or a new `CompositionLocals.kt`)
- [ ] In `MainActivity.kt`, instantiate `EntitlementManager(applicationContext)` with `remember`
- [ ] Wrap `AppNavigation` in `CompositionLocalProvider(LocalEntitlementManager provides entitlementManager)`
- [ ] Confirm build succeeds

### 1.5 Debug toggle (temporary)

- [ ] In `AboutScreen.kt`, add a temporary "Debug: Toggle Premium" switch that calls `entitlementManager.setPremium()` / resets to free
- [ ] Verify toggling updates `isPremium` StateFlow reactively

---

## Phase 2 — Paywall Screen

### 2.1 PaywallScreen.kt

> **New file:** `app/src/main/java/com/example/stax/ui/screens/PaywallScreen.kt`

- [ ] Create `@Composable fun PaywallScreen(onDismiss: () -> Unit, onSubscribe: (productId: String) -> Unit, onRestore: () -> Unit)`
- [ ] Background: `StaxAmbientGradient` filling the screen
- [ ] Hero section:
  - [ ] Stax logo (`R.drawable.ic_stax_logo`) at 80.dp
  - [ ] "Unlock the Full Experience" headline (`headlineMedium`, bold)
  - [ ] "All features. No limits." subhead (`bodyLarge`, `onSurfaceVariant`)
- [ ] Feature comparison list (scrollable `LazyColumn` or `Column`):
  - [ ] Each row: icon (check or lock) + feature name + Free label + Premium label
  - [ ] Features to list:
    - Sessions: "3 sessions" vs "Unlimited"
    - Photos: "10 per session" vs "Unlimited"
    - Chip Scanner: "5/day" vs "Unlimited"
    - AI Stack Counter: "Locked" vs "Included"
    - Chip Configuration: "1 casino" vs "All casinos"
    - Favorites: "3 max" vs "Unlimited"
    - Share: "Watermarked" vs "Clean export"
    - Find Card Rooms: "Free" vs "Free" (show it's always free)
- [ ] Pricing cards row (`Row` with two `Card`s):
  - [ ] Monthly card: "$4.99" + "/month" + "Billed monthly"
  - [ ] Annual card: "$39" + "/year" + "BEST VALUE" badge + "Save 35%" + "Just $3.25/mo"
  - [ ] Selected state toggles between cards (default: annual selected)
- [ ] Primary CTA button:
  - [ ] "Start 7-Day Free Trial" (full width, `MaterialTheme.colorScheme.primary`)
  - [ ] Calls `onSubscribe(selectedProductId)`
- [ ] Footer links row:
  - [ ] "Restore Purchase" text button → `onRestore()`
  - [ ] "Terms" text button (link or placeholder)
  - [ ] "Privacy" text button (link or placeholder)
- [ ] Close/dismiss: top-right `IconButton` with X icon → `onDismiss()`

### 2.2 Navigation route

- [ ] In `AppNavigation.kt`, add `object Paywall : Screen("paywall")` to sealed class
- [ ] Add `composable(Screen.Paywall.route)` block that renders `PaywallScreen`
  - `onDismiss = { navController.popBackStack() }`
  - `onSubscribe = { /* no-op for now, wired in Phase 5 */ }`
  - `onRestore = { /* no-op for now */ }`
- [ ] Do NOT add Paywall to `navItems` (it's not a tab)
- [ ] Confirm paywall is accessible via `navController.navigate(Screen.Paywall.route)` from About screen debug toggle
- [ ] Confirm build succeeds and paywall renders correctly

---

## Phase 3 — Gate Composables

### 3.1 PremiumGate.kt

> **New file:** `app/src/main/java/com/example/stax/ui/composables/PremiumGate.kt`

- [ ] Create `@Composable fun PremiumGate(feature: Feature, onShowPaywall: () -> Unit, extraParams: Map<String, Any> = emptyMap(), content: @Composable () -> Unit)`
- [ ] Read `LocalEntitlementManager.current`
- [ ] Collect `isPremium` as state
- [ ] If premium → render `content()`
- [ ] If not premium → call `entitlementManager.checkLimit(feature, ...)` using `extraParams`
  - `Allowed` → render `content()`
  - `SoftCap` → render `content()` + show `UpgradeBanner` below with the message
  - `Blocked` → render `UpgradeBanner` with "Upgrade to unlock" + call `onShowPaywall` on tap

### 3.2 UpgradeBanner.kt

> **New file:** `app/src/main/java/com/example/stax/ui/composables/UpgradeBanner.kt`

- [ ] Create `@Composable fun UpgradeBanner(message: String, onUpgrade: () -> Unit)`
- [ ] Layout: `Card` with `RoundedCornerShape(16.dp)`, surfaceContainerHigh background
  - [ ] Row: lock icon + message text (weight 1f) + "Upgrade" filled button
- [ ] Create `@Composable fun UpgradeDialog(feature: Feature, onUpgrade: () -> Unit, onDismiss: () -> Unit)`
  - [ ] `AlertDialog` with feature-specific headline, description, and "Upgrade" / "Maybe Later" buttons
- [ ] Confirm build succeeds

---

## Phase 4 — Wire Gates into Screens

### 4.1 ScanScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] On each scan action, call `entitlementManager.recordScan()`
- [ ] Before scan executes, check `entitlementManager.checkLimit(Feature.SCAN)`
  - If `Blocked` → show `UpgradeDialog` instead of scanning
- [ ] Gate the OpenAI toggle: if not premium, disable the toggle and show lock icon with tooltip "Premium feature"
- [ ] Confirm build succeeds

### 4.2 DashboardScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] In `AddSessionDialog` onConfirm, check `entitlementManager.checkLimit(Feature.SESSION_CREATE, totalSessions = casinoFolders.sumOf { it.sessionCount })`
  - If `Blocked` → navigate to `PaywallScreen` instead of creating session
- [ ] Confirm build succeeds

### 4.3 SessionsScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] In "Add New Session" dialog onConfirm, check `entitlementManager.checkLimit(Feature.SESSION_CREATE, totalSessions = sessions.size)`
  - If `Blocked` → navigate to `PaywallScreen` instead of creating session
- [ ] Confirm build succeeds

### 4.4 PhotoGalleryScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] On "Add Photo" (camera button or gallery import), check `entitlementManager.checkLimit(Feature.PHOTO_ADD, sessionPhotoCount = photos.size)`
  - If `Blocked` → show `UpgradeDialog` instead of opening camera/gallery
- [ ] Confirm build succeeds

### 4.5 ChipConfigurationScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] Collect `isPremium` as state
- [ ] If not premium and user selects a casino that is NOT their first casino:
  - Show lock overlay on the chip grid
  - Show `UpgradeBanner` with "Upgrade to configure all casinos"
  - Disable "Edit Chips" button
- [ ] Confirm build succeeds

### 4.6 FindScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] In `onToggleFavorite`, before adding a new favorite, check `entitlementManager.checkLimit(Feature.FAVORITES, favoritesCount = favorites.size)`
  - If `Blocked` → show Toast or Snackbar: "Upgrade to Premium for unlimited favorites"
  - Do NOT add the favorite
- [ ] Confirm build succeeds

### 4.7 AboutScreen.kt

- [ ] Read `LocalEntitlementManager.current`
- [ ] Collect `subscriptionState` as state
- [ ] Add "STAX Premium" section after the version number:
  - If `Free` → show "Free Plan" label + "Upgrade to Premium" button → navigates to PaywallScreen
  - If `Trial` → show "Trial — X days remaining" + "Upgrade Now" button
  - If `Premium` → show "Premium" badge with checkmark + "Manage Subscription" button (opens Play Store subscription management)
  - If `Expired` → show "Subscription Expired" + "Resubscribe" button → PaywallScreen
- [ ] Remove debug toggle (from Phase 1.5) once all gates are verified
- [ ] Confirm build succeeds

---

## Phase 5 — Google Play Billing Integration

### 5.1 BillingRepository.kt

> **New file:** `app/src/main/java/com/example/stax/data/billing/BillingRepository.kt`

- [ ] Create `BillingRepository(context: Context, entitlementManager: EntitlementManager)`
- [ ] Initialize `BillingClient` with `PurchasesUpdatedListener`
- [ ] Implement `fun startConnection()` — connect to Play, retry on disconnect
- [ ] Implement `fun queryProducts()` — query `stax_premium_monthly` and `stax_premium_annual` (type `SUBS`)
- [ ] Expose `val products: StateFlow<List<ProductDetails>>` — updated after query
- [ ] Implement `fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String)`
- [ ] In `PurchasesUpdatedListener.onPurchasesUpdated`:
  - On success → call `handlePurchase(purchase)` for each purchase
  - On cancel → no-op
  - On error → log / show Toast
- [ ] Implement `fun handlePurchase(purchase: Purchase)`:
  - Verify `purchase.purchaseState == PURCHASED`
  - If not acknowledged → call `acknowledgePurchase`
  - Call `entitlementManager.setPremium()`
- [ ] Implement `fun queryExistingPurchases()` — called on app start and on `startConnection` success
  - Query active subscriptions
  - If active → `entitlementManager.setPremium()`
  - If none → `entitlementManager.setExpired()` (only if was previously premium)
- [ ] Implement `fun restorePurchases()` — same as `queryExistingPurchases` but with user-facing feedback
- [ ] Implement `fun endConnection()` — called in `onDestroy`

### 5.2 Wire BillingRepository into MainActivity

- [ ] Instantiate `BillingRepository` in `MainActivity`
- [ ] Call `billingRepository.startConnection()` in `onCreate`
- [ ] Call `billingRepository.endConnection()` in `onDestroy`
- [ ] Provide `BillingRepository` via `CompositionLocal` or pass through navigation

### 5.3 Wire PaywallScreen to real billing

- [ ] Update `PaywallScreen` to accept `billingRepository` (or read from CompositionLocal)
- [ ] Collect `billingRepository.products` to display real prices from Play Store (fall back to hardcoded if not yet loaded)
- [ ] On "Start Free Trial" / subscribe button:
  - Get the selected `ProductDetails` (monthly or annual)
  - Get the offer token (with free trial if available)
  - Call `billingRepository.launchPurchaseFlow(activity, productDetails, offerToken)`
- [ ] On "Restore Purchase" → call `billingRepository.restorePurchases()`
- [ ] After successful purchase, pop paywall and show confirmation Toast

### 5.4 Handle lifecycle edge cases

- [ ] Re-query purchases on `onResume` (user may have managed subscription externally)
- [ ] Handle `ITEM_ALREADY_OWNED` response (user already subscribed)
- [ ] Handle network errors gracefully (don't gate features if billing status is unknown — default to last-known state)
- [ ] Grace period: if subscription lapses, allow 3-day grace before switching to `Expired`

---

## Phase 6 — Soft Upsells & Polish

### 6.1 Contextual upsell banners

- [ ] After 3rd session created (free user): show inline banner at top of SessionsScreen — "You've used 3 of 3 free sessions. Unlock unlimited."
- [ ] After 5th scan in a day (free user): show banner in ScanScreen — "You've used all 5 free scans today."
- [ ] On About screen: show persistent plan badge ("Free Plan" / "Premium")

### 6.2 Trial reminders

- [ ] If trial and ≤ 2 days remaining: show banner on DashboardScreen — "Your free trial ends in X days"
- [ ] If trial expired on this launch: show one-time dialog — "Your trial has ended. Upgrade to keep all features."

### 6.3 Share watermark

- [ ] When sharing a stack image as a free user, overlay small "Tracked with STAX" text in corner
- [ ] Premium users get clean export (no watermark)

### 6.4 Visual polish

- [ ] Add crown/star icon next to premium-only features in ChipConfigurationScreen casino list
- [ ] Add small "PRO" badge on locked features
- [ ] Animate paywall entry (slide up from bottom)
- [ ] Add confetti or success animation on purchase completion

---

## Phase 7 — Play Store Setup (non-code)

- [ ] Create `stax_premium_monthly` subscription in Google Play Console ($4.99/month, 7-day free trial)
- [ ] Create `stax_premium_annual` subscription in Google Play Console ($39/year, 7-day free trial)
- [ ] Add license tester email addresses for internal testing
- [ ] Test full purchase flow on a physical device with a test account
- [ ] Test restore flow on a second device / after reinstall
- [ ] Test subscription cancellation and verify app reverts to free tier
- [ ] Test trial expiry by adjusting device clock (or using Play test subscription renewals)
- [ ] Verify no crashes if Play Store is unavailable (sideloaded APK, no Google Play)

---

## Verification Checklist (end-to-end)

- [ ] Fresh install → trial starts → all features unlocked for 7 days
- [ ] Trial expires → free-tier limits activate on all gated features
- [ ] Free user hits scan limit → paywall shown → can subscribe
- [ ] Free user hits session limit → paywall shown → can subscribe
- [ ] Free user hits photo limit → paywall shown → can subscribe
- [ ] Free user hits favorites limit → toast shown
- [ ] Free user taps AI toggle → locked with premium label
- [ ] Free user on chip config → only first casino editable
- [ ] Paywall shows correct prices from Play Store
- [ ] Monthly purchase succeeds → gates unlock immediately
- [ ] Annual purchase succeeds → gates unlock immediately
- [ ] Restore purchase works on fresh install
- [ ] Subscription cancelled → reverts to free after grace period
- [ ] About screen shows correct plan status at all times
- [ ] Find Card Rooms works fully on free tier (no gates)
- [ ] App does not crash if billing service is unavailable
