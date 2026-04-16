import SwiftUI

private let handRanks     = ["A","K","Q","J","T","9","8","7","6","5","4","3","2"]
private let handSuits     = ["♠","♥","♦","♣"]
private let handPositions = ["BTN","CO","HJ","LJ","BB","SB","UTG","EP"]
private let handResults   = ["Won","Lost","Folded"]

private let heroGreen   = Color(red: 0.18, green: 0.49, blue: 0.20)
private let heroGreenBdr = Color(red: 0.4, green: 0.73, blue: 0.42)
private let villainBlue  = Color(red: 0.08, green: 0.40, blue: 0.75)
private let villainBlueBdr = Color(red: 0.26, green: 0.65, blue: 0.96)

private func suitColor(_ suit: String) -> Color {
    suit == "♥" || suit == "♦" ? Color(red: 0.83, green: 0.18, blue: 0.18) : Color(red: 0.1, green: 0.1, blue: 0.1)
}

private func resultColor(_ result: String) -> Color {
    switch result {
    case "Won":    return .staxProfit
    case "Lost":   return .staxLoss
    default:       return .gray
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Public section
// ─────────────────────────────────────────────────────────────────────────────

struct HandHistorySection: View {
    @ObservedObject var vm: SessionsViewModel
    let sessionId: UUID

    @State private var showAddSheet = false
    private var hands: [Hand] { vm.hands(for: sessionId) }

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Text("Hand History").font(.headline).bold().foregroundColor(.white)
                Spacer()
                Button(action: { showAddSheet = true }) {
                    Label("Add Hand", systemImage: "plus").font(.subheadline).foregroundColor(.staxPrimary)
                }
            }

            if hands.isEmpty {
                Text("No hands recorded yet.\nTap \"Add Hand\" to start tracking.")
                    .font(.subheadline).foregroundColor(.staxOnSurfaceVar)
                    .multilineTextAlignment(.center)
                    .frame(maxWidth: .infinity).padding(24)
                    .background(Color.staxSurfaceHigh).cornerRadius(16)
            } else {
                ForEach(hands) { hand in
                    HandRow(hand: hand, vm: vm)
                }
            }
        }
        .sheet(isPresented: $showAddSheet) {
            AddHandSheet(vm: vm, sessionId: sessionId)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Hand list row
// ─────────────────────────────────────────────────────────────────────────────

private struct HandRow: View {
    let hand: Hand
    @ObservedObject var vm: SessionsViewModel
    @State private var confirmDelete = false

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 6) {
                // Hero hole cards
                HStack(spacing: 4) {
                    if !hand.holeCard1Rank.isEmpty { PlayingCardView(rank: hand.holeCard1Rank, suit: hand.holeCard1Suit) }
                    if !hand.holeCard2Rank.isEmpty { PlayingCardView(rank: hand.holeCard2Rank, suit: hand.holeCard2Suit) }
                }

                // Villain cards
                ForEach(hand.villains.indices, id: \.self) { idx in
                    let v = hand.villains[idx]
                    if !v.card1Rank.isEmpty || !v.card2Rank.isEmpty {
                        Text("vs").font(.caption2).foregroundColor(.staxOnSurfaceVar).padding(.horizontal, 2)
                        HStack(spacing: 3) {
                            if !v.card1Rank.isEmpty { PlayingCardView(rank: v.card1Rank, suit: v.card1Suit, isVillain: true) }
                            if !v.card2Rank.isEmpty { PlayingCardView(rank: v.card2Rank, suit: v.card2Suit, isVillain: true) }
                        }
                    }
                }

                Spacer()

                // Position badge
                if !hand.position.isEmpty { badgeView(hand.position, bg: Color.staxPrimary.opacity(0.15), fg: .staxPrimary) }
                // Result badge
                if !hand.result.isEmpty   { badgeView(hand.result,   bg: resultColor(hand.result).opacity(0.15), fg: resultColor(hand.result)) }

                // Star
                Button(action: { vm.toggleStarHand(id: hand.id) }) {
                    Image(systemName: hand.isStarred ? "star.fill" : "star")
                        .foregroundColor(hand.isStarred ? Color(red:1,green:0.7,blue:0) : .staxOnSurfaceVar)
                        .font(.system(size: 18))
                }.buttonStyle(.plain)

                // Delete
                Button(action: { confirmDelete = true }) {
                    Image(systemName: "trash")
                        .foregroundColor(.staxOnSurfaceVar.opacity(0.5))
                        .font(.system(size: 15))
                }.buttonStyle(.plain)
            }

            if !hand.notes.isEmpty {
                Text(hand.notes).font(.caption).foregroundColor(.staxOnSurfaceVar).lineLimit(2)
            }
            if !hand.timestamp.isEmpty {
                Text(hand.timestamp).font(.caption2).foregroundColor(.staxOnSurfaceVar.opacity(0.5))
            }
        }
        .padding(12)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(14)
        .alert("Delete Hand?", isPresented: $confirmDelete) {
            Button("Delete", role: .destructive) { vm.deleteHand(id: hand.id) }
            Button("Cancel", role: .cancel) {}
        } message: { Text("This hand will be permanently removed.") }
    }

    private func badgeView(_ label: String, bg: Color, fg: Color) -> some View {
        Text(label).font(.caption2).bold().foregroundColor(fg)
            .padding(.horizontal, 7).padding(.vertical, 3).background(bg).cornerRadius(6)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Playing card view
// ─────────────────────────────────────────────────────────────────────────────

struct PlayingCardView: View {
    let rank: String
    let suit: String
    var size: CGFloat = 36
    var selected: Bool = false
    var isVillain: Bool = false

    var body: some View {
        let isRed    = suit == "♥" || suit == "♦"
        let bgCol    = selected ? (isVillain ? villainBlue : heroGreen) : Color.white
        let bdrCol   = selected ? (isVillain ? villainBlueBdr : heroGreenBdr) : Color(white: 0.75)
        let textCol: Color = selected ? .white : (isRed ? Color(red:0.83,green:0.18,blue:0.18) : Color(red:0.1,green:0.1,blue:0.1))

        VStack(spacing: 0) {
            Text(rank).font(.system(size: size * 0.33, weight: .black)).foregroundColor(textCol)
            Text(suit).font(.system(size: size * 0.28, weight: .bold)).foregroundColor(textCol)
        }
        .frame(width: size, height: size * 1.35)
        .background(bgCol).cornerRadius(5)
        .overlay(RoundedRectangle(cornerRadius: 5).stroke(bdrCol, lineWidth: selected ? 2 : 0.5))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Hand sheet
// ─────────────────────────────────────────────────────────────────────────────

private struct AddHandSheet: View {
    @ObservedObject var vm: SessionsViewModel
    let sessionId: UUID
    @Environment(\.dismiss) private var dismiss

    @State private var heroSelected:  [String] = []           // "Rank|Suit"
    @State private var villainSelections: [[String]] = []     // per-villain lists
    @State private var position = ""
    @State private var result   = ""
    @State private var notes    = ""

    private func toggleHero(_ rank: String, _ suit: String) {
        let key = "\(rank)|\(suit)"
        if heroSelected.contains(key)     { heroSelected.removeAll { $0 == key } }
        else if heroSelected.count < 2    { heroSelected.append(key) }
    }

    private func toggleVillain(_ idx: Int, _ rank: String, _ suit: String) {
        let key = "\(rank)|\(suit)"
        var cards = villainSelections[idx]
        if cards.contains(key)   { cards.removeAll { $0 == key } }
        else if cards.count < 2  { cards.append(key) }
        villainSelections[idx] = cards
    }

    private func cardPart(_ list: [String], _ idx: Int, _ field: String) -> String {
        guard idx < list.count else { return "" }
        let parts = list[idx].split(separator: "|").map(String.init)
        return field == "rank" ? (parts.first ?? "") : (parts.last ?? "")
    }

    private func buildVillains() -> [VillainCards] {
        villainSelections.map { cards in
            VillainCards(
                card1Rank: cardPart(cards, 0, "rank"), card1Suit: cardPart(cards, 0, "suit"),
                card2Rank: cardPart(cards, 1, "rank"), card2Suit: cardPart(cards, 1, "suit")
            )
        }
    }

    private func blockedCardsForVillain(_ idx: Int) -> Set<String> {
        Set(heroSelected + villainSelections.enumerated().filter { $0.offset != idx }.flatMap(\.element))
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 18) {

                    // ── Hero hole cards ───────────────────────────────────
                    sectionLabel("Your Hole Cards")
                    cardPreview(selected: heroSelected, isVillain: false) { heroSelected = [] }
                    CardPickerGrid(selected: heroSelected, isVillain: false, onToggle: toggleHero)

                    // ── Opponents ─────────────────────────────────────────
                    sectionLabel("Opponents (optional)")
                    ForEach(villainSelections.indices, id: \.self) { idx in
                        VStack(alignment: .leading, spacing: 8) {
                            HStack {
                                Text("Villain \(idx + 1)").font(.caption).bold().foregroundColor(villainBlueBdr)
                                Spacer()
                                // Inline preview
                                HStack(spacing: 3) {
                                    ForEach(villainSelections[idx], id: \.self) { key in
                                        let parts = key.split(separator: "|").map(String.init)
                                        PlayingCardView(rank: parts[0], suit: parts[1], size: 28, selected: true, isVillain: true)
                                    }
                                }
                                Button(action: { villainSelections.remove(at: idx) }) {
                                    Image(systemName: "trash").font(.caption).foregroundColor(.staxOnSurfaceVar.opacity(0.5))
                                }.buttonStyle(.plain)
                            }
                            CardPickerGrid(
                                selected: villainSelections[idx],
                                blockedKeys: blockedCardsForVillain(idx),
                                isVillain: true,
                                onToggle: { r, s in toggleVillain(idx, r, s) }
                            )
                        }
                        .padding(12)
                        .background(villainBlue.opacity(0.08))
                        .cornerRadius(12)
                    }

                    if villainSelections.count < 4 {
                        Button(action: { villainSelections.append([]) }) {
                            Label("Add Opponent", systemImage: "person.badge.plus")
                                .font(.subheadline).foregroundColor(.staxPrimary)
                        }.buttonStyle(.plain)
                    }

                    // ── Result ────────────────────────────────────────────
                    sectionLabel("Result")
                    HStack(spacing: 8) {
                        ForEach(handResults, id: \.self) { r in
                            let isSel = result == r
                            Button(r) { result = isSel ? "" : r }
                                .font(.subheadline).fontWeight(isSel ? .bold : .regular)
                                .padding(.horizontal, 14).padding(.vertical, 8)
                                .background(isSel ? resultColor(r).opacity(0.2) : Color.staxSurfaceHigh)
                                .foregroundColor(isSel ? resultColor(r) : .white)
                                .cornerRadius(20)
                        }
                    }

                    // ── Position ──────────────────────────────────────────
                    sectionLabel("Position (optional)")
                    LazyVGrid(columns: Array(repeating: .init(.flexible()), count: 4), spacing: 8) {
                        ForEach(handPositions, id: \.self) { p in
                            let isSel = position == p
                            Button(p) { position = isSel ? "" : p }
                                .font(.caption).bold().frame(maxWidth: .infinity)
                                .padding(.vertical, 8)
                                .background(isSel ? Color.staxPrimary.opacity(0.2) : Color.staxSurfaceHigh)
                                .foregroundColor(isSel ? .staxPrimary : .white)
                                .cornerRadius(10)
                        }
                    }

                    // ── Notes ─────────────────────────────────────────────
                    sectionLabel("Notes (optional)")
                    TextField("e.g. flopped top set, villain shoved…", text: $notes, axis: .vertical)
                        .lineLimit(3...5).padding(12)
                        .background(Color.staxSurfaceHigh).cornerRadius(12).foregroundColor(.white)

                    // ── Save ──────────────────────────────────────────────
                    Button(action: save) {
                        Text("Save Hand").font(.body).bold()
                            .frame(maxWidth: .infinity).padding(.vertical, 16)
                    }
                    .buttonStyle(.borderedProminent).tint(.staxPrimary).cornerRadius(16)
                    .disabled(heroSelected.isEmpty)
                }
                .padding(20)
            }
            .background(Color.staxBackground.ignoresSafeArea())
            .navigationTitle("Record Hand")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") { dismiss() }.foregroundColor(.staxOnSurfaceVar)
                }
            }
        }
    }

    private func sectionLabel(_ text: String) -> some View {
        Text(text).font(.subheadline).bold().foregroundColor(.staxOnSurfaceVar)
    }

    private func cardPreview(selected: [String], isVillain: Bool, onClear: @escaping () -> Void) -> some View {
        HStack(spacing: 8) {
            if selected.isEmpty {
                Text("Select up to 2 cards below").font(.caption).foregroundColor(.staxOnSurfaceVar)
            } else {
                ForEach(selected, id: \.self) { key in
                    let parts = key.split(separator: "|").map(String.init)
                    PlayingCardView(rank: parts[0], suit: parts[1], size: 48, selected: true, isVillain: isVillain)
                }
                Button("Clear", action: onClear).font(.caption).foregroundColor(.staxPrimary)
            }
        }
    }

    private func save() {
        vm.addHand(
            sessionId: sessionId,
            holeCard1Rank: cardPart(heroSelected, 0, "rank"),
            holeCard1Suit: cardPart(heroSelected, 0, "suit"),
            holeCard2Rank: cardPart(heroSelected, 1, "rank"),
            holeCard2Suit: cardPart(heroSelected, 1, "suit"),
            position: position, result: result, notes: notes,
            villains: buildVillains()
        )
        dismiss()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Card picker grid
// ─────────────────────────────────────────────────────────────────────────────

private struct CardPickerGrid: View {
    let selected: [String]
    var blockedKeys: Set<String> = []
    let isVillain: Bool
    let onToggle: (String, String) -> Void

    var body: some View {
        let selBg  = isVillain ? villainBlue : heroGreen
        let selBdr = isVillain ? villainBlueBdr : heroGreenBdr

        VStack(spacing: 4) {
            ForEach(handSuits, id: \.self) { suit in
                HStack(spacing: 3) {
                    Text(suit)
                        .font(.system(size: 13, weight: .black))
                        .foregroundColor(suitColor(suit))
                        .frame(width: 16)

                    ForEach(handRanks, id: \.self) { rank in
                        let key = "\(rank)|\(suit)"
                        let isSel     = selected.contains(key)
                        let isBlocked  = blockedKeys.contains(key) && !isSel
                        let isDisabled = isBlocked || (selected.count >= 2 && !isSel)

                        Button(action: { if !isDisabled { onToggle(rank, suit) } }) {
                            Text(rank)
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(isSel ? .white : isDisabled ? Color.gray.opacity(0.4) : suitColor(suit))
                                .frame(maxWidth: .infinity).frame(height: 30)
                                .background(isSel ? selBg : isDisabled ? Color.staxSurfaceHigh.opacity(0.3) : Color.white.opacity(0.9))
                                .cornerRadius(4)
                                .overlay(RoundedRectangle(cornerRadius: 4).stroke(isSel ? selBdr : Color(white: 0.75), lineWidth: 0.5))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
        }
    }
}
