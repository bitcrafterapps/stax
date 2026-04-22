import SwiftUI
import AVFoundation
import StoreKit

struct AboutView: View {
    @ObservedObject var vm: SessionsViewModel
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @Environment(\.showPaywall) private var showPaywall

    @State private var apiKey: String = KeychainHelper.load(forKey: KeychainHelper.Key.openAIApiKey) ?? ""
    @State private var showApiKey = false
    @State private var showChipConfig = false
    @State private var stackedChips = 0
    @State private var stackRunID = UUID()

    var body: some View {
        NavigationStack {
            ZStack {
                LinearGradient(
                    colors: [Color(red: 0.10, green: 0.07, blue: 0.18), Color.black],
                    startPoint: .top, endPoint: .bottom
                )
                .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 32) {

                        // Logo + tagline
                        VStack(spacing: 16) {
                            ZStack(alignment: .bottom) {
                                if stackedChips > 0 {
                                    ForEach(Array((1...stackedChips).reversed()), id: \.self) { index in
                                        StaxLogoImage(size: 120)
                                            .clipShape(Circle())
                                            .offset(y: CGFloat(-14 * index))
                                            .transition(.opacity)
                                    }
                                }
                                StaxLogoImage(size: 120)
                                    .clipShape(Circle())
                                    .onTapGesture {
                                        let runID = UUID()
                                        stackRunID = runID
                                        stackedChips = 0
                                        ChipClickPlayer.shared.play()
                                        for index in 1...9 {
                                            DispatchQueue.main.asyncAfter(deadline: .now() + (Double(index) * 0.055)) {
                                                guard stackRunID == runID else { return }
                                                withAnimation(.easeOut(duration: 0.12)) {
                                                    stackedChips = index
                                                }
                                            }
                                        }
                                        DispatchQueue.main.asyncAfter(deadline: .now() + 1.05) {
                                            guard stackRunID == runID else { return }
                                            withAnimation(.easeInOut(duration: 0.18)) {
                                                stackedChips = 0
                                            }
                                        }
                                    }
                            }
                            .frame(width: 120, height: 260, alignment: .bottom)
                            VStack(spacing: 6) {
                                Text("STAX")
                                    .font(.system(size: 36, weight: .black, design: .rounded))
                                    .foregroundColor(.white)
                                    .tracking(6)
                        Text("Stack it. Snap it. Track it.")
                                .font(.subheadline)
                                .foregroundColor(.white.opacity(0.55))
                                .tracking(1)
                                Text("v1.0.0")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.35))
                            }
                        }
                        .padding(.top, 32)

                        // Settings cards
                        VStack(spacing: 12) {

                            // STAX Premium section
                            premiumSection

                            // OpenAI API Key
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Image(systemName: "key.horizontal.fill")
                                        .foregroundColor(.staxPrimary)
                                    Text("OpenAI API Key")
                                        .font(.subheadline).bold().foregroundColor(.white)
                                    Spacer()
                                    Button {
                                        showApiKey.toggle()
                                    } label: {
                                        Image(systemName: showApiKey ? "eye.slash" : "eye")
                                            .foregroundColor(.staxOnSurfaceVar)
                                    }
                                }

                                Group {
                                    if showApiKey {
                                        TextField("sk-…", text: $apiKey)
                                    } else {
                                        SecureField("sk-…", text: $apiKey)
                                    }
                                }
                                .textFieldStyle(.plain)
                                .padding(12)
                                .background(Color.staxBackground)
                                .cornerRadius(10)
                                .foregroundColor(.white)
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)

                                Button {
                                    KeychainHelper.save(apiKey, forKey: KeychainHelper.Key.openAIApiKey)
                                    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder),
                                                                    to: nil, from: nil, for: nil)
                                } label: {
                                    Text("Save Key")
                                        .font(.subheadline).bold()
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 12)
                                        .background(Color.staxPrimary)
                                        .cornerRadius(12)
                                }

                                Text("Used for the AI chip-counting feature on the Scan tab. Your key is stored locally and never sent anywhere except directly to OpenAI.")
                                    .font(.caption)
                                    .foregroundColor(.staxOnSurfaceVar)
                            }
                            .padding(16)
                            .background(Color.staxSurface)
                            .cornerRadius(18)

                            // Chip configuration
                            Button { showChipConfig = true } label: {
                                HStack {
                                    Image(systemName: "circle.hexagongrid.fill")
                                        .foregroundColor(.staxPrimary)
                                        .font(.title3)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Chip Configuration")
                                            .font(.subheadline).bold().foregroundColor(.white)
                                        Text("Customize chip values and colors")
                                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.staxOnSurfaceVar)
                                }
                                .padding(16)
                                .background(Color.staxSurface)
                                .cornerRadius(18)
                            }
                            .buttonStyle(.plain)

                            NavigationLink(destination: ReportsView(vm: vm)) {
                                HStack {
                                    Image(systemName: "chart.bar.xaxis")
                                        .foregroundColor(.staxPrimary)
                                        .font(.title3)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Reports")
                                            .font(.subheadline).bold().foregroundColor(.white)
                                        Text("Roll up sessions with filters and search")
                                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.staxOnSurfaceVar)
                                }
                                .padding(16)
                                .background(Color.staxSurface)
                                .cornerRadius(18)
                            }
                            .buttonStyle(.plain)

                            NavigationLink(destination: NutzGameView()) {
                                HStack {
                                    Image(systemName: "suit.club.fill")
                                        .foregroundColor(.staxPrimary)
                                        .font(.title3)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Nutz Game")
                                            .font(.subheadline).bold().foregroundColor(.white)
                                        Text("Practice the nuts and second nuts on flop, turn, and river")
                                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.staxOnSurfaceVar)
                                }
                                .padding(16)
                                .background(Color.staxSurface)
                                .cornerRadius(18)
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.horizontal, 16)

                        // Footer
                        VStack(spacing: 4) {
                            Text("Built for poker players who want to track their stacks.")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.35))
                                .multilineTextAlignment(.center)
                        }
                        .padding(.bottom, 32)
                    }
                }
            }
            .navigationTitle("")
            .navigationBarHidden(true)
        }
        .sheet(isPresented: $showChipConfig) {
            ChipConfigView()
        }
    }

    // MARK: – Premium section

    @ViewBuilder
    private var premiumSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "crown.fill")
                    .foregroundColor(.staxPrimary)
                Text("STAX Premium")
                    .font(.subheadline).bold().foregroundColor(.white)
                Spacer()
            }

            switch entitlementManager.subscriptionState {
            case .free:
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Free Plan")
                            .font(.subheadline).foregroundColor(.white)
                        Text("Upgrade to unlock all features")
                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                    }
                    Spacer()
                    Button("Upgrade") { showPaywall() }
                        .font(.subheadline.bold())
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.staxPrimary)
                        .cornerRadius(10)
                }

            case .premium(let inTrial, _):
                if inTrial {
                    let days = entitlementManager.getTrialDaysRemaining()
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Trial — \(days) \(days == 1 ? "day" : "days") remaining")
                                .font(.subheadline).foregroundColor(.white)
                            Text("All features unlocked")
                                .font(.caption).foregroundColor(.staxProfit)
                        }
                        Spacer()
                        Button("Upgrade Now") { showPaywall() }
                            .font(.caption.bold())
                            .foregroundColor(.white)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 7)
                            .background(Color.staxPrimary)
                            .cornerRadius(10)
                    }
                } else {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            HStack(spacing: 4) {
                                Text("Premium")
                                    .font(.subheadline.bold()).foregroundColor(.white)
                                Image(systemName: "checkmark.seal.fill")
                                    .font(.caption).foregroundColor(.staxProfit)
                            }
                            Text("All features unlocked")
                                .font(.caption).foregroundColor(.staxProfit)
                        }
                        Spacer()
                        Button("Manage") {
                            Task {
                                guard let scene = UIApplication.shared.connectedScenes
                                    .first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene
                                else { return }
                                try? await AppStore.showManageSubscriptions(in: scene)
                            }
                        }
                        .font(.caption.bold())
                        .foregroundColor(.staxPrimary)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 7)
                        .background(Color.staxPrimary.opacity(0.15))
                        .cornerRadius(10)
                    }
                }

            case .expired:
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Subscription Expired")
                            .font(.subheadline).foregroundColor(.staxLoss)
                        Text("Resubscribe to restore access")
                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                    }
                    Spacer()
                    Button("Resubscribe") { showPaywall() }
                        .font(.subheadline.bold())
                        .foregroundColor(.white)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.staxPrimary)
                        .cornerRadius(10)
                }
            }

            #if DEBUG
            Divider().background(Color.white.opacity(0.12))
            HStack {
                Text("Debug: Premium")
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
                Spacer()
                Toggle("", isOn: Binding(
                    get: { entitlementManager.isPremium },
                    set: { on in
                        if on {
                            entitlementManager.setDebugPremium()
                        } else {
                            entitlementManager.setFree()
                        }
                    }
                ))
                .tint(.staxPrimary)
                .labelsHidden()
            }
            #endif
        }
        .padding(16)
        .background(Color.staxSurface)
        .cornerRadius(18)
    }

}

private enum ReportDateRange: String, CaseIterable, Identifiable {
    case all = "All"
    case last30 = "30D"
    case last90 = "90D"
    case ytd = "YTD"

    var id: String { rawValue }
}

private struct ReportsView: View {
    @ObservedObject var vm: SessionsViewModel

    @State private var search = ""
    @State private var dateRange: ReportDateRange = .all
    @State private var venueType = "All"
    @State private var sessionType = "All"
    @State private var selectedVenue = "All venues"
    @State private var selectedGameType = "All games"

    private var venueOptions: [String] {
        ["All venues"] + Array(Set(vm.sessions.map(\.casinoName))).sorted()
    }

    private var gameTypeOptions: [String] {
        ["All games"] + Array(Set(vm.sessions.map(\.gameType).filter { !$0.isEmpty })).sorted()
    }

    private var filteredSessions: [Session] {
        vm.sessions.filter { session in
            let venueKind = reportVenueType(for: session, homeGames: vm.homeGames)
            let searchTarget = [session.name, session.casinoName, session.gameType, session.stakes, session.antes]
                .joined(separator: " ")
            let matchesSearch = search.isEmpty || searchTarget.localizedCaseInsensitiveContains(search)
            let matchesVenueType = venueType == "All" || venueKind == venueType
            let matchesSessionType = sessionType == "All" || session.type == sessionType
            let matchesVenue = selectedVenue == "All venues" || session.casinoName == selectedVenue
            let matchesGame = selectedGameType == "All games" || session.gameType == selectedGameType
            let matchesDate = sessionMatchesDateRange(session.date, dateRange: dateRange)
            return matchesSearch && matchesVenueType && matchesSessionType && matchesVenue && matchesGame && matchesDate
        }
        .sorted { $0.date > $1.date }
    }

    private var totalBuyIn: Double { filteredSessions.reduce(0) { $0 + $1.buyInAmount } }
    private var totalCashOut: Double { filteredSessions.reduce(0) { $0 + $1.cashOutAmount } }
    private var totalPL: Double { totalCashOut - totalBuyIn }
    private var avgPL: Double { filteredSessions.isEmpty ? 0 : totalPL / Double(filteredSessions.count) }
    private var winRate: Int {
        guard !filteredSessions.isEmpty else { return 0 }
        return Int((Double(filteredSessions.filter { $0.profitLoss >= 0 }.count) / Double(filteredSessions.count)) * 100)
    }

    private var venueRollups: [(name: String, sessions: [Session], pl: Double)] {
        Dictionary(grouping: filteredSessions, by: \.casinoName)
            .map { key, value in
                (name: key, sessions: value, pl: value.reduce(0) { $0 + $1.profitLoss })
            }
            .sorted { $0.pl > $1.pl }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                VStack(alignment: .leading, spacing: 10) {
                    TextField("Search sessions, venues, stakes, games", text: $search)
                        .textFieldStyle(.plain)
                        .padding(12)
                        .background(Color.staxSurface)
                        .cornerRadius(12)
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.18), lineWidth: 1))
                        .foregroundColor(.white)

                    reportChipRow(options: ReportDateRange.allCases.map(\.rawValue), selected: dateRangeRaw)
                    reportChipRow(options: ["All", "Casino", "Home Game"], selected: $venueType)
                    reportChipRow(options: ["All", "Cash", "Tourney"], selected: $sessionType)

                    DropdownSelector(label: "Venue", options: venueOptions, selected: $selectedVenue)
                    DropdownSelector(label: "Game Type", options: gameTypeOptions, selected: $selectedGameType)
                }
                .padding(16)
                .background(Color.staxSurfaceHigh)
                .cornerRadius(18)

                LazyVGrid(columns: [GridItem(.flexible(), spacing: 10), GridItem(.flexible(), spacing: 10)], spacing: 10) {
                    metricCard("Sessions", "\(filteredSessions.count)")
                    metricCard("Win Rate", "\(winRate)%")
                    metricCard("Buy-In", fmt(totalBuyIn))
                    metricCard("Cash Out", fmt(totalCashOut))
                    metricCard("P&L", signedFmt(totalPL), tint: totalPL >= 0 ? .staxProfit : .staxLoss)
                    metricCard("Avg / Session", signedFmt(avgPL), tint: avgPL >= 0 ? .staxProfit : .staxLoss)
                }

                if filteredSessions.isEmpty {
                    StaxEmptyState(title: "No matching sessions", message: "Adjust your filters or search to see more activity.")
                        .padding(.top, 24)
                } else {
                    sectionTitle("By Venue")
                    ForEach(venueRollups, id: \.name) { item in
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(item.name).font(.headline).foregroundColor(.white)
                                Text("\(item.sessions.count) sessions")
                                    .font(.caption)
                                    .foregroundColor(.staxOnSurfaceVar)
                            }
                            Spacer()
                            Text(signedFmt(item.pl))
                                .font(.headline).bold()
                                .foregroundColor(item.pl >= 0 ? .staxProfit : .staxLoss)
                        }
                        .padding(16)
                        .background(Color.staxSurfaceHigh)
                        .cornerRadius(18)
                    }

                    sectionTitle("Session Activity")
                    ForEach(filteredSessions) { session in
                        HStack(alignment: .top) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(session.name)
                                    .font(.subheadline).bold()
                                    .foregroundColor(.white)
                                Text(session.casinoName)
                                    .font(.caption)
                                    .foregroundColor(.staxOnSurfaceVar)
                                Text("\(session.date) • \(reportVenueType(for: session, homeGames: vm.homeGames)) • \(session.type) • \(session.gameType.isEmpty ? "Game N/A" : session.gameType)")
                                    .font(.caption2)
                                    .foregroundColor(.staxOnSurfaceVar.opacity(0.8))
                            }
                            Spacer()
                            Text(signedFmt(session.profitLoss))
                                .font(.subheadline).bold()
                                .foregroundColor(session.profitLoss >= 0 ? .staxProfit : .staxLoss)
                        }
                        .padding(14)
                        .background(Color.staxSurface)
                        .cornerRadius(16)
                    }
                }
            }
            .padding(16)
        }
        .background(Color.staxBackground.ignoresSafeArea())
        .navigationTitle("Reports")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var dateRangeRaw: Binding<String> {
        Binding(
            get: { dateRange.rawValue },
            set: { dateRange = ReportDateRange(rawValue: $0) ?? .all }
        )
    }

    @ViewBuilder
    private func reportChipRow(options: [String], selected: Binding<String>) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(options, id: \.self) { option in
                    Button {
                        selected.wrappedValue = option
                    } label: {
                        Text(option)
                            .font(.caption).bold()
                            .padding(.horizontal, 12)
                            .padding(.vertical, 8)
                            .background(selected.wrappedValue == option ? Color.staxPrimary : Color.staxSurface)
                            .foregroundColor(selected.wrappedValue == option ? .white : .staxOnSurfaceVar)
                            .cornerRadius(999)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    private func metricCard(_ label: String, _ value: String, tint: Color = .white) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label).font(.caption).foregroundColor(.staxOnSurfaceVar)
            Text(value).font(.headline).bold().foregroundColor(tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(16)
    }

    private func sectionTitle(_ value: String) -> some View {
        Text(value)
            .font(.caption).bold()
            .foregroundColor(.staxPrimary)
            .tracking(1)
            .padding(.top, 6)
    }

    private func fmt(_ value: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: value)) ?? "\(value)"
    }

    private func signedFmt(_ value: Double) -> String {
        value >= 0 ? "+\(fmt(value))" : fmt(value)
    }
}

private func reportVenueType(for session: Session, homeGames: [HomeGame]) -> String {
    if session.game == "Home Game" { return "Home Game" }
    if homeGames.contains(where: { $0.name.caseInsensitiveCompare(session.casinoName) == .orderedSame }) {
        return "Home Game"
    }
    return "Casino"
}

private func sessionMatchesDateRange(_ rawDate: String, dateRange: ReportDateRange) -> Bool {
    guard dateRange != .all else { return true }
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    guard let sessionDate = formatter.date(from: rawDate) else { return true }
    let calendar = Calendar.current
    let now = Date()

    let startDate: Date? = switch dateRange {
    case .all:
        nil
    case .last30:
        calendar.date(byAdding: .day, value: -30, to: now)
    case .last90:
        calendar.date(byAdding: .day, value: -90, to: now)
    case .ytd:
        calendar.date(from: calendar.dateComponents([.year], from: now))
    }

    guard let startDate else { return true }
    return sessionDate >= startDate
}

private let nutzRanks = [14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2]
private let nutzSuits = ["♠", "♥", "♦", "♣"]

private enum NutzStreet: Int, CaseIterable {
    case flop = 0
    case turn = 1
    case river = 2

    var label: String {
        switch self {
        case .flop: return "Flop"
        case .turn: return "Turn"
        case .river: return "River"
        }
    }

    var boardCount: Int { rawValue + 3 }
}

private struct NutzCard: Hashable, Codable {
    let rank: Int
    let suit: String

    var id: String { "\(rank)_\(suit)" }
    var label: String { "\(nutzRankLabel(rank))\(suit)" }
}

private struct NutzHoleCombo: Hashable {
    let cards: [NutzCard]

    var canonicalCards: [NutzCard] {
        cards.sorted {
            if $0.rank != $1.rank { return $0.rank > $1.rank }
            return nutzSuits.firstIndex(of: $0.suit) ?? 0 < nutzSuits.firstIndex(of: $1.suit) ?? 0
        }
    }

    var key: String { canonicalCards.map(\.id).joined(separator: "|") }
    var label: String { canonicalCards.map(\.label).joined(separator: " ") }
}

private struct NutzHandValue: Comparable, Equatable, Hashable {
    let category: Int
    let tiebreakers: [Int]

    static func < (lhs: NutzHandValue, rhs: NutzHandValue) -> Bool {
        if lhs.category != rhs.category { return lhs.category < rhs.category }
        let count = max(lhs.tiebreakers.count, rhs.tiebreakers.count)
        for index in 0..<count {
            let left = index < lhs.tiebreakers.count ? lhs.tiebreakers[index] : 0
            let right = index < rhs.tiebreakers.count ? rhs.tiebreakers[index] : 0
            if left != right { return left < right }
        }
        return false
    }
}

private struct NutzStageSolution {
    let nuts: [NutzHoleCombo]
    let secondNuts: [NutzHoleCombo]
    let bestHandName: String
}

private struct NutzHistoryEntry: Identifiable, Codable {
    var id: UUID = UUID()
    var playedAt: Date = Date()
    var score: Int
    var correctAnswers: Int
    var totalAnswers: Int
    var boardPreview: String
}

private final class NutzGameStore: ObservableObject {
    @Published private(set) var history: [NutzHistoryEntry] = []

    private let key = "stax_nutz_history"
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init() {
        load()
    }

    func add(_ entry: NutzHistoryEntry) {
        history.insert(entry, at: 0)
        history.sort { $0.playedAt > $1.playedAt }
        save()
    }

    private func save() {
        if let data = try? encoder.encode(history) {
            UserDefaults.standard.set(data, forKey: key)
        }
    }

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: key),
              let decoded = try? decoder.decode([NutzHistoryEntry].self, from: data) else { return }
        history = decoded.sorted { $0.playedAt > $1.playedAt }
    }
}

private struct NutzGameView: View {
    @StateObject private var store = NutzGameStore()

    @State private var roundBoard = generateNutzBoard()
    @State private var street: NutzStreet = .flop
    @State private var nutGuess: [NutzCard] = []
    @State private var secondGuess: [NutzCard] = []
    @State private var totalScore = 0
    @State private var correctAnswers = 0
    @State private var totalAnswers = 0
    @State private var feedback: String? = nil
    @State private var answerSummary: [String] = []
    @State private var roundComplete = false
    @State private var streetStartedAt = Date()

    private var board: [NutzCard] { Array(roundBoard.prefix(street.boardCount)) }
    private var solution: NutzStageSolution { solveNutzStage(board: board) }
    private var secondRequired: Bool { !solution.secondNuts.isEmpty }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                nutzSummaryHeader
                nutzBoardCard
                nutzStreetProgress
                NutzGuessSection(title: "Guess the nuts", selected: $nutGuess, blocked: Set(board.map(\.id)).union(secondGuess.map(\.id)))
                if secondRequired {
                    NutzGuessSection(title: "Guess the 2nd nuts", selected: $secondGuess, blocked: Set(board.map(\.id)).union(nutGuess.map(\.id)))
                }

                Button(street == .river ? "Score River" : "Score \(street.label)") {
                    submitStreet()
                }
                .disabled(feedback != nil || nutGuess.count != 2 || (secondRequired && secondGuess.count != 2))
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background((feedback == nil && nutGuess.count == 2 && (!secondRequired || secondGuess.count == 2)) ? Color.staxPrimary : Color.staxPrimary.opacity(0.4))
                .cornerRadius(16)

                if let feedback {
                    VStack(alignment: .leading, spacing: 10) {
                        Text(feedback).font(.headline).bold().foregroundColor(.white)
                        ForEach(answerSummary, id: \.self) { item in
                            Text(item).font(.subheadline).foregroundColor(.staxOnSurfaceVar)
                        }
                        Button(roundComplete ? "Play New Round" : "Go To \(NutzStreet(rawValue: street.rawValue + 1)?.label ?? "Next")") {
                            if roundComplete {
                                resetRound()
                            } else if let next = NutzStreet(rawValue: street.rawValue + 1) {
                                street = next
                                nutGuess = []
                                secondGuess = []
                                self.feedback = nil
                                answerSummary = []
                                streetStartedAt = Date()
                            }
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.staxPrimary)
                        .cornerRadius(14)
                    }
                    .padding(16)
                    .background(Color.staxSurfaceHigh)
                    .cornerRadius(18)
                }

                leaderboardSection
                historySection
            }
            .padding(16)
        }
        .background(Color.staxBackground.ignoresSafeArea())
        .navigationTitle("Nutz Game")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    resetRound()
                } label: {
                    Image(systemName: "arrow.clockwise")
                        .foregroundColor(.staxPrimary)
                }
            }
        }
    }

    private var nutzSummaryHeader: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Board Trainer").font(.title3).bold().foregroundColor(.white)
            Text("Street: \(street.label)").font(.subheadline).foregroundColor(.staxOnSurfaceVar)
            HStack(spacing: 10) {
                nutzMetric(title: "Score", value: "\(totalScore)", tint: totalScore >= 0 ? .staxProfit : .staxLoss)
                nutzMetric(title: "Correct", value: "\(correctAnswers) / \(totalAnswers)")
            }
        }
        .padding(16)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(18)
    }

    private var nutzBoardCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Board").font(.caption).bold().foregroundColor(.staxPrimary)
            HStack(spacing: 8) {
                ForEach(board, id: \.id) { card in
                    PlayingCardView(rank: nutzRankLabel(card.rank), suit: card.suit, size: 42, selected: false)
                }
            }
        }
        .padding(16)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(18)
    }

    private var nutzStreetProgress: some View {
        HStack(spacing: 8) {
            ForEach(NutzStreet.allCases, id: \.rawValue) { stage in
                Text(stage.label)
                    .font(.caption).bold()
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(stage.rawValue < street.rawValue ? Color.staxProfit.opacity(0.18) : (stage == street ? Color.staxPrimary.opacity(0.2) : Color.staxSurface))
                    .foregroundColor(.white)
                    .cornerRadius(999)
            }
        }
        .padding(14)
        .background(Color.staxSurface)
        .cornerRadius(18)
    }

    private var leaderboardSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Leaderboard").font(.headline).bold().foregroundColor(.white)
            if store.history.isEmpty {
                Text("Play a few rounds to build your leaderboard.")
                    .font(.subheadline)
                    .foregroundColor(.staxOnSurfaceVar)
            } else {
                ForEach(Array(store.history.sorted { $0.score > $1.score }.prefix(5).enumerated()), id: \.element.id) { index, entry in
                    HStack {
                        Text("#\(index + 1) • \(formatNutzDate(entry.playedAt))")
                            .font(.subheadline)
                            .foregroundColor(.white)
                        Spacer()
                        Text("\(entry.score) pts")
                            .font(.subheadline).bold()
                            .foregroundColor(entry.score >= 0 ? .staxProfit : .staxLoss)
                    }
                }
            }
        }
        .padding(16)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(18)
    }

    private var historySection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Recent Rounds").font(.headline).bold().foregroundColor(.white)
            if store.history.isEmpty {
                Text("Your round history will show up here.")
                    .font(.subheadline)
                    .foregroundColor(.staxOnSurfaceVar)
            } else {
                ForEach(store.history.prefix(10)) { entry in
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text(formatNutzDate(entry.playedAt))
                                .font(.subheadline).bold()
                                .foregroundColor(.white)
                            Spacer()
                            Text("\(entry.correctAnswers)/\(entry.totalAnswers)")
                                .font(.caption)
                                .foregroundColor(.staxOnSurfaceVar)
                        }
                        Text(entry.boardPreview)
                            .font(.caption)
                            .foregroundColor(.staxOnSurfaceVar)
                        Text("\(entry.score) pts")
                            .font(.caption).bold()
                            .foregroundColor(entry.score >= 0 ? .staxProfit : .staxLoss)
                    }
                    .padding(12)
                    .background(Color.staxSurface)
                    .cornerRadius(14)
                }
            }
        }
        .padding(16)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(18)
    }

    private func nutzMetric(title: String, value: String, tint: Color = .white) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.caption).foregroundColor(.staxOnSurfaceVar)
            Text(value).font(.headline).bold().foregroundColor(tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(Color.staxSurface)
        .cornerRadius(16)
    }

    private func submitStreet() {
        guard nutGuess.count == 2 else { return }
        let nutCombo = NutzHoleCombo(cards: nutGuess)
        let secondCombo = secondGuess.count == 2 ? NutzHoleCombo(cards: secondGuess) : nil

        let nutCorrect = solution.nuts.contains(where: { $0.key == nutCombo.key })
        let secondCorrect = !secondRequired || solution.secondNuts.contains(where: { $0.key == secondCombo?.key })
        let elapsedSeconds = Date().timeIntervalSince(streetStartedAt)
        let correctCount = [nutCorrect, secondCorrect].filter { $0 }.count
        let answerCount = secondRequired ? 2 : 1
        let accuracyPoints = (nutCorrect ? 10 : -5) + (secondRequired ? (secondCorrect ? 8 : -4) : 0)
        let speedBonus = nutzSpeedBonus(
            elapsedSeconds: elapsedSeconds,
            correctAnswers: correctCount,
            totalAnswers: answerCount
        )
        let stagePoints = accuracyPoints + speedBonus

        totalScore += stagePoints
        correctAnswers += correctCount
        totalAnswers += answerCount

        answerSummary = [
            "Best made hand: \(solution.bestHandName)",
            "Nuts: \(formatNutzCombos(solution.nuts))"
        ] + (secondRequired ? ["Second nuts: \(formatNutzCombos(solution.secondNuts))"] : []) + [
            "Response time: \(formatNutzElapsed(elapsedSeconds))\(speedBonus > 0 ? " · Speed bonus +\(speedBonus)" : "")"
        ]

        feedback = "Street score \(stagePoints >= 0 ? "+" : "")\(stagePoints) · Nuts \(nutCorrect ? "correct" : "wrong")" +
            (secondRequired ? " · 2nd nuts \(secondCorrect ? "correct" : "wrong")" : "") +
            (speedBonus > 0 ? " · speed +\(speedBonus)" : "") +
            " · \(formatNutzElapsed(elapsedSeconds))"

        if street == .river {
            roundComplete = true
            store.add(
                NutzHistoryEntry(
                    score: totalScore,
                    correctAnswers: correctAnswers,
                    totalAnswers: totalAnswers,
                    boardPreview: roundBoard.map(\.label).joined(separator: " ")
                )
            )
        }
    }

    private func resetRound() {
        roundBoard = generateNutzBoard()
        street = .flop
        nutGuess = []
        secondGuess = []
        totalScore = 0
        correctAnswers = 0
        totalAnswers = 0
        feedback = nil
        answerSummary = []
        roundComplete = false
        streetStartedAt = Date()
    }
}

private struct NutzGuessSection: View {
    let title: String
    @Binding var selected: [NutzCard]
    let blocked: Set<String>

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title).font(.headline).bold().foregroundColor(.white)
            if selected.isEmpty {
                Text("Select 2 hole cards").font(.subheadline).foregroundColor(.staxOnSurfaceVar)
            } else {
                HStack(spacing: 6) {
                    ForEach(selected, id: \.id) { card in
                        PlayingCardView(rank: nutzRankLabel(card.rank), suit: card.suit, size: 34, selected: true)
                    }
                }
            }
            VStack(spacing: 6) {
                ForEach(nutzSuits, id: \.self) { suit in
                    HStack(spacing: 4) {
                        ForEach(nutzRanks, id: \.self) { rank in
                            let card = NutzCard(rank: rank, suit: suit)
                            let isSelected = selected.contains(card)
                            let enabled = isSelected || !blocked.contains(card.id)
                            Button {
                                toggle(card)
                            } label: {
                                PlayingCardView(
                                    rank: nutzRankLabel(card.rank),
                                    suit: suit,
                                    size: 28,
                                    selected: isSelected,
                                    isVillain: false
                                )
                                .opacity(enabled ? 1 : 0.28)
                            }
                            .buttonStyle(.plain)
                            .disabled(!enabled)
                        }
                    }
                }
            }
        }
        .padding(16)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(18)
    }

    private func toggle(_ card: NutzCard) {
        if let idx = selected.firstIndex(of: card) {
            selected.remove(at: idx)
        } else if !blocked.contains(card.id), selected.count < 2 {
            selected.append(card)
            selected.sort { $0.rank > $1.rank }
        }
    }
}

private func generateNutzBoard() -> [NutzCard] {
    nutzDeck().shuffled().prefix(5).map { $0 }
}

private func nutzDeck() -> [NutzCard] {
    nutzSuits.flatMap { suit in nutzRanks.map { rank in NutzCard(rank: rank, suit: suit) } }
}

private func solveNutzStage(board: [NutzCard]) -> NutzStageSolution {
    let remaining = nutzDeck().filter { !board.contains($0) }
    var evaluations: [(NutzHoleCombo, NutzHandValue)] = []

    for first in 0..<remaining.count {
        for second in (first + 1)..<remaining.count {
            let combo = NutzHoleCombo(cards: [remaining[first], remaining[second]])
            evaluations.append((combo, bestNutzHandValue(cards: board + combo.cards)))
        }
    }

    let uniqueValues = Array(Set(evaluations.map(\.1))).sorted(by: >)
    guard let best = uniqueValues.first else {
        return NutzStageSolution(nuts: [], secondNuts: [], bestHandName: "N/A")
    }
    let second = uniqueValues.count > 1 ? uniqueValues[1] : nil
    return NutzStageSolution(
        nuts: evaluations.filter { $0.1 == best }.map(\.0),
        secondNuts: evaluations.filter { second != nil && $0.1 == second }.map(\.0),
        bestHandName: nutzHandCategoryName(best.category)
    )
}

private func bestNutzHandValue(cards: [NutzCard]) -> NutzHandValue {
    var best: NutzHandValue?
    for a in 0..<(cards.count - 4) {
        for b in (a + 1)..<(cards.count - 3) {
            for c in (b + 1)..<(cards.count - 2) {
                for d in (c + 1)..<(cards.count - 1) {
                    for e in (d + 1)..<cards.count {
                        let value = evaluateNutzFive([cards[a], cards[b], cards[c], cards[d], cards[e]])
                        if best == nil || value > best! {
                            best = value
                        }
                    }
                }
            }
        }
    }
    return best!
}

private func evaluateNutzFive(_ cards: [NutzCard]) -> NutzHandValue {
    let ranksDesc = cards.map(\.rank).sorted(by: >)
    let grouped = Dictionary(grouping: cards.map(\.rank), by: { $0 }).mapValues(\.count)
    let countGroups = grouped.sorted {
        if $0.value != $1.value { return $0.value > $1.value }
        return $0.key > $1.key
    }
    let flush = Set(cards.map(\.suit)).count == 1
    let straight = nutzStraightHigh(ranks: cards.map(\.rank))

    if flush, let straight { return NutzHandValue(category: 8, tiebreakers: [straight]) }
    if countGroups.first?.value == 4 {
        let quad = countGroups[0].key
        let kicker = countGroups.first(where: { $0.value == 1 })?.key ?? 0
        return NutzHandValue(category: 7, tiebreakers: [quad, kicker])
    }
    if countGroups.first?.value == 3, countGroups.dropFirst().first?.value == 2 {
        return NutzHandValue(category: 6, tiebreakers: [countGroups[0].key, countGroups[1].key])
    }
    if flush { return NutzHandValue(category: 5, tiebreakers: ranksDesc) }
    if let straight { return NutzHandValue(category: 4, tiebreakers: [straight]) }
    if countGroups.first?.value == 3 {
        let trip = countGroups[0].key
        let kickers = countGroups.filter { $0.value == 1 }.map(\.key).sorted(by: >)
        return NutzHandValue(category: 3, tiebreakers: [trip] + kickers)
    }
    if countGroups.first?.value == 2, countGroups.dropFirst().first?.value == 2 {
        let pairs = countGroups.filter { $0.value == 2 }.map(\.key).sorted(by: >)
        let kicker = countGroups.first(where: { $0.value == 1 })?.key ?? 0
        return NutzHandValue(category: 2, tiebreakers: pairs + [kicker])
    }
    if countGroups.first?.value == 2 {
        let pair = countGroups[0].key
        let kickers = countGroups.filter { $0.value == 1 }.map(\.key).sorted(by: >)
        return NutzHandValue(category: 1, tiebreakers: [pair] + kickers)
    }
    return NutzHandValue(category: 0, tiebreakers: ranksDesc)
}

private func nutzStraightHigh(ranks: [Int]) -> Int? {
    var distinct = Array(Set(ranks)).sorted(by: >)
    if distinct.contains(14) { distinct.append(1) }
    guard distinct.count >= 5 else { return nil }
    for index in 0...(distinct.count - 5) {
        let slice = Array(distinct[index..<(index + 5)])
        let sequential = zip(slice, slice.dropFirst()).allSatisfy { $0 - 1 == $1 }
        if sequential { return slice.first }
    }
    return nil
}

private func nutzRankLabel(_ rank: Int) -> String {
    switch rank {
    case 14: return "A"
    case 13: return "K"
    case 12: return "Q"
    case 11: return "J"
    case 10: return "T"
    default: return "\(rank)"
    }
}

private func nutzHandCategoryName(_ category: Int) -> String {
    switch category {
    case 8: return "Straight Flush"
    case 7: return "Four of a Kind"
    case 6: return "Full House"
    case 5: return "Flush"
    case 4: return "Straight"
    case 3: return "Three of a Kind"
    case 2: return "Two Pair"
    case 1: return "One Pair"
    default: return "High Card"
    }
}

private func formatNutzCombos(_ combos: [NutzHoleCombo]) -> String {
    guard !combos.isEmpty else { return "No distinct second nuts" }
    let visible = combos.prefix(4).map(\.label).joined(separator: ", ")
    return combos.count > 4 ? "\(visible) +\(combos.count - 4) more" : visible
}

private func nutzSpeedBonus(elapsedSeconds: TimeInterval, correctAnswers: Int, totalAnswers: Int) -> Int {
    guard correctAnswers > 0, totalAnswers > 0 else { return 0 }
    let timeFactor: Double
    switch elapsedSeconds {
    case ...5: timeFactor = 1.0
    case ...10: timeFactor = 0.75
    case ...20: timeFactor = 0.5
    case ...30: timeFactor = 0.25
    default: timeFactor = 0.0
    }
    let maxBonus = totalAnswers > 1 ? 8.0 : 5.0
    let accuracyFactor = Double(correctAnswers) / Double(totalAnswers)
    return Int((maxBonus * timeFactor * accuracyFactor).rounded())
}

private func formatNutzElapsed(_ elapsedSeconds: TimeInterval) -> String {
    String(format: "%.1fs", elapsedSeconds)
}

private func formatNutzDate(_ date: Date) -> String {
    let formatter = DateFormatter()
    formatter.dateFormat = "MMM d, h:mm a"
    return formatter.string(from: date)
}

private final class ChipClickPlayer {
    static let shared = ChipClickPlayer()

    private var player: AVAudioPlayer?
    private lazy var soundData: Data = Self.buildWavData()

    func play() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, options: [.mixWithOthers])
            try session.setActive(true, options: [])
            player = try AVAudioPlayer(data: soundData)
            player?.volume = 1.0
            player?.currentTime = 0
            player?.prepareToPlay()
            player?.play()
        } catch {
            player = nil
        }
    }

    private static func buildWavData() -> Data {
        let sampleRate = 22_050
        let duration: Double = 0.21
        let sampleCount = Int(Double(sampleRate) * duration)
        let clickStarts = [0.0, 0.045, 0.09, 0.135]

        var pcm = Data(capacity: sampleCount * 2)

        for index in 0..<sampleCount {
            let t = Double(index) / Double(sampleRate)
            var sample = 0.0

            for (clickIndex, start) in clickStarts.enumerated() {
                let dt = t - start
                if dt >= 0, dt <= 0.026 {
                    let env = Foundation.exp(-dt * 118.0)
                    let attack = Foundation.exp(-dt * 420.0)
                    let noise = pseudoNoise(seed: index + (clickIndex * 4099))
                    let knock = 0.55 * sin(2.0 * Double.pi * 540.0 * dt)
                    let body = 0.26 * sin(2.0 * Double.pi * 920.0 * dt)
                    let tick = 0.08 * sin(2.0 * Double.pi * 1750.0 * dt)
                    let secondTapDelay = max(dt - 0.004, 0.0)
                    let secondTap = dt >= 0.004 ? Foundation.exp(-secondTapDelay * 520.0) * 0.12 * pseudoNoise(seed: index + 991 * (clickIndex + 1)) : 0.0
                    sample += env * (0.88 * noise + knock + body) + attack * tick + secondTap
                }
            }

            let clipped = max(-1.0, min(1.0, sample))
            var intSample = Int16(clipped * Double(Int16.max))
            pcm.append(Data(bytes: &intSample, count: MemoryLayout<Int16>.size))
        }

        return wavData(fromPCM16Mono: pcm, sampleRate: sampleRate)
    }

    private static func wavData(fromPCM16Mono pcm: Data, sampleRate: Int) -> Data {
        let byteRate = UInt32(sampleRate * 2)
        let blockAlign: UInt16 = 2
        let bitsPerSample: UInt16 = 16
        let subchunk2Size = UInt32(pcm.count)
        let chunkSize = 36 + subchunk2Size

        var data = Data()
        data.append("RIFF".data(using: .ascii)!)
        data.append(littleEndian(chunkSize))
        data.append("WAVE".data(using: .ascii)!)
        data.append("fmt ".data(using: .ascii)!)
        data.append(littleEndian(UInt32(16)))
        data.append(littleEndian(UInt16(1)))
        data.append(littleEndian(UInt16(1)))
        data.append(littleEndian(UInt32(sampleRate)))
        data.append(littleEndian(byteRate))
        data.append(littleEndian(blockAlign))
        data.append(littleEndian(bitsPerSample))
        data.append("data".data(using: .ascii)!)
        data.append(littleEndian(subchunk2Size))
        data.append(pcm)
        return data
    }

    private static func littleEndian<T: FixedWidthInteger>(_ value: T) -> Data {
        var v = value.littleEndian
        return Data(bytes: &v, count: MemoryLayout<T>.size)
    }

    private static func pseudoNoise(seed: Int) -> Double {
        var x = UInt64(bitPattern: Int64(seed))
        x ^= (x << 13)
        x ^= (x >> 17)
        x ^= (x << 5)
        return (Double(x & 0xFFFF) / 32768.0) - 1.0
    }
}
