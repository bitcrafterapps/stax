import SwiftUI
import AVFoundation

struct ScanView: View {
    @EnvironmentObject private var entitlementManager: EntitlementManager
    @Environment(\.showPaywall) private var showPaywall
    @State private var cameraPermission: AVAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .video)

    var body: some View {
        ZStack {
            Color.staxBackground.ignoresSafeArea()

            if cameraPermission == .authorized {
                ScanCameraView(
                    entitlementManager: entitlementManager,
                    onShowPaywall: { showPaywall() }
                )
            } else if cameraPermission == .notDetermined {
                VStack(spacing: 20) {
                    Image(systemName: "camera.viewfinder")
                        .font(.system(size: 56))
                        .foregroundColor(.staxOnSurfaceVar)
                    Text("Camera Access")
                        .font(.title3).bold()
                        .foregroundColor(.white)
                    Text("Allow camera access to scan chip stacks and count their value.")
                        .font(.subheadline)
                        .foregroundColor(.staxOnSurfaceVar)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Button("Continue") {
                        AVCaptureDevice.requestAccess(for: .video) { granted in
                            DispatchQueue.main.async {
                                cameraPermission = granted ? .authorized : .denied
                            }
                        }
                    }
                    .font(.headline)
                    .foregroundColor(.white)
                    .padding(.horizontal, 40)
                    .padding(.vertical, 14)
                    .background(Color.staxPrimary)
                    .cornerRadius(14)
                }
            } else {
                VStack(spacing: 16) {
                    Image(systemName: "camera.slash.fill")
                        .font(.system(size: 56))
                        .foregroundColor(.staxOnSurfaceVar)
                    Text("Camera Access Denied")
                        .font(.title3).bold()
                        .foregroundColor(.white)
                    Text("Please enable camera access in Settings to use the scan feature.")
                        .font(.subheadline)
                        .foregroundColor(.staxOnSurfaceVar)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                    Button("Open Settings") {
                        if let url = URL(string: UIApplication.openSettingsURLString) {
                            UIApplication.shared.open(url)
                        }
                    }
                    .foregroundColor(.staxPrimary)
                }
            }
        }
    }
}

// MARK: – Camera view with OpenAI scanning

struct ScanCameraView: View {
    let entitlementManager: EntitlementManager
    let onShowPaywall: () -> Void

    @State private var sessionType = "Cash"
    @State private var openAiEnabled = UserDefaults.standard.bool(forKey: "openai_enabled")
    @State private var isScanning = false
    @State private var scanResult: String? = nil
    @State private var infoMessage: String? = nil
    @State private var capturedImage: UIImage? = nil
    @State private var showTrainDialog = false
    @State private var trainLabel = ""
    @State private var showScanLimitAlert = false

    private let chipRepo = ChipConfigRepository()
    private var chipHints: String {
        let configs = chipRepo.load(casino: "Default", gameType: sessionType)
        let isCash = sessionType == "Cash"
        return configs.map { chip in
            let display = isCash ? "$\(chip.value)" : chip.value
            return "  \(chip.colorName.capitalized) chips = \(display) each"
        }.joined(separator: "\n")
    }
    private var hasResult: Bool { scanResult != nil }

    var body: some View {
        ZStack(alignment: .bottom) {
            // Camera preview
            CameraPreviewView(onCapture: { img in
                capturedImage = img
            })
            .ignoresSafeArea()
            .onReceive(NotificationCenter.default.publisher(for: .staxScanResult)) { note in
                if let result = note.object as? String {
                    scanResult = result
                    isScanning = false
                }
            }

            // Scanning overlay
            if isScanning {
                Color.black.opacity(0.45).ignoresSafeArea()
                ProgressView()
                    .progressViewStyle(.circular)
                    .tint(.staxPrimary)
                    .scaleEffect(1.5)
            }

            // Bottom panel
            VStack(spacing: 0) {
                Spacer()
                VStack(spacing: 14) {
                    // Scan limit banner for free users at limit
                    if !entitlementManager.isPremium && entitlementManager.getDailyScans() >= FreeTierLimits.maxScansPerDay {
                        UpgradeBanner(
                            message: "You've used all \(FreeTierLimits.maxScansPerDay) free scans today. Upgrade for unlimited.",
                            onUpgrade: { onShowPaywall() }
                        )
                    }

                    // Cash / Tourney toggle
                    HStack(spacing: 8) {
                        FilterChipRow(options: ["Cash", "Tourney"], selected: Binding(
                            get: { sessionType },
                            set: { sessionType = $0; scanResult = nil }
                        ))
                    }

                    // Chip total display
                    ChipTotalView(result: scanResult, openAiEnabled: openAiEnabled, sessionType: sessionType)

                    // Info message
                    if let msg = infoMessage {
                        Text(msg)
                            .font(.subheadline)
                            .foregroundColor(.white)
                            .padding(.horizontal, 14)
                            .padding(.vertical, 10)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.staxSurface)
                            .cornerRadius(14)
                    }

                    // OpenAI toggle
                    HStack {
                        Text("Cloud estimate (OpenAI)")
                            .font(.subheadline)
                            .foregroundColor(entitlementManager.isPremium ? .white : .white.opacity(0.45))
                        Spacer()
                        if entitlementManager.isPremium {
                            Toggle("", isOn: Binding(
                                get: { openAiEnabled },
                                set: {
                                    openAiEnabled = $0
                                    UserDefaults.standard.set($0, forKey: "openai_enabled")
                                }
                            ))
                            .tint(.staxPrimary)
                            .labelsHidden()
                        } else {
                            HStack(spacing: 4) {
                                Image(systemName: "lock.fill")
                                    .font(.caption)
                                    .foregroundColor(.staxOnSurfaceVar)
                                Text("Premium")
                                    .font(.caption)
                                    .foregroundColor(.staxOnSurfaceVar)
                            }
                        }
                    }

                    // Scan / Rescan button
                    HStack(spacing: 12) {
                        Button {
                            if hasResult {
                                scanResult = nil
                                infoMessage = nil
                                capturedImage = nil
                            } else {
                                guardedScan()
                            }
                        } label: {
                            Text(hasResult ? "Rescan" : "Scan")
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal, 40)
                                .padding(.vertical, 14)
                                .background(isScanning ? Color.staxPrimary.opacity(0.4) : Color.staxPrimary)
                                .cornerRadius(14)
                        }
                        .disabled(isScanning)

                        if !openAiEnabled {
                            Button("Train") { showTrainDialog = true }
                                .font(.headline)
                                .foregroundColor(.white)
                                .padding(.horizontal, 28)
                                .padding(.vertical, 14)
                                .background(Color.staxSurface)
                                .cornerRadius(14)
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.white.opacity(0.2), lineWidth: 1))
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.top, 18)
                .padding(.bottom, 28)
                .background(
                    Color(red: 0.13, green: 0.10, blue: 0.18).opacity(0.92)
                        .clipShape(RoundedRectangle(cornerRadius: 24, style: .continuous))
                )
            }
        }
        .alert("Daily Limit Reached", isPresented: $showScanLimitAlert) {
            Button("Upgrade") { onShowPaywall() }
            Button("Maybe Later", role: .cancel) {}
        } message: {
            Text("You've used all \(FreeTierLimits.maxScansPerDay) free scans for today. Upgrade to Premium for unlimited scanning.")
        }
        .alert("Training Label", isPresented: $showTrainDialog) {
            TextField("Chip value", text: $trainLabel)
                .keyboardType(.numberPad)
            Button("Save") {
                // Training: save current captured frame to Documents
                if let img = capturedImage {
                    saveTrainingImage(img, label: trainLabel)
                    infoMessage = "Saved training image for \(trainLabel)"
                    DispatchQueue.main.asyncAfter(deadline: .now() + 3) { infoMessage = nil }
                }
                trainLabel = ""
            }
            Button("Cancel", role: .cancel) { trainLabel = "" }
        } message: {
            Text("Enter the chip value for this training image.")
        }
    }

    private func guardedScan() {
        let result = entitlementManager.checkLimit(for: .scan)
        switch result {
        case .blocked:
            showScanLimitAlert = true
        case .allowed, .softCap:
            entitlementManager.recordScan()
            performScan()
        }
    }

    private func performScan() {
        guard openAiEnabled else { return }
        let apiKey = KeychainHelper.load(forKey: KeychainHelper.Key.openAIApiKey) ?? ""
        guard !apiKey.isEmpty else {
            infoMessage = "Add an API key in About → OpenAI settings."
            DispatchQueue.main.asyncAfter(deadline: .now() + 3) { infoMessage = nil }
            return
        }

        // Store current session type so the camera coordinator can use it
        UserDefaults.standard.set(sessionType, forKey: "scan_session_type")
        isScanning = true
        NotificationCenter.default.post(name: .staxTakePhoto, object: nil)
    }

    private func saveTrainingImage(_ image: UIImage, label: String) {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("training_data/\(label)")
        try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        let file = dir.appendingPathComponent("chip_\(Int(Date().timeIntervalSince1970)).jpg")
        try? image.jpegData(compressionQuality: 1.0)?.write(to: file)
    }
}

extension Notification.Name {
    static let staxTakePhoto = Notification.Name("staxTakePhoto")
}

// MARK: – Chip total display

struct ChipTotalView: View {
    let result: String?
    let openAiEnabled: Bool
    let sessionType: String

    var body: some View {
        VStack(spacing: 4) {
            Text("Total: \(result ?? "…")")
                .font(.title3).bold()
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
            Text("Source · \(openAiEnabled ? "OpenAI" : "On-device")")
                .font(.caption)
                .foregroundColor(.staxOnSurfaceVar)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .padding(.horizontal, 16)
        .background(Color.staxSurface)
        .cornerRadius(16)
    }
}

// MARK: – AVFoundation camera preview

struct CameraPreviewView: UIViewRepresentable {
    let onCapture: (UIImage) -> Void

    func makeCoordinator() -> Coordinator { Coordinator(onCapture: onCapture) }

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        context.coordinator.setup(in: view)
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {}

    class Coordinator: NSObject, AVCapturePhotoCaptureDelegate {
        let onCapture: (UIImage) -> Void
        private var session: AVCaptureSession?
        private var photoOutput: AVCapturePhotoOutput?
        private var pendingCompletion: ((UIImage) -> Void)?

        init(onCapture: @escaping (UIImage) -> Void) {
            self.onCapture = onCapture
        }

        func setup(in view: UIView) {
            let session = AVCaptureSession()
            session.sessionPreset = .photo
            self.session = session

            guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back),
                  let input = try? AVCaptureDeviceInput(device: device) else { return }

            session.addInput(input)

            let output = AVCapturePhotoOutput()
            photoOutput = output
            session.addOutput(output)

            let preview = AVCaptureVideoPreviewLayer(session: session)
            preview.videoGravity = .resizeAspectFill
            preview.frame = view.bounds
            view.layer.addSublayer(preview)

            DispatchQueue.global(qos: .userInitiated).async { session.startRunning() }

            // Observe capture trigger
            NotificationCenter.default.addObserver(
                self,
                selector: #selector(takePicture),
                name: .staxTakePhoto,
                object: nil
            )
        }

        @objc func takePicture() {
            let settings = AVCapturePhotoSettings()
            photoOutput?.capturePhoto(with: settings, delegate: self)
        }

        func photoOutput(_ output: AVCapturePhotoOutput,
                         didFinishProcessingPhoto photo: AVCapturePhoto,
                         error: Error?) {
            guard let data = photo.fileDataRepresentation(),
                  let img = UIImage(data: data) else { return }

            let apiKey = KeychainHelper.load(forKey: KeychainHelper.Key.openAIApiKey) ?? ""
            let sessionType = UserDefaults.standard.string(forKey: "scan_session_type") ?? "Cash"
            let hints = ChipConfigRepository().load(casino: "Default", gameType: sessionType)
                .map { chip -> String in
                    let display = sessionType == "Cash" ? "$\(chip.value)" : chip.value
                    return "  \(chip.colorName.capitalized) chips = \(display) each"
                }.joined(separator: "\n")

            DispatchQueue.main.async { self.onCapture(img) }

            Task {
                do {
                    let service = OpenAIService(apiKey: apiKey)
                    let result = try await service.getChipCount(image: img, chipHints: hints)
                    await MainActor.run {
                        NotificationCenter.default.post(name: .staxScanResult, object: result)
                    }
                } catch {
                    await MainActor.run {
                        NotificationCenter.default.post(
                            name: .staxScanResult,
                            object: "Error: \(error.localizedDescription)"
                        )
                    }
                }
            }
        }
    }
}

extension Notification.Name {
    static let staxScanResult = Notification.Name("staxScanResult")
}
