# Feature: inbox-filters

> Sprint 9. Filter and search the live vault feed.

## Acceptance criteria

- User-visible behavior: Search and Filter icons; unread badge (hides at 0, caps 99+); search toggles closed and IME Done clears; All / Messages / Unread / Pinned / per-app plus App / Category / Time layouts; non-empty search also matches dismissed vault rows under a History header. Grouped stacks: [`inbox-groups.md`](inbox-groups.md).
- Offline/error behavior: empty filter shows dedicated empty copy, not the first-run inbox hint
- Accessibility: chips are selectable with content descriptions
- i18n: keys under `filter_*`

## Smoke scenario

1. Given vault rows of mixed types
2. When the user selects Unread
3. Then only unread items remain

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/vault/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/inbox/` |
| Tests | `src/test/.../vault/` |
| Wiring | `FeedPage` |

## Tests

Automated: yes — `InboxFilterTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A

## Definition of Done

Filter model is unit-tested; chips use `filter_*` strings.

## Notes

- Per-app filter uses packages from visible items. Grouping after `apply` is [`inbox-groups.md`](inbox-groups.md).
