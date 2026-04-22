import SwiftUI
import CoreLocation
import MapKit

struct FindView: View {
    @StateObject private var vm = FindViewModel()
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @Environment(\.showPaywall) private var showPaywall
    @State private var showFavoritesLimitToast = false

    var body: some View {
        NavigationStack {
        ZStack {
            Color.staxBackground.ignoresSafeArea()
            VStack(spacing: 0) {
                StaxHeader(title: "Find Card Rooms", subtitle: "Discover nearby poker action")

                VStack(spacing: 12) {
                    // Search mode tabs
                    FilterChipRow(options: SearchMode.allCases.map(\.rawValue),
                                  selected: Binding(
                                    get: { vm.searchMode.rawValue },
                                    set: { raw in
                                        if let mode = SearchMode(rawValue: raw) {
                                            vm.searchMode = mode
                                            vm.search()
                                        }
                                    }
                                  ))
                    .padding(.top, 12)

                    // Mode-specific controls
                    Group {
                        if vm.searchMode == .nearMe {
                            if vm.locationPermission == .denied || vm.locationPermission == .restricted {
                                LocationDeniedCard()
                            } else if vm.locationPermission == .notDetermined {
                                Button("Enable Location") { vm.requestLocation() }
                                    .font(.headline).foregroundColor(.white)
                                    .padding(.horizontal, 40).padding(.vertical, 14)
                                    .background(Color.staxPrimary).cornerRadius(14)
                            } else {
                                RadiusSelectorCard(radius: $vm.radiusMiles) {
                                    vm.searchNearby()
                                }
                            }
                        } else if vm.searchMode == .byState {
                            StateSelectorCard(
                                states: vm.availableStates,
                                selected: $vm.selectedState,
                                onSearch: { vm.searchByState() }
                            )
                        }
                    }

                    // Results
                    if vm.isLoading {
                        VStack(spacing: 12) {
                            ProgressView()
                                .progressViewStyle(.circular)
                                .tint(.staxPrimary)
                                .scaleEffect(1.3)
                            Text("Searching card rooms…")
                                .font(.subheadline)
                                .foregroundColor(.staxOnSurfaceVar)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.top, 48)
                    } else if vm.hasSearched {
                        let items = vm.result?.items ?? []
                        if items.isEmpty {
                            emptyState
                        } else {
                            Text("\(items.count) \(vm.searchMode == .favorites ? "favorites" : "card rooms found")")
                                .font(.caption)
                                .foregroundColor(.staxOnSurfaceVar)
                                .frame(maxWidth: .infinity, alignment: .leading)

                            List {
                                ForEach(items) { item in
                                    NavigationLink(destination: CardRoomDetailView(item: item, vm: vm)) {
                                        CardRoomRow(
                                            item: item,
                                            isFavorite: vm.favorites.contains(item.room.address),
                                            isHome: item.room.address == vm.homeCasino,
                                            onFavorite: {
                                                let alreadyFav = vm.favorites.contains(item.room.address)
                                                if alreadyFav {
                                                    vm.toggleFavorite(item.room.address)
                                                } else {
                                                    let result = entitlementManager.checkLimit(
                                                        for: .favorites,
                                                        favoritesCount: vm.favorites.count
                                                    )
                                                    if case .blocked = result {
                                                        showFavoritesLimitToast = true
                                                    } else {
                                                        vm.toggleFavorite(item.room.address)
                                                    }
                                                }
                                            },
                                            onHome: { vm.toggleHome(item.room.address) }
                                        )
                                    }
                                    .listRowBackground(Color.clear)
                                    .listRowInsets(EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0))
                                }
                            }
                            .listStyle(.plain)
                            .background(Color.staxBackground)
                            .scrollContentBackground(.hidden)
                        }
                    }
                }
                .padding(.horizontal, 16)
            }
        }
        .onAppear {
            if vm.locationPermission == .notDetermined {
                vm.requestLocation()
            } else if vm.locationPermission == .authorizedWhenInUse || vm.locationPermission == .authorizedAlways {
                vm.search()
            }
        }
        .alert("Favorites Limit Reached", isPresented: $showFavoritesLimitToast) {
            Button("Upgrade") { showPaywall() }
            Button("Maybe Later", role: .cancel) {}
        } message: {
            Text("Free accounts can save up to \(FreeTierLimits.maxFavorites) favorites. Upgrade to Premium for unlimited favorites.")
        }
        } // NavigationStack
        .navigationBarHidden(true)
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: vm.searchMode == .favorites ? "heart" : "location.slash")
                .font(.system(size: 40))
                .foregroundColor(.staxOnSurfaceVar)
            Text(vm.searchMode == .favorites ? "No Favorites Yet" : "No Card Rooms Found")
                .font(.title3).bold().foregroundColor(.white)
            Text(vm.searchMode == .favorites
                 ? "Tap the heart on any card room to add it to your favorites."
                 : "Try increasing the radius or searching by state.")
                .font(.subheadline).foregroundColor(.staxOnSurfaceVar)
                .multilineTextAlignment(.center)
        }
        .padding(28)
        .frame(maxWidth: .infinity)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(22)
    }
}

// MARK: – Radius selector

struct RadiusSelectorCard: View {
    @Binding var radius: Double
    let onChanged: () -> Void

    var body: some View {
        VStack(spacing: 8) {
            HStack {
                Text("Search radius")
                    .font(.subheadline).foregroundColor(.staxOnSurfaceVar)
                Spacer()
                Text("\(Int(radius)) mi")
                    .font(.subheadline).bold().foregroundColor(.staxPrimary)
            }
            Slider(value: $radius, in: 10...1000, step: 5)
                .tint(.staxPrimary)
                .onChange(of: radius) { _, _ in onChanged() }
            HStack {
                Text("10 mi").font(.caption).foregroundColor(.staxOnSurfaceVar)
                Spacer()
                Text("1000 mi").font(.caption).foregroundColor(.staxOnSurfaceVar)
            }
        }
        .padding(16)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(18)
    }
}

// MARK: – State selector

struct StateSelectorCard: View {
    let states: [String]
    @Binding var selected: String
    let onSearch: () -> Void

    var body: some View {
        DropdownSelector(label: "State", options: states, selected: $selected)
            .onChange(of: selected) { _, _ in onSearch() }
    }
}

// MARK: – Location denied

struct LocationDeniedCard: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "location.slash.fill")
                .font(.system(size: 36)).foregroundColor(.staxOnSurfaceVar)
            Text("Location Access Needed")
                .font(.subheadline).bold().foregroundColor(.white)
            Text("Enable location in Settings to find nearby card rooms.")
                .font(.caption).foregroundColor(.staxOnSurfaceVar)
                .multilineTextAlignment(.center)
            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            .foregroundColor(.staxPrimary)
        }
        .padding(24).frame(maxWidth: .infinity)
        .background(Color.staxSurfaceHigh).cornerRadius(22)
    }
}

// MARK: – Card room row

struct CardRoomRow: View {
    let item: CardRoomWithDistance
    let isFavorite: Bool
    let isHome: Bool
    let onFavorite: () -> Void
    let onHome: () -> Void

    private var logoAssetName: String? {
        guard let logo = item.room.logo else { return nil }
        let base = logo.replacingOccurrences(of: ".png", with: "")
        return UIImage(named: base) != nil ? base : nil
    }

    var body: some View {
        HStack(spacing: 12) {
            // Logo: casino logo if available, STAX logo as fallback
            Group {
                if let assetName = logoAssetName {
                    Image(assetName)
                        .resizable()
                        .scaledToFill()
                } else {
                    Image("StaxLogo")
                        .resizable()
                        .scaledToFill()
                }
            }
            .frame(width: 36, height: 36)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text(item.room.name)
                        .font(.subheadline).bold().foregroundColor(.white)
                        .lineLimit(1)
                    if isHome {
                        Text("HOME")
                            .font(.caption2).bold()
                            .foregroundColor(.staxPrimary)
                            .padding(.horizontal, 5).padding(.vertical, 1)
                            .background(Color.staxPrimary.opacity(0.15))
                            .cornerRadius(4)
                    }
                }
                Text(item.room.address)
                    .font(.caption).foregroundColor(.staxOnSurfaceVar).lineLimit(1)
                if let dist = item.distanceMiles {
                    HStack(spacing: 3) {
                        Image(systemName: "location").font(.caption2).foregroundColor(.staxOnSurfaceVar.opacity(0.7))
                        Text(formatDist(dist)).font(.caption2).foregroundColor(.staxOnSurfaceVar)
                    }
                }
            }

            Spacer()

            HStack(spacing: 4) {
                IconAction(systemName: isFavorite ? "heart.fill" : "heart",
                           tint: isFavorite ? .red : .staxOnSurfaceVar, action: onFavorite)
                IconAction(systemName: isHome ? "house.fill" : "house",
                           tint: isHome ? .staxPrimary : .staxOnSurfaceVar, action: onHome)
                IconAction(systemName: "arrow.triangle.turn.up.right.circle",
                           tint: .staxPrimary) {
                    openMaps(lat: item.room.latitude, lon: item.room.longitude, name: item.room.name)
                }
            }
        }
        .padding(.horizontal, 14).padding(.vertical, 10)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color(red: 0.42, green: 0.11, blue: 0.58), lineWidth: isHome ? 2 : 0)
        )
    }

    private func formatDist(_ m: Double) -> String {
        if m < 0.1 { return "Nearby" }
        if m < 10  { return String(format: "%.1f mi", m) }
        return String(format: "%.0f mi", m)
    }

    private func openMaps(lat: Double, lon: Double, name: String) {
        let coords = CLLocationCoordinate2D(latitude: lat, longitude: lon)
        let item = MKMapItem(placemark: MKPlacemark(coordinate: coords))
        item.name = name
        item.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving])
    }
}

struct IconAction: View {
    let systemName: String
    let tint: Color
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: 18))
                .foregroundColor(tint)
                .frame(width: 34, height: 34)
        }
        .buttonStyle(.plain)
    }
}
