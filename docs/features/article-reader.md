# Feature: article-reader

> Sprint 31. Seed Android Authority; lite reading-mode viewer; preferred opener is launcher reader or browser.

## Acceptance criteria

- ✅ User-visible behavior: Android Authority RSS is subscribed after seed; tapping a story opens the launcher reading-mode view (plain article text) unless Feeds → Preferred opener is Browser; reader always offers Open in browser
- ✅ Offline/error behavior: malformed HTML yields empty body copy plus Open in browser; non-http links never open
- ✅ Accessibility: Close article and Open in browser have content descriptions
- ✅ i18n: `feed_reader_*` / `feed_opener_*` in `strings_feeds.xml`

## Smoke scenario

1. Given News page after seed
2. When the user opens an Android Authority card, then Open in browser
3. Then reading-mode text appears (or Open in browser if extract is empty) and the system browser handles the https URL

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/feeds/` (`DefaultFeeds`, `ArticleExtract`, `ArticleOpen`, `ReaderPrefs`) |
| View | `ui/player/FeedReader.kt`, Feeds settings chips |
| Tests | `ArticleExtractTest`, `ArticleOpenTest`, `DefaultFeedsTest` |
| Wiring | `HermesApplication.readerPrefs`; `HermesNewsPage` onOpen |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `DefaultFeeds.ANDROID_AUTHORITY` | `https://www.androidauthority.com/feed/` |
| `DefaultFeeds.urlsForSeed(from)` | AA URL when `from < 1` |
| `ArticleExtract.fromRss` | content:encoded over description; strip tags |
| `ArticleExtract.fromPage` | `<article>` then `<p>` cluster |
| `ArticleTarget` | `LAUNCHER` (default) or `BROWSER` |
| `ArticleOpen.browserIntent` | `ACTION_VIEW` + `NEW_TASK` |
## Tests

- Automated: yes — `ArticleExtractTest`, `ArticleOpenTest`, `DefaultFeedsTest`, RSS html field
- Coverage: entity decode, article vs p fallback, target parse, seed list

## Fallback validation

- Why tests are not feasible: live AA fetch and browser handoff are OP12 ADB
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Seeded AA feed, reading-mode Compose reader, preferred opener in Settings Feeds.

## Notes

- Do not restore `MainActivity` as HOME
- OP12 ADB serial `b5214fc6` only
