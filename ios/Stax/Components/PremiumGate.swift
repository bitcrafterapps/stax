import SwiftUI

// MARK: – Environment action for triggering paywall

struct ShowPaywallAction {
    let action: () -> Void
    func callAsFunction() { action() }
}

private struct ShowPaywallKey: EnvironmentKey {
    static let defaultValue = ShowPaywallAction(action: {})
}

extension EnvironmentValues {
    var showPaywall: ShowPaywallAction {
        get { self[ShowPaywallKey.self] }
        set { self[ShowPaywallKey.self] = newValue }
    }
}

// MARK: – ViewModifier

struct PremiumGate: ViewModifier {
    @EnvironmentObject var entitlementManager: EntitlementManager

    let feature: Feature
    var sessionPhotoCount: Int?
    var totalSessions: Int?
    var favoritesCount: Int?
    var casinoIndex: Int?
    var onShowPaywall: () -> Void

    func body(content: Content) -> some View {
        let result = entitlementManager.checkLimit(
            for: feature,
            sessionPhotoCount: sessionPhotoCount,
            totalSessions: totalSessions,
            favoritesCount: favoritesCount,
            casinoIndex: casinoIndex
        )

        switch result {
        case .allowed:
            content

        case .softCap(let msg):
            VStack(spacing: 8) {
                content
                UpgradeBanner(message: msg, onUpgrade: onShowPaywall)
            }

        case .blocked:
            UpgradeBanner(
                message: blockedMessage(for: feature),
                onUpgrade: onShowPaywall
            )
        }
    }

    private func blockedMessage(for feature: Feature) -> String {
        switch feature {
        case .scan:          return "You've used all \(FreeTierLimits.maxScansPerDay) free scans today. Upgrade for unlimited."
        case .aiScan:        return "AI Stack Counter is a Premium feature."
        case .sessionCreate: return "You've reached the \(FreeTierLimits.maxSessions)-session free limit."
        case .photoAdd:      return "You've reached the \(FreeTierLimits.maxPhotosPerSession)-photo limit per session."
        case .chipConfig:    return "Upgrade to configure all casinos."
        case .favorites:     return "Upgrade to Premium for unlimited favorites."
        case .shareClean:    return "Clean export is a Premium feature."
        }
    }
}

// MARK: – View extension

extension View {
    func premiumGate(
        feature: Feature,
        sessionPhotoCount: Int? = nil,
        totalSessions: Int? = nil,
        favoritesCount: Int? = nil,
        casinoIndex: Int? = nil,
        onShowPaywall: @escaping () -> Void
    ) -> some View {
        self.modifier(PremiumGate(
            feature: feature,
            sessionPhotoCount: sessionPhotoCount,
            totalSessions: totalSessions,
            favoritesCount: favoritesCount,
            casinoIndex: casinoIndex,
            onShowPaywall: onShowPaywall
        ))
    }
}
