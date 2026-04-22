# STAX iOS Paywall Implementation Plan

## Pricing Model

| | Free | Premium |
|---|---|---|
| **Price** | $0 | **$4.99/month** · **$39/year** (save 35%) |
| **Trial** | — | 7-day free trial (store-managed via App Store Connect) |
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
| **Required library** | StoreKit 2 — built into the iOS SDK, no additional dependencies |
| **Minimum iOS version** | iOS 15+ (StoreKit 2 requirement) |
| **Platform cut** | 30% standard → **15% via App Store Small Business Program** |
| **Action required** | **Enroll in Small Business Program** in App Store Connect before launch |
| **Server required** | No — `Transaction.currentEntitlements` handles all verification on-device |
| **Trial management** | Store-managed introductory offer configured in App Store Connect |
| **Own payment processor** | Not permitted for digital subscriptions on the App Store |

> **Action item — do before launch:** Enroll in the App Store Small Business Program at [appstoreconnect.apple.com](https://appstoreconnect.apple.com). Takes ~10 minutes. Cuts Apple's cut from 30% to 15% immediately. At $4.99/month you net ~$4.24; at $39/year you net ~$33.15.

---

## Subscription State Model

Three states only. Trial is **not** a separate state — Apple manages trial eligibility and billing automatically. During a trial the user is `Premium` with `isInTrial = true`.

```swift
// SubscriptionState.swift
enum SubscriptionState {
    case free
    case premium(isInTrial: Bool, expiryDate: Date)
    case expired
}
```

**Why no Trial state:**
- Apple treats a trialing user as an active subscriber with identical entitlements to a paid user.
- App Store Connect introductory offers enforce one-trial-per-user per subscription group automatically — no local tracking needed.
- The `isInTrial` associated value on `premium` is sufficient to drive "Your trial ends in X days" UI.

**Deriving `isInTrial` on iOS:**
Check the `offerType` on the current entitlement transaction:

```swift
let isInTrial = transaction.offerType == .introductoryOffer
```

---

## Architecture

```
StoreKit 2 ──► SubscriptionManager ──► EntitlementManager
                                               │
                                   isPremium (@Published Bool)
                                               │
                        ┌──────────────────────┤
                        ▼                      ▼
                 PaywallView             Feature gates in:
                 (SwiftUI)               - ScanView
                                         - PhotoGalleryView
                                         - SessionsView / DashboardView
                                         - ChipConfigurationView
                                         - FindView (favorites only)
```

---

## New Files

```
ios/Stax/
├── Billing/
│   ├── SubscriptionManager.swift     # StoreKit 2 wrapper — purchase, restore, transaction listener
│   ├── SubscriptionState.swift       # Enum: free / premium(isInTrial, expiryDate) / expired
│   └── EntitlementManager.swift      # ObservableObject, @Published isPremium + usage counters
├── Screens/
│   └── PaywallView.swift             # Full-screen SwiftUI paywall
└── Components/
    ├── PremiumGate.swift             # ViewModifier for gated content
    └── UpgradeBanner.swift           # Inline upsell banner view
```

---

## Implementation Steps

### Step 1 — Configure App Store Connect

Before writing any code, set up products in App Store Connect:

- Create subscription group: **"STAX Premium"**
- Add products and ranking (ranking determines upgrade vs. downgrade behavior):
  - `stax_premium_monthly` — $4.99/month — **ranked lower**
  - `stax_premium_annual` — $39/year — **ranked higher** (annual ranked above monthly = upgrade, enables automatic proration)
- Add a **free trial introductory offer** (7 days) to each product
- Create Sandbox test accounts for QA

> Ranking is critical: Apple uses subscription group ranking to determine if a tier change is an upgrade (immediate + prorated refund) or downgrade (deferred to renewal). Annual should be ranked higher than monthly.

---

### Step 2 — Create SubscriptionManager.swift

Wraps StoreKit 2. Start a background `Task` on app launch to listen for transaction updates.

```swift
@MainActor
class SubscriptionManager: ObservableObject {
    static let productIds = ["stax_premium_monthly", "stax_premium_annual"]
    private var updateListenerTask: Task<Void, Error>?

    init() {
        updateListenerTask = listenForTransactions()
    }

    deinit {
        updateListenerTask?.cancel()
    }

    // Load products from the App Store
    func loadProducts() async throws -> [Product] {
        return try await Product.products(for: Self.productIds)
    }

    // Purchase a product
    func purchase(_ product: Product) async throws -> Transaction? {
        let result = try await product.purchase()
        switch result {
        case .success(let verification):
            let transaction = try verification.payloadValue
            await transaction.finish()
            return transaction
        default:
            return nil
        }
    }

    // Restore purchases (prompts Apple ID sign-in if needed)
    func restorePurchases() async {
        try? await AppStore.sync()
    }

    // Background listener — catches renewals, expirations, refunds
    private func listenForTransactions() -> Task<Void, Error> {
        Task.detached {
            for await result in Transaction.updates {
                if let transaction = try? result.payloadValue {
                    await self.updateEntitlements(for: transaction)
                    await transaction.finish()
                }
            }
        }
    }
}
```

**Check current entitlements on every app launch** (in `App.init` or root view `.task`):

```swift
for await result in Transaction.currentEntitlements {
    if let transaction = try? result.payloadValue {
        let isInTrial = transaction.offerType == .introductoryOffer
        // update EntitlementManager
    }
}
```

---

### Step 3 — Create EntitlementManager.swift

Single source of truth for premium status and usage counters. Provided to the SwiftUI view hierarchy via `.environmentObject`.

```swift
@MainActor
class EntitlementManager: ObservableObject {
    @Published var isPremium: Bool = false
    @Published var isInTrial: Bool = false
    @Published var subscriptionState: SubscriptionState = .free

    // Usage counters (persisted to UserDefaults)
    @Published var dailyScanCount: Int = 0
    @Published var activeSessionCount: Int = 0
    // photoCounts: [sessionId: Int] stored in UserDefaults

    func update(from transaction: Transaction) {
        let inTrial = transaction.offerType == .introductoryOffer
        let expiry = transaction.expirationDate ?? .distantFuture
        subscriptionState = .premium(isInTrial: inTrial, expiryDate: expiry)
        isPremium = true
        isInTrial = inTrial
    }

    func setExpired() {
        subscriptionState = .expired
        isPremium = false
        isInTrial = false
    }

    func checkLimit(for feature: Feature) -> LimitResult {
        guard !isPremium else { return .allowed }
        // enforce per-feature free limits
    }
}
```

---

### Step 4 — Upgrade / Downgrade

Apple handles all proration and billing changes automatically — no custom code required for the transition itself. Surface Apple's native subscription management sheet with one call:

```swift
// In AboutView or SettingsView — "Manage Subscription" button
Button("Manage Subscription") {
    if let windowScene = UIApplication.shared.connectedScenes
        .first as? UIWindowScene {
        Task {
            try? await AppStore.showManageSubscriptions(in: windowScene)
        }
    }
}
```

Apple's sheet handles:
- Monthly → Annual: **immediate**, user gets prorated refund of unused monthly time
- Annual → Monthly: **deferred** to renewal date, user keeps annual until it expires
- Cancellation, pausing, and resubscription

No `ReplacementMode` equivalent exists on iOS — Apple does it all.

---

### Step 5 — Create PaywallView.swift

Full-screen SwiftUI paywall, dark-themed to match the app:

1. **Hero** — Stax logo + "Unlock the Full Experience"
2. **Feature comparison** — list of Free vs Premium rows with SF Symbols check/lock icons
3. **Pricing cards** — two options; Annual card has "BEST VALUE — Save 35%" badge, selected by default
4. **Primary CTA** — "Start 7-Day Free Trial" (filled button, app primary color)
5. **Secondary links** — "Restore Purchases" · "Terms" · "Privacy"
6. **Trial countdown banner** — shown when `entitlementManager.isInTrial`: "Your trial ends in X days"

**Check introductory offer eligibility before showing the trial CTA:**

```swift
let eligible = try? await product.subscription?.isEligibleForIntroOffer
// If false, change CTA to "Subscribe" and hide "7-Day Free Trial" copy
```

**Presentation:** Sheet or full-screen cover from any gate trigger, or from About screen.

---

### Step 6 — Create PremiumGate ViewModifier

```swift
struct PremiumGate: ViewModifier {
    @EnvironmentObject var entitlementManager: EntitlementManager
    let feature: Feature
    var onShowPaywall: () -> Void

    func body(content: Content) -> some View {
        if entitlementManager.isPremium {
            content
        } else {
            UpgradeBanner(feature: feature, onUpgrade: onShowPaywall)
        }
    }
}

extension View {
    func premiumGate(feature: Feature, onShowPaywall: @escaping () -> Void) -> some View {
        modifier(PremiumGate(feature: feature, onShowPaywall: onShowPaywall))
    }
}
```

**`UpgradeBanner`** — inline SwiftUI view:

- Icon + "Upgrade to Premium" + one-line benefit specific to the blocked feature
- "Upgrade" button → presents `PaywallView`
- Dismissible per session via `@AppStorage` flag

---

### Step 7 — Wire gates into existing screens

| Screen | Gate Logic |
|---|---|
| **ScanView** | Increment `dailyScanCount` on each scan. After 5/day (free), present `PaywallView`. Lock OpenAI toggle for free users with a lock icon overlay. |
| **DashboardView** | On "Add Session" confirm, check `activeSessionCount`. If ≥ 3 (free), present `PaywallView` instead of creating. |
| **SessionsView** | Same session-count gate on "Add Session". |
| **PhotoGalleryView** | On "Add Photo", check photo count for that session. If ≥ 10 (free), present `PaywallView`. |
| **ChipConfigurationView** | Free users can only edit their first casino's config. Other casinos show lock overlay + "Upgrade to configure all casinos." |
| **FindView** | Free users max 3 favorites. On 4th favorite tap, show upgrade banner. |
| **AboutView** | "STAX Premium" section showing current plan ("Free" / "Premium ✓" / "Trial — X days left") + "Manage Subscription" / "Upgrade" button. |

---

### Step 8 — Soft upsell touchpoints

| Trigger | Message |
|---|---|
| 3rd session created | "You've used 3 of 3 free sessions. Unlock unlimited sessions." |
| 5th scan in a day | "You've used all 5 free scans today. Upgrade for unlimited." |
| Share action (free) | "Remove the STAX watermark with Premium." |
| About screen | Persistent plan badge: "Free Plan" or "Premium ✓" |
| Trial day 5 | "Your free trial ends in 2 days." (in-app banner only — no push notification required) |

---

### Step 9 — Navigation / App entry point

In the root `App` struct or root `View`:

```swift
@StateObject var entitlementManager = EntitlementManager()
@StateObject var subscriptionManager = SubscriptionManager()
@State var showPaywall = false

var body: some Scene {
    WindowGroup {
        ContentView()
            .environmentObject(entitlementManager)
            .environmentObject(subscriptionManager)
            .sheet(isPresented: $showPaywall) {
                PaywallView()
                    .environmentObject(entitlementManager)
                    .environmentObject(subscriptionManager)
            }
            .task {
                await checkCurrentEntitlements()
            }
    }
}
```

---

## Restore Purchases

Apple strongly recommends a visible "Restore Purchases" button (some App Review situations require it). Implementation:

```swift
Button("Restore Purchases") {
    Task {
        await subscriptionManager.restorePurchases()
        // AppStore.sync() re-queries the App Store and repopulates
        // Transaction.currentEntitlements automatically
    }
}
```

`Transaction.currentEntitlements` is always up to date across all of the user's devices — no manual per-device restore logic needed.

---

## App Store Connect Setup Checklist

- [ ] **Enroll in App Store Small Business Program** (saves 15% — do before launch)
- [ ] Create subscription group: **"STAX Premium"**
- [ ] Add `stax_premium_monthly` ($4.99/month) — ranked **lower** in group
- [ ] Add `stax_premium_annual` ($39/year) — ranked **higher** in group
- [ ] Add 7-day free trial introductory offer to each product
- [ ] Create Sandbox test accounts for QA
- [ ] Test introductory offer eligibility check (`isEligibleForIntroOffer`) with a fresh Sandbox account
- [ ] Test upgrade flow (monthly → annual): verify immediate switch + prorated refund
- [ ] Test downgrade flow (annual → monthly): verify deferred to renewal
- [ ] Test `AppStore.showManageSubscriptions()` on device

---

## Files Modified (existing)

| File | Change |
|---|---|
| `StaxApp.swift` | Initialize `EntitlementManager` + `SubscriptionManager`, inject via `.environmentObject`, add `.task` to check entitlements on launch |
| `AboutView.swift` | Add "STAX Premium" section with plan status badge + "Manage Subscription" / "Upgrade" button |
| `ScanView.swift` | Gate scans (5/day free), lock OpenAI toggle for free users |
| `DashboardView.swift` | Gate session creation (3 max free) |
| `SessionsView.swift` | Gate session creation (3 max free) |
| `PhotoGalleryView.swift` | Gate photo adds (10/session free) |
| `ChipConfigurationView.swift` | Gate multi-casino config with lock overlay |
| `FindView.swift` | Gate favorites count (3 max free) |

---

## Implementation Order

| Phase | What | Why |
|---|---|---|
| **1** | `SubscriptionState` + `EntitlementManager` | Foundation — works standalone with a debug `isPremium` toggle before StoreKit is wired |
| **2** | `PaywallView` UI | Visually testable immediately with mock products |
| **3** | `PremiumGate` ViewModifier + `UpgradeBanner` | Reusable gate components — wire into screens one at a time |
| **4** | Wire gates into each screen | Testable with debug toggle throughout |
| **5** | `SubscriptionManager` + App Store Connect setup | Most complex; requires Sandbox accounts, but all gates already work |

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
