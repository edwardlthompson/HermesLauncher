# Feature: minus-one-overlay

> **Superseded by [workspace-pages.md](workspace-pages.md) (Sprint 29).** News and Inbox are reserved Workspace pages, not a DragLayer overlay.

> Sprint 28. News then inbox Compose on the AOSP minus-one overlay. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: from the first desktop page, overscroll toward minus-one reveals News; a further pager swipe shows Inbox; overscroll settle opens past 35% and closes below that
- ✅ Offline/error behavior: empty news shows the feeds empty copy; empty inbox shows the home hint; malformed feed URLs stay in `FeedRepository.addFromLink`
- ✅ Accessibility: host is `overlay_minus_one`; pages use `launcher_page_news` and `launcher_page_feed`
- ✅ i18n: `overlay_minus_one`, `launcher_page_news` in `res/values/strings.xml`

## Smoke scenario

1. Given Hermes is Home on OP12 (`b5214fc6`)
2. When the user overscrolls the first workspace page toward minus-one, then swipes once more
3. Then News then Inbox are visible, and logcat has no `AndroidRuntime` fatal

## Container map

| Layer | Path |
|-------|------|
| Logic | superseded — was `OverlayProgress.kt` |
| View | superseded — was `HermesMinusOne.kt` |
| Tests | removed with overlay settle helper |
| Wiring | superseded — `HermesLauncherActivity.getDefaultOverlay()` is the stock empty manager |
## Tests

- Automated: no — overlay settle helper was deleted with the overlay
- Coverage: N/A (see workspace-pages)

## Fallback validation

- Why tests are not feasible: this spec is historical; live paging is covered by `docs/features/workspace-pages.md`
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

See `docs/features/workspace-pages.md`.
