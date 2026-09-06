# Feature: feed-unsubscribe

> Sprint 49. Unsubscribe, notify-all / prefetch-all, grouped compact settings. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Settings → Feeds → Subscriptions lists news and podcasts in expanders; Unsubscribe drops the feed and its unstarred articles
- ✅ User-visible behavior: Notify all and Prefetch all switches set every subscription; mixed lists show off until every row is on
- ✅ User-visible behavior: Feeds drawer nests tagged feeds under their folder with indent and chevrons; long-press offers mark-read, move, rename, unsubscribe
- ✅ Offline/error behavior: blank URL is a no-op; starred articles stay after unsubscribe
- ✅ Accessibility: expander titles and Unsubscribe buttons use visible labels
- ✅ i18n: `feed_unsubscribe`, `feed_notify_all*`, `feed_prefetch_all*`, `settings_group_*`

## Smoke scenario

1. Given two news subscriptions
2. When the user opens Settings → Feeds → Subscriptions, turns Notify all on, then taps Unsubscribe on one row
3. Then that feed is gone from the list and the remaining feed still notifies

## Container map

| Layer | Path |
|-------|------|
| Logic | `feeds/FeedSubPolicy.kt`, `feeds/FeedApply.dropSource`, `FeedStore.remove` |
| View | `ui/settings/SettingsFeedSubs.kt`, `SettingsHub.kt`, `SettingsGroups.kt` |
| Tests | `FeedSubPolicyTest`, `FeedApplyTest`, `SettingsHubTest` |
| Wiring | `FeedRepository.unsubscribe`; Settings nested nav |

## Tests

- Automated: yes — policy, dropSource, hub groups

## Fallback validation

- Why tests are not feasible: live Settings taps are OP12 ADB
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty URL | `FeedSubPolicy.withoutUrl` and `unsubscribe` no-op on blank; `FeedApplyTest` |
| Network timeout | N/A — local DataStore + article purge |
| Race | `replaceSubs` writes one DataStore edit; `unsubscribe` removes then purges articles |
| Unhandled exceptions | decode already `runCatching`; starred rows kept so saved items are not lost |

## Definition of Done

Subscriptions live under a Feeds submenu. Unsubscribe works. Hub is four groups. `watch-agent-gates.sh --once --autofix --scope auto` after the slice.
