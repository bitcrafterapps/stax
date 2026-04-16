import Foundation

class HomeGameRepository: ObservableObject {
    static let shared = HomeGameRepository()

    @Published private(set) var homeGames: [HomeGame] = []

    private let key = "stax_home_games"
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init() {
        load()
    }

    func addOrUpdate(name: String, city: String, state: String) {
        let trimmedName = name.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedCity = city.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedState = state.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedName.isEmpty, !trimmedCity.isEmpty, !trimmedState.isEmpty else { return }

        homeGames.removeAll {
            $0.name.caseInsensitiveCompare(trimmedName) == .orderedSame &&
            $0.city.caseInsensitiveCompare(trimmedCity) == .orderedSame &&
            $0.state.caseInsensitiveCompare(trimmedState) == .orderedSame
        }
        homeGames.append(HomeGame(name: trimmedName, city: trimmedCity, state: trimmedState))
        homeGames.sort {
            ($0.name.lowercased(), $0.city.lowercased(), $0.state.lowercased()) <
            ($1.name.lowercased(), $1.city.lowercased(), $1.state.lowercased())
        }
        save()
    }

    private func save() {
        if let data = try? encoder.encode(homeGames) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? decoder.decode([HomeGame].self, from: data) else { return }
        homeGames = decoded
    }
}
