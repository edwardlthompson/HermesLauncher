# Feature: a11y-i18n-leftover

> Sprint 53. TalkBack strings for reader overflow and feed tag. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: reader overflow actions and the feed tag field use string resources
- ✅ Offline/error behavior: N/A — static labels
- ✅ Accessibility: TalkBack matches the visible label
- ✅ i18n: existing `feed_reader_*` and `feed_sub_tag`

## Smoke scenario

1. Given an article is open
2. When TalkBack focuses Read aloud
3. Then it speaks the same string as the menu row

## Container map

| Layer | Path |
|-------|------|
| Logic | N/A |
| View | `ReaderOverflow`, `SettingsFeedSubs` |
| Tests | N/A — string wiring |
| Wiring | none |

## Tests

- Automated: no

## Fallback validation

- Why tests are not feasible: semantics content descriptions are Compose view wiring
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

No hardcoded English in FeedsDrawer, SettingsHub, ReaderOverflow, or feed tag field.

## Notes

- FeedsDrawer and SettingsHub were fixed in a11y-chrome.
