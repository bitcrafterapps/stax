# Android Release Guide

Last updated: April 20, 2026

This guide covers the Android release steps for `com.bitcraftapps.stax`.

## App Identity

- Play package name: `com.bitcraftapps.stax`
- Android namespace: `com.bitcraftapps.stax`
- App name: `Stax`

## Release Signing

The Android app now supports release signing from either:

- `android/keystore.properties`
- Gradle properties
- Environment variables

Supported keys:

- `STAX_UPLOAD_STORE_FILE`
- `STAX_UPLOAD_STORE_PASSWORD`
- `STAX_UPLOAD_KEY_ALIAS`
- `STAX_UPLOAD_KEY_PASSWORD`

An example file is provided at `android/keystore.properties.example`.

## Recommended Local Setup

1. Copy `android/keystore.properties.example` to `android/keystore.properties`.
2. Generate or place your upload keystore at the path referenced by `STAX_UPLOAD_STORE_FILE`.
3. Fill in the passwords and key alias values.
4. Keep `android/keystore.properties` and the keystore file out of git.

## Build Commands

From `android/`:

```bash
./gradlew bundleRelease
```

Output bundle:

```text
app/build/outputs/bundle/release/app-release.aab
```

## Pre-Upload Checklist

- Confirm `applicationId` is still `com.bitcraftapps.stax`
- Confirm `versionCode` and `versionName` are correct for this release
- Test a release build on a real device
- Verify camera permission flow
- Verify location permission flow
- Verify local photo/session storage
- Verify OpenAI cloud estimation with a valid user-supplied API key
- Verify the app behaves correctly with cloud estimation disabled

## Play Console Materials

- App name
- Short description
- Full description
- Contact email
- Privacy policy URL
- Phone screenshots
- Feature graphic
- Content rating answers
- Data safety answers

## Internal Testing Before Production

1. Upload the signed `.aab` to Internal testing.
2. Install from the Play test track.
3. Review Play pre-launch report warnings.
4. Fix crashes, ANRs, policy issues, or compatibility warnings before production rollout.
