<p align="center">
  <img src="frontend/public/stax-logo.png" alt="Stax logo" width="180" />
</p>

<h1 align="center">Stax</h1>

<p align="center">
  <em>Stack it. Snap it. Track it.</em>
</p>

<p align="center">
  Capture, organize, and browse photos of your poker chip stacks across every session you play.
</p>

---

## What is Stax?

Stax is a cross-platform app for poker players to track sessions and chronicle their chip stacks. Create a session for each game you play (cash or tourney, casino or home game), snap photos of your stacks throughout the night, rate them, and browse them later in a clean gallery view.

Built with native Android (Jetpack Compose) and iOS (SwiftUI), backed by a Next.js marketing site.

## Repository layout

| Folder | What's in it |
|---|---|
| `android/` | Native Android app (Kotlin + Jetpack Compose, Room DB) |
| `ios/` | Native iOS app (SwiftUI, generated via XcodeGen) |
| `frontend/` | Marketing & privacy-policy website (Next.js + Tailwind) |
| `frontend-example/` | Reference Next.js dashboard scaffold |
| `docs/` | PRD, privacy policy, release notes, Play Store data-safety, etc. |
| `scripts/` | Data-generation utilities (e.g. `generate_cardrooms.py`) |

## Getting started

### Android

```bash
cd android
./gradlew assembleDebug
```

Open the `android/` folder in Android Studio Hedgehog or newer to run on a device or emulator. The app namespace is `com.bitcraftapps.stax`.

For release builds, copy `android/keystore.properties.example` to `android/keystore.properties` and fill in your signing credentials. See [`docs/ANDROID_RELEASE.md`](docs/ANDROID_RELEASE.md).

### iOS

```bash
brew install xcodegen
cd ios
xcodegen generate
open Stax.xcodeproj
```

Requires Xcode 15+ and iOS 17 deployment target. Full instructions in [`ios/README.md`](ios/README.md).

### Frontend (website)

```bash
cd frontend
npm install
npm run dev
```

Then open [http://localhost:3000](http://localhost:3000).

## Features

- **Session management** — group photos by casino, game type, and stakes
- **Photo gallery** — browse every stack from every session
- **Full-screen viewer** — swipe through photos, rate them 1–5 stars
- **Card-room directory** — find nearby card rooms, sourced from a curated dataset
- **Optional AI features** — chip-stack detection via OpenAI (user-supplied API key, stored only on-device)

See [`docs/PRD.md`](docs/PRD.md) for the full product spec.

## Configuration

Stax does not ship with any embedded API keys. AI features (chip detection) require the user to enter their own OpenAI API key in **Settings**, which is stored locally on the device only. Nothing is uploaded to any Stax-owned server.

## Privacy

We don't collect personal data. See the full [Privacy Policy](docs/PRIVACY_POLICY.md) and the [Play Store data-safety form](docs/PLAY_STORE_DATA_SAFETY.md).

## Built by

[Bitcraft Apps](https://github.com/bitcrafterapps)
