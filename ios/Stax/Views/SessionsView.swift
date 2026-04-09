import SwiftUI

struct SessionsView: View {
    @ObservedObject var vm: SessionsViewModel
    @State private var showAddSession = false
    @State private var selectedFilter = "All"
    @State private var sessionToDelete: UUID? = nil
    @State private var showDeleteConfirm = false

    private var filteredSessions: [Session] {
        switch selectedFilter {
        case "Cash":   return vm.sessions.filter { $0.type == "Cash" }
        case "Tourney": return vm.sessions.filter { $0.type == "Tourney" }
        default:       return vm.sessions
        }
    }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                Color.staxBackground.ignoresSafeArea()

                VStack(spacing: 0) {
                    StaxHeader(title: "Sessions", subtitle: "All play, buy-ins, and results")

                    VStack(spacing: 10) {
                        FilterChipRow(options: ["All", "Cash", "Tourney"], selected: $selectedFilter)
                            .padding(.top, 12)

                        WinLossSummary(sessions: filteredSessions)

                        if filteredSessions.isEmpty {
                            Spacer()
                            StaxEmptyState(title: "No sessions", message: "Create your first session using the + button.")
                            Spacer()
                        } else {
                            List {
                                ForEach(filteredSessions) { session in
                                    NavigationLink(destination: SessionDetailView(vm: vm, session: session)) {
                                        AllSessionRow(session: session)
                                            .listRowBackground(Color.clear)
                                    }
                                    .listRowInsets(EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0))
                                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                        Button(role: .destructive) {
                                            sessionToDelete = session.id
                                            showDeleteConfirm = true
                                        } label: {
                                            Label("Delete", systemImage: "trash")
                                        }
                                    }
                                }
                            }
                            .listStyle(.plain)
                            .background(Color.staxBackground)
                            .scrollContentBackground(.hidden)
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // FAB
                Button { showAddSession = true } label: {
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
        .alert("Delete Session?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) {
                if let id = sessionToDelete { vm.deleteSession(id: id) }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This will permanently delete the session and all its photos.")
        }
    }
}

// MARK: – Win/Loss Summary Card

struct WinLossSummary: View {
    let sessions: [Session]

    private var totalBuyIn: Double   { sessions.reduce(0) { $0 + $1.buyInAmount } }
    private var totalCashOut: Double { sessions.reduce(0) { $0 + $1.cashOutAmount } }
    private var totalPL: Double      { totalCashOut - totalBuyIn }

    var body: some View {
        HStack {
            Text("\(sessions.count) sessions")
                .font(.caption)
                .foregroundColor(.staxOnSurfaceVar)
            Spacer()
            Text("In \(fmt(totalBuyIn))")
                .font(.caption)
                .foregroundColor(.staxOnSurfaceVar)
            Text("Out \(fmt(totalCashOut))")
                .font(.caption)
                .foregroundColor(.staxOnSurfaceVar)
            Text(totalPL >= 0 ? "+\(fmt(totalPL))" : fmt(totalPL))
                .font(.caption).bold()
                .foregroundColor(totalPL >= 0 ? .staxProfit : .staxLoss)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(16)
    }

    private func fmt(_ v: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: v)) ?? "\(v)"
    }
}

// MARK: – Session row for all-sessions list

struct AllSessionRow: View {
    let session: Session

    var profitLoss: Double { session.profitLoss }

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: session.type == "Cash" ? "dollarsign.circle.fill" : "trophy.fill")
                .font(.system(size: 32))
                .foregroundColor(session.type == "Cash" ? .green : .yellow)
                .frame(width: 40)

            VStack(alignment: .leading, spacing: 3) {
                Text(session.name)
                    .font(.subheadline).bold()
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text(session.casinoName)
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
                Text(session.date)
                    .font(.caption2)
                    .foregroundColor(.staxOnSurfaceVar.opacity(0.75))
            }

            Spacer()

            Text(profitLoss >= 0 ? "+\(fmt(profitLoss))" : fmt(profitLoss))
                .font(.subheadline).bold()
                .foregroundColor(profitLoss >= 0 ? .staxProfit : .staxLoss)
        }
        .padding(.vertical, 6)
        .padding(.horizontal, 4)
        .background(Color.staxSurface)
        .cornerRadius(14)
    }

    private func fmt(_ v: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: v)) ?? "\(v)"
    }
}
