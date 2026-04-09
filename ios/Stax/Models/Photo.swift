import Foundation

struct Photo: Identifiable, Codable, Equatable {
    var id: UUID = UUID()
    var sessionId: UUID
    var fileName: String      // stored under Documents/StaxPhotos/<fileName>
    var rating: Int           // 0–5
    var dateAdded: Date = Date()

    static func == (lhs: Photo, rhs: Photo) -> Bool { lhs.id == rhs.id }
}
