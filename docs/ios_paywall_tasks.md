# STAX iOS Paywall — Implementation Tasklist

Every checkbox below is a discrete unit of work that can be handed to a code-generation pass.
Complete them in order — each phase builds on the previous one.

---

## Phase 1 — Entitlement Foundation

### 1.1 App Store Connect setup (do before writing code)

- [ ] **Enroll in App Store Small Business Program** at appstoreconnect.apple.com (cuts Apple's cut from 30% → 15%)
- [ ] Create subscription group: **"STAX Premium"**
- [ ] Add `stax_premium_monthly` ($4.99/month) — rank **lower** in the group
- [ ] Add `stax_premium_annual` ($39/year) — rank **higher** in the group (ranking drives automatic upgrade/downgrade behavior)
- [ ] Add 7-day free trial introductory offer to each product
- [ ] Create Sandbox test accounts for QA

### 1.2 SubscriptionState.swift

> **New file:** `ios/Stax/Billing/SubscriptionState.swift`

- [ ] Create `enum SubscriptionState` with three cases:
  ```swift
  case free
  case premium(isInTrial: Bool, expiryDate: Date)
  case expired
  ```
- [ ] Create `enum Feature` with cases: `scan`, `aiScan`, `sessionCreate`, `photoAdd`, `chipConfig`, `favorites`, `shareClean`
- [ ] Create `enum LimitResult` with cases: `allowed`, `softCap(message: String)`, `blocked(Feature)`
- [ ] Define free-tier limit constants (as a `struct FreeTierLimits` or top-level constants):
  - `maxScansPerDay = 5`
  - `maxSessions = 3`
  - `maxPhotosPerSession = 10`
  - `maxFavorites = 3`
  - `maxChipConfigCasinos = 1`

### 1.3 EntitlementManager.swift

> **New file:** `ios/Stax/Billing/EntitlementManager.swift`

- [ ] Create `@MainActor class EntitlementManager: ObservableObject`
- [ ] `@Published var isPremium: Bool = false`
- [ ] `@Published var isInTrial: Bool = false`
- [ ] `@Published var subscriptionState: SubscriptionState = .free`
- [ ] Usage counters persisted to `UserDefaults`:
  - [ ] `dailyScanCount: Int` — reset daily using key `scans_YYYY-MM-DD`
  - [ ] `activeSessionCount: Int`
  - [ ] `photoCounts: [String: Int]` — keyed by session ID
- [ ] Implement `func update(from transaction: Transaction)`:
  - Set `isInTrial = transaction.offerType == .introductoryOffer`
  - Set `subscriptionState = .premium(isInTrial:, expiryDate: transaction.expirationDate ?? .distantFuture)`
  - Set `isPremium = true`
- [ ] Implement `func setExpired()`:
  - Set `subscriptionState = .expired`, `isPremium = false`, `isInTrial = false`
- [ ] Implement `func setFree()`:
  - Set `subscriptionState = .free`, `isPremium = false`, `isInTrial = false`
- [ ] Implement `func recordScan()` — increments today's scan count in `UserDefaults`
- [ ] Implement `func getDailyScans() -> Int` — reads today's count
- [ ] Implement `func checkLimit(for feature: Feature, sessionPhotoCount: Int? = nil, totalSessions: Int? = nil, favoritesCount: Int? = nil, casinoIndex: Int? = nil) -> LimitResult`:
  - Returns `.allowed` immediately if `isPremium == true`
  - `.scan` → check `getDailyScans() < maxScansPerDay`
  - `.aiScan` → always `.blocked(.aiScan)`
  - `.sessionCreate` → check `totalSessions < maxSessions`
  - `.photoAdd` → check `sessionPhotoCount < maxPhotosPerSession`
  - `.chipConfig` → check `casinoIndex < maxChipConfigCasinos`
  - `.favorites` → check `favoritesCount < maxFavorites`
  - `.shareClean` → always `.blocked(.shareClean)`
- [ ] Implement `func getTrialDaysRemaining() -> Int` — computed from `expiryDate - Date.now`, only meaningful when `isInTrial == true`

### 1.4 Inject into App entry point

- [ ] In `StaxApp.swift`, add `@StateObject var entitlementManager = EntitlementManager()`
- [ ] Inject via `.environmentObject(entitlementManager)` on the root `ContentView`
- [ ] Add `.task { await checkCurrentEntitlements() }` on the root view — iterates `Transaction.currentEntitlements` and calls `entitlementManager.update(from:)`
- [ ] Confirm build succeeds

### 1.5 Debug toggle (temporary)

- [ ] In `AboutView.swift`, add a temporary "Debug: Toggle Premium" toggle that calls:
  - On: `entitlementManager.update(from:)` using a fake transaction (or bypass with a direct `isPremium = true` for debug builds only)
  - Off: `entitlementManager.setFree()`
- [ ] Gate the toggle with `#if DEBUG` so it never ships
- [ ] Verify toggling updates `@Published isPremium` reactively across views

---

## Phase 2 — Paywall Screen

### 2.1 PaywallView.swift

> **New file:** `ios/Stax/Screens/PaywallView.swift`

- [ ] Create `struct PaywallView: View` presented as a sheet or full-screen cover
- [ ] Background: dark gradient matching the app's ambient style
- [ ] Hero section:
  - [ ] Stax logo image at ~80pt
  - [ ] "Unlock the Full Experience" headline (`.title`, bold)
  - [ ] "All features. No limits." subhead (`.subheadline`, secondary color)
- [ ] Feature comparison list (`List` or `VStack`):
  - [ ] Each row: SF Symbol (checkmark or lock) + feature name + Free label + Premium label
  - [ ] Rows: Sessions (3 vs Unlimited), Photos (10/session vs Unlimited), Chip Scanner (5/day vs Unlimited), AI Stack Counter (Locked vs Included), Chip Config (1 casino vs All casinos), Favorites (3 max vs Unlimited), Share (Watermarked vs Clean export), Find Card Rooms (Free vs Free)
- [ ] Pricing cards (`HStack` with two `RoundedRectangle` cards):
  - [ ] Monthly: "$4.99" + "/month" + "Billed monthly"
  - [ ] Annual: "$39" + "/year" + "BEST VALUE" badge + "Save 35%" + "Just $3.25/mo"
  - [ ] `@State var selectedPlan: String` — default `"annual"`
- [ ] Check introductory offer eligibility on appear:
  ```swift
  let eligible = try? await product.subscription?.isEligibleForIntroOffer
  ```
  - If `true` → CTA: "Start 7-Day Free Trial"
  - If `false` → CTA: "Subscribe" (hide trial copy)
- [ ] Primary CTA button: full-width, app primary color, calls `purchase(selectedProduct)`
- [ ] Trial countdown banner: shown when `entitlementManager.isInTrial` — "Your trial ends in X days" using `entitlementManager.getTrialDaysRemaining()`
- [ ] Footer links: "Restore Purchases" · "Terms" · "Privacy"
- [ ] Dismiss button: top-right X → `dismiss()`

### 2.2 Present PaywallView from root

- [ ] In `StaxApp.swift` or `ContentView.swift`, add `@State var showPaywall = false`
- [ ] Add `.sheet(isPresented: $showPaywall) { PaywallView().environmentObject(entitlementManager) }`
- [ ] Expose a `showPaywall` binding or environment action so any child view can trigger the sheet
- [ ] Confirm paywall presents and dismisses correctly from the About screen debug area
- [ ] Confirm build succeeds

---

## Phase 3 — Gate Components

### 3.1 PremiumGate.swift

> **New file:** `ios/Stax/Components/PremiumGate.swift`

- [ ] Create `struct PremiumGate: ViewModifier`:
  ```swift
  @EnvironmentObject var entitlementManager: EntitlementManager
  let feature: Feature
  var onShowPaywall: () -> Void
  ```
- [ ] `body`: if `entitlementManager.isPremium` → render `content`; else check limit:
  - `.allowed` → render `content`
  - `.softCap(let msg)` → render `content` + `UpgradeBanner(message: msg)` below
  - `.blocked` → render `UpgradeBanner` only, `onUpgrade: onShowPaywall`
- [ ] Create `View` extension:
  ```swift
  func premiumGate(feature: Feature, onShowPaywall: @escaping () -> Void) -> some View
  ```
- [ ] Confirm build succeeds

### 3.2 UpgradeBanner.swift

> **New file:** `ios/Stax/Components/UpgradeBanner.swift`

- [ ] Create `struct UpgradeBanner: View` with `message: String` and `onUpgrade: () -> Void`:
  - [ ] `RoundedRectangle` card background (secondary fill)
  - [ ] `HStack`: lock SF Symbol + message text (`.frame(maxWidth: .infinity)`) + "Upgrade" `Button`
- [ ] Create `struct UpgradeAlert: View` (or use `.alert` modifier at call site) for feature-specific blocked dialogs:
  - Feature-specific headline and description
  - "Upgrade" → `onUpgrade()`
  - "Maybe Later" → dismiss
- [ ] Dismissible per session: use `@AppStorage("banner_dismissed_\(feature)")` flag, reset on app restart
- [ ] Confirm build succeeds

---

## Phase 4 — Wire Gates into Screens

### 4.1 ScanView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] On each scan tap, call `entitlementManager.recordScan()`
- [ ] Before scan executes, call `entitlementManager.checkLimit(for: .scan)`:
  - `.blocked` → present `PaywallView` sheet instead of scanning
- [ ] Gate the OpenAI toggle: if `!entitlementManager.isPremium`, disable toggle and overlay lock icon with "Premium feature" label
- [ ] Confirm build succeeds

### 4.2 DashboardView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] On "Add Session" confirm, call `entitlementManager.checkLimit(for: .sessionCreate, totalSessions: sessions.count)`:
  - `.blocked` → present `PaywallView` instead of creating session
- [ ] Confirm build succeeds

### 4.3 SessionsView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] On "Add Session" confirm, same gate as 4.2
- [ ] Confirm build succeeds

### 4.4 PhotoGalleryView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] On "Add Photo" tap, call `entitlementManager.checkLimit(for: .photoAdd, sessionPhotoCount: photos.count)`:
  - `.blocked` → present `PaywallView` instead of opening camera/picker
- [ ] Confirm build succeeds

### 4.5 ChipConfigurationView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] If `!entitlementManager.isPremium` and selected casino index ≥ `FreeTierLimits.maxChipConfigCasinos`:
  - [ ] Overlay lock icon on chip grid using `.overlay`
  - [ ] Show `UpgradeBanner(message: "Upgrade to configure all casinos")`
  - [ ] Disable edit controls (`.disabled(true)`)
- [ ] Confirm build succeeds

### 4.6 FindView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] On favorite toggle, call `entitlementManager.checkLimit(for: .favorites, favoritesCount: favorites.count)`:
  - `.blocked` → show inline toast/banner: "Upgrade to Premium for unlimited favorites"
  - Do NOT add the favorite
- [ ] Confirm build succeeds

### 4.7 AboutView.swift

- [ ] Add `@EnvironmentObject var entitlementManager: EntitlementManager`
- [ ] Add "STAX Premium" section:
  - `.free` → "Free Plan" label + "Upgrade to Premium" button → presents `PaywallView`
  - `.premium(isInTrial: true, _)` → "Trial — X days remaining" + "Upgrade Now" button → `PaywallView`
  - `.premium(isInTrial: false, _)` → "Premium ✓" badge + "Manage Subscription" button:
    ```swift
    try? await AppStore.showManageSubscriptions(in: windowScene)
    ```
  - `.expired` → "Subscription Expired" + "Resubscribe" button → `PaywallView`
- [ ] Remove debug toggle (Phase 1.5) before shipping
- [ ] Confirm build succeeds

---

## Phase 5 — StoreKit 2 Integration

### 5.1 SubscriptionManager.swift

> **New file:** `ios/Stax/Billing/SubscriptionManager.swift`

- [ ] Create `@MainActor class SubscriptionManager: ObservableObject`
- [ ] `static let productIds = ["stax_premium_monthly", "stax_premium_annual"]`
- [ ] `@Published var products: [Product] = []`
- [ ] `private var updateListenerTask: Task<Void, Error>?`
- [ ] In `init()`, start `updateListenerTask = listenForTransactions()`
- [ ] In `deinit`, cancel `updateListenerTask`
- [ ] Implement `func loadProducts() async`:
  - `products = try await Product.products(for: Self.productIds)` (sorted monthly first, annual second)
- [ ] Implement `func purchase(_ product: Product) async throws -> Transaction?`:
  - Call `product.purchase()`; handle `.success`, `.userCancelled`, `.pending`
  - On `.success`: verify JWS, call `await transaction.finish()`, return transaction
- [ ] Implement `func restorePurchases() async`:
  - Call `try? await AppStore.sync()`
- [ ] Implement `private func listenForTransactions() -> Task<Void, Error>`:
  - `Task.detached` loop over `Transaction.updates`
  - For each verified transaction: call `entitlementManager.update(from:)`, `await transaction.finish()`
- [ ] Implement `func checkCurrentEntitlements() async`:
  - Loop over `Transaction.currentEntitlements`
  - For each verified, non-expired subscription: call `entitlementManager.update(from:)`
  - If no active entitlements found: call `entitlementManager.setFree()` (only if was premium)

### 5.2 Inject SubscriptionManager into App

- [ ] Add `@StateObject var subscriptionManager = SubscriptionManager()` to `StaxApp.swift`
- [ ] Inject via `.environmentObject(subscriptionManager)` on root view
- [ ] Call `await subscriptionManager.loadProducts()` in root `.task { }` (alongside entitlement check)
- [ ] Confirm build succeeds

### 5.3 Wire PaywallView to real StoreKit products

- [ ] Update `PaywallView` to read `subscriptionManager.products` via `@EnvironmentObject`
- [ ] Display real prices from products; fall back to hardcoded strings if `products` is empty
- [ ] Check `isEligibleForIntroOffer` on each product on view appear; update CTA copy accordingly
- [ ] On subscribe button:
  - [ ] Get selected `Product` from `subscriptionManager.products`
  - [ ] Call `try await subscriptionManager.purchase(product)`
  - [ ] On success, dismiss paywall and show success confirmation
- [ ] On "Restore Purchases" → call `await subscriptionManager.restorePurchases()`

### 5.4 Handle edge cases

- [ ] Handle `StoreKitError.notEntitled` — user not signed in to App Store; prompt to sign in
- [ ] Handle `.pending` purchase state — show "Purchase Pending" message; do not grant access
- [ ] Handle network unavailability — do not revoke access; default to last-known `UserDefaults` state
- [ ] Verify `Transaction.currentEntitlements` is called on every foreground (`scenePhase == .active`)

---

## Phase 6 — Soft Upsells & Polish

### 6.1 Contextual upsell banners

- [ ] After 3rd session created (free): show inline banner in `SessionsView` — "You've used 3 of 3 free sessions. Unlock unlimited."
- [ ] After 5th scan in a day (free): show banner in `ScanView` — "You've used all 5 free scans today. Upgrade for unlimited."
- [ ] Persistent plan badge in `AboutView` for all states

### 6.2 Trial countdown

- [ ] When `isInTrial == true` and ≤ 2 days remaining: show banner in `DashboardView` — "Your free trial ends in X days"
- [ ] On first foreground entry after trial expiry: show one-time `Alert` — "Your trial has ended. Upgrade to keep all features."

### 6.3 Share watermark

- [ ] When free user exports a stack image: composite "Tracked with STAX" text in bottom corner using `UIGraphicsImageRenderer`
- [ ] Premium users export clean image with no watermark

### 6.4 Visual polish

- [ ] Add `lock.fill` SF Symbol overlay on premium-only casino rows in `ChipConfigurationView`
- [ ] Add small "PRO" badge label on locked feature areas
- [ ] Animate `PaywallView` entry with `.transition(.move(edge: .bottom))` inside the sheet
- [ ] Add success animation (checkmark with scale animation) on purchase completion

---

## Phase 7 — App Store Connect Setup (non-code)

- [ ] Confirm Small Business Program enrollment is active
- [ ] Confirm `stax_premium_monthly` is live in App Store Connect with correct pricing
- [ ] Confirm `stax_premium_annual` is live with correct pricing and ranking above monthly
- [ ] Confirm 7-day free trial introductory offer is attached to both products
- [ ] Test full purchase flow on device with Sandbox account — verify `isInTrial = true`
- [ ] Test introductory offer eligibility: re-test with same Sandbox account — verify `isEligibleForIntroOffer == false`
- [ ] Test upgrade: monthly → annual — verify immediate switch and prorated refund in Sandbox
- [ ] Test downgrade: annual → monthly — verify deferred to renewal date
- [ ] Test `AppStore.showManageSubscriptions()` on a physical device
- [ ] Test `AppStore.sync()` restore on a second device with the same Apple ID
- [ ] Test subscription cancellation — verify app reverts to free on next entitlement check
- [ ] Verify app does not crash or lock features when App Store is unreachable

---

## Verification Checklist (end-to-end)

- [ ] Fresh install → `subscriptionState == .free` → free-tier limits enforced on all gated views
- [ ] Subscribe monthly with Sandbox → `isPremium = true`, `isInTrial = true` → all features unlocked
- [ ] Trial period expires → free-tier limits reactivate
- [ ] Subscribe annual → `isPremium = true`, `isInTrial = true` → all features unlocked
- [ ] Annual trial expires → `Premium(isInTrial: false)` → features remain unlocked
- [ ] Free user hits scan limit → `PaywallView` presented
- [ ] Free user hits session limit → `PaywallView` presented
- [ ] Free user hits photo limit → `PaywallView` presented
- [ ] Free user hits favorites limit → banner shown, favorite not added
- [ ] Free user taps AI toggle → disabled with lock icon
- [ ] Free user views 2nd casino config → lock overlay shown
- [ ] `PaywallView` displays real prices from StoreKit when loaded
- [ ] `isEligibleForIntroOffer` correctly gates trial CTA copy
- [ ] Monthly purchase completes → gates unlock immediately
- [ ] Annual purchase completes → gates unlock immediately
- [ ] "Restore Purchases" works after reinstall on same Apple ID
- [ ] "Manage Subscription" sheet opens correctly via `AppStore.showManageSubscriptions()`
- [ ] Monthly → Annual upgrade is immediate with prorated refund (Sandbox)
- [ ] Annual → Monthly downgrade is deferred to renewal (Sandbox)
- [ ] Subscription cancelled → reverts to free on next `checkCurrentEntitlements()` call
- [ ] `AboutView` shows correct plan badge in all states (Free / Trial / Premium / Expired)
- [ ] Find Card Rooms works fully on free tier — no gates
- [ ] App does not crash or revoke access when App Store is unreachable
- [ ] No `#if DEBUG` code present in release build (debug toggle removed)
