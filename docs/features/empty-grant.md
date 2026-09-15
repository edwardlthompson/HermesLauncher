# Feature: empty-grant

> Sprint 52. Inbox empty shows a notification-access CTA when the listener is off. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: listener off and empty vault shows `settings_listener_body` plus Turn on notification access. ZeroCopy only when the listener is on and the vault is empty. Filter empty stays for a matching filter.
- ✅ Offline/error behavior: leftover vault items still list while the listener is off; bind lag does not flash GRANT
- ✅ Accessibility: grant button uses `settings_listener_open`
- ✅ i18n: existing `settings_listener_*` keys

## Smoke scenario

1. Given notification access is off and Inbox has no cards
2. When the user opens Inbox (do not send HOME)
3. Then they see a grant button, not ZeroCopy

## Container map

| Layer | Path |
|-------|------|
| Logic | `vault/InboxEmpty` |
| View | `ui/inbox/InboxEmptyPane`, `InboxFeed` |
| Tests | `InboxEmptyTest` |
| Wiring | `FeedPage` listener snapshot on resume |

## Tests

- Automated: yes — `InboxEmptyTest` GRANT / FILTER / ZERO / CONTENT

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Inbox never congratulates the user while notification access is off.

## Notes

- Key off `LivePermissions.snapshot.notificationListenerEnabled`, not `ShadeBridge.listener` or `RepairPolicy`.
