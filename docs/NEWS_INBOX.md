# News Inbox

Auto-collected inbox of news, upstream updates, security fixes, and ideas relevant to this repo, appended daily by News Bot.
A Cursor planning agent should review these items and fold the useful ones into BUILD_PLAN; entries are append-only, newest at the bottom.


## 2026-11-13

**News and updates**

- Media3 1.11.1 stable and 1.12.0-beta01 (lower buffer memory, JSpecify nullness, Compose test teardown crash fix). [source](https://developer.android.com/jetpack/androidx/releases/media3)
- Android 17 QPR2 stable: lock-screen widget editing, status-bar notification filters, share-sheet changes, brightness override. [source](https://www.androidauthority.com/android-17-qpr2-stable-3733645/)
- Kotlin 2.5.0-RC released (fixes a reified Enum<T> inference regression); Gradle 9.9.1 / 8.14.7 patch releases. [source](https://github.com/JetBrains/kotlin/releases/tag/v2.5.0-RC)

**Tasks for the planning agent**

- [x] Bump Media3 1.11.0 to 1.11.1 — Picks up stable playback fixes for podcasts. Label: `[AGENT]` · Priority: P2 · https://developer.android.com/jetpack/androidx/releases/media3 (synced to BUILD_PLAN)
- [ ] Check behavior with Android 17 QPR2 notification filters — Status-bar notification filters may change what reaches the inbox. Label: `[ADB]` · Priority: P3 · https://www.androidauthority.com/android-17-qpr2-stable-3733645/
- [ ] Smoke-test Kotlin 2.5.0-RC and bump Gradle wrapper — Catches compiler regressions before stable. Label: `[AGENT]` · Priority: P3 · https://github.com/JetBrains/kotlin/releases/tag/v2.5.0-RC

## 2026-11-14

**News and updates**

- Android 17 QPR3 Beta 4 clarifies the Conscrypt error when TLS 1.0/1.1 connections are rejected for apps targeting API 36+. [source](https://developer.android.com/about/versions/17/qpr3/release-notes)

**Tasks for the planning agent**

- [ ] Show a clear 'outdated TLS' error for failing feeds and podcasts — Old TLS servers will fail on newer target SDKs and the user should know why. Label: `[AGENT]` · Priority: P3 · https://developer.android.com/about/versions/17/qpr3/release-notes

## 2026-11-16

**News and updates**

- A foreground Android app can read the clipboard and hide the Android 12+ "pasted from clipboard" toast with two of its own toasts; Google closed it Won't Fix, so set ClipDescription.EXTRA_IS_SENSITIVE on clips that hold private data. [source](https://www.navsplace.net/android-clipboard-toast-bypass-20261114/)

**Tasks for the planning agent**

- [ ] Mark the feedback-screen clipboard copy sensitive and clear it after use — The feedback preview is the app's only clipboard write, and flagging it keeps any notification or vault text out of clipboard previews and other apps. Label: `[AGENT]` · Priority: P3 · https://www.navsplace.net/android-clipboard-toast-bypass-20261114/

## 2026-11-19

**News and updates**

- Jetpack Compose November '26 release is stable: Compose BOM 2026.11.01 (Compose 1.13) with Material3 1.5.0. The BOM needs Kotlin 2.2+ and compileSdk 37.1. Material3 1.5.0 breaks Slider/RangeSlider source (stateless overloads deprecated, `onValueChange` now required, RangeSliderState activeRangeStart/End renamed to startValue/endValue); SecureTextField now defaults to system obfuscation, and clickables play platform click sounds by default. [source](https://developer.android.com/blog/posts/jetpack-compose-november-2026-release) · [Material3 notes](https://developer.android.com/jetpack/androidx/releases/compose-material3)

**Tasks for the planning agent**

- [x] Bump Compose BOM 2026.09.00 to 2026.11.01 (Compose 1.13 / Material3 1.5.0) and fix Slider/RangeSlider call sites — Material3 1.5.0 makes `onValueChange` required on Slider/RangeSlider (1 file here call Slider/RangeSlider) and the BOM requires Kotlin 2.2+ and compileSdk 37.1, so a Dependabot/Argus BOM bump will fail unless AGP accepts 37.1 and the call sites are fixed first. Label: `[AGENT]` · Priority: P2 · https://developer.android.com/blog/posts/jetpack-compose-november-2026-release (synced to BUILD_PLAN)
