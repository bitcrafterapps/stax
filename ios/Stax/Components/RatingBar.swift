import SwiftUI

struct RatingBar: View {
    @Binding var rating: Int
    var maxRating: Int = 5
    var starSize: CGFloat = 22
    var spacing: CGFloat = 4
    var onColor: Color = .yellow
    var offColor: Color = Color.white.opacity(0.30)
    var interactive: Bool = true

    var body: some View {
        HStack(spacing: spacing) {
            ForEach(1...maxRating, id: \.self) { star in
                Image(systemName: star <= rating ? "star.fill" : "star")
                    .font(.system(size: starSize))
                    .foregroundColor(star <= rating ? onColor : offColor)
                    .onTapGesture {
                        if interactive {
                            rating = (star == rating) ? 0 : star
                        }
                    }
            }
        }
    }
}
