# Feature: first-run-copy

> Sprint 52. First-run leads with notification access; ZeroCopy has no emoji; one Nova path. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: overlay body names notification access before Photos. ZeroCopy strings have no emoji. Nova import lives on `NovaSetupCard` / Backup only, not the first-run overlay. Unused `home_inbox_hint` and `launcher_feed_placeholder` are removed. Drawer empty copy does not name `QUERY_ALL_PACKAGES`.
- ✅ Offline/error behavior: overlay Later still dismisses; grant buttons still open system settings
- ✅ Accessibility: TalkBack reads ZeroCopy as plain sentences
- ✅ i18n: `home_setup_query_all`, `home_setup_photos`, `drawer_empty`, `strings_zero.xml`

## Smoke scenario

1. Given a fresh install
2. When the first-run overlay appears
3. Then the first paragraph asks for notification access, and Nova is not a second button on that sheet

## Container map

| Layer | Path |
|-------|------|
| Logic | N/A — string resources |
| View | `ui/onboarding/FirstRunOverlay`, `NovaSetupCard` |
| Tests | `ZeroCopyTest` still maps day → `zero_*` keys |
| Wiring | overlay strings only |

## Tests

- Automated: yes — `ZeroCopyTest` resource ids; copy is XML

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

First-run copy names notification access, not `QUERY_ALL_PACKAGES`. Inbox empty never uses emoji while celebrating.

## Notes

- Keep `NovaSetupCard`. Do not restore `MainActivity` as HOME.
