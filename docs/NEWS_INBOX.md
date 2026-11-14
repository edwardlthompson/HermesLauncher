# News Inbox

Auto-collected inbox of news, upstream updates, security fixes, and ideas relevant to this repo, appended daily by News Bot.
A Cursor planning agent should review these items and fold the useful ones into BUILD_PLAN; entries are append-only, newest at the bottom.


## 2026-11-13

**News and updates**

- Media3 1.11.1 stable and 1.12.0-beta01 (lower buffer memory, JSpecify nullness, Compose test teardown crash fix). [source](https://developer.android.com/jetpack/androidx/releases/media3)
- Android 17 QPR2 stable: lock-screen widget editing, status-bar notification filters, share-sheet changes, brightness override. [source](https://www.androidauthority.com/android-17-qpr2-stable-3733645/)
- Kotlin 2.5.0-RC released (fixes a reified Enum<T> inference regression); Gradle 9.9.1 / 8.14.7 patch releases. [source](https://github.com/JetBrains/kotlin/releases/tag/v2.5.0-RC)

**Tasks for the planning agent**

- [ ] Bump Media3 1.11.0 to 1.11.1 — Picks up stable playback fixes for podcasts. Label: `[AGENT]` · Priority: P2 · https://developer.android.com/jetpack/androidx/releases/media3
- [ ] Check behavior with Android 17 QPR2 notification filters — Status-bar notification filters may change what reaches the inbox. Label: `[ADB]` · Priority: P3 · https://www.androidauthority.com/android-17-qpr2-stable-3733645/
- [ ] Smoke-test Kotlin 2.5.0-RC and bump Gradle wrapper — Catches compiler regressions before stable. Label: `[AGENT]` · Priority: P3 · https://github.com/JetBrains/kotlin/releases/tag/v2.5.0-RC

## 2026-11-14

**News and updates**

- Android 17 QPR3 Beta 4 clarifies the Conscrypt error when TLS 1.0/1.1 connections are rejected for apps targeting API 36+. [source](https://developer.android.com/about/versions/17/qpr3/release-notes)

**Tasks for the planning agent**

- [ ] Show a clear 'outdated TLS' error for failing feeds and podcasts — Old TLS servers will fail on newer target SDKs and the user should know why. Label: `[AGENT]` · Priority: P3 · https://developer.android.com/about/versions/17/qpr3/release-notes
