import SwiftUI

// Groups all sessions under one casino for the Sessions tab
struct CasinoGroup: Identifiable {
    var id: String { casinoName }
    let casinoName: String
    let sessions: [Session]

    var sessionCount: Int   { sessions.count }
    var cashCount: Int      { sessions.filter { $0.type == "Cash" }.count }
    var tourneyCount: Int   { sessions.filter { $0.type == "Tourney" }.count }
    var totalBuyIn: Double  { sessions.reduce(0) { $0 + $1.buyInAmount } }
    var totalCashOut: Double { sessions.reduce(0) { $0 + $1.cashOutAmount } }
    var totalPL: Double     { totalCashOut - totalBuyIn }
    var latestDate: String  { sessions.map { $0.date }.max() ?? "" }
}

struct SessionsView: View {
    @ObservedObject var vm: SessionsViewModel
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @Environment(\.showPaywall) private var showPaywall
    @State private var showAddSession = false
    @State private var selectedFilter = "All"

    private var filteredSessions: [Session] {
        switch selectedFilter {
        case "Cash":    return vm.sessions.filter { $0.type == "Cash" }
        case "Tourney": return vm.sessions.filter { $0.type == "Tourney" }
        default:        return vm.sessions
        }
    }

    private var casinoGroups: [CasinoGroup] {
        let grouped = Dictionary(grouping: filteredSessions, by: { $0.casinoName })
        return grouped
            .map { CasinoGroup(casinoName: $0.key, sessions: $0.value.sorted { $0.date > $1.date }) }
            .sorted { $0.latestDate > $1.latestDate }
    }

    // Summary across all filtered sessions
    private var totalBuyIn: Double   { filteredSessions.reduce(0) { $0 + $1.buyInAmount } }
    private var totalCashOut: Double { filteredSessions.reduce(0) { $0 + $1.cashOutAmount } }
    private var totalPL: Double      { totalCashOut - totalBuyIn }

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                Color.staxBackground.ignoresSafeArea()

                VStack(spacing: 0) {
                    StaxHeader(title: "Sessions", subtitle: "All play, buy-ins, and results")

                    VStack(spacing: 10) {
                        FilterChipRow(options: ["All", "Cash", "Tourney"], selected: $selectedFilter)
                            .padding(.top, 12)

                        // Overall summary bar
                        OverallSummaryBar(
                            casinoCount: casinoGroups.count,
                            totalBuyIn: totalBuyIn,
                            totalCashOut: totalCashOut,
                            totalPL: totalPL
                        )

                        // Free-tier session limit upsell banner
                        if !entitlementManager.isPremium && vm.sessions.count >= FreeTierLimits.maxSessions {
                            UpgradeBanner(
                                message: "You've used \(FreeTierLimits.maxSessions) of \(FreeTierLimits.maxSessions) free sessions. Unlock unlimited.",
                                onUpgrade: { showPaywall() }
                            )
                        }

                        if casinoGroups.isEmpty {
                            Spacer()
                            StaxEmptyState(title: "No sessions", message: "Tap + to create your first session.")
                            Spacer()
                        } else {
                            List {
                                ForEach(casinoGroups) { group in
                                    NavigationLink(destination: CasinoSessionsView(vm: vm, casinoName: group.casinoName)) {
                                        CasinoGroupRow(
                                            group: group,
                                            logoAssetName: vm.casinoLogoMap[group.casinoName]
                                        )
                                        .listRowBackground(Color.clear)
                                    }
                                    .listRowInsets(EdgeInsets(top: 4, leading: 0, bottom: 4, trailing: 0))
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
                Button {
                    let result = entitlementManager.checkLimit(for: .sessionCreate, totalSessions: vm.sessions.count)
                    if case .blocked = result {
                        showPaywall()
                    } else {
                        showAddSession = true
                    }
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

// MARK: – Casino group row

struct CasinoGroupRow: View {
    let group: CasinoGroup
    let logoAssetName: String?

    var plColor: Color { group.totalPL >= 0 ? .staxProfit : .staxLoss }
    var plPrefix: String { group.totalPL >= 0 ? "+" : "" }

    var typeLabel: String {
        var parts: [String] = []
        if group.cashCount > 0 { parts.append("\(group.cashCount) Cash") }
        if group.tourneyCount > 0 { parts.append("\(group.tourneyCount) Tourney") }
        return parts.joined(separator: " · ")
    }

    var body: some View {
        HStack(spacing: 14) {
            // Logo / fallback
            if let assetName = logoAssetName, let uiImg = UIImage(named: assetName) {
                Image(uiImage: uiImg)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 46, height: 46)
                    .clipShape(RoundedRectangle(cornerRadius: 10))
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .fill(Color.staxPrimaryContainer)
                        .frame(width: 46, height: 46)
                    Image(systemName: "suit.spade.fill")
                        .font(.system(size: 20))
                        .foregroundColor(.staxPrimary)
                }
            }

            // Name + breakdown
            VStack(alignment: .leading, spacing: 3) {
                Text(group.casinoName)
                    .font(.subheadline).bold()
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text(typeLabel)
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
            }

            Spacer()

            // P&L + count
            VStack(alignment: .trailing, spacing: 2) {
                Text("\(plPrefix)\(fmt(group.totalPL))")
                    .font(.subheadline).bold()
                    .foregroundColor(plColor)
                Text("\(group.sessionCount) sessions")
                    .font(.caption2)
                    .foregroundColor(.staxOnSurfaceVar)
            }
        }
        .padding(.vertical, 10)
        .padding(.horizontal, 14)
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

// MARK: – Overall summary bar

struct OverallSummaryBar: View {
    let casinoCount: Int
    let totalBuyIn: Double
    let totalCashOut: Double
    let totalPL: Double

    var plColor: Color { totalPL >= 0 ? .staxProfit : .staxLoss }
    var plPrefix: String { totalPL >= 0 ? "+" : "" }

    var body: some View {
        HStack {
            Text("\(casinoCount) \(casinoCount == 1 ? "casino" : "casinos")")
                .font(.caption).foregroundColor(.staxOnSurfaceVar)
            Spacer()
            Text("In \(fmt(totalBuyIn))")
                .font(.caption).foregroundColor(.staxOnSurfaceVar)
            Text("Out \(fmt(totalCashOut))")
                .font(.caption).foregroundColor(.staxOnSurfaceVar)
            Text("\(plPrefix)\(fmt(totalPL))")
                .font(.caption).bold()
                .foregroundColor(plColor)
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

// MARK: – Win/Loss summary (kept for shared use)

struct WinLossSummary: View {
    let sessions: [Session]
    private var totalBuyIn: Double   { sessions.reduce(0) { $0 + $1.buyInAmount } }
    private var totalCashOut: Double { sessions.reduce(0) { $0 + $1.cashOutAmount } }
    private var totalPL: Double      { totalCashOut - totalBuyIn }

    var body: some View {
        HStack {
            Text("\(sessions.count) sessions").font(.caption).foregroundColor(.staxOnSurfaceVar)
            Spacer()
            Text("In \(fmt(totalBuyIn))").font(.caption).foregroundColor(.staxOnSurfaceVar)
            Text("Out \(fmt(totalCashOut))").font(.caption).foregroundColor(.staxOnSurfaceVar)
            Text(totalPL >= 0 ? "+\(fmt(totalPL))" : fmt(totalPL))
                .font(.caption).bold()
                .foregroundColor(totalPL >= 0 ? .staxProfit : .staxLoss)
        }
        .padding(.horizontal, 16).padding(.vertical, 12)
        .background(Color.staxSurfaceHigh).cornerRadius(16)
    }

    private func fmt(_ v: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: v)) ?? "\(v)"
    }
}

// MARK: – Session row (kept for CasinoSessionsView)

struct AllSessionRow: View {
    let session: Session
    var logoAssetName: String? = nil
    var profitLoss: Double { session.profitLoss }

    var body: some View {
        HStack(spacing: 14) {
            ZStack(alignment: .bottomTrailing) {
                if let assetName = logoAssetName, let uiImg = UIImage(named: assetName) {
                    Image(uiImage: uiImg)
                        .resizable().scaledToFill()
                        .frame(width: 40, height: 40)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                } else {
                    Image(systemName: session.type == "Cash" ? "dollarsign.circle.fill" : "trophy.fill")
                        .font(.system(size: 32))
                        .foregroundColor(session.type == "Cash" ? .green : .yellow)
                        .frame(width: 40, height: 40)
                }
                Image(systemName: session.type == "Cash" ? "dollarsign.circle.fill" : "trophy.fill")
                    .font(.system(size: 14))
                    .foregroundColor(session.type == "Cash" ? .green : .yellow)
                    .background(Color.staxBackground.clipShape(Circle()))
                    .offset(x: 4, y: 4)
            }
            .frame(width: 44, height: 44)

            VStack(alignment: .leading, spacing: 3) {
                Text(session.name).font(.subheadline).bold().foregroundColor(.white).lineLimit(1)
                Text(session.casinoName).font(.caption).foregroundColor(.staxOnSurfaceVar)
                Text(session.date).font(.caption2).foregroundColor(.staxOnSurfaceVar.opacity(0.75))
            }
            Spacer()
            Text(profitLoss >= 0 ? "+\(fmt(profitLoss))" : fmt(profitLoss))
                .font(.subheadline).bold()
                .foregroundColor(profitLoss >= 0 ? .staxProfit : .staxLoss)
        }
        .padding(.vertical, 6).padding(.horizontal, 4)
        .background(Color.staxSurface).cornerRadius(14)
    }

    private func fmt(_ v: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: v)) ?? "\(v)"
    }
}
