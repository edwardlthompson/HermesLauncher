# Feature: feed-alerts

> Sprint 41. Local feed notifications and Always/Wi-Fi/Never image chips. No e-ink.

## Acceptance criteria

- ✅ Channel hermes_feeds; NotificationCompat only
- ✅ Denied POST_NOTIFICATIONS: switch visible; post no-ops
- ✅ Image chips map onto showThumbs + mobileData

## Smoke scenario

1. Given a subscribed feed and notification permission
2. When a refresh finds a new article
3. Then a `hermes_feeds` notification posts (or no-ops if permission is denied)

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedNotify.kt`, `feeds/FeedSyncPolicy.kt` |
| View | Settings Feeds notify switch and image chips |
| Tests | `FeedNotifyTest`, `ImagePolicyTest` |
| Wiring | `FeedWork` / `FeedNotify` from sync |

## Tests

- Automated: yes — `FeedNotifyTest`, `ImagePolicyTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/op12-feed-parity-smoke.py`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | Missing article id opens News list |
| Network timeout | N/A for notify post |
| Race | notify ids = new-this-cycle |
| Unhandled exceptions | post runCatching |
