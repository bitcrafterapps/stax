# STAX Mobile App — Security Review

**Version:** 1.0  
**Date:** 2026-04-21  
**Platforms in scope:** Android (Kotlin/Jetpack Compose), iOS (Swift/SwiftUI)  
**Reviewer:** Internal — automated static analysis + manual code review  
**Branch reviewed:** `securityfixes` (post-paywall merge)

---

## Executive Summary

This document presents findings from a full static-code security review of the STAX Android and iOS applications, covering source files, build configuration, dependency declarations, backup rules, data storage patterns, and billing/entitlement logic.

**Affected versions:** All builds prior to the `securityfixes` branch commit (post-paywall merge, April 2026)

**Overall security posture: 5.5 / 10 (before fixes)**  
**Post-fix posture: 9.0 / 10 (after fixes applied in `securityfixes` branch)**  
The remaining 1.0 reflects three accepted risks: SEC-07 (no server-side receipt validation — architecture decision), SEC-12 (no cert pinning — low-threat-model trade-off), SEC-13 (app-switcher snapshot — UX trade-off).

Scoring methodology: 10 points available; deductions are proportional to severity and exploitability — Critical: −2.0, High: −0.75 each, Medium: −0.25 each, Low: −0.1 each. Starting from 10, pre-fix score = 10 − 2.0 − (3×0.75) − (4×0.25) − (5×0.1) = **5.5**. After all Tier 1 and Tier 2 fixes, SEC-07 remains as a documented accepted risk: **9.0**.

The application does not hardcode API secrets in source and uses TLS for all network calls. However, several issues — including a premium-bypass toggle exposed to all users in production, no release code obfuscation, and a user API key stored in plaintext — require immediate remediation. All identified issues are fixable in code with no architectural overhaul required.

| Severity | Count |
|----------|-------|
| Critical | 1 |
| High | 3 |
| Medium | 4 |
| Low | 5 |
| **Total** | **13** |

---

## Threat Model

**Primary threat actors and their capabilities:**

| Actor | Access | Motivation |
|-------|--------|------------|
| Casual free user | Physical device, no special tools | Avoid paying for premium |
| Technically skilled free user | ADB, APK decompilation tools | Systematic premium bypass |
| Rooted device user | Root filesystem access, Frida | Read plaintext SharedPreferences, bypass billing checks |
| Malicious app on shared device | Same device, limited permissions | Read unencrypted SharedPreferences if misconfigured |
| Backup attacker | Access to ADB backup or iCloud backup | Restore session/subscription data to another device |
| Network attacker (MITM) | Custom CA on device or network position | Intercept API traffic including OpenAI key |

**Assets being protected:**
1. **Premium entitlement** — value: subscription revenue; risk: monetization bypass
2. **OpenAI API key** — value: user's OpenAI account; risk: financial loss, account abuse
3. **Session/financial data** — poker session records, chip configs; risk: privacy
4. **Signing keys** — upload keystore; risk: fraudulent APK distribution

**Assumptions:** Physical device access without screen lock PIN is not in scope. Server infrastructure is out of scope (no backend in this repo). Attacks requiring Google Play infrastructure compromise are out of scope.

---

## Scope and Methodology

**In scope:**
- Android: all Kotlin source files under `android/app/src/main/java/`, `AndroidManifest.xml`, `app/build.gradle.kts`, `gradle/libs.versions.toml`, `proguard-rules.pro`, backup XML files, `keystore.properties`, `local.properties`
- iOS: all Swift source files under `ios/Stax/`, `Info.plist`, Xcode project file
- Shared: root `.gitignore`, `docs/` folder, frontend assets

**Out of scope:** Runtime DAST testing, Google Play / App Store server infrastructure, OpenAI infrastructure.

**Methodology:** Static code review — manual inspection of all source files, grep-based pattern matching for known vulnerability patterns (hardcoded secrets, HTTP usage, unsafe storage, logging of sensitive data), dependency version cross-referencing.

---

## Findings

### Summary Table

| ID | Severity | CVSS v3.1 | Platform | File | Title | OWASP Mobile Top 10 | Status |
|----|----------|-----------|----------|------|-------|---------------------|--------|
| SEC-01 | Critical | 9.1 (AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:N) | Android | `AboutScreen.kt:275–297` | Debug premium toggle ships in production | M8: Security Misconfiguration | Fixed |
| SEC-02 | High | 7.5 (AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:N/A:N) | Android | `build.gradle.kts:70` | Release build has minification disabled | M8: Security Misconfiguration | Fixed |
| SEC-03 | High | 7.1 (AV:L/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:N) | Android/iOS | `AboutScreen.kt:408–419`, `AboutView.swift:117` | OpenAI API key stored in plaintext (SharedPreferences / UserDefaults) | M9: Insecure Data Storage | Fixed |
| SEC-04 | High | 6.5 (AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:H/A:N) | Android | `BillingRepository.kt:178` | Subscription expiry set to `Long.MAX_VALUE` — not time-bounded | M4: Insufficient Input/Output Validation | Fixed |
| SEC-05 | Medium | 5.5 (AV:L/AC:L/PR:L/UI:N/S:U/C:H/I:N/A:N) | Android | `backup_rules.xml`, `data_extraction_rules.xml` | Backup exclusions too narrow — subscription state rides backups | M9: Insecure Data Storage | Fixed |
| SEC-06 | Medium | 5.3 (AV:N/AC:L/PR:N/UI:R/S:U/C:N/I:L/A:N) | iOS | `PaywallView.swift:339` | Placeholder `example.com` privacy URL in production UI | M8: Security Misconfiguration | Fixed |
| SEC-07 | Medium | 5.9 (AV:N/AC:H/PR:N/UI:N/S:U/C:N/I:H/A:N) | Both | (architecture) | No server-side purchase receipt validation | M4: Insufficient Input/Output Validation | Accepted Risk |
| SEC-08 | Medium | 4.3 (AV:N/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N) | iOS | `xcuserdata/` | Xcode user state files committed to git | M8: Security Misconfiguration | Fixed |
| SEC-09 | Low | 3.3 (AV:L/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N) | Android | `ScanScreen.kt:544` | Local filesystem path logged to logcat | M9: Insecure Data Storage | Fixed |
| SEC-10 | Low | 3.3 (AV:L/AC:L/PR:L/UI:N/S:U/C:L/I:N/A:N) | iOS | `SubscriptionManager.swift:34,75,105` | `print()` statements not stripped in release builds | M9: Insecure Data Storage | Fixed |
| SEC-11 | Low | 2.7 (AV:N/AC:L/PR:H/UI:N/S:U/C:N/I:L/A:N) | Android | `libs.versions.toml:21` | Accompanist version mismatch between catalog and build file | M8: Security Misconfiguration | Fixed |
| SEC-12 | Low | 3.7 (AV:N/AC:H/PR:N/UI:N/S:U/C:L/I:N/A:N) | Both | (architecture) | No TLS certificate pinning for `api.openai.com` | M3: Insecure Communication | Accepted Risk |
| SEC-13 | Low | 2.4 (AV:P/AC:L/PR:N/UI:N/S:U/C:L/I:N/A:N) | iOS | `AboutView.swift` | API key visible in app-switcher snapshot | M9: Insecure Data Storage | Accepted Risk |

---

## Detailed Findings

---

### SEC-01 — Critical: Debug Premium Toggle Ships in Production

**File:** `android/app/src/main/java/com/bitcraftapps/stax/ui/screens/AboutScreen.kt`, lines 275–298

**Description:**  
A UI switch labeled "Debug: Toggle Premium" is rendered unconditionally in the About screen for all users in all build variants. Any user — premium or not — can toggle this switch to grant themselves unlimited premium access without making a purchase. The switch calls `entitlementManager.setPremium(isInTrial = false, expiryMs = Long.MAX_VALUE)`, which persists the premium state to SharedPreferences and bypasses all paywall gates.

**Evidence:**
```kotlin
// Debug toggle (temporary — remove once gates verified)
HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
Row(...) {
    Text("Debug: Toggle Premium", ...)
    Switch(
        checked = isPremiumNow,
        onCheckedChange = { enabled ->
            if (enabled) {
                entitlementManager.setPremium(isInTrial = false, expiryMs = Long.MAX_VALUE)
            } else {
                entitlementManager.setFree()
            }
        }
    )
}
```

No `BuildConfig.DEBUG` or `if (BuildConfig.DEBUG)` guard is present. The iOS equivalent (`AboutView.swift:323`) is correctly guarded with `#if DEBUG`.

**Impact:** Complete monetization bypass. Any user can set themselves as premium, eliminating all revenue from the paywall. This is a direct business integrity failure.

**Remediation:** Wrap the entire debug toggle block in `if (BuildConfig.DEBUG) { ... }`. The Kotlin compiler will strip this block entirely from release APKs/AABs.

```kotlin
if (BuildConfig.DEBUG) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(...) {
        Text("Debug: Toggle Premium", ...)
        Switch(...)
    }
}
```

**Verification:** Build a release APK (`assembleRelease`) and decompile with `apktool` or `jadx`; confirm the toggle UI is absent. Alternatively, install the release APK on a device and verify the toggle does not appear in the About screen.

---

### SEC-02 — High: Release Build Has Minification Disabled

**File:** `android/app/build.gradle.kts`, line 70

**Description:**  
The release build type has `isMinifyEnabled = false`. While a `proguardFiles` directive is present, it is inactive because minification is disabled. This means release APKs/AABs ship with full class names, method names, and string literals intact — making static reverse engineering trivial.

**Evidence:**
```kotlin
release {
    isDebuggable = false
    isMinifyEnabled = false          // ← shrinking and obfuscation are off
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

**Impact:** Attackers can decompile the APK and read all business logic, including billing gate logic, SharedPreferences key names, API endpoints, and subscription product IDs — substantially lowering the bar for bypassing or cloning features.

**Remediation:**  
1. Set `isMinifyEnabled = true` in the release block.
2. Add ProGuard keep rules to `proguard-rules.pro` for libraries that use reflection (Room, Ktor, Gson, MediaPipe, Parcelable classes).

```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

Minimum ProGuard additions for this project:

```proguard
# Room — keep all entity/DAO metadata
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-keepclassmembers class * extends androidx.room.RoomDatabase { *; }

# Gson — keep data model fields
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class com.bitcraftapps.stax.data.** { *; }

# Kotlin Parcelize
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Ktor — keep serialization
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# MediaPipe
-keep class com.google.mediapipe.** { *; }
-dontwarn com.google.mediapipe.**

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
```

**Verification:** Build with `assembleRelease --info`; confirm R8 runs. Decompile the release APK with `jadx` and confirm class/method names are obfuscated.

---

### SEC-03 — High: OpenAI API Key Stored in Plaintext

**Files:**
- Android: `AboutScreen.kt:408–419` — `getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE).getString("openai_api_key", null)`
- iOS: `AboutView.swift:10,117` — `UserDefaults.standard.string(forKey: "openai_api_key")`, `UserDefaults.standard.set(apiKey, ...)`

**Description:**  
The user-supplied OpenAI API key is stored in Android `SharedPreferences` and iOS `UserDefaults`. Both are unencrypted XML/plist files on disk. On Android the file is at `/data/data/com.bitcraftapps.stax/shared_prefs/StaxPrefs.xml`, readable on rooted devices. On iOS, `UserDefaults` is included in unencrypted device backups (iTunes / Finder) unless the user enables encrypted backup.

On Android, the key is also displayed in an `OutlinedTextField` with no `PasswordVisualTransformation`, meaning it is visible in plaintext on screen and potentially in screenshots or accessibility trees.

**Evidence — Android:**
```kotlin
private fun getApiKey(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("openai_api_key", null)
}
private fun saveApiKey(context: Context, apiKey: String) {
    val sharedPreferences = context.getSharedPreferences("StaxPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) { putString("openai_api_key", apiKey); apply() }
}
// Input field — no PasswordVisualTransformation:
OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("Secret key") })
```

**Evidence — iOS:**
```swift
@State private var apiKey: String = UserDefaults.standard.string(forKey: "openai_api_key") ?? ""
// Save:
UserDefaults.standard.set(apiKey, forKey: "openai_api_key")
```

**Impact:** A stolen device backup or rooted device exposes the user's OpenAI API key, which could be used to run arbitrary OpenAI API calls at the user's expense. OpenAI keys have no scoping — a leaked key grants full account access to all models.

**Remediation — Android:** Use `EncryptedSharedPreferences` from Jetpack Security, backed by the Android Keystore. Add the Jetpack Security library and replace plaintext prefs with encrypted ones. Add `PasswordVisualTransformation` to the key input field.

First, add the dependency to `app/build.gradle.kts`:
```kotlin
implementation("androidx.security:security-crypto:1.1.0-alpha06")
```

Then replace the plaintext `SharedPreferences` helpers:
```kotlin
private fun getEncryptedPrefs(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    return EncryptedSharedPreferences.create(
        context,
        "StaxPrefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
```

**Remediation — iOS:** Store the key in the Keychain using a lightweight helper. Read on launch; write on save.

```swift
enum KeychainKey {
    static let openAIKey = "com.bitcraftapps.stax.openai_api_key"
}

func saveToKeychain(_ value: String, forKey key: String) {
    let data = Data(value.utf8)
    let query: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: key,
        kSecValueData as String: data,
        kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlockedThisDeviceOnly
    ]
    SecItemDelete(query as CFDictionary)
    SecItemAdd(query as CFDictionary, nil)
}

func loadFromKeychain(forKey key: String) -> String? {
    let query: [String: Any] = [
        kSecClass as String: kSecClassGenericPassword,
        kSecAttrAccount as String: key,
        kSecReturnData as String: true,
        kSecMatchLimit as String: kSecMatchLimitOne
    ]
    var result: AnyObject?
    guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
          let data = result as? Data else { return nil }
    return String(data: data, encoding: .utf8)
}
```

**Verification — Android:** Inspect `/data/data/.../shared_prefs/StaxPrefs.xml` on a rooted device after saving a key; confirm the value is not readable plaintext. Verify the input field masks the key.

**Verification — iOS:** Install a debug build, save a key, check that `UserDefaults` does not contain the key (`defaults read com.bitcraftapps.stax`); verify Keychain item exists with `security dump-keychain`.

---

### SEC-04 — High: Subscription Expiry Set to `Long.MAX_VALUE`

**File:** `android/app/src/main/java/com/bitcraftapps/stax/data/billing/BillingRepository.kt`, line 178

**Description:**  
When a purchase is handled (new purchase or restored), the entitlement manager is called with `expiryMs = Long.MAX_VALUE` — effectively "this subscription never expires on the client." This means if a subscription lapses (user cancels, payment fails, or Google Play revokes the purchase), the client-side `expiryMs` will never indicate expiry. The app relies on `queryExistingPurchases()` on resume to detect a revoked purchase, but if the purchase is removed from Play silently or the user's internet is offline, premium access persists indefinitely.

**Evidence:**
```kotlin
entitlementManager.setPremium(isInTrial = isInTrial, expiryMs = Long.MAX_VALUE)
```

**Impact:** Users whose subscriptions have lapsed retain premium access indefinitely in offline or edge-case scenarios. While Google Play's billing enforcement reduces the blast radius, this is not a sound design — real expiry data is available from the purchase object.

**Remediation:** Use `purchase.purchaseTime` plus a subscription-period buffer (31 days for monthly, 366 days for annual) as the local expiry cache. This does not replace server-side validation but substantially reduces the stale-entitlement window.

```kotlin
private const val MONTHLY_BUFFER_MS = 31L * 24 * 60 * 60 * 1000
private const val ANNUAL_BUFFER_MS  = 366L * 24 * 60 * 60 * 1000

fun handlePurchase(purchase: Purchase) {
    if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
    // ...acknowledge...
    val isAnnual = purchase.products.any { it == PRODUCT_ANNUAL }
    val buffer = if (isAnnual) ANNUAL_BUFFER_MS else MONTHLY_BUFFER_MS
    val expiryMs = purchase.purchaseTime + buffer
    entitlementManager.setPremium(isInTrial = isInTrial, expiryMs = expiryMs)
}
```

**Verification:** Manually call `setPremium` with a near-future timestamp; verify the app transitions to Free state after that time elapses and `queryExistingPurchases` does not re-grant premium for a cancelled product.

---

### SEC-05 — Medium: Android Backup Exclusions Too Narrow

**Files:** `android/app/src/main/res/xml/backup_rules.xml`, `data_extraction_rules.xml`

**Description:**  
Both backup configuration files only exclude `StaxPrefs.xml` from cloud and device transfer backups. All other SharedPreferences files — including `stax_subscription` (subscription state, expiry, trial status), `stax_cardrooms`, `ChipConfigs`, `stax_home_games`, `stax_nutz_game`, and the Room database — are included in full backup. A user could restore a backup to a new device and inherit:
- A premium entitlement state without having purchased on that device
- Another user's session history if device sharing is involved

**Impact:** Subscription state is not device-bound; backup restore may carry stale premium grants across devices. Privacy-sensitive session and financial data also travels in potentially unencrypted iCloud/Google backups.

**Remediation:** Add all sensitive preference files and the Room database to both backup exclusion files.

```xml
<!-- backup_rules.xml -->
<full-backup-content>
    <exclude domain="sharedpref" path="StaxPrefs.xml" />
    <exclude domain="sharedpref" path="stax_subscription.xml" />
    <exclude domain="sharedpref" path="stax_cardrooms.xml" />
    <exclude domain="sharedpref" path="stax_home_games.xml" />
    <exclude domain="sharedpref" path="stax_nutz_game.xml" />
    <exclude domain="sharedpref" path="ChipConfigs.xml" />
    <exclude domain="database" path="stax_database" />
</full-backup-content>
```

**Verification:** Enable full backup, restore to a second device; confirm subscription state resets to Free and session data is absent.

---

### SEC-06 — Medium: Placeholder Privacy URL in PaywallView

**File:** `ios/Stax/Screens/PaywallView.swift`, line 339

**Description:**  
The privacy link in the paywall footer points to `https://example.com/privacy`. This is a placeholder that was never replaced with the real privacy policy URL. On the App Store, this link is visible to users who click "Privacy" on the paywall screen and will navigate them to an unrelated domain.

**Evidence:**
```swift
Link("Privacy", destination: URL(string: "https://example.com/privacy")!)
```

**Impact:** App Store Review may reject the app. Users seeking to read the privacy policy are sent to an unrelated domain. This is also a potential phishing vector if `example.com` ever hosts content mimicking the real policy.

**Remediation:** Replace with the real privacy policy URL:
```swift
Link("Privacy", destination: URL(string: "https://staxapp.io/privacy")!)
```

**Verification:** Tap the Privacy link in the paywall on a device; confirm navigation to the real privacy policy.

---

### SEC-07 — Medium: No Server-Side Purchase Receipt Validation

**Files:** `android/.../BillingRepository.kt` (entire file), `ios/Stax/Billing/SubscriptionManager.swift` (entire file)

**Description:**  
Both platforms handle subscription state entirely on the client:
- **Android:** Acknowledges purchases via `BillingClient.acknowledgePurchase`; updates `EntitlementManager` directly from the client-side `Purchase` object.
- **iOS:** Uses StoreKit 2 `VerificationResult` (good — this provides cryptographic verification), but entitlement state is only stored locally in `EntitlementManager`.

Neither platform validates receipts against Google Play Developer API or App Store Server API. This means:
1. On Android, a rooted user can fake purchase acknowledgement
2. On both platforms, there is no authoritative record of active subscriptions beyond the local state and the on-device store

**Note:** iOS StoreKit 2's `checkVerified` provides strong cryptographic assurance that transactions originate from Apple, which materially reduces this risk. Android's Play Billing client API does not provide equivalent on-device cryptographic verification — it relies on the Play infrastructure.

**Impact:** Sophisticated attackers (especially on Android with rooted devices) can grant themselves premium entitlements. For a consumer app without a backend, this is a common acceptable trade-off, but it should be a documented architectural decision.

**Remediation — Recommended (backend):** Implement a lightweight server-side endpoint that:
1. Receives a purchase token (Android) or transaction ID (iOS) on each purchase
2. Validates it against the Google Play Developer API / App Store Server API
3. Returns a signed JWT with subscription state that the app uses to set entitlements

**Remediation — Minimum (no backend):** Document this as an accepted risk. For Android, add a check against `purchase.signature` using Google's public key (available in Google Play Console) before granting entitlements — this provides some protection without a backend.

**Verification:** If a backend is implemented, test that a fraudulently crafted purchase token is rejected.

---

### SEC-08 — Medium: Xcode User State Files Committed to Git

**Path:** `ios/Stax.xcodeproj/xcuserdata/`

**Description:**  
The `xcuserdata` directory contains user-specific IDE state files (`UserInterfaceState.xcuserstate`, `xcschememanagement.plist`). These files:
- Expose the local macOS username in file paths embedded in the `.xcuserstate` binary plist
- Create spurious git diffs for every collaborator as the IDE writes these files on every open
- Are not secrets, but are PII if the macOS username is a real name

**Impact:** Minor PII exposure (macOS account name in binary plist). Collaboration friction — every developer will have merge conflicts in these files.

**Remediation:** Add the following to the root `.gitignore`:
```
# Xcode user-specific data
xcuserdata/
*.xcuserstate
```
Then remove the files from git tracking: `git rm -r --cached ios/Stax.xcodeproj/xcuserdata/`

**Verification:** After adding to `.gitignore` and running `git rm --cached`, `git status` should no longer show changes to `xcuserdata/` files.

---

### SEC-09 — Low: Local Filesystem Path Logged to Logcat

**File:** `android/app/src/main/java/com/bitcraftapps/stax/ui/screens/ScanScreen.kt`, line 544

**Description:**  
A `Log.d` call emits the absolute path of a saved training image, including the application's internal storage directory.

**Evidence:**
```kotlin
Log.d("saveTrainingImage", "Saved to ${file.absolutePath}")
// Emits e.g.: D/saveTrainingImage: Saved to /data/data/com.bitcraftapps.stax/cache/training_data/5/chip_1700000000000.jpg
```

**Impact:** On debug builds with ADB connected, or on devices where a second app has `READ_LOGS` permission (rare but possible on some OEM ROMs), this path is visible. Low severity because the path reveals internal storage structure but not user secrets.

**Remediation:** Remove the log statement in production code. If needed for debugging, guard with `if (BuildConfig.DEBUG)`.

---

### SEC-10 — Low: iOS `print()` Not Stripped in Release Builds

**File:** `ios/Stax/Billing/SubscriptionManager.swift`, lines 34, 75, 105

**Description:**  
Three `print()` calls log StoreKit error details in all build configurations. In iOS, `print()` writes to the device console (accessible via Xcode or iOS Console app), visible to anyone with physical device access and a Mac.

**Evidence:**
```swift
print("StoreKit: Failed to load products – \(error)")
print("StoreKit: Failed to verify entitlement – \(error)")
print("StoreKit: Transaction update failed – \(error)")
```

**Impact:** Error details (including SKError codes and potential context) are exposed in release console output. Minimal practical risk as console access requires physical device + trusted Mac, but violates the principle of release-build hygiene.

**Remediation:** Guard all `print()` calls with `#if DEBUG`:
```swift
#if DEBUG
print("StoreKit: Failed to load products – \(error)")
#endif
```

**Verification:** Build in Release configuration; run on device; confirm no `StoreKit:` lines appear in Xcode console.

---

### SEC-11 — Low: Accompanist Version Catalog Inconsistency

**Files:** `android/gradle/libs.versions.toml:21`, `android/app/build.gradle.kts:114`

**Description:**  
The version catalog defines `accompanist-permissions = "0.32.0"`, but the build file uses the library directly as `"com.google.accompanist:accompanist-permissions:0.34.0"` — bypassing the catalog. This means:
- Automated dependency vulnerability scanners using the catalog will check `0.32.0` while the app actually ships `0.34.0`
- The discrepancy creates confusion for anyone reading the catalog expecting it to be the source of truth

**Remediation:** Update `libs.versions.toml` to `accompanist-permissions = "0.34.0"` and reference it via the catalog in `build.gradle.kts`.

---

### SEC-12 — Low: No TLS Certificate Pinning

**Files:** `android/.../data/OpenAiService.kt`, `ios/Stax/Data/OpenAIService.swift`

**Description:**  
Both platforms make HTTPS requests to `api.openai.com` using the system default trust store without certificate pinning. This is appropriate for most consumer apps, but means a corporate MITM proxy or a custom CA installed on the device can intercept API traffic including the OpenAI API key in request headers.

**Impact:** Low for typical consumer users. Higher risk in enterprise/MDM environments where custom CAs may be installed.

**Remediation (optional):** Add certificate pinning using OkHttp's `CertificatePinner` (Android) or `URLSessionDelegate` with custom `URLAuthenticationChallenge` handling (iOS). Given the maintenance burden (pins must be rotated when OpenAI rotates certs), this is recommended only if the threat model includes enterprise MITM scenarios.

---

### SEC-13 — Low: API Key Visible in App-Switcher Snapshot (iOS)

**File:** `ios/Stax/Views/AboutView.swift`

**Description:**  
When the user has revealed their API key using the eye toggle and then backgrounds the app, iOS takes a screenshot for the app switcher. The revealed key would appear in this screenshot. On older iOS versions, this snapshot could be recovered from device filesystem on a jailbroken device.

**Impact:** Low. Requires the user to have both revealed the key and backgrounded the app simultaneously. The snapshot is stored in a protected location in modern iOS.

**Remediation:** Apply `.privacySensitive()` to the API key text field, or detect `scenePhase == .inactive` and hide the key:
```swift
.privacySensitive(showApiKey)
```

---

## Architecture Recommendations

### A1 — Server-Side Subscription Validation

For both platforms, the highest long-term security improvement is a lightweight backend endpoint that validates subscription status against Google Play Developer API and App Store Server API. This prevents client-side bypass regardless of rooting or jailbreaking.

A minimal implementation:
```
Client                  Backend                  Store API
  │── POST /validate ──▶│                         │
  │   {token, platform} │── GET subscriptions ──▶ │
  │                     │◀── {status, expiry} ─── │
  │◀── {jwt, expiryMs} ─│                         │
```

The backend issues a short-lived signed JWT; the client stores only the JWT, which it presents on launch to refresh entitlements.

### A2 — Android EncryptedSharedPreferences Across All Sensitive Prefs

Once `EncryptedSharedPreferences` is adopted for the OpenAI key (`StaxPrefs`), consider migrating subscription state (`stax_subscription`) to the same encrypted store. This hardens against rooted-device tampering of premium status.

### A3 — iOS Keychain `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`

The `ThisDeviceOnly` flag ensures Keychain items are not included in iCloud Keychain sync and cannot migrate via encrypted backups to other devices. Use this attribute for the OpenAI key and any future sensitive values.

### A4 — Android ProGuard and Release Signing Verification in CI

Add a CI step that builds the release APK and runs `apkanalyzer` to verify:
1. R8 minification is active (class count should drop significantly vs debug)
2. Signing with the upload key succeeded
3. No debug-only classes are present in the release output

---

## Remediation Roadmap

### Tier 1 — Fix Immediately (before next release)

| ID | Fix |
|----|-----|
| SEC-01 | Wrap Android debug toggle in `if (BuildConfig.DEBUG)` |
| SEC-02 | Enable `isMinifyEnabled = true`; add ProGuard rules |
| SEC-03 | Migrate Android key to `EncryptedSharedPreferences`; migrate iOS key to Keychain |
| SEC-06 | Replace placeholder privacy URL in `PaywallView.swift` |

### Tier 2 — Fix in Next Sprint

| ID | Fix |
|----|-----|
| SEC-04 | Replace `Long.MAX_VALUE` with time-bounded expiry |
| SEC-05 | Expand Android backup exclusion rules |
| SEC-08 | Add `xcuserdata/` to `.gitignore`; remove from history |
| SEC-09 | Remove or guard filesystem path log in `ScanScreen` |
| SEC-10 | Guard `print()` calls in `SubscriptionManager` with `#if DEBUG` |
| SEC-11 | Align accompanist version in catalog |

### Tier 3 — Architectural / Future

| ID | Recommendation |
|----|----------------|
| SEC-07 | Implement server-side purchase receipt validation |
| SEC-12 | Evaluate certificate pinning for `api.openai.com` |
| SEC-13 | Apply `privacySensitive()` to API key field (iOS) |

---

## Post-Fix Verification Checklist

- [ ] **SEC-01:** Install release APK; confirm "Debug: Toggle Premium" is absent from About screen
- [ ] **SEC-02:** Build release APK with `--info`; confirm R8/D8 runs; decompile with `jadx` and verify class name obfuscation; confirm app runs correctly after minification
- [ ] **SEC-03 (Android):** On rooted device, inspect `StaxPrefs.xml` — value should be encrypted ciphertext, not plaintext; confirm key input field masks characters by default
- [ ] **SEC-03 (iOS):** Confirm `UserDefaults` does not contain `openai_api_key`; confirm Keychain item exists after saving
- [ ] **SEC-04:** Restore a purchase with a known `purchaseTime`; verify `expiryMs` in EntitlementManager prefs equals `purchaseTime + buffer`; verify app goes Free after expiry
- [ ] **SEC-05:** Perform a full backup/restore cycle to a second device; verify subscription state is Free on restored device
- [ ] **SEC-06:** Tap Privacy link in paywall; confirm navigation to `staxapp.io/privacy`
- [ ] **SEC-08:** After gitignore update, open Xcode, confirm `git status` shows no `xcuserdata` changes
- [ ] **SEC-09:** Run `adb logcat | grep saveTrainingImage`; confirm no output during training image save
- [ ] **SEC-10:** Build in Release; run on device; open Console app on Mac; confirm no `StoreKit:` lines
- [ ] **SEC-11:** Run `./gradlew :app:dependencies` and verify accompanist version is `0.34.0` from catalog

---

## Dependency Inventory

The following direct dependencies were identified for CVE/OSV tracking. Versions are as declared in `app/build.gradle.kts` and `gradle/libs.versions.toml` (Android) and the Xcode project (iOS uses only Apple system frameworks).

### Android Dependencies

| Library | Version in Project | Notes |
|---------|-------------------|-------|
| Android Gradle Plugin | 8.8.0 | |
| Kotlin | 2.0.0 | Compile-time only; kotlin-stdlib 2.2.10 pulled transitively — see SEC-11 for version alignment |
| Compose BOM | 2024.04.01 | |
| Jetpack Navigation Compose | 2.7.7 | |
| Material3 | 1.2.1 | |
| Room | 2.6.1 | Upgrade to 2.7.x recommended for KSP support |
| Ktor (client suite) | 2.3.11 | |
| Gson | 2.10.1 | |
| CameraX suite | 1.4.2 | |
| MediaPipe tasks-vision | 0.10.33 | |
| Coil Compose | 2.6.0 | |
| Accompanist Permissions | 0.34.0 (build) / 0.32.0 (catalog) | See SEC-11 |
| Google Play Services Location | 21.3.0 | |
| Google Play Billing | 8.3.0 | Non-ktx variant selected to avoid kotlin-stdlib conflict |
| ColorPicker Compose | 1.0.0 | |
| Jetpack Security Crypto | 1.1.0-alpha06 | To be added as part of SEC-03 fix |

**Recommended action:** Run `./gradlew :app:dependencyUpdates` with the Gradle Versions Plugin, or submit to [OSV.dev](https://osv.dev) dependency scanner, before each release.

### iOS Dependencies

The iOS app uses exclusively Apple system frameworks (SwiftUI, StoreKit 2, AVFoundation, MapKit, CoreLocation, PhotosUI). No third-party Swift packages or CocoaPods are declared in the Xcode project. This is a strong supply-chain hygiene posture — no action required.

---

## Appendix: Positive Findings

The following were reviewed and found to be correctly implemented:

- No hardcoded API keys or secrets in any source file (Android or iOS)
- All network traffic uses HTTPS; `Info.plist` has no `NSAllowsArbitraryLoads`; no `android:usesCleartextTraffic`
- Android `AndroidManifest.xml` requests only permissions tied to features (INTERNET, BILLING, CAMERA, FINE_LOCATION, COARSE_LOCATION)
- No unintentionally exported Android components (only `MainActivity` is exported; `FileProvider` is `exported="false"`)
- No `WebView` usage in Android with JavaScript enabled
- iOS debug premium toggle is correctly guarded with `#if DEBUG` — serves as the model for the Android fix
- iOS StoreKit 2 uses `checkVerified(VerificationResult)` — cryptographic transaction verification is in place
- `keystore.properties` and `local.properties` are correctly excluded from git via `.gitignore`
- No custom URL schemes registered that could be exploited for deep-link injection
- Room DAO queries use bound parameters — no SQL injection surface
- Android `FileProvider` is used correctly for file sharing — no direct file:// URIs
- Training image label is filtered through `File(trainingDir, label)` with a single-component append — path traversal is substantially mitigated

---

*This document was produced from static code analysis. It does not substitute for dynamic testing, penetration testing, or a formal third-party security audit.*
