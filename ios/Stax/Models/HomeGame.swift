import Foundation

struct HomeGame: Identifiable, Codable, Equatable {
    var id: UUID = UUID()
    var name: String
    var city: String
    var state: String

    var displayName: String { "\(name) · \(city), \(state)" }
}
