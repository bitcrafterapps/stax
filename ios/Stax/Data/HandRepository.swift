import Foundation
import Combine

class HandRepository: ObservableObject {
    static let shared = HandRepository()

    @Published private(set) var hands: [Hand] = []

    private let key = "stax_hands"

    init() { load() }

    // MARK: – Persistence

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? JSONDecoder().decode([Hand].self, from: data)
        else { return }
        hands = decoded
    }

    private func save() {
        if let data = try? JSONEncoder().encode(hands) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    // MARK: – Queries

    func hands(for sessionId: UUID) -> [Hand] {
        hands.filter { $0.sessionId == sessionId }.sorted { $0.timestamp > $1.timestamp }
    }

    // MARK: – Mutations

    func addHand(_ hand: Hand) {
        hands.insert(hand, at: 0)
        save()
    }

    func toggleStar(id: UUID) {
        if let idx = hands.firstIndex(where: { $0.id == id }) {
            hands[idx].isStarred.toggle()
            save()
        }
    }

    func deleteHand(id: UUID) {
        hands.removeAll { $0.id == id }
        save()
    }
}
