# Stax iOS

iOS companion to the Stax Android app — track your poker chip stacks, sessions, and find nearby card rooms.

## Requirements

- Xcode 15+
- iOS 17 deployment target
- [XcodeGen](https://github.com/yonaskolb/XcodeGen) to generate the Xcode project

## Setup

### 1. Install XcodeGen (first time only)

```bash
brew install xcodegen
```

### 2. Generate the Xcode project

```bash
cd ios
xcodegen generate
```

This creates `Stax.xcodeproj` in the `ios/` folder.

### 3. Open in Xcode

```bash
open Stax.xcodeproj
```

### 4. Set your Team

In Xcode → select the **Stax** target → **Signing & Capabilities** → set your Apple Development Team.

### 5. Add App Logo (optional)

Drop your `ic_stax_logo.png` (the infinity-glasses logo) into `Stax/Assets.xcassets/` as an image set named **`StaxLogo`**. The app shows a purple "∞" placeholder if the asset is absent.

## Features

- **Photos / Dashboard**: Browse sessions by casino. Create sessions with state, casino, game type, stakes.
- **Sessions**: All sessions with buy-in/cash-out tracking, profit/loss summary, filter by Cash/Tourney.
- **Scan**: Camera-based chip stack scanning powered by OpenAI Vision API.
- **Find**: Discover nearby card rooms via CoreLocation. Search by proximity or state. Favorites & home casino.
- **Chip Config**: Configure chip values and colors per casino for Cash and Tourney games.
- **About**: App info, OpenAI API key management.

## Architecture

- **SwiftUI** throughout
- **UserDefaults + JSON** for session/photo metadata and chip configuration (mirrors Android SharedPreferences)
- **FileManager** for photo file storage
- **PhotosUI** (PHPickerViewController) for Apple Photos library access
- **AVFoundation** for camera
- **CoreLocation** for geolocation
- **URLSession** for OpenAI API

## Notes

- Apple Photos replaces Google Photos (same UX, native iOS picker)
- Tagline: "Stack it. Snap it. Track it." (matches Android)
- No on-device TensorFlow model — cloud scanning via OpenAI Vision only
- Add your OpenAI API key in the **About** tab
