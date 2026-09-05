# Feature: feed-background

> Sprint 42. WorkManager + boot sync; News unread widget.

## Acceptance criteria

- ✅ PeriodicWork + BOOT_COMPLETED
- ✅ FeedSync.loop expire-only when work registered
- ✅ AppWidgetProvider + RemoteViews; global unread

## Smoke scenario

1. Given Hermes is installed with a News unread widget
2. When WorkManager runs a refresh
3. Then unread count on the widget matches the vault

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedWork.kt`, `feeds/FeedUnread.kt` |
| View | `feeds/NewsUnreadWidget.kt` |
| Tests | `FeedWorkTest`, `FeedUnreadTest` |
| Wiring | `HermesApplication` WorkManager + boot receiver |

## Tests

- Automated: yes — `FeedWorkTest`, `FeedUnreadTest`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/op12-feed-parity-smoke.py`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | Widget shows 0 |
| Network timeout | Worker runCatching retry |
| Race | loop vs Work split |
| Unhandled exceptions | Work Result.retry |
