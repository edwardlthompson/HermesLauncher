# Feature: a11y-chrome

> Sprint 52. 48dp star, decorative icons silent, expand/collapse labels, reduce-motion helper. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: FeedCard star is a 48dp `IconButton`. Inbox source and group glyphs are decorative (`contentDescription = null`). Group header TalkBack says expand or collapse. Feeds drawer and Settings hub rows use string resources, not hardcoded English.
- ✅ Offline/error behavior: star with no handler is still a 48dp target; missing package icon falls back to Apps glyph, still decorative
- ✅ Accessibility: expand/collapse via `inbox_group_*`; star via `feed_reader_star` / `feed_reader_unstar`
- ✅ i18n: `inbox_group_collapse`, `settings_hub_open`, `feed_drawer_title`

## Smoke scenario

1. Given grouped Inbox cards and a News article
2. When TalkBack focuses the group header and the star
3. Then it names collapse when expanded, and the star is a 48dp control

## Container map

| Layer | Path |
|-------|------|
| Logic | `ui/theme/MotionPrefs` |
| View | `FeedCard`, `InboxCard`, `InboxGroup`, `InboxAppGlyph`, `FeedsDrawer`, `SettingsHub` |
| Tests | `MotionPrefsTest` |
| Wiring | string resources |

## Tests

- Automated: yes — `MotionPrefsTest` (`reduced` true only at animator scale 0)

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

TalkBack names expand/collapse and star. Motion-feel later rows must call `MotionPrefs.reduced`.

## Notes

- Motion-feel (search expand, dismiss fade) waits until this row is done. Haptic is a system setting, not animator scale.
