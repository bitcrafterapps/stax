import SwiftUI

struct SessionDetailView: View {
    @ObservedObject var vm: SessionsViewModel
    @State var session: Session
    @State private var editingCashOut = false
    @State private var cashOutText = ""
    @State private var showPhotos = false

    var profitLoss: Double { session.cashOutAmount - session.buyInAmount }

    var body: some View {
        ZStack {
            Color.staxBackground.ignoresSafeArea()
            ScrollView {
                VStack(spacing: 16) {

                    // P&L card
                    VStack(spacing: 8) {
                        Text(profitLoss >= 0 ? "+\(fmt(profitLoss))" : fmt(profitLoss))
                            .font(.system(size: 48, weight: .black))
                            .foregroundColor(profitLoss >= 0 ? .staxProfit : .staxLoss)
                        Text("Profit / Loss")
                            .font(.caption)
                            .foregroundColor(.staxOnSurfaceVar)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 28)
                    .background(Color.staxSurfaceHigh)
                    .cornerRadius(20)

                    // Details
                    detailCard

                    // Cash out entry
                    cashOutCard

                    // View photos button
                    NavigationLink(destination: PhotoGalleryView(vm: vm, session: session)) {
                        Label("View Photos", systemImage: "photo.stack")
                            .font(.headline)
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.staxPrimary)
                            .cornerRadius(16)
                    }
                    .buttonStyle(.plain)
                }
                .padding(16)
            }
        }
        .navigationTitle(session.name)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var detailCard: some View {
        VStack(spacing: 12) {
            DetailRow(label: "Casino",      value: session.casinoName)
            DetailRow(label: "Date",        value: session.date)
            DetailRow(label: "Type",        value: session.type)
            DetailRow(label: "Game",        value: session.gameType)
            if session.type == "Cash" {
                DetailRow(label: "Stakes",  value: session.stakes)
                if session.antes != "None" {
                    DetailRow(label: "Antes", value: "$\(session.antes)")
                }
            }
            DetailRow(label: "Buy-in",      value: fmt(session.buyInAmount))
        }
        .padding(16)
        .background(Color.staxSurface)
        .cornerRadius(16)
    }

    private var cashOutCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            SessionSectionLabel(text: "Cash Out")

            if session.cashOutAmount > 0 && !editingCashOut {
                HStack {
                    Text(fmt(session.cashOutAmount))
                        .font(.title2).bold()
                        .foregroundColor(.white)
                    Spacer()
                    Button("Edit") {
                        cashOutText = String(Int(session.cashOutAmount))
                        editingCashOut = true
                    }
                    .foregroundColor(.staxPrimary)
                }
            } else {
                HStack {
                    Text("$").foregroundColor(.staxOnSurfaceVar)
                    TextField("0", text: $cashOutText)
                        .keyboardType(.numberPad)
                        .foregroundColor(.white)
                }
                .padding(12)
                .background(Color.staxBackground)
                .cornerRadius(10)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.white.opacity(0.18), lineWidth: 1))

                Button {
                    session.cashOutAmount = Double(cashOutText) ?? 0
                    vm.updateSession(session)
                    editingCashOut = false
                } label: {
                    Text("Save")
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.staxPrimary)
                        .cornerRadius(14)
                }
            }
        }
        .padding(16)
        .background(Color.staxSurface)
        .cornerRadius(16)
    }

    private func fmt(_ v: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: v)) ?? "\(v)"
    }
}

struct DetailRow: View {
    let label: String
    let value: String
    var body: some View {
        HStack {
            Text(label)
                .font(.subheadline)
                .foregroundColor(.staxOnSurfaceVar)
            Spacer()
            Text(value)
                .font(.subheadline).bold()
                .foregroundColor(.white)
        }
    }
}
