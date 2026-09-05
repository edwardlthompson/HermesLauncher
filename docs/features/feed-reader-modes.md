# Feature: feed-reader-modes

> Sprint 36. News list sort stays at the top; refresh fills thumbnails; in-reader Reading / Full / Web chips; settings hub respects the status bar and uses per-section colors; 30-day keep and 24-hour read purge.

## Acceptance criteria

- ✅ User-visible behavior: toggling newest/oldest keeps the list scrolled to the top; Refresh stores an image URL when the feed or article page has one; reader bottom bar has Reading, Full, and Web between prev/next; settings hub sits below the clock; unstarred read rows expire after 24 hours; unstarred unread rows expire after 30 days
- ✅ Offline/error behavior: page-image enrich is best-effort; Web chip with no URL shows the empty extract message; purge never drops starred rows
- ✅ Accessibility: chips use `feed_reader_mode_*` content descriptions
- ✅ i18n: new keys in `strings_feeds.xml`

## Smoke scenario

1. Given News is open with several cards
2. When the user toggles oldest/newest, taps Refresh, opens an article, and switches Reading / Full / Web
3. Then the list stays at the top, more cards show thumbs when images exist, the three chips sit between the arrows, and Settings no longer draws under the clock

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedFilter.kt`, `feeds/ArticleImages.kt`, `feeds/ArticleEnrich.kt`, `feeds/MixPolicy.kt` |
| View | `ui/launcher/FeedsPage.kt`, `ui/player/FeedReader.kt`, `ui/settings/SettingsHub.kt` |
| Tests | `FeedFilterTest`, `ArticleImagesTest`, `ArticleCodecTest` |
| Wiring | `FeedRepository.refresh` / `expire`; `FeedSync.loop` hourly expire |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `ReaderMode` | `READING` (RSS/lite), `FULL` (fetched page, simplified), `WEB` (in-app WebView) |
| `MixPolicy.WINDOW_MS` | 30 days; fetch and unread keep window |
| `MixPolicy.READ_TTL_MS` | 24 hours; unstarred read rows |
| `ArticleRecord.readAt` | epoch ms of last mark-read; `0` if unread |
| `ArticleImages.allFromHtml` | one URL per photo: largest `srcset` (else src/data-src) plus og/twitter if it is a different photo; skip tiny URLs |
| `FeedFilter.purge` | starred always; else read uses 24h; else 30-day window |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Skip blank/tiny URLs in `ArticleImages.absUrl`; no image stays without a thumb; test extract/purge |
| Network timeout | Existing `FeedFetcher` 10s/15s; enrich is best-effort per article |
| Race | `LazyColumn` `scrollToItem(0)` after sort; purge on merge and `FeedSync` expire |
| Unhandled exceptions | `runCatching` on image/page fetch; WebView failures stay in-reader |
## Tests

- Automated: yes — `FeedFilterTest` 24h/30d purge; `ArticleImagesTest` multi-URL HTML; `ArticleCodecTest` `readAt`
- Coverage: purge matrix; og/srcset/data-src; codec round-trip

## Fallback validation

- Why tests are not feasible: OP12 sort, chips, settings inset, live thumbs
- Command: `python3 scripts/op12-feed-inbox-smoke.py`; `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Sort no longer jumps to the old bottom; Refresh backfills thumbs; three reader chips work; settings clears the status bar; retention matches 30d/24h; Feeder gaps documented.

## Notes

- Do not restore `MainActivity` as HOME
- OP12 ADB serial `b5214fc6` only
- RSS payloads that omit items older than a few days cannot invent 30 days of history
- Feeder inventory: `docs/features/feeder-gaps.md` (ideas only, no GPL)
