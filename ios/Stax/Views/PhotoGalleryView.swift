import SwiftUI
import PhotosUI
import AVFoundation

struct PhotoGalleryView: View {
    @ObservedObject var vm: SessionsViewModel
    let session: Session

    @ObservedObject private var photoRepo = PhotoRepository.shared

    @State private var selectedPhotoItem: [PhotosPickerItem] = []
    @State private var showCamera = false
    @State private var showPhotoSource = false
    @State private var showPhotosPicker = false
    @State private var showingImage: Photo? = nil
    @State private var cameraImage: UIImage? = nil

    private let columns = [GridItem(.flexible(), spacing: 3), GridItem(.flexible(), spacing: 3), GridItem(.flexible(), spacing: 3)]

    private var photos: [Photo] {
        photoRepo.photos(for: session.id)
    }

    var body: some View {
        ZStack {
            Color.staxBackground.ignoresSafeArea()

            Group {
                if photos.isEmpty {
                    VStack {
                        Spacer()
                        VStack(spacing: 12) {
                            Image(systemName: "photo.badge.plus")
                                .font(.system(size: 48))
                                .foregroundColor(.staxOnSurfaceVar)
                            Text("No photos yet. Add one!")
                                .font(.subheadline)
                                .foregroundColor(.staxOnSurfaceVar)
                        }
                        .padding(.bottom, 40)
                        Spacer()
                    }
                } else {
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: 3) {
                            ForEach(photos) { photo in
                                PhotoTile(photo: photo, photoRepo: photoRepo)
                                    .onTapGesture { showingImage = photo }
                            }
                        }
                        .padding(.bottom, 80)
                    }
                }
            }
        }
        .navigationTitle(session.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showPhotoSource = true
                } label: {
                    Image(systemName: "photo.badge.plus")
                }
                .foregroundColor(.staxPrimary)
            }
        }
        .confirmationDialog("Add Photos", isPresented: $showPhotoSource, titleVisibility: .visible) {
            Button("Take Photo") { showCamera = true }
            Button("Apple Photos") { showPhotosPicker = true }
            Button("Cancel", role: .cancel) {}
        }
        .photosPicker(
            isPresented: $showPhotosPicker,
            selection: $selectedPhotoItem,
            maxSelectionCount: 10,
            matching: .images,
            photoLibrary: .shared()
        )
        .onChange(of: selectedPhotoItem) { _, items in
            for item in items {
                item.loadTransferable(type: Data.self) { result in
                    if case .success(let data) = result, let data = data,
                       let img = UIImage(data: data) {
                        DispatchQueue.main.async {
                            photoRepo.savePhoto(sessionId: session.id, image: img)
                        }
                    }
                }
            }
            selectedPhotoItem = []
        }
        .fullScreenCover(isPresented: $showCamera) {
            CameraPickerView { img in
                photoRepo.savePhoto(sessionId: session.id, image: img)
                showCamera = false
            }
        }
        .fullScreenCover(item: $showingImage) { photo in
            FullScreenImageView(
                photos: photos,
                initialPhoto: photo,
                photoRepo: photoRepo
            )
        }
    }
}

// MARK: – Photo tile

struct PhotoTile: View {
    let photo: Photo
    let photoRepo: PhotoRepository

    @State private var showDeleteConfirm = false

    private var image: UIImage? {
        UIImage(contentsOfFile: photoRepo.fullPath(for: photo))
    }

    var body: some View {
        ZStack(alignment: .bottomLeading) {
            if let img = image {
                Image(uiImage: img)
                    .resizable()
                    .scaledToFill()
                    .clipped()
            } else {
                Color.staxSurface
                Image(systemName: "photo")
                    .foregroundColor(.staxOnSurfaceVar)
            }

            if photo.rating > 0 {
                LinearGradient(colors: [.clear, .black.opacity(0.65)], startPoint: .center, endPoint: .bottom)
                HStack(spacing: 2) {
                    ForEach(1...photo.rating, id: \.self) { _ in
                        Image(systemName: "star.fill").font(.system(size: 9)).foregroundColor(.yellow)
                    }
                }
                .padding(4)
            }
        }
        .aspectRatio(1, contentMode: .fit)
        .contextMenu {
            Button(role: .destructive) {
                showDeleteConfirm = true
            } label: {
                Label("Delete Photo", systemImage: "trash")
            }
        }
        .alert("Delete Photo?", isPresented: $showDeleteConfirm) {
            Button("Delete", role: .destructive) { photoRepo.deletePhoto(id: photo.id) }
            Button("Cancel", role: .cancel) {}
        }
    }
}

// MARK: – Camera picker (UIImagePickerController wrapper)

struct CameraPickerView: UIViewControllerRepresentable {
    let onCapture: (UIImage) -> Void

    func makeCoordinator() -> Coordinator { Coordinator(onCapture: onCapture) }

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.sourceType = .camera
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}

    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let onCapture: (UIImage) -> Void
        init(onCapture: @escaping (UIImage) -> Void) { self.onCapture = onCapture }

        func imagePickerController(_ picker: UIImagePickerController,
                                   didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let img = info[.originalImage] as? UIImage {
                onCapture(img)
            }
            picker.dismiss(animated: true)
        }
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            picker.dismiss(animated: true)
        }
    }
}
