import Foundation

struct CasinoFolder: Identifiable, Equatable {
    var id: String { casinoName }
    let casinoName: String
    let sessionCount: Int
    let latestPhotoPath: String?    // full filesystem path to most-recent photo
    let logoAssetName: String?      // e.g. "logo_commerce_casino" — nil if no logo
}
