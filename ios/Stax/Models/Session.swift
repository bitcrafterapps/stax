import Foundation

struct Session: Identifiable, Codable, Equatable {
    var id: UUID = UUID()
    var name: String
    var casinoName: String
    var date: String
    var type: String          // "Cash" or "Tourney"
    var game: String          // legacy field
    var gameType: String      // e.g. "NLH", "PLO"
    var stakes: String        // e.g. "1/2"
    var antes: String         // e.g. "None"
    var buyInAmount: Double
    var cashOutAmount: Double

    var profitLoss: Double { cashOutAmount - buyInAmount }

    static func == (lhs: Session, rhs: Session) -> Bool { lhs.id == rhs.id }
}
