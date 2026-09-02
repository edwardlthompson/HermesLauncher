# Feature: notification-vault

> Sprint 2 vertical slice. Persist-first Room vault and X-only dismiss. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: inbox cards dismiss only via an explicit X (48dp target); swipe does not archive
- Offline/error behavior: notifications from every app are kept unless an explicit deny policy exists; oversized images skip the bitmap and keep text; no network
- Accessibility: X has a content description; card title and body are readable by TalkBack
- i18n: keys under `inbox_*` in `res/values/strings.xml`

## Smoke scenario

1. Given a posted notification fixture and a granted `AppStorePolicy`
2. When `VaultMapper.decide` runs before any UI bind
3. Then the decision is persist-text (and images when under the cap); a missing policy keeps the notification; an explicit `storeContent=false` policy skips

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/vault/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/inbox/` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/vault/` |
| Wiring | `FeedPage` binds `InboxCard`; no `NotificationListenerService` in this slice |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `PostedNotification` | Fixture-friendly input: sbnKey, packageName, channelId, postedAt, title, text, extrasJson, conversationTitle, type, priority, imageByteSize |
| `AppStorePolicy` | `packageName`, `storeContent`, `storeImages` — both flags default false |
| `VaultItem` / `MessagePart` | Room rows per ADR-0002 |
| `VaultItemType` | `MESSAGE`, `SYSTEM`, `MEDIA`, `OTHER` |
| `ImageLimits` | `ORIGINAL_MAX_BYTES` = 5 MiB; `THUMB_MAX_BYTES` = 256 KiB; images under `filesDir/vault/images/{id}/` |
| `VaultMapper.decide(posted, policy)` | `PersistDecision`: `SKIP`, `PERSIST_TEXT`, `PERSIST_TEXT_AND_IMAGES` |
| `VaultMapper.toItem(posted, decision)` | `VaultItem?` — null on skip |
| `InboxFeedState.dismissed(id)` | Removes one item; never a swipe target |

`VaultMapper` must not import `Notification` or `StatusBarNotification`. Listener mapping is a later adapter.

## Tests

Automated: yes

- `VaultMapperTest`, `InboxFeedStateTest` under `examples/android/app/src/test/java/org/hermeslauncher/app/vault/`
- Room in-memory insert/read when KSP generates `VaultDatabase`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — mapper and dismiss are unit-tested without a live listener.

## Definition of Done

Mapper honors default-off policy and image caps. Inbox X archives one card. `SwipePolicy` still rejects card horizontal swipe. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- NotificationListener persist-in-`onNotificationPosted` shipped
- SQLCipher shipped (ADR-0002)
- Archived rows prune after 30 days (switch) and cap at 2000; pinned kept; Settings lists Hermes history
- SQLCipher is a single writer; `VaultRepository.persist` throttles prune to at most once per 60s (`VaultPrune.shouldRun`). Startup and Settings still force a prune. Do not enlarge the Room pool.
