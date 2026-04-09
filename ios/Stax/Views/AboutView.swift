import SwiftUI

struct AboutView: View {
    @State private var apiKey: String = UserDefaults.standard.string(forKey: "openai_api_key") ?? ""
    @State private var showApiKey = false
    @State private var showChipConfig = false

    var body: some View {
        NavigationStack {
            ZStack {
                LinearGradient(
                    colors: [Color(red: 0.10, green: 0.07, blue: 0.18), Color.black],
                    startPoint: .top, endPoint: .bottom
                )
                .ignoresSafeArea()

                ScrollView {
                    VStack(spacing: 32) {

                        // Logo + tagline
                        VStack(spacing: 16) {
                            StaxLogoImage(size: 120)
                            VStack(spacing: 6) {
                                Text("STAX")
                                    .font(.system(size: 36, weight: .black, design: .rounded))
                                    .foregroundColor(.white)
                                    .tracking(6)
                                Text("Poker Porn, No Shame.")
                                    .font(.subheadline)
                                    .foregroundColor(.white.opacity(0.55))
                                    .tracking(1)
                                Text("v1.0.0")
                                    .font(.caption)
                                    .foregroundColor(.white.opacity(0.35))
                            }
                        }
                        .padding(.top, 32)

                        // Settings cards
                        VStack(spacing: 12) {

                            // OpenAI API Key
                            VStack(alignment: .leading, spacing: 12) {
                                HStack {
                                    Image(systemName: "key.horizontal.fill")
                                        .foregroundColor(.staxPrimary)
                                    Text("OpenAI API Key")
                                        .font(.subheadline).bold().foregroundColor(.white)
                                    Spacer()
                                    Button {
                                        showApiKey.toggle()
                                    } label: {
                                        Image(systemName: showApiKey ? "eye.slash" : "eye")
                                            .foregroundColor(.staxOnSurfaceVar)
                                    }
                                }

                                Group {
                                    if showApiKey {
                                        TextField("sk-…", text: $apiKey)
                                    } else {
                                        SecureField("sk-…", text: $apiKey)
                                    }
                                }
                                .textFieldStyle(.plain)
                                .padding(12)
                                .background(Color.staxBackground)
                                .cornerRadius(10)
                                .foregroundColor(.white)
                                .autocorrectionDisabled()
                                .textInputAutocapitalization(.never)

                                Button {
                                    UserDefaults.standard.set(apiKey, forKey: "openai_api_key")
                                    UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder),
                                                                    to: nil, from: nil, for: nil)
                                } label: {
                                    Text("Save Key")
                                        .font(.subheadline).bold()
                                        .foregroundColor(.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 12)
                                        .background(Color.staxPrimary)
                                        .cornerRadius(12)
                                }

                                Text("Used for the AI chip-counting feature on the Scan tab. Your key is stored locally and never sent anywhere except directly to OpenAI.")
                                    .font(.caption)
                                    .foregroundColor(.staxOnSurfaceVar)
                            }
                            .padding(16)
                            .background(Color.staxSurface)
                            .cornerRadius(18)

                            // Chip configuration
                            Button { showChipConfig = true } label: {
                                HStack {
                                    Image(systemName: "circle.hexagongrid.fill")
                                        .foregroundColor(.staxPrimary)
                                        .font(.title3)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Chip Configuration")
                                            .font(.subheadline).bold().foregroundColor(.white)
                                        Text("Customize chip values and colors")
                                            .font(.caption).foregroundColor(.staxOnSurfaceVar)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundColor(.staxOnSurfaceVar)
                                }
                                .padding(16)
                                .background(Color.staxSurface)
                                .cornerRadius(18)
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.horizontal, 16)

                        // Footer
                        VStack(spacing: 4) {
                            Text("Built for poker players who want to track their stacks.")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.35))
                                .multilineTextAlignment(.center)
                        }
                        .padding(.bottom, 32)
                    }
                }
            }
            .navigationTitle("")
            .navigationBarHidden(true)
        }
        .sheet(isPresented: $showChipConfig) {
            ChipConfigView()
        }
    }
}
