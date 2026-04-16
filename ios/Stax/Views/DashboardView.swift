import SwiftUI

struct DashboardView: View {
    @ObservedObject var vm: SessionsViewModel
    @State private var showAddSession = false
    @State private var selectedCasino: String? = nil

    private let columns = [GridItem(.flexible(), spacing: 6), GridItem(.flexible(), spacing: 6)]

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                Color.staxBackground.ignoresSafeArea()

                VStack(spacing: 0) {
                    StaxHeader(title: "Casino / Card Rooms", subtitle: "Browse sessions by venue")

                    if vm.casinoFolders.isEmpty {
                        StaxEmptyState(
                            title: "No sessions yet",
                            message: "Tap the + button to create your first session and start building your gallery."
                        )
                        .padding(.horizontal, 16)
                        .padding(.top, 14)
                        Spacer()
                    } else {
                        ScrollView {
                            LazyVGrid(columns: columns, spacing: 6) {
                                ForEach(vm.casinoFolders) { folder in
                                    NavigationLink(destination: CasinoSessionsView(vm: vm, casinoName: folder.casinoName, showPhotos: true)) {
                                        CasinoFolderCard(folder: folder)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(6)
                            .padding(.bottom, 90)
                        }
                    }
                }

                // FAB
                Button {
                    showAddSession = true
                } label: {
                    Image(systemName: "plus")
                        .font(.title2.bold())
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.staxPrimary)
                        .clipShape(Circle())
                        .shadow(color: .black.opacity(0.4), radius: 8, x: 0, y: 4)
                }
                .padding(.trailing, 20)
                .padding(.bottom, 24)
            }
            .navigationBarHidden(true)
        }
        .sheet(isPresented: $showAddSession) {
            AddSessionSheet(
                casinoData: vm.casinoData,
                homeGames: vm.homeGames,
                onSaveHomeGame: { name, city, state in
                    vm.saveHomeGame(name: name, city: city, state: state)
                }
            ) { name, casino, date, type, game, gameType, stakes, antes, buyIn, cashOut in
                vm.addSession(name: name, casinoName: casino, date: date, type: type, game: game,
                              gameType: gameType, stakes: stakes, antes: antes, buyIn: buyIn, cashOut: cashOut)
            }
        }
    }
}

// MARK: – Casino folder card

struct CasinoFolderCard: View {
    let folder: CasinoFolder

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            // Background: latest session photo or dark placeholder
            if let path = folder.latestPhotoPath,
               let img = UIImage(contentsOfFile: path) {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFill()
            } else {
                Color.staxSurface
                Image(systemName: "folder.fill")
                    .font(.system(size: 44))
                    .foregroundColor(.white.opacity(0.20))
            }

            // Gradient scrim
            LinearGradient(
                colors: [.clear, .black.opacity(0.80)],
                startPoint: .init(x: 0.5, y: 0.30),
                endPoint: .bottom
            )

            // Bottom overlay: casino logo badge + name/count
            HStack(alignment: .bottom, spacing: 8) {
                if let assetName = folder.logoAssetName {
                    Image(assetName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 36, height: 36)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.white.opacity(0.30), lineWidth: 1)
                        )
                        .shadow(color: .black.opacity(0.6), radius: 4, x: 0, y: 2)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(folder.casinoName)
                        .font(.subheadline).bold()
                        .foregroundColor(.white)
                        .lineLimit(1)
                    Text("\(folder.sessionCount) \(folder.sessionCount == 1 ? "session" : "sessions")")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.65))
                }
            }
            .padding(10)
        }
        .frame(maxWidth: .infinity)
        .aspectRatio(1, contentMode: .fit)
        .clipShape(RoundedRectangle(cornerRadius: 10))
    }
}

// MARK: – Add Session Sheet

struct AddSessionSheet: View {
    let casinoData: [String: [String]]
    let homeGames: [HomeGame]
    let onSaveHomeGame: (String, String, String) -> Void
    let onConfirm: (String, String, String, String, String, String, String, String, Double, Double) -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var selectedState: String = ""
    @State private var selectedCasino: String = ""
    @State private var venueMode = "Casino"
    @State private var selectedHomeGameLabel = "New home game"
    @State private var homeGameName = ""
    @State private var homeGameCity = ""
    @State private var homeGameState = ""
    @State private var sessionName: String = ""
    @State private var userEditedName = false
    @State private var sessionType = "Cash"
    @State private var gameType = "NLH"
    @State private var stakes = "1/2"
    @State private var antes = "None"
    @State private var buyIn = ""
    @State private var cashOut = ""

    private var dateDisplay: String {
        let fmt = DateFormatter()
        fmt.dateFormat = "MMM d, yyyy"
        return fmt.string(from: Date())
    }
    private var dateKey: String {
        let fmt = DateFormatter()
        fmt.dateFormat = "yyyy-MM-dd"
        return fmt.string(from: Date())
    }

    private var stateList: [String] { casinoData.keys.sorted() }
    private var casinoList: [String] { casinoData[selectedState] ?? [] }
    private var homeGameOptions: [String] { ["New home game"] + homeGames.map(\.displayName) }
    private var selectedVenueName: String { venueMode == "Casino" ? selectedCasino : homeGameName.trimmingCharacters(in: .whitespacesAndNewlines) }

    private func initDefaults() {
        guard !casinoData.isEmpty else { return }
        if selectedState.isEmpty || !stateList.contains(selectedState) {
            selectedState = stateList.first ?? ""
        }
        let casinos = casinoData[selectedState] ?? []
        if selectedCasino.isEmpty || !casinos.contains(selectedCasino) {
            selectedCasino = casinos.first ?? ""
        }
        if homeGameState.isEmpty || !stateList.contains(homeGameState) {
            homeGameState = stateList.first ?? ""
        }
        if !userEditedName && !selectedVenueName.isEmpty {
            sessionName = "\(selectedVenueName) · \(dateDisplay)"
        }
    }

    private let gameTypes = ["NLH", "PLO", "Limit Hold'em", "7-Card Stud", "Razz", "Omaha Hi/Lo", "2-7 Triple Draw", "Badugi"]
    private let stakesList = ["1/2", "2/3", "2/5", "5/5", "5/10", "10/20", "20/40", "25/50", "50/100", "100/200", "200/400", "500/1000"]
    private let antesList = ["None", "10", "20", "40", "50", "100", "200", "400", "1000"]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    // Header
                    VStack(alignment: .leading, spacing: 4) {
                        Text("New Session")
                            .font(.title2).bold()
                            .foregroundColor(.white)
                        Text(dateDisplay)
                            .font(.subheadline)
                            .foregroundColor(.staxOnSurfaceVar)
                    }
                    .padding(.bottom, 28)

                    // Venue
                    SessionSectionLabel(text: "Venue")
                    Spacer().frame(height: 10)
                    HStack(spacing: 10) {
                        SessionTypeCard(label: "Casino", emoji: "🎰", selected: venueMode == "Casino") {
                            venueMode = "Casino"
                        }
                        SessionTypeCard(label: "Home Game", emoji: "🏠", selected: venueMode == "Home Game") {
                            venueMode = "Home Game"
                        }
                    }
                    Spacer().frame(height: 16)
                    if venueMode == "Casino" {
                        DropdownSelector(label: "State / Region", options: stateList, selected: $selectedState)
                            .onChange(of: selectedState) { _, new in
                                selectedCasino = casinoData[new]?.first ?? ""
                            }
                        Spacer().frame(height: 8)
                        DropdownSelector(label: "Casino / Card Room", options: casinoList, selected: $selectedCasino)
                            .onChange(of: selectedCasino) { _, new in
                                if !userEditedName && !new.isEmpty {
                                    sessionName = "\(new) · \(dateDisplay)"
                                }
                            }
                    } else {
                        if !homeGames.isEmpty {
                            DropdownSelector(label: "Saved Home Games", options: homeGameOptions, selected: $selectedHomeGameLabel)
                                .onChange(of: selectedHomeGameLabel) { _, new in
                                    if let game = homeGames.first(where: { $0.displayName == new }) {
                                        homeGameName = game.name
                                        homeGameCity = game.city
                                        homeGameState = game.state
                                    } else {
                                        homeGameName = ""
                                        homeGameCity = ""
                                        homeGameState = stateList.first ?? ""
                                    }
                                }
                            Spacer().frame(height: 8)
                        }
                        TextField("Home game name", text: $homeGameName)
                            .textFieldStyle(.plain)
                            .padding(12)
                            .background(Color.staxSurface)
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))
                            .foregroundColor(.white)
                        Spacer().frame(height: 8)
                        TextField("City", text: $homeGameCity)
                            .textFieldStyle(.plain)
                            .padding(12)
                            .background(Color.staxSurface)
                            .cornerRadius(10)
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))
                            .foregroundColor(.white)
                        Spacer().frame(height: 8)
                        DropdownSelector(label: "State / Region", options: stateList, selected: $homeGameState)
                    }

                    Spacer().frame(height: 24)

                    // Session name
                    SessionSectionLabel(text: "Session Name")
                    Spacer().frame(height: 10)
                    TextField("e.g. Commerce · Apr 7", text: $sessionName)
                        .textFieldStyle(.plain)
                        .padding(12)
                        .background(Color.staxSurface)
                        .cornerRadius(10)
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))
                        .foregroundColor(.white)
                        .onChange(of: sessionName) { _, _ in userEditedName = true }

                    Spacer().frame(height: 24)

                    // Session type
                    SessionSectionLabel(text: "Session Type")
                    Spacer().frame(height: 10)
                    HStack(spacing: 10) {
                        SessionTypeCard(label: "Cash Game", emoji: "💵", selected: sessionType == "Cash") {
                            sessionType = "Cash"
                        }
                        SessionTypeCard(label: "Tournament", emoji: "🏆", selected: sessionType == "Tourney") {
                            sessionType = "Tourney"
                        }
                    }

                    Spacer().frame(height: 24)

                    // Game
                    SessionSectionLabel(text: "Game")
                    Spacer().frame(height: 10)
                    DropdownSelector(label: "Game Type", options: gameTypes, selected: $gameType)

                    if sessionType == "Cash" {
                        Spacer().frame(height: 8)
                        HStack(spacing: 8) {
                            DropdownSelector(label: "Stakes", options: stakesList, selected: $stakes)
                            DropdownSelector(label: "Antes", options: antesList, selected: $antes)
                        }
                    }

                    Spacer().frame(height: 24)

                    // Money
                    SessionSectionLabel(text: "Money")
                    Spacer().frame(height: 10)
                    HStack(spacing: 8) {
                        HStack {
                            Text("$")
                                .foregroundColor(.staxOnSurfaceVar)
                                .padding(.leading, 12)
                            TextField("Buy-in", text: $buyIn)
                                .keyboardType(.decimalPad)
                                .textFieldStyle(.plain)
                                .foregroundColor(.white)
                        }
                        .padding(.vertical, 12)
                        .padding(.trailing, 12)
                        .background(Color.staxSurface)
                        .cornerRadius(10)
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))

                        HStack {
                            Text("$")
                                .foregroundColor(.staxOnSurfaceVar)
                                .padding(.leading, 12)
                            TextField("Cash out", text: $cashOut)
                                .keyboardType(.decimalPad)
                                .textFieldStyle(.plain)
                                .foregroundColor(.white)
                        }
                        .padding(.vertical, 12)
                        .padding(.trailing, 12)
                        .background(Color.staxSurface)
                        .cornerRadius(10)
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))
                    }

                    Spacer().frame(height: 36)

                    // Create button
                    Button {
                        if venueMode == "Home Game" {
                            onSaveHomeGame(homeGameName, homeGameCity, homeGameState)
                        }
                        let venueName = selectedVenueName
                        let name = sessionName.isEmpty ? "\(venueName) · \(dateDisplay)" : sessionName
                        onConfirm(name, venueName, dateKey, sessionType, venueMode, gameType, stakes, antes,
                                  Double(buyIn) ?? 0, Double(cashOut) ?? 0)
                        dismiss()
                    } label: {
                        Text("Create Session")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(
                                selectedVenueName.isEmpty || (venueMode == "Home Game" && (homeGameCity.isEmpty || homeGameState.isEmpty))
                                ? Color.staxPrimary.opacity(0.4)
                                : Color.staxPrimary
                            )
                            .cornerRadius(16)
                    }
                    .disabled(selectedVenueName.isEmpty || (venueMode == "Home Game" && (homeGameCity.isEmpty || homeGameState.isEmpty)))
                }
                .padding(24)
            }
            .background(Color.staxBackground.ignoresSafeArea())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                        .foregroundColor(.staxPrimary)
                }
            }
        }
        .preferredColorScheme(.dark)
        .onAppear { initDefaults() }
        .onChange(of: casinoData) { _, _ in
            if selectedState.isEmpty { initDefaults() }
        }
        .onChange(of: selectedVenueName) { _, new in
            if !userEditedName && !new.isEmpty {
                sessionName = "\(new) · \(dateDisplay)"
            }
        }
    }
}
