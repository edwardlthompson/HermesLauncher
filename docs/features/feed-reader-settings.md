# Feature: feed-reader-settings

> Sprint 34. News refresh control plus Settings → Feeds options taken from the common FOSS reader set.

## Inventory — [Feeder](https://github.com/spacecowboy/Feeder) (GPL-3.0, F-Droid)

Source: Feeder `app/src/main/res/values/strings.xml` (settings labels). Hermes copies **behavior ideas only**, not code.

| Area | Feeder offers | Hermes this sprint |
|------|---------------|--------------------|
| Manual refresh | Sync action | News FilterBar refresh + Settings button |
| Scan interval | Manual, 15m, 30m, 1h, 3h, 6h, 12h, daily | Same chips |
| Metered network | Only on Wi-Fi; include mobile hotspots | Download over mobile data (on/off) |
| Power | Only when charging | Same toggle |
| Open-time sync | Upon app start | Refresh when opening News |
| Thumbnails | Show thumbnails / image loading | Show thumbnails (cache still used) |
| Opener | Reader / browser / custom tab | Reader / browser (already shipped) |
| Sort | Newest / oldest first | Settings default (list filter still overrides) |
| OPML | Import/export | Already shipped |
| Add feed | URL subscribe | FAB + Android Authority button |
| Unread/star/search | Filters, saved, search | Already shipped |
| Skip | TTS, notifications, block list, JSONFeed/Nostr, and the rest in `docs/features/feeder-gaps.md` | Out of scope this sprint |
## Acceptance criteria

- ✅ User-visible behavior: News top bar has Refresh; Settings → Feeds has scan interval, mobile-data, charging, open-refresh, thumbnails, sort, opener, OPML
- ✅ Offline/error behavior: auto-scan skips when offline, on cellular when mobile-data is off, or when charging-only and unplugged; manual refresh still runs when online
- ✅ Accessibility: refresh control uses `feed_refresh` content description
- ✅ i18n: new keys in `strings_feeds.xml`

## Smoke scenario

1. Given News is open with a subscribed feed
2. When the user taps Refresh, then opens Settings → Feeds and sets scan to 15 minutes and turns mobile data off
3. Then a fetch runs from the button; auto-scan uses the new interval; thumbs stay cached-only on cellular

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedSyncPolicy.kt`, `feeds/FeedSync.kt`, `feeds/ReaderPrefs.kt` |
| View | `FilterBar`, `FeedsPage`, `SettingsFeeds.kt` |
| Tests | `FeedSyncPolicyTest` |
| Wiring | `HermesApplication` ticker (≤10 lines) |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `ScanInterval.OPTIONS` | `0, 15, 30, 60, 180, 360, 720, 1440` minutes; `0` = manual |
| `FeedSyncPolicy.allowAuto` | online + mobile/charging gates |
| `FeedSyncPolicy.allowDownload` | online + mobile gate (images) |
| `ReaderSettings.mobileData` | default `true` (keep current fetches) |
| `ReaderSettings.refreshOnOpen` | default `true` |
| `FeedRepository.refreshing` | true while a fetch is in flight |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | `ScanInterval.clamp` maps unknown ints to `60`; blank URLs unchanged |
| Network timeout | Reuse existing 10s/15s `FeedFetcher`; auto-scan no-ops when offline |
| Race | `refreshing` flag; ticker `collectLatest` cancels the previous interval loop |
| Unhandled exceptions | `runCatching` on network snapshot; failed fetch still sets `refreshFailed` |
## Tests

- Automated: yes — `FeedSyncPolicyTest` (wifi vs cellular, charging, clamp)
- Coverage: allowAuto matrix; allowDownload; ScanInterval.clamp

## Fallback validation

- Why tests are not feasible: OP12 FilterBar refresh tap and Settings chips
- Command: `python3 scripts/op12-feed-settings-smoke.py` (OP12); `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Refresh is always on the News bar; Settings → Feeds exposes Feeder-class sync gates without GPL code.

## Notes

- Do not restore `MainActivity` as HOME
- OP12 ADB serial `b5214fc6` only
- Interval ticker runs in the launcher process (HOME stays resident)
