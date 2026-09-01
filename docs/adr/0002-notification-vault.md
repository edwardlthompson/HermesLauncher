# ADR-0002: Notification capture and vault schema

- **Status:** Accepted
- **Date:** 2026-09-01
- **Deciders:** HUMAN approved the Sprint 0 plan (2026-09-01)

## Context

The product promise is a history-keeping inbox: chat text and images must survive source-app dismissal and deletion. Capture must finish before `onNotificationPosted` returns.

## Decision

Persist first, then emit to UI. Room tables:

- `VaultItem` — id, sbnKey, packageName, channelId, postedAt, removedAt, type (`message` / `system` / `media` / `other`), priority, title, text, extrasJson, conversationTitle, pinned, archived, unread, contentStored, imagesStored
- `MessagePart` — itemId, sender, text, timestamp, imageRef
- `AppStorePolicy` — packageName, storeContent, storeImages (default off until the user grants)

Images live under `filesDir/vault/images/{id}/` with a 5 MiB original cap and 256 KiB thumbnail. Extract `EXTRA_PICTURE`, MessagingStyle image URIs, and large icon. Skip the image (keep text) when the cap is exceeded or decode fails.

Do not store body/images unless `AppStorePolicy` allows. Revoke stops new writes; existing rows remain until the user wipes.

SQLCipher / at-rest encryption is a tracked follow-up after this schema locks. Sprint 2 ships Room + app-private storage + `allowBackup=false`.

## Consequences

- Listener work is Sequential schema-lock before Parallel UI
- Unit tests use fixtures, not a live `NotificationListenerService`
- Huge bitmaps cannot crash the listener

## Alternatives Considered

| Pattern | Rejected because |
|---------|------------------|
| Persist after UI bind | Source app can cancel first |
| Store all apps by default | Violates the grant/revoke non-goal |
| SQLCipher in this ADR | Extra native dep before the schema is proven |
| Swipe-to-archive | Conflicts with pager swipe invariant |
