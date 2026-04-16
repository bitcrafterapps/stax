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
    var notes: String = ""
    var timeIn: String = ""
    var timeOut: String = ""

    var profitLoss: Double { cashOutAmount - buyInAmount }

    static func == (lhs: Session, rhs: Session) -> Bool { lhs.id == rhs.id }

    init(
        id: UUID = UUID(),
        name: String,
        casinoName: String,
        date: String,
        type: String,
        game: String,
        gameType: String,
        stakes: String,
        antes: String,
        buyInAmount: Double,
        cashOutAmount: Double,
        notes: String = "",
        timeIn: String = "",
        timeOut: String = ""
    ) {
        self.id = id
        self.name = name
        self.casinoName = casinoName
        self.date = date
        self.type = type
        self.game = game
        self.gameType = gameType
        self.stakes = stakes
        self.antes = antes
        self.buyInAmount = buyInAmount
        self.cashOutAmount = cashOutAmount
        self.notes = notes
        self.timeIn = timeIn
        self.timeOut = timeOut
    }

    private enum CodingKeys: String, CodingKey {
        case id
        case name
        case casinoName
        case date
        case type
        case game
        case gameType
        case stakes
        case antes
        case buyInAmount
        case cashOutAmount
        case notes
        case timeIn
        case timeOut
    }

    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decodeIfPresent(UUID.self, forKey: .id) ?? UUID()
        name = try container.decode(String.self, forKey: .name)
        casinoName = try container.decode(String.self, forKey: .casinoName)
        date = try container.decode(String.self, forKey: .date)
        type = try container.decode(String.self, forKey: .type)
        game = try container.decode(String.self, forKey: .game)
        gameType = try container.decode(String.self, forKey: .gameType)
        stakes = try container.decode(String.self, forKey: .stakes)
        antes = try container.decode(String.self, forKey: .antes)
        buyInAmount = try container.decode(Double.self, forKey: .buyInAmount)
        cashOutAmount = try container.decode(Double.self, forKey: .cashOutAmount)
        notes = try container.decodeIfPresent(String.self, forKey: .notes) ?? ""
        timeIn = try container.decodeIfPresent(String.self, forKey: .timeIn) ?? ""
        timeOut = try container.decodeIfPresent(String.self, forKey: .timeOut) ?? ""
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(id, forKey: .id)
        try container.encode(name, forKey: .name)
        try container.encode(casinoName, forKey: .casinoName)
        try container.encode(date, forKey: .date)
        try container.encode(type, forKey: .type)
        try container.encode(game, forKey: .game)
        try container.encode(gameType, forKey: .gameType)
        try container.encode(stakes, forKey: .stakes)
        try container.encode(antes, forKey: .antes)
        try container.encode(buyInAmount, forKey: .buyInAmount)
        try container.encode(cashOutAmount, forKey: .cashOutAmount)
        try container.encode(notes, forKey: .notes)
        try container.encode(timeIn, forKey: .timeIn)
        try container.encode(timeOut, forKey: .timeOut)
    }
}
