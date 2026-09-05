# Feature: feed-inbox

> Sprint 32. News page matches inbox chrome: unread bubble, search, read/star filters, thumbnails, reading-mode images, 30-day unstarred purge.

## Acceptance criteria

- ✅ User-visible behavior: News top bar matches Inbox (search, filter, centered unread count); cards show a downloaded thumbnail; opening an article marks it read; reader can star or mark unread; filter menu offers starred / read / unread; search matches title, feed, and body; Unread/Read/Starred survives the reader; previous/next arrows walk the current list
- ✅ Offline/error behavior: tiny/icon images are skipped; failed thumbnails leave the card text-only; purge never drops starred rows; article cache stores original bytes, list thumbs stay sampled
- ✅ Accessibility: unread count, search, star, and mark-unread have content descriptions
- ✅ i18n: `feed_filter_*` / `feed_reader_star*` in `strings_feeds.xml`

## Smoke scenario

1. Given Android Authority stories on News
2. When the user searches, opens a card, stars it, and marks another unread
3. Then the unread bubble updates, the thumbnail remains, reader images render when large enough, and unstarred rows older than 30 days disappear after refresh

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedFilter.kt`, `ArticleStore.kt`, `ArticleCodec.kt`, `ArticleImages.kt`, `ArticleThumb.kt` |
| View | `FilterBar`, `FeedFilterMenu`, `FeedsPage`, `FeedCard`, `FeedReader` |
| Tests | `FeedFilterTest`, `ArticleImagesTest`, `ArticleCodecTest` |
| Wiring | `HermesApplication` + `FeedRepository.merge` |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `FeedChip` | `ALL`, `UNREAD`, `READ`, `STARRED` |
| `FeedFilter.apply` | text + chip; unread = not read |
| `FeedFilter.purge` | drop unstarred when age ≥ 30 days |
| `ArticleImages.looksTinyUrl` | skip icon/pixel/favicon/sprite URLs |
| `ArticleImages.MIN_EDGE` | 64px; skip if both edges below |
| `ArticleThumb.preview` | sampled JPEG under `feed-thumbs/` |
| `ArticleThumb.article` | original bytes under `feed-article/`; decode cap 4096px |
| `FeedFilter.adjacent` | previous/next ids in the open list |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Skip blank image URLs; `FeedFetcher.isHttpUrl`; empty search shows all chip matches |
| Network timeout | Reuse 10s/15s fetch; thumb failure is non-fatal |
| Race | Single `ArticleStore` mutex via DataStore edits; UI reads StateFlow |
| Unhandled exceptions | `runCatching` on decode/download; reader still shows text |
## Tests

- Automated: yes — filter chips, search, purge keeps starred, tiny URL skip, codec round-trip
- Coverage: unread count, 30-day cutoff, img src extract

## Fallback validation

- Why tests are not feasible: live thumbnail bytes and OP12 FilterBar layout
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

News chrome matches Inbox; thumbs and reader images skip tiny icons; read/star persist; unstarred 30-day purge.

## Notes

- Do not restore `MainActivity` as HOME
- OP12 ADB serial `b5214fc6` only
