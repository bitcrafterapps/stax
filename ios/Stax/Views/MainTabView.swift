import SwiftUI

struct MainTabView: View {
    @StateObject private var vm = SessionsViewModel()
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @EnvironmentObject private var subscriptionManager: SubscriptionManager
    @State private var showPaywall = false

    var body: some View {
        TabView {
            DashboardView(vm: vm)
                .tabItem {
                    Label("Photos", systemImage: "photo.stack.fill")
                }

            SessionsView(vm: vm)
                .tabItem {
                    Label("Sessions", systemImage: "list.bullet.rectangle.portrait.fill")
                }

            ScanView()
                .tabItem {
                    Label("Scan", systemImage: "camera.viewfinder")
                }

            FindView()
                .tabItem {
                    Label("Find", systemImage: "location.magnifyingglass")
                }

            AboutView(vm: vm)
                .tabItem {
                    Label("About", systemImage: "info.circle.fill")
                }
        }
        .tint(.staxPrimary)
        .preferredColorScheme(.dark)
        .environment(\.showPaywall, ShowPaywallAction { showPaywall = true })
        .sheet(isPresented: $showPaywall) {
            PaywallView()
                .environmentObject(entitlementManager)
                .environmentObject(subscriptionManager)
        }
    }
}
