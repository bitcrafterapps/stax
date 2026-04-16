import SwiftUI
import UIKit
import CoreImage
import CoreImage.CIFilterBuiltins

private struct CropPreset: Identifiable, Equatable {
    let id: String
    let label: String
    let aspectRatio: CGFloat
}

private struct ResizePreset: Identifiable, Equatable {
    let id: String
    let label: String
    let maxDimension: CGFloat?
}

private let cropPresets: [CropPreset] = [
    .init(id: "classic", label: "Classic", aspectRatio: 4.0 / 3.0),
    .init(id: "square", label: "Square", aspectRatio: 1.0),
    .init(id: "portrait", label: "Portrait", aspectRatio: 4.0 / 5.0),
    .init(id: "story", label: "Story", aspectRatio: 9.0 / 16.0)
]

private let resizePresets: [ResizePreset] = [
    .init(id: "original", label: "Original", maxDimension: nil),
    .init(id: "large", label: "Large", maxDimension: 2048),
    .init(id: "medium", label: "Medium", maxDimension: 1600),
    .init(id: "small", label: "Small", maxDimension: 1080)
]

struct PhotoEditorSheet: View {
    let photo: Photo
    let image: UIImage
    let initialCaption: String
    let photoRepo: PhotoRepository

    @Environment(\.dismiss) private var dismiss
    @State private var cropPreset = cropPresets[0]
    @State private var resizePreset = resizePresets[0]
    @State private var zoom: CGFloat = 1
    @State private var offsetX: CGFloat = 0
    @State private var offsetY: CGFloat = 0
    @State private var brightness: Double = 0
    @State private var contrast: Double = 1
    @State private var hue: Double = 0
    @State private var caption: String

    init(photo: Photo, image: UIImage, initialCaption: String, photoRepo: PhotoRepository) {
        self.photo = photo
        self.image = image
        self.initialCaption = initialCaption
        self.photoRepo = photoRepo
        _caption = State(initialValue: initialCaption)
    }

    var body: some View {
        let previewWidth = UIScreen.main.bounds.width - 40
        let previewHeight = previewWidth / cropPreset.aspectRatio

        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 0) {
                    ZStack {
                        Color.black
                        Image(uiImage: image)
                            .resizable()
                            .scaledToFill()
                            .brightness(brightness)
                            .contrast(contrast)
                            .hueRotation(.degrees(hue))
                            .scaleEffect(zoom)
                            .offset(
                                x: offsetX * previewWidth * 0.22,
                                y: offsetY * previewHeight * 0.22
                            )
                    }
                    .frame(width: previewWidth, height: previewHeight)
                    .clipShape(RoundedRectangle(cornerRadius: 22))

                    Spacer().frame(height: 20)

                    Text("Crop").font(.caption).bold().foregroundColor(.staxOnSurfaceVar)
                    Spacer().frame(height: 8)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(cropPresets) { preset in
                                presetButton(title: preset.label, selected: cropPreset == preset) {
                                    cropPreset = preset
                                }
                            }
                        }
                    }

                    Spacer().frame(height: 16)

                    Text("Resize").font(.caption).bold().foregroundColor(.staxOnSurfaceVar)
                    Spacer().frame(height: 8)
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(resizePresets) { preset in
                                presetButton(title: preset.label, selected: resizePreset == preset) {
                                    resizePreset = preset
                                }
                            }
                        }
                    }

                    Spacer().frame(height: 16)
                    labeledSlider("Zoom", value: $zoom, range: 1...3)
                    labeledSlider("Horizontal", value: $offsetX, range: -1...1)
                    labeledSlider("Vertical", value: $offsetY, range: -1...1)
                    labeledSlider("Brightness", value: $brightness, range: -0.4...0.4)
                    labeledSlider("Contrast", value: $contrast, range: 0.5...1.8)
                    labeledSlider("Hue", value: $hue, range: -180...180)

                    Spacer().frame(height: 16)
                    Text("Caption").font(.caption).bold().foregroundColor(.staxOnSurfaceVar)
                    Spacer().frame(height: 8)
                    TextField("Add a caption", text: $caption, axis: .vertical)
                        .textFieldStyle(.plain)
                        .padding(12)
                        .background(Color.staxSurface)
                        .cornerRadius(12)
                        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.white.opacity(0.18), lineWidth: 1))
                        .foregroundColor(.white)

                    Spacer().frame(height: 24)

                    HStack(spacing: 10) {
                        Button("Cancel") { dismiss() }
                            .font(.headline)
                            .foregroundColor(.staxPrimary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 16)
                            .background(Color.staxSurface)
                            .cornerRadius(16)

                        Button("Save") {
                            let edited = renderEditedImage(
                                source: image,
                                aspectRatio: cropPreset.aspectRatio,
                                zoom: zoom,
                                offsetX: offsetX,
                                offsetY: offsetY,
                                maxDimension: resizePreset.maxDimension,
                                brightness: brightness,
                                contrast: contrast,
                                hueDegrees: hue
                            )
                            photoRepo.saveEdits(photoId: photo.id, image: edited, caption: caption)
                            dismiss()
                        }
                        .font(.headline)
                        .foregroundColor(.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(Color.staxPrimary)
                        .cornerRadius(16)
                    }

                    Spacer().frame(height: 28)
                }
                .padding(20)
            }
            .background(Color.staxBackground.ignoresSafeArea())
            .navigationTitle("Edit Photo")
            .navigationBarTitleDisplayMode(.inline)
        }
        .preferredColorScheme(.dark)
    }

    private func presetButton(title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.caption).bold()
                .foregroundColor(selected ? .white : .staxOnSurfaceVar)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(selected ? Color.staxPrimary : Color.staxSurface)
                .cornerRadius(999)
        }
        .buttonStyle(.plain)
    }

    private func labeledSlider(_ title: String, value: Binding<CGFloat>, range: ClosedRange<CGFloat>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.caption).bold().foregroundColor(.staxOnSurfaceVar)
            Slider(value: value, in: range)
                .tint(.staxPrimary)
        }
    }

    private func labeledSlider(_ title: String, value: Binding<Double>, range: ClosedRange<Double>) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.caption).bold().foregroundColor(.staxOnSurfaceVar)
            Slider(value: value, in: range)
                .tint(.staxPrimary)
        }
    }
}

private func renderEditedImage(
    source: UIImage,
    aspectRatio: CGFloat,
    zoom: CGFloat,
    offsetX: CGFloat,
    offsetY: CGFloat,
    maxDimension: CGFloat?,
    brightness: Double,
    contrast: Double,
    hueDegrees: Double
) -> UIImage {
    let size = source.size
    var cropWidth = size.width
    var cropHeight = cropWidth / aspectRatio
    if cropHeight > size.height {
        cropHeight = size.height
        cropWidth = cropHeight * aspectRatio
    }

    let safeZoom = min(max(zoom, 1), 3)
    cropWidth /= safeZoom
    cropHeight /= safeZoom

    let maxHorizontal = (size.width - cropWidth) / 2
    let maxVertical = (size.height - cropHeight) / 2
    let originX = min(max((size.width - cropWidth) / 2 + offsetX * maxHorizontal, 0), size.width - cropWidth)
    let originY = min(max((size.height - cropHeight) / 2 + offsetY * maxVertical, 0), size.height - cropHeight)
    let cropRect = CGRect(x: originX, y: originY, width: cropWidth, height: cropHeight)

    guard let cgImage = source.cgImage?.cropping(to: cropRect.scaled(by: source.scale)) else { return source }
    let cropped = UIImage(cgImage: cgImage, scale: source.scale, orientation: source.imageOrientation)
    let adjusted = applyColorAdjustments(
        to: cropped,
        brightness: brightness,
        contrast: contrast,
        hueDegrees: hueDegrees
    )

    guard let maxDimension else { return adjusted }
    let outputSize: CGSize
    if cropRect.width >= cropRect.height {
        outputSize = CGSize(width: maxDimension, height: maxDimension / aspectRatio)
    } else {
        outputSize = CGSize(width: maxDimension * aspectRatio, height: maxDimension)
    }

    let renderer = UIGraphicsImageRenderer(size: outputSize)
    return renderer.image { _ in
        adjusted.draw(in: CGRect(origin: .zero, size: outputSize))
    }
}

private func applyColorAdjustments(
    to image: UIImage,
    brightness: Double,
    contrast: Double,
    hueDegrees: Double
) -> UIImage {
    guard let inputImage = CIImage(image: image) else { return image }
    let context = CIContext()

    let controls = CIFilter.colorControls()
    controls.inputImage = inputImage
    controls.brightness = Float(brightness)
    controls.contrast = Float(contrast)
    controls.saturation = 1

    let hueFilter = CIFilter.hueAdjust()
    hueFilter.inputImage = controls.outputImage
    hueFilter.angle = Float(hueDegrees * .pi / 180)

    guard let output = hueFilter.outputImage,
          let cgImage = context.createCGImage(output, from: output.extent) else {
        return image
    }

    return UIImage(cgImage: cgImage, scale: image.scale, orientation: image.imageOrientation)
}

private extension CGRect {
    func scaled(by scale: CGFloat) -> CGRect {
        CGRect(x: origin.x * scale, y: origin.y * scale, width: size.width * scale, height: size.height * scale)
    }
}
