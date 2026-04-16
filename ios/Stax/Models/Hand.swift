import Foundation

struct VillainCards: Codable {
    var card1Rank: String = ""
    var card1Suit: String = ""
    var card2Rank: String = ""
    var card2Suit: String = ""
}

struct Hand: Codable, Identifiable {
    var id: UUID = UUID()
    var sessionId: UUID
    var timestamp: String = ""
    var holeCard1Rank: String = ""
    var holeCard1Suit: String = ""
    var holeCard2Rank: String = ""
    var holeCard2Suit: String = ""
    var position: String = ""
    var result: String = ""   // "Won", "Lost", "Folded"
    var notes: String = ""
    var isStarred: Bool = false
    var villains: [VillainCards] = []
}
