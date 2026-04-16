import SwiftUI

struct SplashView: View {
    let onDone: () -> Void

    @State private var headerOpacity: Double = 0
    @State private var chipOpacity: Double = 0

    var body: some View {
        ZStack(alignment: .top) {
            Color.staxBackground.ignoresSafeArea()

            // ── Header — identical layout to the real screen headers ──────
            HStack(spacing: 12) {
                StaxLogoImage(size: 72)
                VStack(alignment: .leading, spacing: 2) {
                    Text("STAX")
                        .font(.system(size: 28, weight: .black, design: .rounded))
                        .foregroundColor(.white)
                        .tracking(4)
                    Text("Stack it. Snap it. Track it.")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.60))
                }
                Spacer()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(staxHeaderGradient)
            .opacity(headerOpacity)

            // ── Centered logo chip — fades in then out ────────────────────
            StaxLogoImage(size: 320)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .opacity(chipOpacity)
        }
        .onAppear {
            // Fade in header and chip together
            withAnimation(.easeIn(duration: 0.6)) {
                headerOpacity = 1.0
            }
            withAnimation(.easeIn(duration: 1.4)) {
                chipOpacity = 1.0
            }
            // Fade chip out before handing off
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
                withAnimation(.easeOut(duration: 1.2)) {
                    chipOpacity = 0.0
                }
            }
            // Notify parent to transition away
            DispatchQueue.main.asyncAfter(deadline: .now() + 3.2) {
                onDone()
            }
        }
    }
}
