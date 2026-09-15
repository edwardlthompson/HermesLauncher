# Feature: page-settle-haptic

> Sprint 51. Light tick when the L3 workspace snap finishes a user swipe. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: swiping between Podcasts, News, Inbox, and desktop pages plays a click haptic when the page settles
- ✅ Offline/error behavior: no tick on first layout, immediate snaps, drag-lock snaps, or when system haptics are off; vibrator failures are ignored
- ✅ Accessibility: respects Settings haptic feedback; not tied to animator duration scale
- ✅ i18n: N/A — no new user-visible strings

## Smoke scenario

1. Given Hermes is Home and haptics are on
2. When the user swipes from Inbox to News
3. Then a short click plays when the page settles, and Inbox search is not opened (no HOME key)

## Container map

| Layer | Path |
|-------|------|
| Logic | `l3/PageSettleHaptic` |
| View | `HermesWorkspace` dispatch / `onPageEndTransition` |
| Tests | `PageSettleHapticTest` |
| Wiring | `HermesWorkspace` ≤10 lines |

## Tests

- Automated: yes — `PageSettleHapticTest` shouldTick first layout / immediate / same page / user pan

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

User page snaps tick; programmatic/immediate snaps stay silent.

## Notes

- `PagedView` disables view haptics. Use `VibratorWrapper`, not `performHapticFeedback`.
