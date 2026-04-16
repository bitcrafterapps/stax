import Foundation
import Combine

class SessionsViewModel: ObservableObject {
    let sessionRepo: SessionRepository
    let photoRepo: PhotoRepository
    let handRepo: HandRepository
    let homeGameRepo: HomeGameRepository

    @Published var casinoData: [String: [String]] = [:]
    private(set) var casinoLogoMap: [String: String] = [:]  // casinoName → asset name

    private var cancellables = Set<AnyCancellable>()

    init(
        sessionRepo: SessionRepository = .shared,
        photoRepo: PhotoRepository = .shared,
        handRepo: HandRepository = .shared,
        homeGameRepo: HomeGameRepository = .shared
    ) {
        self.sessionRepo = sessionRepo
        self.photoRepo = photoRepo
        self.handRepo = handRepo
        self.homeGameRepo = homeGameRepo

        // Load casino dropdown data
        if let url = Bundle.main.url(forResource: "casinos", withExtension: "json"),
           let data = try? Data(contentsOf: url),
           let dict = try? JSONDecoder().decode([String: [String]].self, from: data) {
            casinoData = dict
        }

        // Build logo map from cardrooms.json  (casinoName → "logo_xxx" asset name)
        if let url = Bundle.main.url(forResource: "cardrooms", withExtension: "json"),
           let data = try? Data(contentsOf: url),
           let rooms = try? JSONDecoder().decode([CardRoom].self, from: data) {
            var map: [String: String] = [:]
            for room in rooms {
                if let logo = room.logo {
                    let stem = logo
                        .replacingOccurrences(of: ".png", with: "")
                        .replacingOccurrences(of: "-", with: "_")
                    map[room.name] = "logo_\(stem)"
                }
            }
            casinoLogoMap = map
        }

        // Republish changes from underlying repos
        sessionRepo.objectWillChange.sink { [weak self] in
            self?.objectWillChange.send()
        }.store(in: &cancellables)

        photoRepo.objectWillChange.sink { [weak self] in
            self?.objectWillChange.send()
        }.store(in: &cancellables)

        handRepo.objectWillChange.sink { [weak self] in
            self?.objectWillChange.send()
        }.store(in: &cancellables)

        homeGameRepo.objectWillChange.sink { [weak self] in
            self?.objectWillChange.send()
        }.store(in: &cancellables)
    }

    // MARK: – Hand operations

    func hands(for sessionId: UUID) -> [Hand] { handRepo.hands(for: sessionId) }

    func addHand(
        sessionId: UUID,
        holeCard1Rank: String, holeCard1Suit: String,
        holeCard2Rank: String, holeCard2Suit: String,
        position: String, result: String, notes: String,
        villains: [VillainCards] = []
    ) {
        let fmt = DateFormatter()
        fmt.dateFormat = "MMM d, h:mm a"
        let hand = Hand(
            sessionId: sessionId,
            timestamp: fmt.string(from: Date()),
            holeCard1Rank: holeCard1Rank, holeCard1Suit: holeCard1Suit,
            holeCard2Rank: holeCard2Rank, holeCard2Suit: holeCard2Suit,
            position: position, result: result, notes: notes,
            villains: villains
        )
        handRepo.addHand(hand)
    }

    func toggleStarHand(id: UUID) { handRepo.toggleStar(id: id) }

    func deleteHand(id: UUID) { handRepo.deleteHand(id: id) }

    var sessions: [Session] { sessionRepo.sessions }
    var homeGames: [HomeGame] { homeGameRepo.homeGames }

    var casinoFolders: [CasinoFolder] { sessionRepo.casinoFolders(photoRepo: photoRepo, logoMap: casinoLogoMap) }

    func addSession(
        name: String,
        casinoName: String,
        date: String,
        type: String,
        game: String,
        gameType: String,
        stakes: String,
        antes: String,
        buyIn: Double,
        cashOut: Double
    ) {
        let session = Session(
            name: name,
            casinoName: casinoName,
            date: date,
            type: type,
            game: game,
            gameType: gameType,
            stakes: stakes,
            antes: antes,
            buyInAmount: buyIn,
            cashOutAmount: cashOut
        )
        sessionRepo.addSession(session)
    }

    func deleteSession(id: UUID) {
        sessionRepo.deleteSession(id: id, photoRepo: photoRepo)
    }

    func updateSession(_ session: Session) {
        sessionRepo.updateSession(session)
    }

    func saveHomeGame(name: String, city: String, state: String) {
        homeGameRepo.addOrUpdate(name: name, city: city, state: state)
    }

}
