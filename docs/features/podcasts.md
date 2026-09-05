# Feature: podcasts

> Vertical slice for the Podcasts workspace page, Inoreader OPML seed, and AntennaPod-parity player on that page. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: swipe order is Podcasts → News → Inbox → desktop; Home stays Inbox; News shows `SubKind.NEWS`; Podcasts shows `SubKind.PODCAST`
- ✅ Offline/error behavior: HTTPS rewrite on import; failed fetches stay `FeedSub.lastError`; empty podcast OPML export is valid; streamed play if a download misses
- ✅ Accessibility: reserved page content description `launcher_page_podcasts`; icon transport, playhead, and elapsed/remaining/total times have strings
- ✅ i18n: `launcher_page_podcasts`, `feed_import_podcast_opml`, `feed_export_podcast_opml`, player extras in `strings_feeds.xml`

## Smoke scenario

1. _Given_ a debug APK on OP12 serial `b5214fc6`
2. _When_ the user swipes from Inbox through News to Podcasts, taps FAB, and uses Settings → Feeds podcast OPML
3. _Then_ Home still lands on Inbox, News export has no podcast URLs, and play continues after leaving the page

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/feeds/` (`PodcastDetect`, `InoreaderSeed`, `FeedOpml`, player stores) |
| View | `ui/launcher/HermesPodcastsPage.kt`, `ui/player/MiniPlayerBar.kt`, `ui/player/MiniPlayerScrub.kt`, `ui/settings/SettingsOpml.kt` |
| Tests | `OpmlParserTest`, `OpmlExporterTest`, `PodcastDetectTest`, `WorkspaceCodecTest`, `EpisodeProgressTest`, `PlayQueueTest`, `SleepTimerTest`, `PodcastAudioTest`, `PlayerClockTest` |
| Wiring | `HermesPages.ensure`, `WorkspacePager` PODCASTS branch, `HermesApplication.player` |
## Tests

- Automated: yes — unit tests listed above
- Coverage: parser tags, Sony NEWS, detect promote/demote, workspace prepend, player codecs

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py feature-gate --stack android`

## Definition of Done

See `docs/FEATURE_MODULES.md` and BUILD_PLAN Sprints 43–45.

## Notes

- Seed asset: `examples/android/app/src/main/assets/inoreader.opml` (PII title stripped)
- Do not restore `MainActivity` as HOME
- AntennaPod is ideas-only — see `docs/features/antennapod-gaps.md`
