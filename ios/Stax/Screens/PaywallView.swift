import SwiftUI
import StoreKit

struct PaywallView: View {
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    @Environment(\.dismiss) private var dismiss

    @State private var selectedPlan: String = "annual"
    @State private var isEligibleForTrial = false
    @State private var isPurchasing = false
    @State private var purchaseError: String? = nil
    @State private var showSuccess = false
    @State private var showNotSignedInAlert = false

    private var monthlyProduct: Product? {
        subscriptionManager.products.first { $0.id.contains("monthly") }
    }

    private var annualProduct: Product? {
        subscriptionManager.products.first { $0.id.contains("annual") }
    }

    private var selectedProduct: Product? {
        selectedPlan == "monthly" ? monthlyProduct : annualProduct
    }

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(red: 0.08, green: 0.05, blue: 0.16), Color.black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 28) {
                    headerSection
                    featureComparisonSection
                    pricingCardsSection
                    ctaSection
                    if entitlementManager.isInTrial {
                        trialCountdownBanner
                    }
                    footerLinks
                }
                .padding(.horizontal, 20)
                .padding(.top, 20)
                .padding(.bottom, 40)
            }

            // Dismiss button
            VStack {
                HStack {
                    Spacer()
                    Button { dismiss() } label: {
                        Image(systemName: "xmark")
                            .font(.subheadline.bold())
                            .foregroundColor(.white.opacity(0.6))
                            .frame(width: 32, height: 32)
                            .background(Color.white.opacity(0.12))
                            .clipShape(Circle())
                    }
                    .padding(.top, 16)
                    .padding(.trailing, 20)
                }
                Spacer()
            }

            // Success overlay
            if showSuccess {
                successOverlay
            }
        }
        .transition(.move(edge: .bottom))
        .task {
            await checkTrialEligibility()
        }
        .alert("Purchase Pending", isPresented: Binding(
            get: { purchaseError != nil },
            set: { if !$0 { purchaseError = nil } }
        )) {
            Button("OK", role: .cancel) { purchaseError = nil }
        } message: {
            Text(purchaseError ?? "")
        }
        .alert("Sign In to App Store", isPresented: $showNotSignedInAlert) {
            Button("OK", role: .cancel) {}
        } message: {
            Text("Please sign in to your Apple ID in Settings to complete this purchase.")
        }
    }

    // MARK: – Hero

    private var headerSection: some View {
        VStack(spacing: 16) {
            StaxLogoImage(size: 80)
                .clipShape(Circle())
                .shadow(color: Color.staxPrimary.opacity(0.5), radius: 20)

            VStack(spacing: 6) {
                Text("Unlock the Full Experience")
                    .font(.title.bold())
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Text("All features. No limits.")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.65))
            }
        }
        .padding(.top, 32)
    }

    // MARK: – Feature comparison

    private var featureComparisonSection: some View {
        VStack(spacing: 0) {
            featureHeader
            Divider().background(Color.white.opacity(0.12))
            featureRow(icon: "list.bullet.rectangle", name: "Sessions", free: "3", premium: "Unlimited")
            featureRow(icon: "photo.stack", name: "Photos / Session", free: "10", premium: "Unlimited")
            featureRow(icon: "camera.viewfinder", name: "Chip Scanner", free: "5/day", premium: "Unlimited")
            featureRow(icon: "sparkles", name: "AI Stack Counter", free: "—", premium: "Included", freeIsLocked: true)
            featureRow(icon: "circle.hexagongrid", name: "Chip Config", free: "1 casino", premium: "All casinos")
            featureRow(icon: "heart.fill", name: "Favorites", free: "3 max", premium: "Unlimited")
            featureRow(icon: "square.and.arrow.up", name: "Share", free: "Watermarked", premium: "Clean export")
            featureRow(icon: "location.magnifyingglass", name: "Find Card Rooms", free: "Free", premium: "Free", freeIsLocked: false, alwaysFree: true)
        }
        .background(Color.white.opacity(0.05))
        .cornerRadius(16)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color.white.opacity(0.12), lineWidth: 1))
    }

    private var featureHeader: some View {
        HStack {
            Text("Feature")
                .font(.caption.bold())
                .foregroundColor(.staxOnSurfaceVar)
                .frame(maxWidth: .infinity, alignment: .leading)
            Text("Free")
                .font(.caption.bold())
                .foregroundColor(.staxOnSurfaceVar)
                .frame(width: 72, alignment: .center)
            Text("Premium")
                .font(.caption.bold())
                .foregroundColor(.staxPrimary)
                .frame(width: 80, alignment: .center)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
    }

    private func featureRow(
        icon: String,
        name: String,
        free: String,
        premium: String,
        freeIsLocked: Bool = false,
        alwaysFree: Bool = false
    ) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.caption)
                .foregroundColor(.staxPrimary)
                .frame(width: 20)

            Text(name)
                .font(.caption)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity, alignment: .leading)

            Group {
                if freeIsLocked {
                    Image(systemName: "lock.fill")
                        .font(.caption)
                        .foregroundColor(.staxOnSurfaceVar.opacity(0.5))
                } else {
                    Text(free)
                        .font(.caption)
                        .foregroundColor(alwaysFree ? .staxProfit : .staxOnSurfaceVar)
                }
            }
            .frame(width: 72, alignment: .center)

            HStack(spacing: 3) {
                if !alwaysFree {
                    Image(systemName: "checkmark")
                        .font(.caption2.bold())
                        .foregroundColor(.staxProfit)
                }
                Text(premium)
                    .font(.caption.bold())
                    .foregroundColor(alwaysFree ? .staxProfit : .white)
            }
            .frame(width: 80, alignment: .center)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 9)
        .background(Color.clear)
        Divider().background(Color.white.opacity(0.07))
    }

    // MARK: – Pricing cards

    private var pricingCardsSection: some View {
        HStack(spacing: 12) {
            planCard(
                id: "monthly",
                price: monthlyProduct?.displayPrice ?? "$4.99",
                period: "/month",
                subtitle: "Billed monthly",
                badge: nil
            )

            planCard(
                id: "annual",
                price: annualProduct?.displayPrice ?? "$39",
                period: "/year",
                subtitle: "Just $3.25/mo",
                badge: "BEST VALUE · Save 35%"
            )
        }
    }

    private func planCard(
        id: String,
        price: String,
        period: String,
        subtitle: String,
        badge: String?
    ) -> some View {
        let selected = selectedPlan == id
        return Button { selectedPlan = id } label: {
            VStack(spacing: 8) {
                if let badge {
                    Text(badge)
                        .font(.caption2.bold())
                        .foregroundColor(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Color.staxPrimary)
                        .cornerRadius(6)
                }

                HStack(alignment: .firstTextBaseline, spacing: 2) {
                    Text(price)
                        .font(.title2.bold())
                        .foregroundColor(.white)
                    Text(period)
                        .font(.caption)
                        .foregroundColor(.staxOnSurfaceVar)
                }

                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .padding(.horizontal, 8)
            .background(selected ? Color.staxPrimary.opacity(0.25) : Color.white.opacity(0.06))
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(selected ? Color.staxPrimary : Color.white.opacity(0.15), lineWidth: selected ? 2 : 1)
            )
        }
        .buttonStyle(.plain)
    }

    // MARK: – CTA

    private var ctaSection: some View {
        VStack(spacing: 14) {
            Button {
                Task { await performPurchase() }
            } label: {
                HStack {
                    if isPurchasing {
                        ProgressView().tint(.white)
                    } else {
                        Text(ctaTitle)
                            .font(.headline)
                            .foregroundColor(.white)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(isPurchasing ? Color.staxPrimary.opacity(0.6) : Color.staxPrimary)
                .cornerRadius(16)
            }
            .disabled(isPurchasing || subscriptionManager.products.isEmpty)

            if subscriptionManager.products.isEmpty {
                Text("Loading subscription options…")
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
            }
        }
    }

    private var ctaTitle: String {
        if isEligibleForTrial {
            return "Start 7-Day Free Trial"
        }
        return "Subscribe Now"
    }

    // MARK: – Trial banner

    private var trialCountdownBanner: some View {
        let days = entitlementManager.getTrialDaysRemaining()
        return HStack(spacing: 10) {
            Image(systemName: "clock.fill")
                .foregroundColor(.staxPrimary)
            Text("Your trial ends in \(days) \(days == 1 ? "day" : "days")")
                .font(.subheadline.bold())
                .foregroundColor(.white)
            Spacer()
        }
        .padding(14)
        .background(Color.staxPrimaryContainer)
        .cornerRadius(12)
    }

    // MARK: – Footer

    private var footerLinks: some View {
        HStack(spacing: 16) {
            Button("Restore Purchases") {
                Task { await subscriptionManager.restorePurchases() }
            }
            Text("·").foregroundColor(.staxOnSurfaceVar)
            Link("Terms", destination: URL(string: "https://www.apple.com/legal/internet-services/itunes/dev/stdeula/")!)
            Text("·").foregroundColor(.staxOnSurfaceVar)
            Link("Privacy", destination: URL(string: "https://staxapp.io/privacy")!)
        }
        .font(.caption)
        .foregroundColor(.staxOnSurfaceVar)
    }

    // MARK: – Success overlay

    private var successOverlay: some View {
        ZStack {
            Color.black.opacity(0.7).ignoresSafeArea()
            VStack(spacing: 20) {
                Image(systemName: "checkmark.circle.fill")
                    .font(.system(size: 64))
                    .foregroundColor(.staxProfit)
                    .scaleEffect(showSuccess ? 1 : 0.3)
                    .animation(.spring(response: 0.4, dampingFraction: 0.6), value: showSuccess)

                Text("Welcome to Premium!")
                    .font(.title2.bold())
                    .foregroundColor(.white)

                Text("All features are now unlocked.")
                    .font(.subheadline)
                    .foregroundColor(.staxOnSurfaceVar)
            }
        }
    }

    // MARK: – Actions

    private func checkTrialEligibility() async {
        let product = selectedPlan == "annual" ? annualProduct : monthlyProduct
        guard let product else {
            isEligibleForTrial = false
            return
        }
        let eligible = try? await product.subscription?.isEligibleForIntroOffer
        isEligibleForTrial = eligible ?? false
    }

    private func performPurchase() async {
        guard let product = selectedProduct else { return }
        isPurchasing = true
        defer { isPurchasing = false }

        do {
            let transaction = try await subscriptionManager.purchase(product)
            if transaction != nil {
                withAnimation { showSuccess = true }
                DispatchQueue.main.asyncAfter(deadline: .now() + 1.8) {
                    dismiss()
                }
            }
        } catch StoreKitError.notEntitled {
            showNotSignedInAlert = true
        } catch {
            let errorDesc = error.localizedDescription
            if errorDesc.lowercased().contains("cancel") == false {
                purchaseError = errorDesc
            }
        }
    }
}

// MARK: – Preview helper

#if DEBUG
struct PaywallView_Previews: PreviewProvider {
    static var previews: some View {
        PaywallView()
            .environmentObject(EntitlementManager())
            .environmentObject(SubscriptionManager(entitlementManager: EntitlementManager()))
    }
}
#endif
