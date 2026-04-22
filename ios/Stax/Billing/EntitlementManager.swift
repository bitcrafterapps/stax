import Foundation
import StoreKit

@MainActor
class EntitlementManager: ObservableObject {

    @Published var isPremium: Bool = false
    @Published var isInTrial: Bool = false
    @Published var subscriptionState: SubscriptionState = .free

    private var cachedExpiryDate: Date = .distantFuture

    // MARK: – Scan counter (keyed by date)

    private func todayKey() -> String {
        let fmt = DateFormatter()
        fmt.dateFormat = "yyyy-MM-dd"
        return "scans_\(fmt.string(from: Date()))"
    }

    func recordScan() {
        let key = todayKey()
        let current = UserDefaults.standard.integer(forKey: key)
        UserDefaults.standard.set(current + 1, forKey: key)
    }

    func getDailyScans() -> Int {
        UserDefaults.standard.integer(forKey: todayKey())
    }

    // MARK: – State mutations

    func update(from transaction: Transaction) {
        let inTrial = transaction.offerType == .introductoryOffer
        let expiry = transaction.expirationDate ?? .distantFuture
        isInTrial = inTrial
        cachedExpiryDate = expiry
        subscriptionState = .premium(isInTrial: inTrial, expiryDate: expiry)
        isPremium = true
    }

    func setExpired() {
        subscriptionState = .expired
        isPremium = false
        isInTrial = false
    }

    func setFree() {
        subscriptionState = .free
        isPremium = false
        isInTrial = false
    }

    // MARK: – Limit checking

    func checkLimit(
        for feature: Feature,
        sessionPhotoCount: Int? = nil,
        totalSessions: Int? = nil,
        favoritesCount: Int? = nil,
        casinoIndex: Int? = nil
    ) -> LimitResult {
        if isPremium { return .allowed }

        switch feature {
        case .scan:
            if getDailyScans() < FreeTierLimits.maxScansPerDay { return .allowed }
            return .blocked(.scan)

        case .aiScan:
            return .blocked(.aiScan)

        case .sessionCreate:
            let count = totalSessions ?? 0
            if count < FreeTierLimits.maxSessions { return .allowed }
            return .blocked(.sessionCreate)

        case .photoAdd:
            let count = sessionPhotoCount ?? 0
            if count < FreeTierLimits.maxPhotosPerSession { return .allowed }
            return .blocked(.photoAdd)

        case .chipConfig:
            let index = casinoIndex ?? 0
            if index < FreeTierLimits.maxChipConfigCasinos { return .allowed }
            return .blocked(.chipConfig)

        case .favorites:
            let count = favoritesCount ?? 0
            if count < FreeTierLimits.maxFavorites { return .allowed }
            return .blocked(.favorites)

        case .shareClean:
            return .blocked(.shareClean)
        }
    }

    #if DEBUG
    func setDebugPremium() {
        let expiry = Calendar.current.date(byAdding: .year, value: 1, to: Date()) ?? .distantFuture
        cachedExpiryDate = expiry
        subscriptionState = .premium(isInTrial: false, expiryDate: expiry)
        isPremium = true
        isInTrial = false
    }
    #endif

    // MARK: – Trial countdown

    func getTrialDaysRemaining() -> Int {
        guard case .premium(let inTrial, let expiry) = subscriptionState, inTrial else { return 0 }
        let days = Calendar.current.dateComponents([.day], from: Date(), to: expiry).day ?? 0
        return max(0, days)
    }
}
