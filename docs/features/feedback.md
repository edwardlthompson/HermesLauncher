# Feature: feedback

> About / Help review dialogs for bug and feature reports. Not a donate nag.

## Acceptance criteria

- ✅ User-visible behavior: About has Report a bug and Request a feature; review panel shows escaped preview, Copy, Open GitHub, Discard
- ✅ Offline/error behavior: Copy still works; Open GitHub disabled with i18n reason; search fail-soft
- ✅ Accessibility: dialog `role="dialog"` with labelled buttons; no Android Toast
- ✅ i18n: `feedback.*` web / `feedback_*` Android

## Smoke scenario

1. Given crash-capture is off
2. When the user opens About and Report a bug, types a description
3. Then they can copy sanitized markdown; Open GitHub is enabled only when description or stack exists

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| View | `examples/web/src/components/FeedbackPanel.ts` | `examples/android/.../ui/feedback/` |
| Logic | `examples/web/src/feedback/` | `examples/android/.../feedback/` |
| Tests | `FeedbackPanel.test.ts`, `preview.test.ts` | `FeedbackPreviewTest.kt` |
| Wiring | `appBootstrap.ts` / `AppShell.ts` ≤10 lines | `HermesApp.kt` ≤10 lines |
## Tests

- Automated: yes — `FeedbackPanel.test.ts`, `preview.test.ts`, `FeedbackPreviewTest.kt`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack <active>`

## Definition of Done

See `docs/FEATURE_MODULES.md`. XSS test: preview never uses `innerHTML` of reporter text.

## Notes

- Settings toggle “Save crash details for me to review” defaults off (`feedback.save_crashes`)
- Discard best-effort clears clipboard text we wrote
