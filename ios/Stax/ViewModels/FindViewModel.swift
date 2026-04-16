import Foundation
import CoreLocation
import Combine

enum SearchMode: String, CaseIterable {
    case nearMe = "Near Me"
    case byState = "By State"
    case favorites = "Favorites"
}

enum FindResult {
    case nearby([CardRoomWithDistance])
    case byState([CardRoomWithDistance], String)
    case favorites([CardRoomWithDistance])

    var items: [CardRoomWithDistance] {
        switch self {
        case .nearby(let i), .byState(let i, _), .favorites(let i): return i
        }
    }
}

class FindViewModel: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let repo = CardRoomRepository()
    private let locationManager = CLLocationManager()

    @Published var result: FindResult? = nil
    @Published var hasSearched = false
    @Published var isLoading = false
    @Published var location: CLLocation? = nil
    @Published var locationPermission: CLAuthorizationStatus = .notDetermined

    @Published var searchMode: SearchMode = .nearMe
    @Published var radiusMiles: Double = 100
    @Published var selectedState: String = ""
    @Published var favorites: Set<String> = []
    @Published var homeCasino: String? = nil

    var availableStates: [String] { repo.availableStates }

    override init() {
        super.init()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
        favorites = repo.getFavorites()
        homeCasino = repo.getHomeCasino()
        selectedState = repo.availableStates.first ?? "California"
    }

    // MARK: – Location permission

    func requestLocation() {
        locationManager.requestWhenInUseAuthorization()
    }

    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        locationPermission = manager.authorizationStatus
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            manager.requestLocation()
        }
    }

    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.first else { return }
        location = loc
        // Detect state and auto-search
        if let state = repo.getStateFromLocation(latitude: loc.coordinate.latitude, longitude: loc.coordinate.longitude),
           repo.availableStates.contains(state) {
            selectedState = state
        }
        if searchMode == .nearMe {
            searchNearby()
        }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {}

    // MARK: – Search

    func search() {
        switch searchMode {
        case .nearMe: searchNearby()
        case .byState: searchByState()
        case .favorites: loadFavorites()
        }
    }

    func searchNearby() {
        guard let loc = location else {
            if locationPermission == .authorizedWhenInUse || locationPermission == .authorizedAlways {
                locationManager.requestLocation()
            }
            return
        }
        isLoading = true
        Task {
            let items = repo.searchNearby(latitude: loc.coordinate.latitude, longitude: loc.coordinate.longitude, radiusMiles: radiusMiles)
            let sorted = CardRoomRepository.sortWithFavorites(items, favorites: favorites, homeCasino: homeCasino)
            await MainActor.run {
                result = .nearby(sorted)
                hasSearched = true
                isLoading = false
            }
        }
    }

    func searchByState() {
        let state = selectedState
        let lat = location?.coordinate.latitude
        let lon = location?.coordinate.longitude
        isLoading = true
        Task {
            let items = repo.searchByState(state, latitude: lat, longitude: lon)
            let sorted = CardRoomRepository.sortWithFavorites(items, favorites: favorites, homeCasino: homeCasino)
            await MainActor.run {
                result = .byState(sorted, state)
                hasSearched = true
                isLoading = false
            }
        }
    }

    func loadFavorites() {
        let lat = location?.coordinate.latitude
        let lon = location?.coordinate.longitude
        isLoading = true
        Task {
            let items = repo.getFavoriteRooms(latitude: lat, longitude: lon)
            await MainActor.run {
                result = .favorites(items)
                hasSearched = true
                isLoading = false
            }
        }
    }

    // MARK: – Favorites & home

    func toggleFavorite(_ address: String) {
        favorites = repo.toggleFavorite(address)
        homeCasino = repo.getHomeCasino()
        if searchMode == .favorites { loadFavorites() }
    }

    func toggleHome(_ address: String) {
        let current = homeCasino
        homeCasino = repo.setHomeCasino(current == address ? nil : address)
        favorites = repo.getFavorites()
        if searchMode == .favorites { loadFavorites() }
    }
}
