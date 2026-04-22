import Foundation

// MARK: – Subscription state

enum SubscriptionState {
    case free
    case premium(isInTrial: Bool, expiryDate: Date)
    case expired
}

// MARK: – Feature enum

enum Feature {
    case scan
    case aiScan
    case sessionCreate
    case photoAdd
    case chipConfig
    case favorites
    case shareClean
}

// MARK: – Limit check result

enum LimitResult {
    case allowed
    case softCap(message: String)
    case blocked(Feature)
}

// MARK: – Free tier constants

struct FreeTierLimits {
    static let maxScansPerDay        = 5
    static let maxSessions           = 3
    static let maxPhotosPerSession   = 10
    static let maxFavorites          = 3
    static let maxChipConfigCasinos  = 1
}
