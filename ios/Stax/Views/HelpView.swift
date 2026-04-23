import SwiftUI

struct HelpView: View {
    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(red: 0.10, green: 0.07, blue: 0.18), Color.black],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            ScrollView {
                VStack(spacing: 16) {
                    HelpSection(emoji: "📸", title: "Photos (Home)", items: [
                        "Tap the **+** button to create a new poker session.",
                        "Sessions are grouped by casino or venue in a photo-grid view.",
                        "Tap any casino tile to browse sessions and photos from that venue.",
                        "Each session stores its buy-in, cash-out, and all chip-stack photos.",
                        "Free accounts can track up to **3 sessions**. Upgrade for unlimited."
                    ])

                    HelpSection(emoji: "📋", title: "Sessions", items: [
                        "View all sessions in a list, grouped by casino.",
                        "Filter between Cash, Tournament, or All session types.",
                        "See overall profit/loss, total buy-in, and cash-out at a glance.",
                        "Tap any casino group to drill into individual session details.",
                        "Tap the **+** button to add a new session from this screen.",
                        "Session dates, stakes, antes, and game type are stored for each entry."
                    ])

                    HelpSection(emoji: "🔍", title: "Find Card Rooms", items: [
                        "Search for poker rooms and casinos near your current location.",
                        "**Near Me** mode uses GPS to find rooms within a chosen radius.",
                        "Use **By State** mode to list all rooms in a chosen US state.",
                        "**Favorites** shows rooms you've saved as favorites.",
                        "Tap the ♥ icon on any room's detail page to save it as a favorite.",
                        "Set a **Home Casino** (🏠) so it always floats to the top of your lists.",
                        "Tap **Directions** to open Maps navigation to any room.",
                        "Free accounts can save up to **3 favorites**. Upgrade for unlimited."
                    ])

                    HelpSection(emoji: "📷", title: "Chip-Stack Photos", items: [
                        "From any session's photo gallery, tap the camera icon to take a new chip-stack photo.",
                        "Tap the gallery icon to import an existing photo from your library.",
                        "Swipe through photos in full-screen view.",
                        "Tap the trash icon on a photo to delete it.",
                        "Add captions to photos in the full-screen viewer.",
                        "Rate photos with a star rating for easy reference later.",
                        "Free accounts can add up to **10 photos per session**. Upgrade for unlimited."
                    ])

                    HelpSection(emoji: "🤖", title: "Chip Scanning (Premium)", items: [
                        "Chip Scanning is a **STAX Premium** feature.",
                        "Point your camera at a chip stack and tap **Scan** to count the total value.",
                        "On-device AI uses your configured chip colors and values to estimate the stack.",
                        "**Cloud estimate** sends the camera frame to OpenAI using your saved API key.",
                        "Add your OpenAI API key under **About → OpenAI API Key**.",
                        "Switch between **Cash** and **Tourney** modes to use the correct denominations.",
                        "Use **Train** to capture labelled chip photos and improve on-device accuracy.",
                        "On-device scanning works fully offline — only OpenAI requires internet."
                    ])

                    HelpSection(emoji: "🎰", title: "Chip Configuration", items: [
                        "Access via **About → Chip Configuration**.",
                        "Configure the color and value of each chip denomination for any casino.",
                        "Separate configurations for **Cash** and **Tournament** chip sets.",
                        "The on-device AI scanner uses these configurations to calculate stack totals.",
                        "Free accounts can configure chips for **1 casino**. Upgrade to configure all."
                    ])

                    HelpSection(emoji: "📊", title: "Reports", items: [
                        "Access via **About → Reports**.",
                        "View aggregate stats across all your sessions: total buy-in, net profit/loss.",
                        "Break down results by session type (Cash vs Tournament).",
                        "Filter by date range — last 30 days, 90 days, YTD, or all-time.",
                        "Search and filter by venue, game type, and more."
                    ])

                    HelpSection(emoji: "🏠", title: "Home Games", items: [
                        "When creating a session, switch to **Home Game** mode to log a private game.",
                        "Enter a name, city, and state for the home game venue.",
                        "Home games you've entered are saved in **Saved Home Games** for future sessions.",
                        "Home game sessions appear alongside casino sessions in all views."
                    ])

                    HelpSection(emoji: "🎮", title: "Nutz Game", items: [
                        "Access via **About → Nutz Game**.",
                        "Practice identifying the nuts and second nuts on any board.",
                        "Boards run through flop, turn, and river — test your hand-reading skills.",
                        "Score tracks how often you correctly identify the best possible hand."
                    ])

                    HelpSection(emoji: "⭐", title: "STAX Premium", items: [
                        "Upgrade to Premium from the **About** tab or any feature gate prompt.",
                        "**Unlimited sessions** — no 3-session cap.",
                        "**Unlimited photos** per session — no 10-photo cap.",
                        "**Unlimited favorites** — no 3-favorite cap.",
                        "**Full chip configuration** — configure chips for every casino.",
                        "**Chip scanning** — on-device AI + optional OpenAI cloud estimation.",
                        "A free trial is available when you subscribe for the first time.",
                        "Manage or cancel your subscription at any time from the App Store."
                    ])

                    HelpSection(emoji: "🔑", title: "OpenAI Settings", items: [
                        "Access via **About → OpenAI API Key**.",
                        "Enter your OpenAI API key to enable cloud chip estimation on the Scan tab.",
                        "Your key is stored **only on this device** using the iOS Keychain — never sent to Stax servers.",
                        "When cloud estimation is on, the scan image is sent directly from your device to OpenAI.",
                        "Get an API key at platform.openai.com."
                    ])

                    HelpSection(emoji: "💡", title: "Tips & Tricks", items: [
                        "Tap the **STAX logo** on the About screen for a surprise.",
                        "The state/region dropdown in new-session dialogs auto-detects your location.",
                        "Long-press a session in the Sessions list to access quick-delete.",
                        "Session names are auto-filled from the venue name and date, but you can customise them.",
                        "Share chip-stack photos with a watermark-free export when you're on Premium."
                    ])

                    Spacer().frame(height: 16)
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
            }
        }
        .navigationTitle("Help & Guide")
        .navigationBarTitleDisplayMode(.large)
    }
}

// MARK: – Section card

private struct HelpSection: View {
    let emoji: String
    let title: String
    let items: [String]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 10) {
                Text(emoji)
                    .font(.title3)
                Text(title)
                    .font(.subheadline.bold())
                    .foregroundColor(.white)
                Spacer()
            }
            .padding(.bottom, 10)

            Divider()
                .background(Color.white.opacity(0.12))
                .padding(.bottom, 10)

            VStack(alignment: .leading, spacing: 8) {
                ForEach(items, id: \.self) { item in
                    HelpBullet(text: item)
                }
            }
        }
        .padding(16)
        .background(Color(red: 0.13, green: 0.10, blue: 0.18))
        .cornerRadius(16)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.white.opacity(0.08), lineWidth: 1)
        )
    }
}

// MARK: – Bullet row with inline bold support

private struct HelpBullet: View {
    let text: String

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Text("•")
                .font(.caption)
                .foregroundColor(.staxPrimary)
                .frame(width: 12, alignment: .center)
            BoldableText(text: text)
                .font(.caption)
                .foregroundColor(.white.opacity(0.75))
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}

/// Renders **bold** markers as bold inline text using AttributedString.
private struct BoldableText: View {
    let text: String

    var body: some View {
        Text(attributedString)
    }

    private var attributedString: AttributedString {
        var result = AttributedString()
        let parts = text.components(separatedBy: "**")
        for (idx, part) in parts.enumerated() {
            var segment = AttributedString(part)
            if idx % 2 == 1 {
                segment.font = .caption.bold()
                segment.foregroundColor = .white
            }
            result.append(segment)
        }
        return result
    }
}
