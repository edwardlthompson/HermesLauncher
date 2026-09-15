# Feature: pull-to-refresh

> Sprint 53. Pull-to-refresh on Inbox, News, and Podcasts. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: pulling Inbox rebinds the notification listener; News and Podcasts call the existing feed refresh
- ✅ Offline/error behavior: fetch-failed chip stays; rebind no-ops if the listener is off
- ✅ Accessibility: FilterBar refresh control remains
- ✅ i18n: existing `feed_refresh`

## Smoke scenario

1. Given News has subscriptions
2. When the user pulls the list
3. Then the refresh indicator runs and articles update

## Container map

| Layer | Path |
|-------|------|
| Logic | `vault/InboxRefresh` |
| View | `PageRefreshBox`, `FeedPage`, `FeedsPage` |
| Tests | `InboxRefreshTest` |
| Wiring | reserved pages only |

## Tests

- Automated: yes — `InboxRefreshTest` listener class name

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Pull gesture refreshes Inbox via listener rebind and News/Podcasts via `feeds.refresh`.

## Notes

- FilterBar refresh buttons stay.
