import SwiftUI

struct MainTabView: View {
    @StateObject private var vm = SessionsViewModel()

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

            AboutView()
                .tabItem {
                    Label("About", systemImage: "info.circle.fill")
                }
        }
        .tint(.staxPrimary)
        .preferredColorScheme(.dark)
    }
}
