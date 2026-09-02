# Feature: widget-resize

> Sprint 11. Persist widget span and drag order.

## Acceptance criteria

- User-visible behavior: a bound widget can be resized; size survives process death
- Offline/error behavior: invalid spans clamp to 1×1 minimum; cancel does not leave a ghost id
- Accessibility: resize controls have content descriptions
- i18n: keys under `widget_resize_*`

## Smoke scenario

1. Given a bound widget on page 1
2. When the user grows it and leaves Home
3. Then the host view restores that span

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/widgets/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/widgets/` |
| Tests | `src/test/.../widgets/` |
| Wiring | `WidgetPage` |

## Tests

Automated: yes — codec round-trip includes span

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A for persist; live drag is ADB

## Definition of Done

`WidgetBinding` stores width/height cells; codec version bump is backward compatible.

## Notes

- Icon pack Resources ship in the same sprint via `launcher-chrome.md`
