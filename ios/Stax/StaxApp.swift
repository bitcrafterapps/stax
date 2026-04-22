import SwiftUI

@main
struct StaxApp: App {
    @StateObject private var entitlementManager: EntitlementManager
    @StateObject private var subscriptionManager: SubscriptionManager
    @State private var showSplash = true
    @Environment(\.scenePhase) private var scenePhase

    init() {
        let em = EntitlementManager()
        _entitlementManager = StateObject(wrappedValue: em)
        _subscriptionManager = StateObject(wrappedValue: SubscriptionManager(entitlementManager: em))
    }

    var body: some Scene {
        WindowGroup {
            ZStack {
                if showSplash {
                    SplashView(onDone: {
                        withAnimation(.easeInOut(duration: 0.4)) {
                            showSplash = false
                        }
                    })
                    .transition(.opacity)
                } else {
                    MainTabView()
                        .transition(.opacity)
                }
            }
            .environmentObject(entitlementManager)
            .environmentObject(subscriptionManager)
            .task {
                await subscriptionManager.loadProducts()
                await subscriptionManager.checkCurrentEntitlements()
            }
            .onChange(of: scenePhase) { _, newPhase in
                if newPhase == .active {
                    Task { await subscriptionManager.checkCurrentEntitlements() }
                }
            }
        }
    }
}
