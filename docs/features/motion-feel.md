# Feature: motion-feel

> Sprint 53. Search expand, group size, dismiss fade, and stick-to-top scroll respect reduce-motion. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: FilterBar search and Inbox group expand use `animateContentSize` when animator duration scale is non-zero. Stick-to-top uses `animateScrollToItem`. Dismissing a list row uses `animateItem` fade.
- ✅ Offline/error behavior: animator scale 0 or missing setting snaps with no animation; `Settings.Global` read failures default to scale 1
- ✅ Accessibility: `MotionPrefs.reduced` is true when animator duration scale is 0
- ✅ i18n: N/A — no new strings

## Smoke scenario

1. Given animator duration scale is 1 and Inbox is newest-first at the top
2. When a new card arrives and the user expands search
3. Then the list animates to top and the search field expands

## Container map

| Layer | Path |
|-------|------|
| Logic | `ui/theme/MotionPrefs` |
| View | `FilterBar`, `InboxGroup`, `InboxFeed`, `InboxStickToTop` |
| Tests | `MotionPrefsTest` |
| Wiring | none |

## Tests

- Automated: yes — `MotionPrefsTest` reduced / animateScroll / animateSize / animateDismiss

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Reduce-motion users get instant search, group, dismiss, and stick-to-top.

## Notes

- Pull-to-refresh and sound stay out of this row. Haptic is a system setting, not animator scale.
