import SwiftUI

struct SplashView: View {
    @State private var scale: CGFloat = 0.8
    @State private var opacity: Double = 0

    var body: some View {
        ZStack {
            LinearGradient(
                colors: [Color(red: 0.10, green: 0.07, blue: 0.18), Color.black],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()

            VStack(spacing: 24) {
                StaxLogoImage(size: 140)
                    .scaleEffect(scale)
                    .opacity(opacity)

                VStack(spacing: 6) {
                    Text("STAX")
                        .font(.system(size: 42, weight: .black, design: .rounded))
                        .foregroundColor(.white)
                        .tracking(8)
                    Text("Poker Porn, No Shame.")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.55))
                        .tracking(1)
                }
                .opacity(opacity)
            }
        }
        .onAppear {
            withAnimation(.spring(duration: 0.8)) {
                scale = 1.0
                opacity = 1.0
            }
        }
    }
}
