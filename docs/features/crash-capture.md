# Feature: crash-capture

> Opt-in local crash queue. Never auto-sends. Sanitize before persist.

## Acceptance criteria

- ✅ User-visible behavior: after a captured crash, one review dialog; never auto-open GitHub
- ✅ Offline/error behavior: write failure drops the record; handler errors do not re-enter
- ✅ Accessibility: same dialog contract as Feedback
- ✅ i18n: uses `feedback.*` / `feedback_*`

## Smoke scenario

1. Given the save-crashes setting is off
2. When an unhandled error occurs
3. Then nothing is persisted
4. When the setting is on, at most one sanitized record is stored; turning the setting off deletes it

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| Logic | `examples/web/src/crash-capture/` | `examples/android/.../crashcapture/` |
| Tests | `pendingCrash.test.ts` | `PendingCrashTest.kt` |
| Wiring | `appBootstrap.ts` ≤10 lines | `GoldenPathApp.kt` / `MainActivity` ≤10 lines |

## Definition of Done

Unit tests for queue-at-most-one, sanitize-before-persist, opt-in false, no re-entry. Fallback: `[HUMAN]` manual throw smoke.

## Notes

- Web: `sessionStorage` unless save-crashes is on (`localStorage`)
- Android: one app-internal file; chain previous `UncaughtExceptionHandler`
