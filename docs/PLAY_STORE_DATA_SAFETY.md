# Play Store Data Safety Notes

Last updated: April 20, 2026

This file is a release checklist for answering the Google Play Data safety form based on the current Android implementation in `android/app`.

## Features Covered

- Camera-based chip scans
- Optional location-based nearby casino search
- Local session and photo storage
- Optional OpenAI-powered cloud chip estimation using a user-provided API key

## Likely Data Types In Scope

Review against the current Play Console questionnaire before submitting.

### Photos and videos

- Source: camera capture and user-selected photos
- Purpose: app functionality
- Handling: stored locally on device; a current scan image can be sent to OpenAI only when the user enables cloud estimation and starts a scan

### Location

- Source: device location permission
- Purpose: app functionality
- Handling: used to find nearby card rooms and prefill state-related flows; not used for advertising

### App info and performance

- Source: standard Android and Play telemetry
- Purpose: may be collected by Google Play services outside app-specific code paths
- Handling: review the final Play Console prompts at submission time

### User credentials or identifiers

- Source: user-supplied OpenAI API key
- Purpose: app functionality
- Handling: stored locally in app preferences and sent only to OpenAI when the user enables cloud scans

## Suggested Play Console Positioning

Use this as a draft, then verify each answer in the final console flow.

1. Data collected:
   - Photos: yes, when the user triggers cloud chip estimation
   - Location: yes, when the user grants location access and uses nearby-search features
   - API key / app credentials: yes, user-provided key stored on-device and transmitted to OpenAI for the requested feature

2. Is data shared:
   - Photos: shared with OpenAI only for the optional cloud scan workflow
   - Location: not shared with Stax servers in the current Android implementation
   - API key: shared with OpenAI only for the optional cloud scan workflow

3. Is data processed ephemerally:
   - Photos sent to OpenAI are feature-driven request payloads, not permanent Stax server uploads
   - Location is used in-app for feature behavior and not stored as a location history by current code paths

4. Is data required:
   - Camera: optional for scan features
   - Location: optional for nearby search and state assistance
   - OpenAI API key: optional and only needed for cloud chip estimation

5. Security disclosure:
   - State that user-created data is primarily stored locally on-device
   - State that the OpenAI API key is excluded from Android backup/device transfer rules

## Release Review Sources

- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/java/com/bitcraftapps/stax/data/OpenAiService.kt`
- `android/app/src/main/java/com/bitcraftapps/stax/ui/screens/AboutScreen.kt`
- `android/app/src/main/java/com/bitcraftapps/stax/ui/screens/ScanScreen.kt`
- `android/app/src/main/java/com/bitcraftapps/stax/ui/screens/FindScreen.kt`
- `android/app/src/main/java/com/bitcraftapps/stax/ui/screens/SessionsScreen.kt`
- `android/app/src/main/res/xml/backup_rules.xml`
- `android/app/src/main/res/xml/data_extraction_rules.xml`
