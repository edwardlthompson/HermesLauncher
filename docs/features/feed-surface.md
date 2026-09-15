# Feature: feed-surface

> Sprint 52. Opaque News/Podcasts cards and readable empty/error chips. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: FeedCard uses opaque `surface`. Feeds empty and fetch-failed copy sit on a `surfaceContainerHigh` chip with `onSurface` type. Inbox empty panes use the same chip. FilterBar keeps `ElevationLevel2`.
- ✅ Offline/error behavior: `workspace_feeds_fetch_failed` stays a clickable retry; empty list with no fetch error uses `workspace_feeds_empty`
- ✅ Accessibility: empty/error text is `onSurface`, not white on wallpaper
- ✅ i18n: existing `workspace_feeds_*` / `feed_empty_filter` keys

## Smoke scenario

1. Given News has no subscriptions
2. When the user opens News on a light wallpaper
3. Then empty copy is readable on a tonal chip, and cards that appear later match Inbox opacity

## Container map

| Layer | Path |
|-------|------|
| Logic | N/A — color tokens only |
| View | `ui/player/FeedCard`, `ui/launcher/FeedsPage`, `ui/inbox/FilterBar`, `InboxEmptyPane` |
| Tests | N/A — color not unit-testable without screenshot |
| Wiring | `FeedsPage` empty/error chips |

## Tests

- Automated: no

## Fallback validation

- Why tests are not feasible: Compose color on wallpaper needs a screenshot harness this slice does not add
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

News and Inbox cards look like the same product on a light wallpaper. No `Color.White` HUD on Feeds empty/error.

## Notes

- Do not restore widget X/Y fill. Do not send HOME from Inbox.
