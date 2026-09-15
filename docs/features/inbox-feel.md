# Feature: inbox-feel

> Sprint 51. Quiet Material 3 chrome on reserved pages; unread vs read like the system shade. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Inbox, News, and Podcasts share a tonal FilterBar (title + unread badge + search/filter). Unread inbox titles are bold; read titles are regular. Pin shows only when pinned; long-press pins. Dismiss stays a 48dp X. Cards use opaque `surface`, not 0.78 wallpaper wash.
- ✅ Offline/error behavior: missing unread defaults false (regular weight); empty search still closes via back; long-press with no pin handler is a no-op
- ✅ Accessibility: search/filter/X keep content descriptions; long-press uses `inbox_feel_pin_hint`; unread count uses `inbox_unread_count`
- ✅ i18n: `inbox_feel_pin_hint` plus existing `inbox_*` / `filter_*` keys

## Smoke scenario

1. Given an unread and a read notification on Inbox
2. When the user opens Inbox (do not send HOME if already there)
3. Then the unread title is bold, the read title is not, search expands in the bar, and X still archives

## Container map

| Layer | Path |
|-------|------|
| Logic | `vault/InboxDisplay` |
| View | `ui/inbox/FilterBar`, `InboxCard`, `VaultItemCard`, `InboxGroup` |
| Tests | `InboxDisplayTest` |
| Wiring | `FeedPage`, `FeedsPage` FilterBar title only |
## Tests

- Automated: yes — `InboxDisplayTest` titleBold / showPinIcon / groupBold
- Coverage: unread true/false; pinned pin visible; empty group not bold

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Reserved-page chrome matches MiniPlayer tonal surface. Unread is visible without HUD bubbles. HOME stays Launcher3.

## Notes

- Do not restore widget X/Y fill. Do not `keyevent 3` from Inbox (Home-again search).
- Settings honesty and page haptics are later Sprint 51 rows.
