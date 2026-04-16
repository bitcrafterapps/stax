import SwiftUI

struct SessionDetailView: View {
    @ObservedObject var vm: SessionsViewModel
    @State var session: Session
    @State private var isEditMode = false
    @State private var editCashOut = ""
    @State private var editName = ""
    @State private var editNotes = ""
    @State private var editBuyIn = ""
    @State private var editTimeIn = ""
    @State private var editTimeOut = ""

    private var profitLoss: Double { session.cashOutAmount - session.buyInAmount }
    private var profitColor: Color { profitLoss >= 0 ? .staxProfit : .staxLoss }
    private var profitPrefix: String { profitLoss >= 0 ? "+" : "" }
    private var logoAssetName: String? { vm.casinoLogoMap[session.casinoName] }
    private var timeValue: String {
        var parts: [String] = []
        if !session.timeIn.isEmpty { parts.append("In \(session.timeIn)") }
        if !session.timeOut.isEmpty { parts.append("Out \(session.timeOut)") }
        return parts.joined(separator: "  →  ")
    }

    var body: some View {
        ZStack {
            Color.staxBackground.ignoresSafeArea()
            if isEditMode {
                editView
            } else {
                viewModeContent
            }
        }
        .navigationTitle(isEditMode ? "Edit Session" : "")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                if isEditMode {
                    Button("Save") {
                        session.cashOutAmount = Double(editCashOut) ?? session.cashOutAmount
                        session.name = editName.isEmpty ? session.name : editName
                        session.notes = editNotes
                        session.buyInAmount = Double(editBuyIn) ?? session.buyInAmount
                        session.timeIn = editTimeIn
                        session.timeOut = editTimeOut
                        vm.updateSession(session)
                        isEditMode = false
                    }
                    .bold()
                    .foregroundColor(.staxPrimary)
                } else {
                    Button {
                        editCashOut = String(Int(session.cashOutAmount))
                        editName = session.name
                        editNotes = session.notes
                        editBuyIn = String(Int(session.buyInAmount))
                        editTimeIn = session.timeIn
                        editTimeOut = session.timeOut
                        isEditMode = true
                    } label: {
                        Image(systemName: "pencil")
                    }
                    .foregroundColor(.white)
                }
            }
        }
    }

    // MARK: – View Mode

    private var viewModeContent: some View {
        ScrollView {
            VStack(spacing: 0) {
                heroSection
                    .padding(.bottom, 16)

                VStack(spacing: 14) {
                    sessionInfoCard
                    if !session.notes.isEmpty { notesCard }
                    photosButton

                    // ── Hand History ──────────────────────────────────────
                    HandHistorySection(vm: vm, sessionId: session.id)
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 40)
            }
        }
    }

    // MARK: – Hero Banner

    private var heroSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Gradient background
            ZStack(alignment: .bottomLeading) {
                LinearGradient(
                    colors: [Color(red: 0.18, green: 0.10, blue: 0.30), Color(red: 0.07, green: 0.05, blue: 0.12)],
                    startPoint: .topLeading, endPoint: .bottomTrailing
                )

                VStack(alignment: .leading, spacing: 6) {
                    // Casino identity
                    HStack(spacing: 10) {
                        if let assetName = logoAssetName, let uiImg = UIImage(named: assetName) {
                            Image(uiImage: uiImg)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 42, height: 42)
                                .clipShape(RoundedRectangle(cornerRadius: 10))
                        } else {
                            ZStack {
                                RoundedRectangle(cornerRadius: 10)
                                    .fill(Color.staxPrimaryContainer)
                                    .frame(width: 42, height: 42)
                                Image(systemName: "suit.spade.fill")
                                    .font(.system(size: 18))
                                    .foregroundColor(.staxPrimary)
                            }
                        }
                        VStack(alignment: .leading, spacing: 1) {
                            Text(session.casinoName)
                                .font(.subheadline).bold()
                                .foregroundColor(.white)
                            Text(session.name)
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.55))
                                .lineLimit(1)
                        }
                        Spacer()
                        TypeBadge(type: session.type)
                    }

                    Spacer().frame(height: 16)

                    // Big P&L
                    Text("\(profitPrefix)\(fmt(profitLoss))")
                        .font(.system(size: 52, weight: .black, design: .rounded))
                        .foregroundColor(profitColor)
                    Text("Profit / Loss")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.50))

                    Spacer().frame(height: 16)

                    // Buy-in / Cash-out chips
                    HStack(spacing: 10) {
                        StatChip(label: "Buy-in", value: fmt(session.buyInAmount))
                        StatChip(label: "Cash-out", value: fmt(session.cashOutAmount))
                        StatChip(label: session.date, value: session.type == "Cash" ? session.stakes : "Tourney")
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 24)
            }
        }
    }

    // MARK: – Session Info Card

    private var sessionInfoCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(text: "Session Info")
            VStack(spacing: 0) {
                IconDetailRow(icon: "calendar", label: "Date", value: session.date)
                if !timeValue.isEmpty {
                    Divider().background(Color.white.opacity(0.08))
                    IconDetailRow(icon: "clock", label: "Time", value: timeValue)
                }
                Divider().background(Color.white.opacity(0.08))
                IconDetailRow(icon: "suit.club.fill", label: "Game", value: session.gameType)
                if session.type == "Cash" {
                    Divider().background(Color.white.opacity(0.08))
                    IconDetailRow(icon: "dollarsign.circle", label: "Stakes", value: session.stakes)
                    if session.antes != "None" && !session.antes.isEmpty {
                        Divider().background(Color.white.opacity(0.08))
                        IconDetailRow(icon: "plus.circle", label: "Antes", value: "$\(session.antes)")
                    }
                }
            }
            .background(Color.staxSurfaceHigh)
            .clipShape(RoundedRectangle(cornerRadius: 18))
        }
    }

    // MARK: – Notes Card

    private var notesCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(text: "Notes")
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: "text.quote")
                    .foregroundColor(.staxPrimary)
                    .font(.system(size: 14))
                    .padding(.top, 2)
                Text(session.notes)
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.85))
                    .lineSpacing(4)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(Color.staxSurfaceHigh)
            .clipShape(RoundedRectangle(cornerRadius: 18))
        }
    }

    // MARK: – Photos Button

    private var photosButton: some View {
        VStack(alignment: .leading, spacing: 10) {
            SectionLabel(text: "Photos")
            NavigationLink(destination: PhotoGalleryView(vm: vm, session: session)) {
                HStack {
                    Image(systemName: "photo.stack.fill")
                        .font(.headline)
                    Text("View & Add Photos")
                        .font(.headline).bold()
                }
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(Color.staxPrimary)
                .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .buttonStyle(.plain)
        }
    }

    // MARK: – Edit Mode

    private var editView: some View {
        ScrollView {
            VStack(spacing: 14) {
                EditField(label: "Session Name", text: $editName)
                EditField(label: "Buy-in Amount", text: $editBuyIn, keyboard: .numberPad)
                EditField(label: "Cash-out Amount", text: $editCashOut, keyboard: .numberPad)
                EditField(label: "Time In (HH:MM)", text: $editTimeIn)
                EditField(label: "Time Out (HH:MM)", text: $editTimeOut)
                VStack(alignment: .leading, spacing: 6) {
                    Text("NOTES".uppercased())
                        .font(.caption).bold()
                        .foregroundColor(.staxPrimary)
                        .tracking(1)
                    TextEditor(text: $editNotes)
                        .scrollContentBackground(.hidden)
                        .foregroundColor(.white)
                        .frame(minHeight: 100)
                        .padding(10)
                        .background(Color.staxSurfaceHigh)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.15), lineWidth: 1))
                }
            }
            .padding(16)
        }
    }

    private func fmt(_ v: Double) -> String {
        let f = NumberFormatter()
        f.numberStyle = .currency
        f.locale = .init(identifier: "en_US")
        return f.string(from: NSNumber(value: v)) ?? "\(v)"
    }
}

// MARK: – Sub-components

private struct StatChip: View {
    let label: String
    let value: String
    var body: some View {
        VStack(spacing: 3) {
            Text(value)
                .font(.caption).bold()
                .foregroundColor(.white)
                .lineLimit(1)
                .minimumScaleFactor(0.7)
            Text(label)
                .font(.caption2)
                .foregroundColor(.white.opacity(0.50))
                .lineLimit(1)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 9)
        .padding(.horizontal, 6)
        .background(Color.white.opacity(0.09))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct TypeBadge: View {
    let type: String
    var isCash: Bool { type == "Cash" }
    var body: some View {
        Text(type)
            .font(.caption).bold()
            .foregroundColor(isCash ? Color(red: 0.29, green: 0.86, blue: 0.50) : Color(red: 0.67, green: 0.55, blue: 0.98))
            .padding(.horizontal, 12)
            .padding(.vertical, 5)
            .background(isCash ? Color(red: 0.10, green: 0.30, blue: 0.18) : Color(red: 0.18, green: 0.10, blue: 0.30))
            .clipShape(Capsule())
    }
}

private struct IconDetailRow: View {
    let icon: String
    let label: String
    let value: String
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(.staxPrimary)
                .frame(width: 20)
            Text(label)
                .font(.caption)
                .foregroundColor(.white.opacity(0.50))
                .frame(width: 52, alignment: .leading)
            Text(value)
                .font(.subheadline).fontWeight(.medium)
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
    }
}

private struct SectionLabel: View {
    let text: String
    var body: some View {
        Text(text.uppercased())
            .font(.caption).bold()
            .foregroundColor(.staxPrimary)
            .tracking(1)
    }
}

private struct EditField: View {
    let label: String
    @Binding var text: String
    var keyboard: UIKeyboardType = .default
    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(label.uppercased())
                .font(.caption).bold()
                .foregroundColor(.staxPrimary)
                .tracking(1)
            TextField("", text: $text)
                .keyboardType(keyboard)
                .foregroundColor(.white)
                .padding(12)
                .background(Color.staxSurfaceHigh)
                .clipShape(RoundedRectangle(cornerRadius: 12))
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.15), lineWidth: 1))
        }
    }
}

struct DetailRow: View {
    let label: String
    let value: String
    var body: some View {
        HStack {
            Text(label).font(.subheadline).foregroundColor(.staxOnSurfaceVar)
            Spacer()
            Text(value).font(.subheadline).bold().foregroundColor(.white)
        }
    }
}
