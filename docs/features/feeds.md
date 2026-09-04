# Feature: feeds

> Sprint 4 vertical slice. RSS/OPML parse, mix policy, mini-player state. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: parsed feed items can mix into the inbox; an episode shows a mini-player with play/pause copy
- ✅ User-visible behavior: Feeds FAB accepts a site or feed URL, discovers RSS/Atom, and shows the last 30 days
- Offline/error behavior: parsers take strings only; malformed XML yields empty items and does not throw; no network in this slice
- Accessibility: play/pause controls have content descriptions
- i18n: keys under `player_*` and `feed_*` in `res/values/strings.xml`

## Smoke scenario

1. Given a fixture RSS item with an audio enclosure and an OPML outline
2. When `RssParser` / `OpmlParser` run and `MixPolicy.merge` combines them with an empty vault
3. Then the mixed list contains the article/episode and `MiniPlayerState.toggle` flips playing

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/feeds/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/player/` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/feeds/` |
| Wiring | `FeedPage` can host `MiniPlayerBar`; HTTP fetch is later |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `OpmlOutline` | title, xmlUrl, htmlUrl, type |
| `FeedItem` | id, feedTitle, title, link, publishedAt, enclosureUrl, enclosureMime |
| `RssParser.parse(xml)` | RSS 2.0 or Atom items; never throws |
| `FeedFetcher.resolve` | HTTP GET; HTML alternate-link discovery; null on miss |
| `MixPolicy.withinWindow` | keep items from the last 30 days (`publishedAt == 0` kept) |
| `OpmlParser.parse(xml)` | OPML outlines with `xmlUrl`; never throws |
| `MixPolicy.merge(vault, feeds)` | newest `publishedAt` / `postedAt` first; episodes stay `FeedKind.EPISODE` |
| `MiniPlayerState` | optional `FeedItem`, `playing`; `toggle()` / `load(item)` |
| `FeedKind` | `ARTICLE`, `EPISODE` — enclosure audio/* → episode |
| `FeedFetcher` | HTTP(S) GET with 10s/15s timeouts; `itemsFromXml` never throws |
| `OpmlImporter.read` | OPML outlines with http(s) `xmlUrl` only |
| `HermesPlayer` | Media3 ExoPlayer; audio-focus pause; blank enclosure is a no-op |

Media3 `ExoPlayer` bind is a follow-up adapter. This slice locks state + UI chrome only.

## Tests

Automated: yes

- `RssParserTest`, `OpmlParserTest`, `MixPolicyTest`, `MiniPlayerStateTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — parsers and mix are fixture-tested.

## Definition of Done

OPML/RSS fixtures parse. Mix order is deterministic. Mini-player i18n renders. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Simple enclosures only; no queue, chapters, or cloud sync
- HTTP fetch with timeouts and OPML file import: shipped (`FeedFetcher`, `OpmlImporter`, Settings import)
- Feeds FAB: `FeedFetcher.resolve` follows `<link rel="alternate" type="application/rss+xml">`; `MixPolicy.withinWindow` keeps 30 days
- Media3 ExoPlayer bind: shipped (`HermesPlayer`)
