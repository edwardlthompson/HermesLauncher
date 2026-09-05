# Feature: feed-fulltext-json

> Sprint 39. JSON Feed, feed-full prefetch, HtmlCompat no-op images.

## Acceptance criteria

- ✅ JSON Feed 1.1 parses into FeedItem
- ✅ Prefetch HTML under feed-full/; skipped offline
- ✅ Purge deletes feed-full and feed-article for dropped ids
- ✅ HtmlCompat ImageGetter is a no-op

## Smoke scenario

1. Given a JSON Feed URL
2. When the user adds it and opens Full
3. Then the article body comes from `feed-full/` after prefetch

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/JsonFeedParser.kt`, `feeds/FeedFull.kt`, `feeds/ReaderHtml.kt` |
| View | Reader Full / Web chips |
| Tests | `JsonFeedParserTest`, `FeedFullTest`, `ReaderHtmlTest` |
| Wiring | `FeedFetcher` + `ArticleStore` |

## Tests

- Automated: yes — `JsonFeedParserTest`, `FeedFullTest`, `ReaderHtmlTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/op12-feed-parity-smoke.py`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | Parser empty list |
| Network timeout | FeedFetcher 10s/15s; prefetch runCatching |
| Race | Same expire/refresh path deletes files |
| Unhandled exceptions | runCatching HtmlCompat |
