# AntennaPod feature gaps

> Ideas only. [AntennaPod](https://github.com/AntennaPod/AntennaPod) is GPLv3 — Hermes copies **behavior names**, not code.
> Public sources: AntennaPod README and F-Droid listing.

Hermes already covers (after Podcasts page work): subscribe by URL, unread/star/search/newest-oldest, per-sub tag, metered/charging gates for feed XML, News + podcast OPML SAF, and play/pause/stop on the Podcasts page.

## Must-close on the Podcasts page (this plan)

| Gap | Hermes home |
|-----|-------------|
| Foreground MediaSession so audio survives swipe-away / screen off | `PodcastService` (`mediaPlayback`) |
| Playback notification + headset / BT skip | `MediaSession` + existing `POST_NOTIFICATIONS` path; no FCM |
| Per-episode resume | `EpisodeProgress` DataStore |
| Played-when-complete | Mark read at ≥95% or `STATE_ENDED` |
| Skip back/forward, speed, sleep timer | Mini-player −10s / +30s, 0.8–2.0×, sleep 15/30/45/off |
| Queue / play next | `PlayQueue`; mini-player Next; long-press play next |
| Offline audio download | `files/podcast-audio/`; Always/Wi-Fi/Never; auto latest N (default 1) |
| Shownotes on the same page | In-page `FeedReader`; Close/Back returns to the list |
## Intentionally skipped

- Chromecast / Play services
- Gpodder / cloud accounts
- Android Auto as a second UI
- Video-podcast cinema (open browser)
- AI transcripts
- Swipe-to-open a second drawer

## Acceptance criteria

- User-visible behavior: this file is a gap list, not a shippable slice
- Offline/error behavior: N/A
- Accessibility: N/A
- i18n: N/A

## Smoke scenario

1. Given Sprint 45 player work is on device
2. When a podcast plays with the screen off
3. Then `PodcastService` keeps MediaSession in the foreground

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/PodcastService.kt`, `feeds/PlayQueue.kt` |
| View | Podcasts page mini-player |
| Tests | `EpisodeProgress` / player unit tests |
| Wiring | `HermesApplication` player |

## Tests

- Automated: yes — player/queue unit tests shipped in Sprint 45

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`
