import SwiftUI

struct ChipConfig: Identifiable, Codable, Equatable {
    var id: Int
    var colorHex: Int         // ARGB int (same encoding as Android Color.toArgb())
    var value: String         // denomination string e.g. "25", "1000"
    var colorName: String

    var color: Color {
        Color(argb: colorHex)
    }
}

extension Color {
    /// Construct from Android-style ARGB int
    init(argb: Int) {
        let a = Double((argb >> 24) & 0xFF) / 255.0
        let r = Double((argb >> 16) & 0xFF) / 255.0
        let g = Double((argb >> 8)  & 0xFF) / 255.0
        let b = Double( argb        & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: a)
    }

    /// Convert to Android-style ARGB int for persistence
    var argb: Int {
        guard let components = UIColor(self).cgColor.components else { return 0xFF_00_00_00 }
        let r = Int((components[0] * 255).rounded()) & 0xFF
        let g = Int((components[1] * 255).rounded()) & 0xFF
        let b = Int((components[2] * 255).rounded()) & 0xFF
        let a = components.count > 3 ? Int((components[3] * 255).rounded()) & 0xFF : 0xFF
        return (a << 24) | (r << 16) | (g << 8) | b
    }

    var luminance: Double {
        guard let c = UIColor(self).cgColor.components else { return 0 }
        return 0.2126 * Double(c[0]) + 0.7152 * Double(c[1]) + 0.0722 * Double(c[2])
    }
}
