import SwiftUI

// MARK: – Inline upgrade banner

struct UpgradeBanner: View {
    let message: String
    let onUpgrade: () -> Void

    @State private var dismissed = false

    var body: some View {
        if !dismissed {
            HStack(spacing: 12) {
                Image(systemName: "lock.fill")
                    .foregroundColor(.staxPrimary)
                    .font(.subheadline)

                Text(message)
                    .font(.caption)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Button("Upgrade") {
                    onUpgrade()
                }
                .font(.caption.bold())
                .foregroundColor(.white)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(Color.staxPrimary)
                .cornerRadius(8)

                Button {
                    withAnimation { dismissed = true }
                } label: {
                    Image(systemName: "xmark")
                        .font(.caption)
                        .foregroundColor(.staxOnSurfaceVar)
                }
            }
            .padding(12)
            .background(Color.staxSurfaceHigh)
            .cornerRadius(14)
            .overlay(
                RoundedRectangle(cornerRadius: 14)
                    .stroke(Color.staxPrimary.opacity(0.35), lineWidth: 1)
            )
        }
    }
}

// MARK: – Session usage progress bar for free users

struct SessionUsageBar: View {
    let used: Int
    let limit: Int
    let onUpgrade: () -> Void

    private var atLimit: Bool { used >= limit }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack(spacing: 0) {
                Image(systemName: atLimit ? "exclamationmark.circle.fill" : "chart.bar.fill")
                    .font(.caption)
                    .foregroundColor(atLimit ? .staxLoss : .staxPrimary)
                    .padding(.trailing, 6)
                Text(atLimit ? "Session limit reached" : "Free plan · \(used) / \(limit) sessions used")
                    .font(.caption.bold())
                    .foregroundColor(atLimit ? .staxLoss : .white)
                Spacer()
                Button("Upgrade") { onUpgrade() }
                    .font(.caption.bold())
                    .foregroundColor(.white)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 5)
                    .background(Color.staxPrimary)
                    .cornerRadius(8)
            }
            ProgressView(value: Double(min(used, limit)), total: Double(limit))
                .tint(atLimit ? .staxLoss : .staxPrimary)
                .scaleEffect(x: 1, y: 1.6, anchor: .center)
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 10)
        .background(Color.staxSurfaceHigh)
        .cornerRadius(14)
        .overlay(
            RoundedRectangle(cornerRadius: 14)
                .stroke(atLimit ? Color.staxLoss.opacity(0.35) : Color.staxPrimary.opacity(0.25), lineWidth: 1)
        )
    }
}

// MARK: – Feature-specific upgrade alert

struct UpgradeAlert: View {
    let feature: Feature
    let onUpgrade: () -> Void
    let onDismiss: () -> Void

    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "lock.fill")
                .font(.system(size: 40))
                .foregroundColor(.staxPrimary)

            VStack(spacing: 8) {
                Text(headline)
                    .font(.title3.bold())
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)

                Text(description)
                    .font(.subheadline)
                    .foregroundColor(.staxOnSurfaceVar)
                    .multilineTextAlignment(.center)
            }

            VStack(spacing: 10) {
                Button("Upgrade to Premium") {
                    onUpgrade()
                }
                .font(.headline)
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(Color.staxPrimary)
                .cornerRadius(14)

                Button("Maybe Later") {
                    onDismiss()
                }
                .font(.subheadline)
                .foregroundColor(.staxOnSurfaceVar)
            }
        }
        .padding(28)
        .background(Color.staxSurface)
        .cornerRadius(22)
        .shadow(color: .black.opacity(0.5), radius: 20)
        .padding(.horizontal, 24)
    }

    private var headline: String {
        switch feature {
        case .scan:        return "Daily Scan Limit Reached"
        case .aiScan:      return "AI Scan is Premium"
        case .sessionCreate: return "Session Limit Reached"
        case .photoAdd:    return "Photo Limit Reached"
        case .chipConfig:  return "Multiple Casino Config is Premium"
        case .favorites:   return "Favorites Limit Reached"
        case .shareClean:  return "Clean Export is Premium"
        }
    }

    private var description: String {
        switch feature {
        case .scan:        return "You've used all \(FreeTierLimits.maxScansPerDay) free scans for today. Upgrade for unlimited scanning."
        case .aiScan:      return "The AI Stack Counter is available exclusively to Premium subscribers."
        case .sessionCreate: return "Free accounts can track up to \(FreeTierLimits.maxSessions) sessions. Upgrade for unlimited sessions."
        case .photoAdd:    return "Free accounts can add up to \(FreeTierLimits.maxPhotosPerSession) photos per session. Upgrade for unlimited photos."
        case .chipConfig:  return "Configure chips for all casinos with a Premium subscription."
        case .favorites:   return "Free accounts can save up to \(FreeTierLimits.maxFavorites) favorites. Upgrade for unlimited favorites."
        case .shareClean:  return "Export clean images without watermarks with a Premium subscription."
        }
    }
}
