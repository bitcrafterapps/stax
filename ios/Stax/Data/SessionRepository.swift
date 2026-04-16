import Foundation

class SessionRepository: ObservableObject {
    @Published private(set) var sessions: [Session] = []

    private let key = "stax_sessions"
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init() {
        load()
    }

    // MARK: – CRUD

    func addSession(_ session: Session) {
        sessions.append(session)
        save()
    }

    func updateSession(_ session: Session) {
        if let idx = sessions.firstIndex(where: { $0.id == session.id }) {
            sessions[idx] = session
            save()
        }
    }

    func deleteSession(id: UUID, photoRepo: PhotoRepository) {
        photoRepo.deleteAllPhotos(for: id)
        sessions.removeAll { $0.id == id }
        save()
    }

    // MARK: – Derived

    func casinoFolders(photoRepo: PhotoRepository, logoMap: [String: String] = [:]) -> [CasinoFolder] {
        let grouped = Dictionary(grouping: sessions, by: \.casinoName)
        return grouped.map { name, sessionsForCasino -> CasinoFolder in
            let latest = sessionsForCasino.flatMap { photoRepo.photos(for: $0.id) }
                .sorted { $0.dateAdded > $1.dateAdded }
                .first
            let path = latest.map { photoRepo.fullPath(for: $0) }
            return CasinoFolder(
                casinoName: name,
                sessionCount: sessionsForCasino.count,
                latestPhotoPath: path,
                logoAssetName: logoMap[name]
            )
        }
        .sorted { $0.casinoName < $1.casinoName }
    }

    func sessions(for casinoName: String) -> [Session] {
        sessions.filter { $0.casinoName == casinoName }
    }

    // MARK: – Persistence

    private func save() {
        if let data = try? encoder.encode(sessions) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? decoder.decode([Session].self, from: data) else { return }
        sessions = decoded
    }

    static let shared = SessionRepository()
}
