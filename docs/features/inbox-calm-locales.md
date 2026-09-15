# Feature: inbox-calm-locales

> Sprint 53. Spanish `values-es` for inbox-calm strings. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Spanish locale uses translated first-run, ZeroCopy, Settings honesty, and grant empty copy
- ✅ Offline/error behavior: missing keys fall back to default `values/`
- ✅ Accessibility: TalkBack uses the locale strings
- ✅ i18n: `values-es` overlays, not a full `values-*` dump

## Smoke scenario

1. Given the device language is Spanish
2. When first-run and empty Inbox appear
3. Then copy is Spanish, not English

## Container map

| Layer | Path |
|-------|------|
| Logic | N/A |
| View | `res/values-es/` |
| Tests | N/A — XML resources |
| Wiring | Android resource overlay |

## Tests

- Automated: no

## Fallback validation

- Why tests are not feasible: locale overlay is aapt merge, not JVM logic
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Inbox-calm user-facing keys have a Spanish overlay. Other languages still fall back to English.

## Notes

- Full `values-*` catalog remains a later expansion.
