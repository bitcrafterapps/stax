import Foundation
import Combine

class SessionsViewModel: ObservableObject {
    let sessionRepo: SessionRepository
    let photoRepo: PhotoRepository

    @Published var casinoData: [String: [String]] = [:]

    private var cancellables = Set<AnyCancellable>()

    init(sessionRepo: SessionRepository = .shared, photoRepo: PhotoRepository = .shared) {
        self.sessionRepo = sessionRepo
        self.photoRepo = photoRepo
        loadCasinoData()

        // Republish changes from underlying repos
        sessionRepo.objectWillChange.sink { [weak self] in
            self?.objectWillChange.send()
        }.store(in: &cancellables)

        photoRepo.objectWillChange.sink { [weak self] in
            self?.objectWillChange.send()
        }.store(in: &cancellables)
    }

    var sessions: [Session] { sessionRepo.sessions }

    var casinoFolders: [CasinoFolder] { sessionRepo.casinoFolders(photoRepo: photoRepo) }

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

    // MARK: – Casino data

    private func loadCasinoData() {
        DispatchQueue.global().async { [weak self] in
            guard let url = Bundle.main.url(forResource: "casinos", withExtension: "json"),
                  let data = try? Data(contentsOf: url),
                  let dict = try? JSONDecoder().decode([String: [String]].self, from: data) else { return }
            DispatchQueue.main.async {
                self?.casinoData = dict
            }
        }
    }
}
