# Feature: inbox-groups

> Sprint 13. Posidon-style per-app stacks on the home inbox. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: after filters, vault rows stack by sending app (newest group first) unless Category or Time; a group expands to individually dismissible cards; header tap expands or collapses only; Open-in-new (left of dismiss-all, expanded app groups) launches the source app; group X archives every id in the stack
- Offline/error behavior: empty inbox keeps the first-run hint (no group chrome); blank `packageName` is its own group `""`; missing icon uses the Apps vector; already-archived child dismiss is a no-op
- Accessibility: Search, Filter, group expand, group open (when expanded), group dismiss, and per-item X have content descriptions
- i18n: keys under `filter_*` and `inbox_*` in `res/values/strings.xml`

## Smoke scenario

1. Given two apps each with two vault rows
2. When the inbox is shown
3. Then two collapsed groups appear; expanding one shows both cards; group X archives both ids for that package

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/vault/InboxFilter.kt` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/inbox/` + `ui/launcher/FeedPage.kt` |
| Tests | `InboxFilterTest` + `InboxGroupLaunchTest` |
| Wiring | `FeedPage` lists groups then feed cards |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `InboxAppGroup` | `packageName` + `items` (newest first inside) |
| `InboxFilter.apply` | still filters before grouping |
| `InboxFilter.groups` | newest-group-first; blank package buckets as `""` |

## Tests

Automated: yes — `InboxFilterTest` group ordering; `InboxGroupLaunchTest` for `showGroupLaunch`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A for grouping. Live expand/dismiss is OP12 ADB.

## Definition of Done

Groups are unit-tested. Feed rows are never nested inside an app stack. Filter menu stays scrollable.

## Notes

- Inspiration: Posidon feed grouping; do not copy GPL sources
- MixPolicy merge is not used on the grouped inbox; feeds list below groups when the query is pristine All
- `showGroupLaunch`: Open-in-new only when expanded and `packageName` is not blank; header click target excludes the action buttons
