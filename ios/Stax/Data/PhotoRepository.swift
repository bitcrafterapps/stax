import Foundation
import UIKit

class PhotoRepository: ObservableObject {
    static let shared = PhotoRepository()

    @Published private(set) var allPhotos: [Photo] = []

    private let key = "stax_photos"
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    private var photosDirectory: URL {
        let docs = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let dir = docs.appendingPathComponent("StaxPhotos")
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        return dir
    }

    init() {
        load()
    }

    // MARK: – Query

    func photos(for sessionId: UUID) -> [Photo] {
        allPhotos.filter { $0.sessionId == sessionId }
            .sorted { $0.dateAdded < $1.dateAdded }
    }

    func fullPath(for photo: Photo) -> String {
        photosDirectory.appendingPathComponent(photo.fileName).path
    }

    func fullURL(for photo: Photo) -> URL {
        photosDirectory.appendingPathComponent(photo.fileName)
    }

    // MARK: – Add

    @discardableResult
    func savePhoto(sessionId: UUID, image: UIImage) -> Photo? {
        let fileName = "\(UUID().uuidString).jpg"
        let url = photosDirectory.appendingPathComponent(fileName)
        guard let data = image.jpegData(compressionQuality: 0.9) else { return nil }
        try? data.write(to: url)
        let photo = Photo(sessionId: sessionId, fileName: fileName, rating: 0)
        allPhotos.append(photo)
        save()
        return photo
    }

    // MARK: – Update

    func updateRating(photoId: UUID, rating: Int) {
        if let idx = allPhotos.firstIndex(where: { $0.id == photoId }) {
            allPhotos[idx].rating = rating
            save()
        }
    }

    // MARK: – Delete

    func deletePhoto(id: UUID) {
        if let photo = allPhotos.first(where: { $0.id == id }) {
            let url = fullURL(for: photo)
            try? FileManager.default.removeItem(at: url)
        }
        allPhotos.removeAll { $0.id == id }
        save()
    }

    func deleteAllPhotos(for sessionId: UUID) {
        let toDelete = allPhotos.filter { $0.sessionId == sessionId }
        for photo in toDelete {
            try? FileManager.default.removeItem(at: fullURL(for: photo))
        }
        allPhotos.removeAll { $0.sessionId == sessionId }
        save()
    }

    // MARK: – Persistence

    private func save() {
        if let data = try? encoder.encode(allPhotos) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? decoder.decode([Photo].self, from: data) else { return }
        allPhotos = decoded
    }
}
