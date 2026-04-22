# STAX Android Paywall Implementation Plan

## Pricing Model

| | Free | Premium |
|---|---|---|
| **Price** | $0 | **$4.99/month** · **$39/year** (save 35%) |
| **Trial** | — | 7-day free trial (store-managed via Play Console) |
| **Target** | Casual users (80–90%) | Hobbyists + grinders (10–20%) |

---

## Feature Gating Map

| Feature | Free | Premium |
|---|---|---|
| Find Card Rooms (all tabs) | Unlimited | Unlimited |
| Casino Finder / Directions | Unlimited | Unlimited |
| Photo Gallery | 10 photos per session | Unlimited |
| Sessions | 3 active sessions | Unlimited |
| Chip Scanner (on-device MediaPipe) | 5 scans/day | Unlimited |
| AI Stack Counter (OpenAI) | Locked | Unlimited |
| Chip Configuration | 1 casino | All casinos |
| Share Stack Image | Watermarked "via STAX" | Clean export |
| Favorites / Home Casino | 3 favorites | Unlimited |

### Gating Philosophy

- **Find Card Rooms stays free** — it drives downloads and is the growth engine.
- **Gates hit at natural friction points** — users discover value before they hit a wall.
- **Limits are generous enough to hook, tight enough to convert.**

---

## Platform & Cost

| | Detail |
|---|---|
| **Required library** | Play Billing Library 8.3.0 |
| **Platform cut** | 15% flat on all auto-renewing subscriptions, from day 1 |
| **Server required** | No — all verification is client-side via the Billing Library |
| **Trial management** | Store-managed offer configured in Play Console |
| **Own payment processor** | Not permitted for digital goods on the Play Store |

At $4.99/month you net ~$4.24. At $39/year you net ~$33.15.

---

## Subscription State Model

Three states only. Trial is **not** a separate state — Play manages trial eligibility and billing automatically. During a trial the user is `Premium` with `isInTrial = true`.

```kotlin
// SubscriptionState.kt
sealed interface SubscriptionState {
    data object Free : SubscriptionState
    data class Premium(val isInTrial: Boolean, val expiryMs: Long) : SubscriptionState
    data class Expired(val gracePeriodEndsMs: Long?) : SubscriptionState
}
```

**Why no Trial state:**
- Google Play treats a trialing user as an active subscriber with full entitlements — identical access to a paid user.
- Store-managed trials enforce one-trial-per-user automatically; a local timestamp approach is bypassable via reinstall.
- The `isInTrial` flag on `Premium` is sufficient to drive "Your trial ends in X days" UI.

**Deriving `isInTrial` on Android:**
Inspect the active `PricingPhase` on the purchase — if `priceAmountMicros == 0` it is a trial phase.

```kotlin
val isInTrial = purchase.products
    .flatMap { productDetails.subscriptionOfferDetails ?: emptyList() }
    .any { offer -> offer.pricingPhases.pricingPhaseList.first().priceAmountMicros == 0L }
```

---

## Architecture

```
Google Play Billing ──► BillingRepository ──► EntitlementManager
                                                    │
                                        SubscriptionState (StateFlow)
                                                    │
                         ┌──────────────────────────┤
                         ▼                          ▼
                  PaywallScreen              Feature gates in:
                  (conversion UI)            - ScanScreen
                                             - PhotoGalleryScreen
                                             - SessionsScreen / DashboardScreen
                                             - ChipConfigurationScreen
                                             - FindScreen (favorites only)
```

---

## New Files

```
app/src/main/java/com/bitcraftapps/stax/
├── data/billing/
│   ├── BillingRepository.kt          # Google Play BillingClient wrapper
│   ├── SubscriptionState.kt          # Sealed interface: Free / Premium(isInTrial) / Expired
│   └── EntitlementManager.kt         # Single source of truth for "is premium?"
├── ui/screens/
│   └── PaywallScreen.kt              # Full paywall conversion screen
└── ui/composables/
    ├── PremiumGate.kt                # Reusable wrapper for gated features
    └── UpgradeBanner.kt              # Soft upsell banner for free users
```

---

## Implementation Steps

### Step 1 — Add Google Play Billing dependency

In `app/build.gradle.kts`:

```kotlin
implementation("com.android.billingclient:billing-ktx:8.3.0")
```

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.android.vending.BILLING" />
```

---

### Step 2 — Create SubscriptionState and EntitlementManager

**`SubscriptionState.kt`** — sealed interface (three states):

- `Free` — default, usage counters enforced
- `Premium(isInTrial: Boolean, expiryMs: Long)` — active subscription or active trial
- `Expired(gracePeriodEndsMs: Long?)` — lapsed subscription during grace period (user loses access)

**`EntitlementManager.kt`** — singleton, single source of truth:

- Persists state to `SharedPreferences` (`stax_subscription`) as a cache only — always verify against Play on launch
- Exposes `val isPremium: StateFlow<Boolean>` — `true` for `Premium` state regardless of `isInTrial`
- Exposes `val subscriptionState: StateFlow<SubscriptionState>`
- Tracks daily scan count, total session count, per-session photo count
- Provides `fun checkLimit(feature: Feature): LimitResult` → `Allowed` / `ShowPaywall` / `SoftCap`

---

### Step 3 — Create BillingRepository

Wraps Google Play `BillingClient`:

- Connect on app start (`MainActivity.onCreate`)
- Query two products:
  - `stax_premium_monthly` — $4.99/month
  - `stax_premium_annual` — $39/year
- On purchase, inspect `PricingPhase.priceAmountMicros`: if `0`, set `isInTrial = true` on the `Premium` state
- Verify purchases via `queryPurchasesAsync` on **every app start** (not just on purchase)
- Acknowledge purchases within 3 days (unacknowledged purchases are auto-refunded by Google)
- Update `EntitlementManager` on purchase, expiry, or refund
- Handle disconnection / reconnection gracefully
- Expose `fun launchPurchaseFlow(activity, productId, replacementParams?)` and `val products: StateFlow<List<ProductDetails>>`

---

### Step 4 — Upgrade / Downgrade

Android requires explicit `SubscriptionUpdateParams.ReplacementMode`. Pass the old purchase token when launching the billing flow.

| Transition | ReplacementMode | Behavior |
|---|---|---|
| Monthly → Annual | `CHARGE_PRORATED_PRICE` | Immediate switch; user charged prorated difference only |
| Annual → Monthly | `DEFERRED` | User keeps annual until expiry, then monthly starts |

**Simpler first-pass option:** Deep-link to the Play Store subscription management page. Google's UI handles all transitions — no `ReplacementMode` code required for initial release:

```kotlin
val uri = Uri.parse(
    "https://play.google.com/store/account/subscriptions" +
    "?sku=stax_premium_monthly&package=com.bitcraftapps.stax"
)
startActivity(Intent(Intent.ACTION_VIEW, uri))
```

---

### Step 5 — Create PaywallScreen

Full-screen conversion page, dark-themed (consistent with app):

1. **Hero** — Stax logo + "Unlock the Full Experience"
2. **Feature comparison** — two-column checklist (Free vs Premium) with check/lock icons
3. **Pricing cards** — two options; Annual card has "BEST VALUE — Save 35%" badge, selected by default
4. **Primary CTA** — "Start 7-Day Free Trial" (filled button, app primary color)
5. **Secondary links** — "Restore Purchase" · "Terms" · "Privacy"
6. **Trial countdown banner** — shown only when `state is Premium && isInTrial`: "Your trial ends in X days"

**Navigation:** Accessible from any gate trigger or from About screen.

---

### Step 6 — Create PremiumGate composable

```kotlin
@Composable
fun PremiumGate(
    entitlementManager: EntitlementManager,
    feature: Feature,
    onShowPaywall: () -> Unit,
    fallback: @Composable () -> Unit = { UpgradeBanner(onShowPaywall) },
    content: @Composable () -> Unit
)
```

- If the user has access → render `content`
- If gated → render `fallback` (banner with upgrade CTA or redirect to paywall)

**`UpgradeBanner`** — inline card composable:

- Icon + "Upgrade to Premium" + one-line benefit
- "Upgrade" button → navigates to `PaywallScreen`
- Dismissible (reappears next session)

---

### Step 7 — Wire gates into existing screens

| Screen | Gate Logic |
|---|---|
| **ScanScreen** | Increment daily scan counter on each scan. After 5/day (free), show paywall. Lock OpenAI toggle entirely for free users. |
| **DashboardScreen** | On "Add Session" confirm, check total active sessions. If ≥ 3 (free), show paywall instead of creating. |
| **SessionsScreen** | Same session-count gate on "Add Session" dialog. |
| **PhotoGalleryScreen** | On "Add Photo", check photo count for that session. If ≥ 10 (free), show paywall. |
| **ChipConfigurationScreen** | Free users can only edit chip config for their first casino. Other casinos show lock icon + "Upgrade to configure all casinos." |
| **FindScreen** | Free users can have max 3 favorites. On 4th favorite tap, show upgrade toast/banner. |
| **AboutScreen** | Add "STAX Premium" section showing current plan + "Manage Subscription" / "Upgrade" button. |

---

### Step 8 — Soft upsell touchpoints

| Trigger | Message |
|---|---|
| 3rd session created | "You've used 3 of 3 free sessions. Unlock unlimited sessions." |
| 5th scan in a day | "You've used all 5 free scans today. Upgrade for unlimited." |
| Share action (free) | "Remove the STAX watermark with Premium." |
| About screen | Persistent "Free Plan" or "Premium ✓" badge with plan status. |
| Trial day 5 | "Your free trial ends in 2 days." (in-app banner, not a notification) |

---

### Step 9 — Navigation changes

In `AppNavigation.kt`:

- Add `Screen.Paywall` route
- `EntitlementManager` provided via `CompositionLocalProvider` at the `Scaffold` level
- Any screen can navigate to paywall: `navController.navigate(Screen.Paywall.route)`
- Paywall pops back on dismiss or successful purchase

---

## Grace Period & Account Hold

When a renewal payment fails, Google enters a two-phase recovery window:

| Phase | Duration | User Access | Action |
|---|---|---|---|
| **Grace period** | 7 days (default, monthly) | Full access retained | Prompt user to fix payment |
| **Account hold** | ~53 days (default after grace) | Access blocked | Block premium features; show payment banner |
| **Expired** | After account hold | Permanently expired | Treat as Free |

State mapping: `SUBSCRIPTION_STATE_IN_GRACE_PERIOD` → `Premium` (keep access). `SUBSCRIPTION_STATE_ON_HOLD` → `Expired(gracePeriodEndsMs)` (block access).

---

## Play Console Setup Checklist

- [ ] Create subscription: `stax_premium_monthly` — $4.99/month
- [ ] Create subscription: `stax_premium_annual` — $39/year
- [ ] Add 7-day free trial as an **offer** on each base plan
- [ ] Add license tester accounts for development
- [ ] Test purchase flows with Play Billing test SKUs before production release
- [ ] Handle edge cases in testing: refunds, account holds, grace periods, subscription pauses

---

## Files Modified (existing)

| File | Change |
|---|---|
| `app/build.gradle.kts` | Update to `billing-ktx:8.3.0` |
| `AndroidManifest.xml` | Add `BILLING` permission |
| `MainActivity.kt` | Initialize `BillingRepository`, provide `EntitlementManager` via `CompositionLocal` |
| `AppNavigation.kt` | Add `Screen.Paywall` route, wrap content with entitlement provider |
| `ScanScreen.kt` | Gate scans (5/day free), lock AI toggle for free users |
| `DashboardScreen.kt` | Gate session creation (3 max free) |
| `SessionsScreen.kt` | Gate session creation (3 max free) |
| `PhotoGalleryScreen.kt` | Gate photo adds (10/session free) |
| `ChipConfigurationScreen.kt` | Gate multi-casino config |
| `FindScreen.kt` | Gate favorites count (3 max free) |
| `AboutScreen.kt` | Add subscription status section + manage/upgrade button |

---

## Implementation Order

| Phase | What | Why |
|---|---|---|
| **1** | `SubscriptionState` + `EntitlementManager` | Foundation — works standalone with a debug toggle before billing is wired up |
| **2** | `PaywallScreen` UI | Visually testable immediately |
| **3** | `PremiumGate` + `UpgradeBanner` composables | Reusable gate components |
| **4** | Wire gates into each screen | One screen at a time, testable with debug toggle |
| **5** | `BillingRepository` + Play Console setup | Most complex; requires Play Console config, but all gates already work |

---

## Revenue Projections

| Downloads | Convert (3%) | Paying Users | Monthly Rev (after 15%) | Annual Rev |
|---|---|---|---|---|
| 1,000 | 30 | 30 | $127 | $1,526 |
| 10,000 | 300 | 300 | $1,270 | $15,264 |
| 50,000 | 1,500 | 1,500 | $6,350 | $76,320 |
| 100,000 | 3,000 | 3,000 | $12,699 | $152,640 |

---

## Future Expansion (Pro+ Tier — $9.99/month)

- Session bankroll tracking with analytics
- EV / win-rate calculations
- Export session history (CSV/PDF)
- Advanced AI chip recognition models
- Social features (leaderboards, "top stacks")
- Cloud backup / cross-device sync
