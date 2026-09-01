# Feature: launcher-shell

> Sprint 1 vertical slice. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: Home activity pages between a feed placeholder and two empty widget pages; dock stub stays at the bottom; swipe-up opens a drawer stub
- Offline/error behavior: no network required; missing default-home state is informational only
- Accessibility: X is not in this slice; pager pages and dock/drawer controls have content descriptions
- i18n: keys under `launcher.*` in `res/values/strings.xml`

## Smoke scenario

1. Given the debug APK is installed
2. When the user sets Hermes as Home or launches the activity
3. Then page 0 shows the inbox placeholder, horizontal swipe reaches widget placeholders, and no card consumes that swipe

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/launcher/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/launcher/` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/launcher/` |
| Wiring | `HermesApp.kt` / `HermesScreen.kt` composition only |

## Tests

Automated: yes

- `HomePagerStateTest`, `SwipePolicyTest`, `DrawerStateTest` under `examples/android/app/src/test/java/org/hermeslauncher/app/launcher/`
- Instrumented: `HermesUiTest.showsInboxPlaceholderOnHome`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — unit tests cover the swipe contract.

## Swipe contract

Cards must not register horizontal dismiss. `SwipePolicy.consumesHorizontalSwipe(target)` returns false for `Target.Card`. Pager page index is the only horizontal consumer.

## Definition of Done

Unit tests for pager/dock/drawer state and swipe policy. Instrumented smoke still launches MainActivity. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Widget bind/resize is ADR-0003 later, not this slice
- NotificationListener is ADR-0002 later
