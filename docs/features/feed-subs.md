# Feature: feed-subs

> Sprint 40. FeedSub schema, tags in drawer, block list, lastError.

## Acceptance criteria

- ✅ URL set migrates to JSON FeedSub list
- ✅ Tags appear as expandable groups in the Feeds drawer
- ✅ Block list ANDs in FeedFilter.apply
- ✅ lastError shows on the News retry line

## Smoke scenario

1. Given a migrated FeedSub list
2. When the user opens Feeds and a blocked tag
3. Then tags group in the drawer and lastError shows on retry

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedSub.kt`, `feeds/FeedFilter.kt` |
| View | Feeds drawer tags; News retry line |
| Tests | `FeedSubCodecTest`, `FeedFilterTest` |
| Wiring | `FeedStore` migrate |

## Tests

- Automated: yes — `FeedSubCodecTest`, `FeedFilterTest` block list

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/op12-feed-parity-smoke.py`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | Skip non-HTTP on migrate |
| Network timeout | lastError from fetch |
| Race | DataStore upsert |
| Unhandled exceptions | decode runCatching |
