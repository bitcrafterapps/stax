import SwiftUI

struct ChipConfigView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @Environment(\.showPaywall) private var showPaywall

    private let repo = ChipConfigRepository()
    @State private var selectedState: String = ""
    @State private var selectedCasino: String = ""
    @State private var gameType = "Cash"
    @State private var chips: [ChipConfig] = []
    @State private var showEditDialog = false
    @State private var showResetConfirm = false
    @State private var editingChip: ChipConfig? = nil

    private var casinoData: [String: [String]] = {
        guard let url = Bundle.main.url(forResource: "casinos", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let dict = try? JSONDecoder().decode([String: [String]].self, from: data) else { return [:] }
        return dict
    }()

    private var stateList: [String] { casinoData.keys.sorted() }
    private var casinoList: [String] { casinoData[selectedState] ?? [] }
    private var casinoName: String { selectedCasino.isEmpty ? "Default" : selectedCasino }
    private var showDollar: Bool { gameType == "Cash" }
    private var selectedCasinoIndex: Int {
        casinoList.firstIndex(of: selectedCasino) ?? 0
    }
    private var casinoIsLocked: Bool {
        guard !entitlementManager.isPremium else { return false }
        if case .allowed = entitlementManager.checkLimit(for: .chipConfig, casinoIndex: selectedCasinoIndex) {
            return false
        }
        return true
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Color.staxBackground.ignoresSafeArea()
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {

                        // State picker
                        DropdownSelector(label: "State / Region", options: stateList, selected: $selectedState)
                            .onChange(of: selectedState) { _, new in
                                selectedCasino = casinoData[new]?.first ?? ""
                                reloadChips()
                            }

                        // Casino picker
                        VStack(alignment: .leading, spacing: 4) {
                            DropdownSelector(label: "Casino / Card Room", options: casinoList, selected: $selectedCasino)
                                .onChange(of: selectedCasino) { _, _ in reloadChips() }
                            if casinoIsLocked {
                                HStack(spacing: 4) {
                                    Image(systemName: "lock.fill")
                                        .font(.caption2)
                                        .foregroundColor(.staxPrimary)
                                    Text("PRO — Upgrade to edit this casino's chips")
                                        .font(.caption2)
                                        .foregroundColor(.staxPrimary)
                                }
                            }
                        }

                        // Cash / Tourney toggle
                        HStack(spacing: 0) {
                            ForEach(["Cash", "Tourney"], id: \.self) { t in
                                Button(t) { gameType = t; reloadChips() }
                                    .font(.subheadline.bold())
                                    .foregroundColor(gameType == t ? .white : .staxOnSurfaceVar)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 10)
                                    .background(gameType == t ? Color.staxPrimary : Color.clear)
                            }
                        }
                        .background(Color.staxSurface)
                        .cornerRadius(12)
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.15), lineWidth: 1))

                        // Chip grid
                        ZStack {
                            LazyVGrid(columns: [GridItem(.adaptive(minimum: 72), spacing: 12)], spacing: 12) {
                                ForEach(chips) { chip in
                                    VStack(spacing: 4) {
                                        CasinoChipView(chip: chip, size: 64, showDollar: showDollar)
                                        Text(chip.colorName)
                                            .font(.caption2)
                                            .foregroundColor(.staxOnSurfaceVar)
                                            .lineLimit(1)
                                    }
                                    .onTapGesture {
                                        guard !casinoIsLocked else { return }
                                        editingChip = chip
                                        showEditDialog = true
                                    }
                                    .contextMenu {
                                        if !casinoIsLocked {
                                            Button(role: .destructive) {
                                                chips.removeAll { $0.id == chip.id }
                                                saveChips()
                                            } label: {
                                                Label("Delete Chip", systemImage: "trash")
                                            }
                                        }
                                    }
                                }

                                if !casinoIsLocked {
                                    // Add chip button
                                    Button {
                                        let newId = (chips.map(\.id).max() ?? 0) + 1
                                        editingChip = ChipConfig(id: newId, colorHex: 0xFFAAAAAA, value: "5", colorName: "grey")
                                        showEditDialog = true
                                    } label: {
                                        VStack(spacing: 4) {
                                            ZStack {
                                                Circle().fill(Color.staxSurface)
                                                    .frame(width: 64, height: 64)
                                                Image(systemName: "plus")
                                                    .font(.title2)
                                                    .foregroundColor(.staxOnSurfaceVar)
                                            }
                                            Text("Add")
                                                .font(.caption2)
                                                .foregroundColor(.staxOnSurfaceVar)
                                        }
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .disabled(casinoIsLocked)

                            // Lock overlay for premium casino slots
                            if casinoIsLocked {
                                RoundedRectangle(cornerRadius: 12)
                                    .fill(Color.black.opacity(0.55))
                                VStack(spacing: 8) {
                                    Image(systemName: "lock.fill")
                                        .font(.system(size: 32))
                                        .foregroundColor(.staxPrimary)
                                    Text("Premium Only")
                                        .font(.headline.bold())
                                        .foregroundColor(.white)
                                    Text("Upgrade to configure all casinos")
                                        .font(.caption)
                                        .foregroundColor(.staxOnSurfaceVar)
                                        .multilineTextAlignment(.center)
                                        .padding(.horizontal, 16)
                                }
                            }
                        }

                        if casinoIsLocked {
                            UpgradeBanner(
                                message: "Upgrade to configure chips for all casinos",
                                onUpgrade: { showPaywall() }
                            )
                        }

                        // Action buttons row
                        HStack(spacing: 10) {
                            Button {
                                showEditDialog = true
                                editingChip = nil
                            } label: {
                                Label("Edit Chips", systemImage: "pencil")
                                    .font(.subheadline.bold())
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                                    .background(casinoIsLocked ? Color.staxPrimary.opacity(0.35) : Color.staxPrimary)
                                    .cornerRadius(14)
                            }
                            .disabled(casinoIsLocked)

                            Button { showResetConfirm = true } label: {
                                Label("Defaults", systemImage: "arrow.clockwise")
                                    .font(.subheadline.bold())
                                    .foregroundColor(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 14)
                                    .background(casinoIsLocked ? Color.staxPrimary.opacity(0.35) : Color.staxPrimary)
                                    .cornerRadius(14)
                            }
                            .disabled(casinoIsLocked)
                        }
                    }
                    .padding(16)
                }
            }
            .navigationTitle("Chip Configuration")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }.foregroundColor(.staxPrimary)
                }
            }
            .alert("Reset to Defaults?", isPresented: $showResetConfirm) {
                Button("Reset", role: .destructive) {
                    repo.resetToDefaults(casino: casinoName, gameType: gameType)
                    reloadChips()
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("This will replace your current chip configuration with the defaults.")
            }
            .sheet(isPresented: $showEditDialog) {
                if let chip = editingChip {
                    ChipEditSheet(chip: chip, showDollar: showDollar) { updated in
                        if let idx = chips.firstIndex(where: { $0.id == updated.id }) {
                            chips[idx] = updated
                        } else {
                            chips.append(updated)
                        }
                        saveChips()
                        showEditDialog = false
                    } onDismiss: {
                        showEditDialog = false
                    }
                } else {
                    // Generic add — open with a blank chip
                    let newId = (chips.map(\.id).max() ?? 0) + 1
                    ChipEditSheet(
                        chip: ChipConfig(id: newId, colorHex: 0xFFAAAAAA, value: "5", colorName: "grey"),
                        showDollar: showDollar
                    ) { updated in
                        chips.append(updated)
                        saveChips()
                        showEditDialog = false
                    } onDismiss: {
                        showEditDialog = false
                    }
                }
            }
        }
        .preferredColorScheme(.dark)
        .onAppear {
            if selectedState.isEmpty {
                selectedState = stateList.first ?? ""
                selectedCasino = casinoData[selectedState]?.first ?? ""
            }
            reloadChips()
        }
    }

    private func reloadChips() {
        chips = repo.load(casino: casinoName, gameType: gameType)
    }

    private func saveChips() {
        repo.save(chips, casino: casinoName, gameType: gameType)
    }
}

// MARK: – Chip edit sheet

struct ChipEditSheet: View {
    let chip: ChipConfig
    let showDollar: Bool
    let onSave: (ChipConfig) -> Void
    let onDismiss: () -> Void

    @State private var value: String
    @State private var colorName: String
    @State private var selectedColor: Color

    init(chip: ChipConfig, showDollar: Bool, onSave: @escaping (ChipConfig) -> Void, onDismiss: @escaping () -> Void) {
        self.chip = chip
        self.showDollar = showDollar
        self.onSave = onSave
        self.onDismiss = onDismiss
        _value = State(initialValue: chip.value)
        _colorName = State(initialValue: chip.colorName)
        _selectedColor = State(initialValue: chip.color)
    }

    var body: some View {
        NavigationStack {
            ZStack {
                Color.staxBackground.ignoresSafeArea()
                VStack(spacing: 24) {
                    // Preview
                    CasinoChipView(
                        chip: ChipConfig(id: chip.id, colorHex: selectedColor.argb, value: value, colorName: colorName),
                        size: 100,
                        showDollar: showDollar
                    )

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Value (\(showDollar ? "$" : "chips"))")
                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                        TextField("e.g. 25", text: $value)
                            .keyboardType(.numberPad)
                            .textFieldStyle(.plain)
                            .padding(12)
                            .background(Color.staxSurface)
                            .cornerRadius(10)
                            .foregroundColor(.white)
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Color Name")
                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                        TextField("e.g. red", text: $colorName)
                            .textFieldStyle(.plain)
                            .padding(12)
                            .background(Color.staxSurface)
                            .cornerRadius(10)
                            .foregroundColor(.white)
                    }

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Color")
                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                        ColorPicker("Pick a color", selection: $selectedColor)
                            .labelsHidden()
                            .frame(height: 44)
                    }

                    Spacer()

                    Button {
                        let updated = ChipConfig(id: chip.id, colorHex: selectedColor.argb, value: value, colorName: colorName)
                        onSave(updated)
                    } label: {
                        Text("Save Chip")
                            .font(.headline).foregroundColor(.white)
                            .frame(maxWidth: .infinity).padding(.vertical, 16)
                            .background(Color.staxPrimary).cornerRadius(16)
                    }
                    .disabled(value.isEmpty || colorName.isEmpty)
                }
                .padding(24)
            }
            .navigationTitle("Edit Chip")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onDismiss() }.foregroundColor(.staxPrimary)
                }
            }
        }
        .preferredColorScheme(.dark)
    }
}
