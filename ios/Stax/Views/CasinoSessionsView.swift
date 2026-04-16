import SwiftUI

struct CasinoSessionsView: View {
    @ObservedObject var vm: SessionsViewModel
    let casinoName: String
    var showPhotos: Bool = false   // true = came from Photos tab, false = came from Sessions tab
    @State private var sessionToDelete: UUID? = nil
    @State private var showDeleteConfirm = false

    private var sessions: [Session] {
        vm.sessionRepo.sessions(for: casinoName)
    }

    @ViewBuilder
    private func destination(for session: Session) -> some View {
        if showPhotos {
            PhotoGalleryView(vm: vm, session: session)
        } else {
            SessionDetailView(vm: vm, session: session)
        }
    }

    var body: some View {
        ZStack {
            Color.staxBackground.ignoresSafeArea()

            VStack(spacing: 0) {
                if sessions.isEmpty {
                    Spacer()
                    StaxEmptyState(title: "No sessions", message: "No sessions found for \(casinoName).")
                    Spacer()
                } else {
                    List {
                        ForEach(sessions) { session in
                            NavigationLink(destination: destination(for: session)) {
                                SessionRow(session: session)
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
        }
        .navigationTitle(casinoName)
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            if let assetName = vm.casinoLogoMap[casinoName] {
                ToolbarItem(placement: .topBarTrailing) {
                    Image(assetName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 32, height: 32)
                        .clipShape(RoundedRectangle(cornerRadius: 7))
                }
            }
        }
        .alert("Delete Session?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) {
                if let id = sessionToDelete {
                    vm.deleteSession(id: id)
                }
            }
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("This will permanently delete the session and all its photos.")
        }
    }
}

struct SessionRow: View {
    let session: Session

    var icon: String {
        session.type == "Cash" ? "dollarsign.circle.fill" : "trophy.fill"
    }
    var iconColor: Color {
        session.type == "Cash" ? .green : .yellow
    }

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: icon)
                .font(.system(size: 32))
                .foregroundColor(iconColor)
                .frame(width: 40)

            VStack(alignment: .leading, spacing: 3) {
                Text(session.name)
                    .font(.subheadline).bold()
                    .foregroundColor(.white)
                    .lineLimit(1)
                Text("\(session.gameType) • \(session.stakes.isEmpty ? session.type : session.stakes)")
                    .font(.caption)
                    .foregroundColor(.staxOnSurfaceVar)
                Text(session.date)
                    .font(.caption2)
                    .foregroundColor(.staxOnSurfaceVar.opacity(0.75))
            }

            Spacer()

            if session.cashOutAmount > 0 || session.buyInAmount > 0 {
                let pl = session.profitLoss
                Text(pl >= 0 ? "+\(formatCurrency(pl))" : formatCurrency(pl))
                    .font(.subheadline).bold()
                    .foregroundColor(pl >= 0 ? .staxProfit : .staxLoss)
            }
        }
        .padding(.vertical, 6)
        .padding(.horizontal, 4)
        .background(Color.staxSurface)
        .cornerRadius(14)
    }

    private func formatCurrency(_ value: Double) -> String {
        let fmt = NumberFormatter()
        fmt.numberStyle = .currency
        fmt.locale = .init(identifier: "en_US")
        return fmt.string(from: NSNumber(value: value)) ?? "\(value)"
    }
}
