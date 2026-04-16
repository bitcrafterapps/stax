import Foundation
import CoreLocation

class CardRoomRepository {
    static let shared = CardRoomRepository()

    private(set) var allRooms: [CardRoom] = []
    private(set) var availableStates: [String] = []

    private let favKey   = "stax_favorites"
    private let homeKey  = "stax_home_casino"

    init() {
        loadRooms()
    }

    // MARK: – Load

    private func loadRooms() {
        guard let url = Bundle.main.url(forResource: "cardrooms", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let rooms = try? JSONDecoder().decode([CardRoom].self, from: data) else { return }
        allRooms = rooms
        availableStates = Array(Set(rooms.map(\.state))).sorted()
    }

    // MARK: – Search

    func searchNearby(latitude: Double, longitude: Double, radiusMiles: Double) -> [CardRoomWithDistance] {
        let origin = CLLocation(latitude: latitude, longitude: longitude)
        return allRooms.compactMap { room -> CardRoomWithDistance? in
            let loc = CLLocation(latitude: room.latitude, longitude: room.longitude)
            let miles = loc.distance(from: origin) / 1609.344
            guard miles <= radiusMiles else { return nil }
            return CardRoomWithDistance(room: room, distanceMiles: miles)
        }
        .sorted { ($0.distanceMiles ?? .infinity) < ($1.distanceMiles ?? .infinity) }
    }

    func searchByState(_ state: String, latitude: Double? = nil, longitude: Double? = nil) -> [CardRoomWithDistance] {
        let filtered = allRooms.filter { $0.state.lowercased() == state.lowercased() }
        return filtered.map { room -> CardRoomWithDistance in
            var dist: Double? = nil
            if let lat = latitude, let lon = longitude {
                let origin = CLLocation(latitude: lat, longitude: lon)
                let loc = CLLocation(latitude: room.latitude, longitude: room.longitude)
                dist = loc.distance(from: origin) / 1609.344
            }
            return CardRoomWithDistance(room: room, distanceMiles: dist)
        }
        .sorted { ($0.distanceMiles ?? .infinity) < ($1.distanceMiles ?? .infinity) }
    }

    func getFavoriteRooms(latitude: Double? = nil, longitude: Double? = nil) -> [CardRoomWithDistance] {
        let favs = getFavorites()
        let favRooms = allRooms.filter { favs.contains($0.address) }
        return favRooms.map { room -> CardRoomWithDistance in
            var dist: Double? = nil
            if let lat = latitude, let lon = longitude {
                let origin = CLLocation(latitude: lat, longitude: lon)
                let loc = CLLocation(latitude: room.latitude, longitude: room.longitude)
                dist = loc.distance(from: origin) / 1609.344
            }
            return CardRoomWithDistance(room: room, distanceMiles: dist)
        }
    }

    // MARK: – State from location (simple bounding-box approximation)

    func getStateFromLocation(latitude: Double, longitude: Double) -> String? {
        // Find the nearest room and use its state
        let origin = CLLocation(latitude: latitude, longitude: longitude)
        return allRooms
            .min(by: {
                CLLocation(latitude: $0.latitude, longitude: $0.longitude).distance(from: origin) <
                CLLocation(latitude: $1.latitude, longitude: $1.longitude).distance(from: origin)
            })?.state
    }

    // MARK: – Favorites

    func getFavorites() -> Set<String> {
        let arr = UserDefaults.standard.stringArray(forKey: favKey) ?? []
        return Set(arr)
    }

    @discardableResult
    func toggleFavorite(_ address: String) -> Set<String> {
        var favs = getFavorites()
        if favs.contains(address) { favs.remove(address) } else { favs.insert(address) }
        UserDefaults.standard.set(Array(favs), forKey: favKey)
        return favs
    }

    // MARK: – Home casino

    func getHomeCasino() -> String? {
        UserDefaults.standard.string(forKey: homeKey)
    }

    @discardableResult
    func setHomeCasino(_ address: String?) -> String? {
        UserDefaults.standard.set(address, forKey: homeKey)
        if let address = address {
            var favs = getFavorites()
            favs.insert(address)
            UserDefaults.standard.set(Array(favs), forKey: favKey)
        }
        return address
    }

    // MARK: – Logo lookup

    /// Returns the asset name for a casino's logo given its display name.
    /// Matches Android's:  "logo_" + logo.removeSuffix(".png").replace('-','_')
    func logoAssetName(for casinoName: String) -> String? {
        // Find the room whose name matches (case-insensitive)
        guard let room = allRooms.first(where: { $0.name.lowercased() == casinoName.lowercased() }),
              let logo = room.logo else { return nil }
        let stem = logo
            .replacingOccurrences(of: ".png", with: "")
            .replacingOccurrences(of: "-", with: "_")
        return "logo_\(stem)"
    }

    // MARK: – Sorting (favorites + home first)

    static func sortWithFavorites(
        _ items: [CardRoomWithDistance],
        favorites: Set<String>,
        homeCasino: String?
    ) -> [CardRoomWithDistance] {
        items.sorted { a, b in
            let aHome = a.room.address == homeCasino
            let bHome = b.room.address == homeCasino
            if aHome != bHome { return aHome }
            let aFav = favorites.contains(a.room.address)
            let bFav = favorites.contains(b.room.address)
            if aFav != bFav { return aFav }
            return (a.distanceMiles ?? .infinity) < (b.distanceMiles ?? .infinity)
        }
    }
}
