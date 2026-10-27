# Noor Islamic — Native Android Source

Premium lime/white Islamic companion starter project built with Kotlin, Jetpack Compose, AndroidX Media3, OkHttp, and public APIs.

## Features included
- Premium Compose dashboard and navigation
- Quran Surah list and reciter list from Al Furqan API
- Per-ayah Quran audio URL queue with Media3 background playback service
- Surah audio download into app-managed storage and offline play action
- Prayer times via AlAdhan API (defaults to Dhaka, Bangladesh)
- Basic Dua/Azkar and Tasbih screens
- Islamic tools and calendar UI placeholders

## Build
1. Open this folder in Android Studio (Hedgehog or newer recommended).
2. Use JDK 17.
3. Let Gradle sync. Android SDK Platform 35 is required by this project.
4. Run on an Android device/emulator.

## Important implementation notes
- The Qibla screen is currently a clearly labelled UI placeholder; live bearing calculation, location permission flow and compass sensor calibration must be implemented before production release.
- Hijri calendar UI is a placeholder; connect a documented calendar endpoint and validate local moon-sighting rules before release.
- Quran text/translation reader, notification scheduling, full settings persistence, robust resumable downloads and tests need further production implementation.
- Audio endpoints are based on the published Al Furqan docs: https://alfurqan.online/docs. Verify current terms, endpoint behavior, attribution, and offline-download permission before publishing.
- Prayer times use AlAdhan: https://aladhan.com/prayer-times-api. Confirm the calculation method with local authority; API timings may differ.
- YouTube downloading/extraction is not implemented.
- No API secrets are required for the public endpoints used here.

## Package
`com.arian.noorislamic`

## Current status
This is a source-code starter with core screens and initial integrations, not a release-certified production app. Build and test it before distributing.
