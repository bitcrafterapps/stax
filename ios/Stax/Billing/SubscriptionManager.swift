import Foundation
import StoreKit

enum StoreError: Error {
    case failedVerification
}

@MainActor
class SubscriptionManager: ObservableObject {

    static let productIds = ["stax_premium_monthly", "stax_premium_annual"]

    @Published var products: [Product] = []

    private let entitlementManager: EntitlementManager
    private var updateListenerTask: Task<Void, Error>?

    init(entitlementManager: EntitlementManager) {
        self.entitlementManager = entitlementManager
        updateListenerTask = listenForTransactions()
    }

    deinit {
        updateListenerTask?.cancel()
    }

    // MARK: – Product loading

    func loadProducts() async {
        do {
            let loaded = try await Product.products(for: Self.productIds)
            products = loaded.sorted { first, _ in first.id.contains("monthly") }
        } catch {
            #if DEBUG
            print("StoreKit: Failed to load products – \(error)")
            #endif
        }
    }

    // MARK: – Purchase

    func purchase(_ product: Product) async throws -> Transaction? {
        let result = try await product.purchase()
        switch result {
        case .success(let verification):
            let transaction = try checkVerified(verification)
            await entitlementManager.update(from: transaction)
            await transaction.finish()
            return transaction
        case .userCancelled:
            return nil
        case .pending:
            return nil
        @unknown default:
            return nil
        }
    }

    // MARK: – Restore

    func restorePurchases() async {
        try? await AppStore.sync()
    }

    // MARK: – Entitlement check

    func checkCurrentEntitlements() async {
        var hasActive = false
        for await result in Transaction.currentEntitlements {
            do {
                let transaction = try checkVerified(result)
                if transaction.revocationDate == nil {
                    await entitlementManager.update(from: transaction)
                    hasActive = true
                }
            } catch {
                #if DEBUG
                print("StoreKit: Failed to verify entitlement – \(error)")
                #endif
            }
        }
        if !hasActive && entitlementManager.isPremium {
            entitlementManager.setFree()
        }
    }

    // MARK: – Verification

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreError.failedVerification
        case .verified(let value):
            return value
        }
    }

    // MARK: – Transaction listener

    private func listenForTransactions() -> Task<Void, Error> {
        Task { [weak self] in
            for await result in Transaction.updates {
                guard let self else { return }
                do {
                    let transaction = try self.checkVerified(result)
                    await self.entitlementManager.update(from: transaction)
                    await transaction.finish()
                } catch {
                    #if DEBUG
                    print("StoreKit: Transaction update failed – \(error)")
                    #endif
                }
            }
        }
    }
}
