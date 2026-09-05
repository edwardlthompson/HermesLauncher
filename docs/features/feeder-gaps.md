# Feeder feature gaps

> Ideas only. [Feeder](https://github.com/spacecowboy/Feeder) is GPLv3 — Hermes copies **behavior names**, not code.
> Public sources: Feeder README, F-Droid listing, and the Sprint 34 skip list in `feed-reader-settings.md`.

Hermes already covers: OPML, scan interval, charging gate, open-time refresh, Always/Wi-Fi/Never images, reader vs browser vs Custom Tabs, newest/oldest sort, unread/star/search, add-by-URL, RSS/Atom/JSON Feed, Reading/Full/Web, share, TTS/find/scale, Feeds drawer (All/Saved/tags), block list, local feed alerts, WorkManager+boot sync, and a News unread widget.

## Remaining Feeder-only (skipped)

| Area | Why skipped |
|------|-------------|
| Nostr | Full protocol client; JSON Feed covers the extra format |
| E-ink | Removed at human request |
| Cloud AI | Translation/summaries stay off the FOSS path |
| Feedly/FreshRSS | Local-only; no cloud accounts |
| FCM | Proprietary push |
| Swipe-to-open drawer | Workspace pager owns that axis; Feeds bubble opens the drawer |
| Read-on-scroll / open-next | Prefs default false; prev/next arrows remain |
## Intentionally skipped (FOSS / launcher scope)

- Cloud feed backends (Feedly, Inoreader, FreshRSS)
- Proprietary push (FCM) for feed alerts
- Play Billing / closed analytics

## Acceptance criteria

- User-visible behavior: this file is a skip list, not a shippable slice
- Offline/error behavior: N/A
- Accessibility: N/A
- i18n: N/A

## Smoke scenario

1. Given News/Podcasts features from Sprints 38–42
2. When a user opens Feeds and the unread widget
3. Then listed Feeder gaps stay skipped (no Feedly/FCM/Nostr)

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/` |
| View | News / Feeds drawer / widget |
| Tests | feed unit tests from those sprints |
| Wiring | `FeedWork`, `NewsUnreadWidget` |

## Tests

- Automated: yes — existing feed unit tests (this file adds no new code)

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
