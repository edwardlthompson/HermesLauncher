# Feature: feed-list-actions

> Sprint 38. Feeds bubble + drawer; Filter funnel stays status/sort; second seed feed.

## Acceptance criteria

- ✅ Feeds bubble left of unread on News; Inbox unchanged
- ✅ Drawer: All feeds, Saved, search, unread-only A–Z, pin current
- ✅ Filter menu has Mark all read and no feed titles

## Smoke scenario

1. Given News has at least one feed
2. When the user opens the Feeds bubble
3. Then All / Saved / tags appear and Filter has no feed titles

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedFilter.kt`, `feeds/FeedDrawerModel.kt` |
| View | News Feeds bubble and drawer |
| Tests | `FeedFilterTest` |
| Wiring | `ui/launcher` News chrome |

## Tests

- Automated: yes — `FeedFilterTest` drawerRows / sourceUrl / savedOnly

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/op12-feed-parity-smoke.py`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | All feeds always present; codec missing sourceUrl groups by title |
| Network timeout | F-Droid URL confirmed 200 |
| Race | scrollToItem(0) on source change |
| Unhandled exceptions | Drawer BackHandler + scrim |
