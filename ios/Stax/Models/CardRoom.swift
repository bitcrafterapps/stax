import Foundation

struct CardRoom: Codable, Identifiable, Equatable {
    var id: String { address }
    let name: String
    let city: String
    let state: String
    let address: String
    let latitude: Double
    let longitude: Double
    let logo: String?
}

struct CardRoomWithDistance: Identifiable {
    let room: CardRoom
    let distanceMiles: Double?
    var id: String { room.id }
}
