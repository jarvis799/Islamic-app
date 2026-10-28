# Noor Islamic — Android source project

Native Android app starter built with Kotlin, Jetpack Compose, Material 3, and AndroidX Media3.

## Build with GitHub Actions

1. Create a GitHub repository and upload the contents of this project.
2. Push to `main` or `master`, or open **Actions → Android CI - Build Noor Islamic → Run workflow**.
3. Open the completed workflow run and download the `noor-islamic-debug-apk` artifact.

The workflow installs JDK 17, Gradle 8.9, Android SDK platform 35/build tools 35.0.0, runs Gradle validation, and builds `assembleDebug`.

## Build locally

Requirements: JDK 17, Android SDK platform 35, Android build-tools 35.0.0, Gradle 8.9.

From this directory:

```bash
gradle help
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Current feature scope

- Premium white/lime Compose dashboard
- Quran/reciter list and API client (API response schema should be verified against the live provider docs)
- Media3 background playback service
- Basic per-ayah download and local-file playback flow
- AlAdhan prayer-time request
- Tasbih and sample Dua/Azkar screens

## Known limitations — not yet production-ready

- Qibla screen is a placeholder; live location/orientation sensor calculation is not implemented.
- Hijri calendar screen currently displays the Gregorian date; Hijri API integration is pending.
- Quran reader does not yet render verse-by-verse Arabic text/translations.
- User preferences/bookmarks/download state are not persisted in Room/DataStore yet.
- The current downloader is a simple sequential downloader, not Media3's managed download service; interruption/resume and storage controls need further work.
- Verify the current Al Furqan API endpoints and JSON response schema before relying on them. Provider availability/terms may change.
- No APK build has been confirmed in this environment; use the included workflow to compile and inspect the build log.

## API references

- Al Furqan: https://alfurqan.online/docs
- AlAdhan: https://aladhan.com/prayer-times-api
- Android Media3 background playback: https://developer.android.com/media/media3/session/background-playback

Do not download or extract YouTube audio. Download audio only from providers whose terms permit it.
