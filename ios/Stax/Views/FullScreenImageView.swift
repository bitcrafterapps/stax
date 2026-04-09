import SwiftUI

struct FullScreenImageView: View {
    let photos: [Photo]
    let initialPhoto: Photo
    let photoRepo: PhotoRepository

    @Environment(\.dismiss) private var dismiss
    @State private var currentIndex: Int
    @State private var showShare = false
    @State private var shareImage: UIImage? = nil

    init(photos: [Photo], initialPhoto: Photo, photoRepo: PhotoRepository) {
        self.photos = photos
        self.initialPhoto = initialPhoto
        self.photoRepo = photoRepo
        _currentIndex = State(initialValue: photos.firstIndex(where: { $0.id == initialPhoto.id }) ?? 0)
    }

    private var currentPhoto: Photo {
        guard currentIndex < photos.count else { return photos[0] }
        return photos[currentIndex]
    }

    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()

            // Swipeable pager
            TabView(selection: $currentIndex) {
                ForEach(Array(photos.enumerated()), id: \.element.id) { idx, photo in
                    ZoomableImageView(url: photoRepo.fullURL(for: photo))
                        .tag(idx)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .never))

            // Top bar
            VStack {
                HStack {
                    Button { dismiss() } label: {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundStyle(.white, .black.opacity(0.5))
                    }
                    Spacer()
                    Text("\(currentIndex + 1) / \(photos.count)")
                        .font(.subheadline)
                        .foregroundColor(.white.opacity(0.85))
                    Spacer()
                    Button {
                        if let img = UIImage(contentsOfFile: photoRepo.fullPath(for: currentPhoto)) {
                            shareImage = img
                            showShare = true
                        }
                    } label: {
                        Image(systemName: "square.and.arrow.up")
                            .font(.title3)
                            .foregroundColor(.white)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 60)
                Spacer()
            }

            // Bottom: rating bar
            VStack {
                Spacer()
                VStack(spacing: 8) {
                    Text("Rate this photo")
                        .font(.caption)
                        .foregroundColor(.white.opacity(0.65))
                    RatingBar(rating: Binding(
                        get: { currentPhoto.rating },
                        set: { photoRepo.updateRating(photoId: currentPhoto.id, rating: $0) }
                    ))
                }
                .padding(.vertical, 20)
                .padding(.horizontal, 32)
                .background(.ultraThinMaterial)
                .cornerRadius(20, corners: [.topLeft, .topRight])
            }
        }
        .sheet(isPresented: $showShare) {
            if let img = shareImage {
                ShareSheet(items: [img])
            }
        }
        .preferredColorScheme(.dark)
        .statusBarHidden()
    }
}

// MARK: – Zoomable image

struct ZoomableImageView: View {
    let url: URL
    @State private var scale: CGFloat = 1.0
    @State private var offset: CGSize = .zero
    @State private var lastOffset: CGSize = .zero

    private var image: UIImage? { UIImage(contentsOfFile: url.path) }

    var body: some View {
        Group {
            if let img = image {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFit()
                    .scaleEffect(scale)
                    .offset(offset)
                    .gesture(
                        MagnificationGesture()
                            .onChanged { value in scale = max(1, min(5, value)) }
                            .onEnded { _ in
                                withAnimation(.spring) {
                                    if scale < 1.05 { scale = 1; offset = .zero }
                                }
                            }
                    )
                    .gesture(
                        DragGesture()
                            .onChanged { value in
                                if scale > 1 {
                                    offset = CGSize(
                                        width: lastOffset.width + value.translation.width,
                                        height: lastOffset.height + value.translation.height
                                    )
                                }
                            }
                            .onEnded { _ in lastOffset = offset }
                    )
                    .onTapGesture(count: 2) {
                        withAnimation(.spring) {
                            if scale > 1 { scale = 1; offset = .zero; lastOffset = .zero }
                            else { scale = 2.5 }
                        }
                    }
            } else {
                Color.black
                Image(systemName: "photo").foregroundColor(.white.opacity(0.3))
            }
        }
    }
}

// MARK: – Corner radius helper

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners,
                                cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

// MARK: – Share sheet

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
