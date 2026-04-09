import SwiftUI

class ChipConfigRepository {
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    private func key(casino: String, gameType: String) -> String {
        "chip_config_\(casino)_\(gameType)"
    }

    func save(_ configs: [ChipConfig], casino: String, gameType: String) {
        if let data = try? encoder.encode(configs) {
            UserDefaults.standard.set(data, forKey: key(casino: casino, gameType: gameType))
        }
    }

    func load(casino: String, gameType: String) -> [ChipConfig] {
        let k = key(casino: casino, gameType: gameType)
        guard let data = UserDefaults.standard.data(forKey: k),
              let configs = try? decoder.decode([ChipConfig].self, from: data) else {
            return defaults(for: gameType)
        }
        return configs
    }

    func resetToDefaults(casino: String, gameType: String) {
        save(defaults(for: gameType), casino: casino, gameType: gameType)
    }

    func defaults(for gameType: String) -> [ChipConfig] {
        gameType == "Tourney" ? defaultTourney() : defaultCash()
    }

    // MARK: – Cash defaults
    private func defaultCash() -> [ChipConfig] {
        [
            ChipConfig(id: 1,  colorHex: 0xFFF5F5F5, value: "1",      colorName: "white"),
            ChipConfig(id: 2,  colorHex: 0xFFFF69B4, value: "2",      colorName: "pink"),
            ChipConfig(id: 3,  colorHex: 0xFF008080, value: "3",      colorName: "teal"),
            ChipConfig(id: 4,  colorHex: 0xFFFF7F7F, value: "4",      colorName: "coral"),
            ChipConfig(id: 5,  colorHex: 0xFFCC2200, value: "5",      colorName: "red"),
            ChipConfig(id: 6,  colorHex: 0xFF4169E1, value: "10",     colorName: "royal blue"),
            ChipConfig(id: 7,  colorHex: 0xFF32CD32, value: "20",     colorName: "lime"),
            ChipConfig(id: 8,  colorHex: 0xFF228B22, value: "25",     colorName: "green"),
            ChipConfig(id: 9,  colorHex: 0xFF1E90FF, value: "50",     colorName: "blue"),
            ChipConfig(id: 10, colorHex: 0xFF1A1A1A, value: "100",    colorName: "black"),
            ChipConfig(id: 11, colorHex: 0xFF800080, value: "500",    colorName: "purple"),
            ChipConfig(id: 12, colorHex: 0xFFFFD700, value: "1000",   colorName: "yellow"),
            ChipConfig(id: 13, colorHex: 0xFFFFA500, value: "5000",   colorName: "orange"),
            ChipConfig(id: 14, colorHex: 0xFF006400, value: "25000",  colorName: "dark green"),
            ChipConfig(id: 15, colorHex: 0xFF800000, value: "100000", colorName: "burgundy"),
        ]
    }

    // MARK: – Tourney defaults
    private func defaultTourney() -> [ChipConfig] {
        [
            ChipConfig(id: 1, colorHex: 0xFF228B22, value: "25",      colorName: "green"),
            ChipConfig(id: 2, colorHex: 0xFF1A1A1A, value: "100",     colorName: "black"),
            ChipConfig(id: 3, colorHex: 0xFF800080, value: "500",     colorName: "purple"),
            ChipConfig(id: 4, colorHex: 0xFFFFD700, value: "1000",    colorName: "yellow"),
            ChipConfig(id: 5, colorHex: 0xFFFFA500, value: "5000",    colorName: "orange"),
            ChipConfig(id: 6, colorHex: 0xFF800000, value: "25000",   colorName: "burgundy"),
            ChipConfig(id: 7, colorHex: 0xFF9E9E9E, value: "100000",  colorName: "grey"),
            ChipConfig(id: 8, colorHex: 0xFF0D2B6E, value: "1000000", colorName: "dark blue"),
        ]
    }
}
