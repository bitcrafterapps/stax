import SwiftUI
import UIKit

struct FullScreenImageView: View {
    let initialPhoto: Photo
    @ObservedObject var photoRepo: PhotoRepository
    @EnvironmentObject private var entitlementManager: EntitlementManager

    @Environment(\.dismiss) private var dismiss
    @State private var currentIndex: Int
    @State private var showShare = false
    @State private var shareImage: UIImage? = nil
    @State private var showEditor = false

    init(photos: [Photo], initialPhoto: Photo, photoRepo: PhotoRepository) {
        self.initialPhoto = initialPhoto
        _photoRepo = ObservedObject(wrappedValue: photoRepo)
        _currentIndex = State(initialValue: photos.firstIndex(where: { $0.id == initialPhoto.id }) ?? 0)
    }

    private var livePhotos: [Photo] {
        photoRepo.photos(for: initialPhoto.sessionId)
    }

    private var currentPhoto: Photo? {
        guard !livePhotos.isEmpty else { return nil }
        let safeIndex = min(max(currentIndex, 0), livePhotos.count - 1)
        return livePhotos[safeIndex]
    }

    var body: some View {
        Group {
            if let currentPhoto {
                ZStack {
                    Color.black.ignoresSafeArea()

                    // Swipeable pager
                    TabView(selection: $currentIndex) {
                        ForEach(Array(livePhotos.enumerated()), id: \.element.id) { idx, photo in
                            ZoomableImageView(
                                url: photoRepo.fullURL(for: photo),
                                refreshVersion: photoRepo.editVersion(for: photo.id)
                            )
                            .id("\(photo.id.uuidString)-\(photoRepo.editVersion(for: photo.id))")
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
                            Text("\(min(currentIndex + 1, max(livePhotos.count, 1))) / \(livePhotos.count)")
                                .font(.subheadline)
                                .foregroundColor(.white.opacity(0.85))
                            Spacer()
                            Button { showEditor = true } label: {
                                Image(systemName: "slider.horizontal.3")
                                    .font(.title3)
                                    .foregroundColor(.white)
                            }
                            Button {
                                if let img = UIImage(contentsOfFile: photoRepo.fullPath(for: currentPhoto)) {
                                    shareImage = entitlementManager.isPremium ? img : img.addingWatermark()
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
                            if !photoRepo.caption(for: currentPhoto.id).isEmpty {
                                Text(photoRepo.caption(for: currentPhoto.id))
                                    .font(.subheadline)
                                    .foregroundColor(.white.opacity(0.9))
                                    .multilineTextAlignment(.center)
                            }
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
            } else {
                Color.black
                    .ignoresSafeArea()
                    .onAppear { dismiss() }
            }
        }
        .sheet(isPresented: $showShare) {
            if let img = shareImage {
                ShareSheet(items: [img])
            }
        }
        .sheet(isPresented: $showEditor) {
            if let currentPhoto,
               let image = UIImage(contentsOfFile: photoRepo.fullPath(for: currentPhoto)) {
                PhotoEditorSheet(
                    photo: currentPhoto,
                    image: image,
                    initialCaption: photoRepo.caption(for: currentPhoto.id),
                    photoRepo: photoRepo
                )
            }
        }
        .preferredColorScheme(.dark)
        .statusBarHidden()
        .onChange(of: livePhotos.count) { _, newCount in
            if newCount > 0 {
                currentIndex = min(currentIndex, newCount - 1)
            }
        }
    }
}

// MARK: – Zoomable image

struct ZoomableImageView: View {
    let url: URL
    let refreshVersion: Int
    @State private var scale: CGFloat = 1.0
    @State private var offset: CGSize = .zero
    @State private var lastOffset: CGSize = .zero

    private var image: UIImage? {
        _ = refreshVersion
        return UIImage(contentsOfFile: url.path)
    }

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
                    // simultaneousGesture lets TabView's page-swipe gesture also fire
                    // so at scale == 1 the pager handles horizontal swipes normally
                    .simultaneousGesture(
                        DragGesture()
                            .onChanged { value in
                                guard scale > 1 else { return }
                                offset = CGSize(
                                    width: lastOffset.width + value.translation.width,
                                    height: lastOffset.height + value.translation.height
                                )
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

// MARK: – Watermark helper

extension UIImage {
    func addingWatermark() -> UIImage {
        let text = "Tracked with STAX"
        let padding: CGFloat = 12
        let font = UIFont.boldSystemFont(ofSize: max(size.width * 0.04, 14))
        let attrs: [NSAttributedString.Key: Any] = [
            .font: font,
            .foregroundColor: UIColor.white.withAlphaComponent(0.85)
        ]
        let textSize = (text as NSString).size(withAttributes: attrs)

        let renderer = UIGraphicsImageRenderer(size: size)
        return renderer.image { ctx in
            draw(in: CGRect(origin: .zero, size: size))

            let bgRect = CGRect(
                x: size.width - textSize.width - padding * 2 - 4,
                y: size.height - textSize.height - padding * 2 - 4,
                width: textSize.width + padding * 2,
                height: textSize.height + padding
            )
            UIColor.black.withAlphaComponent(0.45).setFill()
            UIBezierPath(roundedRect: bgRect, cornerRadius: 6).fill()

            let textRect = CGRect(
                x: bgRect.origin.x + padding,
                y: bgRect.origin.y + padding / 2,
                width: textSize.width,
                height: textSize.height
            )
            (text as NSString).draw(in: textRect, withAttributes: attrs)
        }
    }
}
