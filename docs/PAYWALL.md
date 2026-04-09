# STAX Paywall Implementation Plan

## Pricing Model

| | Free | Premium |
|---|---|---|
| **Price** | $0 | **$4.99/month** · **$39/year** (save 35%) |
| **Trial** | — | 7-day free trial |
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
app/src/main/java/com/example/stax/
├── data/billing/
│   ├── BillingRepository.kt          # Google Play BillingClient wrapper
│   ├── SubscriptionState.kt          # Sealed class: Free / Trial / Premium / Expired
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
implementation("com.android.billingclient:billing-ktx:7.1.1")
```

In `AndroidManifest.xml`:

```xml
<uses-permission android:name="com.android.vending.BILLING" />
```

---

### Step 2 — Create SubscriptionState and EntitlementManager

**`SubscriptionState.kt`** — sealed interface representing the user's current plan:

- `Free` — default, with usage counters enforced
- `Trial` — 7-day trial with expiry timestamp
- `Premium` — active paid subscription
- `Expired` — lapsed subscription, grace period

**`EntitlementManager.kt`** — singleton, single source of truth:

- Persists state to `SharedPreferences` (`stax_subscription`)
- Exposes `val isPremium: StateFlow<Boolean>`
- Exposes `val subscriptionState: StateFlow<SubscriptionState>`
- Tracks daily scan count, total session count, per-session photo count
- Provides `fun checkLimit(feature: Feature): LimitResult` → `Allowed` / `ShowPaywall` / `SoftCap`
- Records `first_launch_timestamp` for trial window

---

### Step 3 — Create BillingRepository

Wraps Google Play `BillingClient`:

- Connect on app start (`MainActivity.onCreate`)
- Query two products:
  - `stax_premium_monthly` — $4.99/month
  - `stax_premium_annual` — $39/year
- Launch purchase flow from `PaywallScreen`
- Verify purchases via `queryPurchasesAsync` on each app start
- Acknowledge purchases (required by Google within 3 days)
- Update `EntitlementManager` on purchase, expiry, or refund
- Handle disconnection/reconnection gracefully
- Expose `fun launchPurchaseFlow(activity, productId)` and `val products: StateFlow<List<ProductDetails>>`

---

### Step 4 — Create PaywallScreen

Full-screen conversion page, dark-themed (consistent with app):

**Layout:**

1. **Hero** — Stax logo + "Unlock the Full Experience"
2. **Feature comparison** — two-column checklist (Free vs Premium) with check/lock icons
3. **Pricing cards** — two options:
   - Monthly: "$4.99/month"
   - Annual: "$39/year" with "BEST VALUE — Save 35%" badge
4. **Primary CTA** — "Start 7-Day Free Trial" (filled button, app primary color)
5. **Secondary links** — "Restore Purchase" · "Terms" · "Privacy"
6. **Background** — `StaxAmbientGradient`

**Navigation:** Accessible from any gate trigger or from About screen.

---

### Step 5 — Create PremiumGate composable

Reusable wrapper for any gated feature:

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
- Dismissible (but reappears next session)

---

### Step 6 — Wire gates into existing screens

| Screen | Gate Logic |
|---|---|
| **ScanScreen** | Increment daily scan counter on each scan. After 5/day (free), show paywall. Lock OpenAI toggle entirely for free users. |
| **DashboardScreen** | On "Add Session" confirm, check total active sessions. If ≥ 3 (free), show paywall instead of creating. |
| **SessionsScreen** | Same session-count gate on "Add Session" dialog. |
| **PhotoGalleryScreen** | On "Add Photo" (camera or gallery import), check photo count for that session. If ≥ 10 (free), show paywall. |
| **ChipConfigurationScreen** | Free users can only edit chip config for their first casino. Other casinos show lock icon + "Upgrade to configure all casinos." |
| **FindScreen** | Free users can have max 3 favorites. On 4th favorite tap, show upgrade toast/banner. |
| **AboutScreen** | Add "STAX Premium" section showing current plan + "Manage Subscription" / "Upgrade" button. |

---

### Step 7 — Add soft upsell touchpoints

Non-blocking prompts at natural moments (not dialogs — inline banners or toasts):

| Trigger | Message |
|---|---|
| 3rd session created | "You've used 3 of 3 free sessions. Unlock unlimited sessions." |
| 5th scan in a day | "You've used all 5 free scans today. Upgrade for unlimited." |
| Share action (free) | "Remove the STAX watermark with Premium." |
| About screen | Persistent "Free Plan" or "Premium ✓" badge with plan status. |
| After trial day 5 | "Your free trial ends in 2 days." (notification or in-app banner) |

---

### Step 8 — Navigation changes

In `AppNavigation.kt`:

- Add `Screen.Paywall` route
- `EntitlementManager` provided via `CompositionLocalProvider` at the `Scaffold` level
- Any screen can navigate to paywall: `navController.navigate(Screen.Paywall.route)`
- Paywall pops back on dismiss or successful purchase

---

### Step 9 — Trial logic

- On **first app launch**, record `first_launch_timestamp` in SharedPreferences
- 7-day trial starts automatically (no credit card required)
- During trial, all premium features are unlocked
- After 7 days, free-tier limits activate
- Trial countdown shown on paywall: "Your trial expires in X days"
- Trial state is one-time — cannot be reset without app reinstall (tied to device)

---

### Step 10 — Play Store setup and testing

- Create subscription products in **Google Play Console**:
  - `stax_premium_monthly` — $4.99/month, 7-day free trial
  - `stax_premium_annual` — $39/year, 7-day free trial
- Add license tester accounts for development
- Test purchase flows with `BillingClient` test SKUs
- Handle edge cases: refunds, account holds, grace periods, subscription pauses
- Verify on both new installs and upgrades

---

## Files Modified (existing)

| File | Change |
|---|---|
| `app/build.gradle.kts` | Add `billing-ktx` dependency |
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
| **1** | `SubscriptionState` + `EntitlementManager` | Foundation — works standalone with SharedPreferences and a debug toggle before billing is wired up |
| **2** | `PaywallScreen` UI | The conversion screen — can be tested visually immediately |
| **3** | `PremiumGate` + `UpgradeBanner` composables | Reusable gate components |
| **4** | Wire gates into each screen | One screen at a time, testable with debug toggle |
| **5** | `BillingRepository` + Google Play integration | Last — requires Play Console setup, most complex, but all gates already work |

---

## Revenue Projections

| Downloads | Convert (3%) | Paying Users | Monthly Rev ($5) | Annual Rev |
|---|---|---|---|---|
| 1,000 | 30 | 30 | $150 | $1,800 |
| 10,000 | 300 | 300 | $1,500 | $18,000 |
| 50,000 | 1,500 | 1,500 | $7,500 | $90,000 |
| 100,000 | 3,000 | 3,000 | $15,000 | $180,000 |

---

## Future Expansion (Pro+ Tier — $9.99/month)

Potential features for a higher tier (post-launch):

- Session bankroll tracking with analytics
- EV / win-rate calculations
- Export session history (CSV/PDF)
- Advanced AI chip recognition models
- Social features (leaderboards, "top stacks")
- Cloud backup / cross-device sync
