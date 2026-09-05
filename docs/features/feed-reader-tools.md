# Feature: feed-reader-tools

> Sprint 37. Share, overflow reader tools, Custom Tabs, one high-res image file used as the card thumb.

## Acceptance criteria

- ✅ User-visible behavior: Share on the reader top bar; TTS/find/scale/enclosure in overflow; Open-in-new uses Custom Tabs with browser fallback; list thumbs decode the original cache only
- ✅ Offline/error behavior: Share hidden without URL; TTS no-ops without an engine; Custom Tabs falls back to ACTION_VIEW
- ✅ Accessibility: `feed_reader_share` content description
- ✅ i18n: keys in `strings_feeds.xml`

## Smoke scenario

1. Given an article is open in the reader
2. When the user taps Share, then overflow TTS/find
3. Then a share sheet appears and thumbs still render from `feed-article/`

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedShare.kt`, `feeds/ArticleImages.kt`, `feeds/ArticleThumb.kt`, `feeds/ArticleOpen.kt` |
| View | `ui/player/ReaderBars.kt`, `ReaderOverflow.kt`, `FeedReader.kt` |
| Tests | `FeedShareTest`, `ArticleImagesTest`, `ArticleThumbTest` |
| Wiring | `FeedReader` top bar + overflow |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `FeedShare.intent` | `ACTION_SEND` text/plain; null if URL is not HTTP |
| `ArticleImages.canonicalHero` | one URL: largest srcset, then media/thumb, then og/html |
| `ArticleThumb.preview` | in-sample decode of original only; never reads `feed-thumbs/` |
| `ArticleThumb.purgeLegacyThumbs` | deletes `feed-thumbs/` |
| `ArticleTarget.CUSTOM_TAB` | OpenInNew prefers Custom Tabs |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | Hide Share; canonicalHero null if no HTTP image |
| Network timeout | Existing FeedFetcher timeouts on original GET |
| Race | One original file per URL hash |
| Unhandled exceptions | runCatching on TTS/Custom Tabs |
## Tests

- Automated: yes — FeedShareTest, canonicalHero, purgeLegacyThumbs
- Coverage: send extras; srcset wins; preview without thumbs dir

## Fallback validation

- Command: `python3 scripts/op12-feed-parity-smoke.py`

## Definition of Done

Share works; one-res thumbs; overflow tools present.

## Notes

- Do not restore MainActivity as HOME
- OP12 serial `b5214fc6` only
