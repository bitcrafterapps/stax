import SwiftUI

// MARK: – Brand colors

extension Color {
    static let staxBackground    = Color(red: 0.07, green: 0.05, blue: 0.12)
    static let staxSurface       = Color(red: 0.13, green: 0.11, blue: 0.18).opacity(0.85)
    static let staxSurfaceHigh   = Color(red: 0.18, green: 0.15, blue: 0.24).opacity(0.85)
    static let staxPrimary       = Color(red: 0.55, green: 0.25, blue: 0.85)
    static let staxPrimaryContainer = Color(red: 0.35, green: 0.12, blue: 0.60)
    static let staxProfit        = Color(red: 0.20, green: 0.80, blue: 0.40)
    static let staxLoss          = Color(red: 0.95, green: 0.30, blue: 0.30)
    static let staxOnSurface     = Color.white
    static let staxOnSurfaceVar  = Color.white.opacity(0.60)
}

// MARK: – Gradient

struct StaxHeaderGradient: View {
    var body: some View {
        LinearGradient(
            colors: [Color(red: 0.15, green: 0.12, blue: 0.22), Color(red: 0.05, green: 0.04, blue: 0.08)],
            startPoint: .leading,
            endPoint: .trailing
        )
        .ignoresSafeArea(edges: .top)
    }
}

var staxHeaderGradient: LinearGradient {
    LinearGradient(
        colors: [Color(red: 0.15, green: 0.12, blue: 0.22), Color(red: 0.05, green: 0.04, blue: 0.08)],
        startPoint: .leading,
        endPoint: .trailing
    )
}

// MARK: – Screen header

struct StaxScreenHeader: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(title)
                .font(.title2).bold()
                .foregroundColor(.white)
            Text(subtitle)
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.65))
        }
    }
}

// MARK: – Header row (logo + title/subtitle)

struct StaxHeader: View {
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: 12) {
            StaxLogoImage(size: 72)
            StaxScreenHeader(title: title, subtitle: subtitle)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(staxHeaderGradient)
    }
}

// MARK: – Logo image (uses bundled asset or fallback)

struct StaxLogoImage: View {
    let size: CGFloat

    var body: some View {
        if let img = UIImage(named: "StaxLogo") {
            Image(uiImage: img)
                .resizable()
                .scaledToFit()
                .frame(width: size, height: size)
        } else {
            ZStack {
                Circle().fill(Color.staxPrimary.opacity(0.25))
                Text("∞")
                    .font(.system(size: size * 0.5, weight: .bold))
                    .foregroundColor(.staxPrimary)
            }
            .frame(width: size, height: size)
        }
    }
}

// MARK: – Empty state

struct StaxEmptyState: View {
    let title: String
    let message: String

    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "photo.stack")
                .font(.system(size: 48))
                .foregroundColor(.staxOnSurfaceVar)
            Text(title)
                .font(.title3).bold()
                .foregroundColor(.staxOnSurface)
            Text(message)
                .font(.subheadline)
                .foregroundColor(.staxOnSurfaceVar)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
        }
    }
}

// MARK: – Section label (like Android's SessionSectionLabel)

struct SessionSectionLabel: View {
    let text: String
    var body: some View {
        Text(text.uppercased())
            .font(.caption).bold()
            .foregroundColor(.staxPrimary)
            .tracking(1)
    }
}

// MARK: – Session type card

struct SessionTypeCard: View {
    let label: String
    let emoji: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            VStack(spacing: 8) {
                Text(emoji).font(.system(size: 28))
                Text(label)
                    .font(.subheadline)
                    .fontWeight(selected ? .bold : .regular)
                    .foregroundColor(selected ? .white : .staxOnSurfaceVar)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 20)
            .background(selected ? Color.staxPrimaryContainer : Color.staxSurface)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(selected ? Color.staxPrimary : Color.clear, lineWidth: 2)
            )
        }
        .buttonStyle(.plain)
    }
}

// MARK: – Dropdown selector (picker-style)

struct DropdownSelector: View {
    let label: String
    let options: [String]
    @Binding var selected: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Menu {
                ForEach(options, id: \.self) { opt in
                    Button(opt) { selected = opt }
                }
            } label: {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(label)
                            .font(.caption)
                            .foregroundColor(.staxOnSurfaceVar)
                        Text(selected.isEmpty ? "Select…" : selected)
                            .font(.body)
                            .foregroundColor(.white)
                    }
                    Spacer()
                    Image(systemName: "chevron.up.chevron.down")
                        .font(.caption)
                        .foregroundColor(.staxOnSurfaceVar)
                }
                .padding(12)
                .background(Color.staxSurface)
                .cornerRadius(10)
                .overlay(
                    RoundedRectangle(cornerRadius: 10)
                        .stroke(Color.white.opacity(0.18), lineWidth: 1)
                )
            }
        }
    }
}

// MARK: – Filter chip row

struct FilterChipRow: View {
    let options: [String]
    @Binding var selected: String

    var body: some View {
        HStack(spacing: 8) {
            ForEach(options, id: \.self) { opt in
                Button(action: { selected = opt }) {
                    Text(opt)
                        .font(.subheadline)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 7)
                        .background(selected == opt ? Color.staxPrimary : Color.staxSurface)
                        .foregroundColor(selected == opt ? .white : .staxOnSurfaceVar)
                        .cornerRadius(20)
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(selected == opt ? Color.staxPrimary : Color.white.opacity(0.18), lineWidth: 1)
                        )
                }
                .buttonStyle(.plain)
            }
        }
    }
}
