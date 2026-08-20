# Feature: display-refresh

Window requests the fastest same-resolution display mode. Scroll surfaces vote HIGH so adaptive-refresh panels can ramp during flings.

## Acceptance criteria

- User-visible behavior: About and Settings scroll at the panel's peak same-resolution rate when the OS allows it
- Offline/error behavior: missing display or empty mode list leaves `preferredDisplayModeId` unchanged
- Accessibility: no new controls; motion follows OS refresh (respects battery saver / ARR)
- i18n: N/A — no user-facing strings

## Smoke scenario

1. Given the app is running on a high-refresh device
2. When About or Settings is opened and flung
3. Then the window's preferred mode matches the fastest same-size `Display.Mode` and scroll uses `FrameRateCategory.High`

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/.../display/DisplayModeSelector.kt` |
| Adapter | `examples/android/.../display/WindowRefresh.kt`, `HighRefreshScroll.kt` |
| Tests | `examples/android/app/src/test/.../display/DisplayModeSelectorTest.kt` |
| Wiring | `MainActivity` one call; About/Settings scroll modifiers |

## Notes

Fallback validation: `./gradlew :app:testDebugUnitTest --tests dev.foss.goldenpath.display.DisplayModeSelectorTest`
