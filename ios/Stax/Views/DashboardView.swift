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
                        Spacer()
                        StaxEmptyState(
                            title: "No sessions yet",
                            message: "Tap the + button to create your first session and start building your gallery."
                        )
                        Spacer()
                    } else {
                        ScrollView {
                            LazyVGrid(columns: columns, spacing: 6) {
                                ForEach(vm.casinoFolders) { folder in
                                    NavigationLink(destination: CasinoSessionsView(vm: vm, casinoName: folder.casinoName)) {
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
            AddSessionSheet(casinoData: vm.casinoData) { name, casino, date, type, game, gameType, stakes, antes, buyIn, cashOut in
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
            // Background image or placeholder
            Group {
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
            }

            // Gradient scrim
            LinearGradient(
                colors: [.clear, .black.opacity(0.75)],
                startPoint: .init(x: 0.5, y: 0.35),
                endPoint: .bottom
            )

            VStack(alignment: .leading, spacing: 2) {
                Text(folder.casinoName)
                    .font(.subheadline).bold()
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text("\(folder.sessionCount) \(folder.sessionCount == 1 ? "session" : "sessions")")
                    .font(.caption)
                    .foregroundColor(.white.opacity(0.65))
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
    let onConfirm: (String, String, String, String, String, String, String, String, Double, Double) -> Void

    @Environment(\.dismiss) private var dismiss

    @State private var selectedState: String = ""
    @State private var selectedCasino: String = ""
    @State private var sessionName: String = ""
    @State private var userEditedName = false
    @State private var sessionType = "Cash"
    @State private var gameType = "NLH"
    @State private var stakes = "1/2"
    @State private var antes = "None"
    @State private var buyIn = ""

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

                    // Casino
                    SessionSectionLabel(text: "Casino")
                    Spacer().frame(height: 10)
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

                    // Buy-in
                    SessionSectionLabel(text: "Buy-in (optional)")
                    Spacer().frame(height: 10)
                    HStack {
                        Text("$")
                            .foregroundColor(.staxOnSurfaceVar)
                            .padding(.leading, 12)
                        TextField("0", text: $buyIn)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.plain)
                            .foregroundColor(.white)
                    }
                    .padding(.vertical, 12)
                    .padding(.trailing, 12)
                    .background(Color.staxSurface)
                    .cornerRadius(10)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))

                    Spacer().frame(height: 36)

                    // Create button
                    Button {
                        let name = sessionName.isEmpty ? "\(selectedCasino) · \(dateDisplay)" : sessionName
                        onConfirm(name, selectedCasino, dateKey, sessionType, "", gameType, stakes, antes,
                                  Double(buyIn) ?? 0, 0)
                        dismiss()
                    } label: {
                        Text("Create Session")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(selectedCasino.isEmpty ? Color.staxPrimary.opacity(0.4) : Color.staxPrimary)
                            .cornerRadius(16)
                    }
                    .disabled(selectedCasino.isEmpty)
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
        .onAppear {
            selectedState = stateList.first ?? ""
            selectedCasino = casinoList.first ?? ""
            if !selectedCasino.isEmpty {
                sessionName = "\(selectedCasino) · \(dateDisplay)"
            }
        }
    }
}
