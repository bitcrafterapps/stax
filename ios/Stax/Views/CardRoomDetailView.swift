import SwiftUI
import MapKit

private let homePurple      = Color(red: 0.42, green: 0.11, blue: 0.58)   // #6A1B9A deep
private let homePurpleLight = Color(red: 0.81, green: 0.58, blue: 0.85)   // #CE93D8

struct CardRoomDetailView: View {
    let item: CardRoomWithDistance
    @ObservedObject var vm: FindViewModel

    private var room: CardRoom { item.room }
    private var isFavorite:  Bool { vm.favorites.contains(room.address) }
    private var isHome:      Bool { room.address == vm.homeCasino }

    var body: some View {
        ZStack {
            Color.staxBackground.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 14) {
                    // ── Hero card ──────────────────────────────────────────
                    VStack(spacing: 10) {
                        logoView
                            .frame(width: 100, height: 100)
                            .clipShape(RoundedRectangle(cornerRadius: 20))

                        Text(room.name)
                            .font(.title2).bold()
                            .foregroundColor(.white)
                            .multilineTextAlignment(.center)

                        Text("\(room.city), \(room.state)")
                            .font(.subheadline)
                            .foregroundColor(.staxOnSurfaceVar)

                        if isHome {
                            Text("HOME CASINO")
                                .font(.caption).bold()
                                .foregroundColor(homePurpleLight)
                                .padding(.horizontal, 12).padding(.vertical, 5)
                                .background(homePurple.opacity(0.22))
                                .cornerRadius(8)
                        }
                    }
                    .padding(28)
                    .frame(maxWidth: .infinity)
                    .background(Color.staxSurfaceHigh)
                    .cornerRadius(22)

                    // ── Info card ──────────────────────────────────────────
                    VStack(spacing: 0) {
                        DetailInfoRow(
                            icon: "location.fill",
                            label: "Address",
                            value: room.address
                        )
                        Divider().background(Color.white.opacity(0.08)).padding(.leading, 52)
                        DetailInfoRow(
                            icon: "mappin.circle.fill",
                            label: "City / State",
                            value: "\(room.city), \(room.state)"
                        )
                        if let dist = item.distanceMiles, dist > 0 {
                            Divider().background(Color.white.opacity(0.08)).padding(.leading, 52)
                            DetailInfoRow(
                                icon: "location.north.fill",
                                label: "Distance",
                                value: formatDist(dist)
                            )
                        }
                    }
                    .background(Color.staxSurfaceHigh)
                    .cornerRadius(22)

                    // ── Directions ─────────────────────────────────────────
                    Button(action: openMaps) {
                        Label("Get Directions", systemImage: "arrow.triangle.turn.up.right.circle.fill")
                            .font(.body).bold()
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.staxPrimary)
                    .cornerRadius(16)

                    // ── Favorite + Home ────────────────────────────────────
                    HStack(spacing: 10) {
                        Button(action: { vm.toggleFavorite(room.address) }) {
                            Label(
                                isFavorite ? "Favorited" : "Favorite",
                                systemImage: isFavorite ? "heart.fill" : "heart"
                            )
                            .font(.subheadline).bold()
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(isFavorite ? Color.red : Color.red.opacity(0.18))
                            .foregroundColor(isFavorite ? .white : .red)
                            .cornerRadius(16)
                        }
                        .buttonStyle(.plain)

                        Button(action: { vm.toggleHome(room.address) }) {
                            Label(
                                isHome ? "Home Casino" : "Set as Home",
                                systemImage: isHome ? "house.fill" : "house"
                            )
                            .font(.subheadline).bold()
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(isHome ? homePurple : homePurple.opacity(0.25))
                            .foregroundColor(isHome ? .white : homePurpleLight)
                            .cornerRadius(16)
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(16)
                .padding(.bottom, 40)
            }
        }
        .navigationTitle(room.name)
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: – Logo

    @ViewBuilder
    private var logoView: some View {
        if let logoName = room.logo.map({ assetName(from: $0) }),
           UIImage(named: logoName) != nil {
            Image(logoName)
                .resizable()
                .scaledToFill()
        } else {
            Image("StaxLogo")
                .resizable()
                .scaledToFill()
        }
    }

    // MARK: – Helpers

    private func assetName(from logo: String) -> String {
        let base = logo
            .replacingOccurrences(of: ".png", with: "")
        return base
    }

    private func formatDist(_ m: Double) -> String {
        if m < 0.1 { return "Nearby" }
        if m < 10  { return String(format: "%.1f miles away", m) }
        return String(format: "%.0f miles away", m)
    }

    private func openMaps() {
        let coords = CLLocationCoordinate2D(latitude: room.latitude, longitude: room.longitude)
        let mapItem = MKMapItem(placemark: MKPlacemark(coordinate: coords))
        mapItem.name = room.name
        mapItem.openInMaps(launchOptions: [MKLaunchOptionsDirectionsModeKey: MKLaunchOptionsDirectionsModeDriving])
    }
}

// MARK: – Detail info row

private struct DetailInfoRow: View {
    let icon: String
    let label: String
    let value: String

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(.staxPrimary)
                .frame(width: 24)
            VStack(alignment: .leading, spacing: 2) {
                Text(label)
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
                Text(value)
                    .font(.subheadline)
                    .fontWeight(.medium)
                    .foregroundColor(.white)
            }
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 13)
    }
}
