import SwiftUI

struct CasinoChipView: View {
    let chip: ChipConfig
    let size: CGFloat
    let showDollar: Bool

    var chipColor: Color { chip.color }
    var textColor: Color { chip.color.luminance > 0.45 ? .black : .white }

    var label: String {
        guard let val = Int(chip.value) else { return chip.value }
        if showDollar {
            if val >= 1_000_000 { return "$\(val / 1_000_000)M" }
            if val >= 1_000     { return "$\(val / 1_000)k" }
            return "$\(val)"
        } else {
            if val >= 1_000_000 { return "\(val / 1_000_000)M" }
            if val >= 1_000     { return "\(val / 1_000)k" }
            return "\(val)"
        }
    }

    var body: some View {
        Canvas { ctx, sz in
            let center = CGPoint(x: sz.width / 2, y: sz.height / 2)
            let radius = sz.width / 2 - 2

            // Outer shadow ring
            let shadowPath = Path(ellipseIn: CGRect(x: center.x - radius - 1, y: center.y - radius - 1,
                                                     width: (radius + 1) * 2, height: (radius + 1) * 2))
            ctx.fill(shadowPath, with: .color(.black.opacity(0.35)))

            // White rim
            let rimPath = Path(ellipseIn: CGRect(x: center.x - radius, y: center.y - radius,
                                                  width: radius * 2, height: radius * 2))
            ctx.fill(rimPath, with: .color(.white))

            // Main chip body
            let bodyRadius = radius * 0.87
            let bodyRect = CGRect(x: center.x - bodyRadius, y: center.y - bodyRadius,
                                  width: bodyRadius * 2, height: bodyRadius * 2)
            let bodyPath = Path(ellipseIn: bodyRect)
            ctx.fill(bodyPath, with: .color(chipColor))

            // Edge spots (8 spots on the rim)
            let spotRadius = (radius - bodyRadius) * 0.38
            let spotDist = (radius + bodyRadius) / 2
            for i in 0..<8 {
                let angle = Double(i) * .pi / 4
                let sx = center.x + CGFloat(cos(angle)) * spotDist
                let sy = center.y + CGFloat(sin(angle)) * spotDist
                let spotRect = CGRect(x: sx - spotRadius, y: sy - spotRadius,
                                      width: spotRadius * 2, height: spotRadius * 2)
                ctx.fill(Path(ellipseIn: spotRect), with: .color(chipColor))
            }

            // Inner white ring
            let innerRingRadius = bodyRadius * 0.72
            let innerRingRect = CGRect(x: center.x - innerRingRadius, y: center.y - innerRingRadius,
                                       width: innerRingRadius * 2, height: innerRingRadius * 2)
            ctx.stroke(Path(ellipseIn: innerRingRect),
                       with: .color(.white.opacity(0.55)),
                       lineWidth: max(1, sz.width * 0.025))

            // Subtle depth gradient overlay
            let innerPath = Path(ellipseIn: bodyRect)
            let shimmer = GraphicsContext.Shading.linearGradient(
                Gradient(colors: [.white.opacity(0.18), .clear]),
                startPoint: CGPoint(x: center.x - bodyRadius * 0.3, y: center.y - bodyRadius * 0.7),
                endPoint: CGPoint(x: center.x + bodyRadius * 0.3, y: center.y + bodyRadius * 0.2)
            )
            ctx.fill(innerPath, with: shimmer)
        }
        .frame(width: size, height: size)
        .overlay(
            Text(label)
                .font(.system(size: size * 0.185, weight: .bold))
                .foregroundColor(textColor)
                .shadow(color: .black.opacity(0.4), radius: 1, x: 0, y: 1)
        )
    }
}
