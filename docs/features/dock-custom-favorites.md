# Feature: dock-custom-favorites

> Sprint 53. Dock CUSTOM writes L3 Favorites. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: when Dock most-used is off, assigned dock apps are written to L3 hotseat Favorites
- ✅ Offline/error behavior: empty apps store skips write and retries next paint; unused slots are left
- ✅ Accessibility: Settings body says pins come from Settings or long-press
- ✅ i18n: `settings_dock_most_used_body`

## Smoke scenario

1. Given Dock most-used is off and a custom pin exists
2. When the user returns to Home
3. Then that app is in the L3 dock

## Container map

| Layer | Path |
|-------|------|
| Logic | `l3/L3Dock` |
| View | Settings copy |
| Tests | `L3HomeEffect.dockCustomWritesFavorites` |
| Wiring | `L3Live` already collects dock layout |

## Tests

- Automated: yes — `L3HomeEffect.dockCustomWritesFavorites()` is true

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

CUSTOM dock pins write L3 Favorites. Settings copy matches.

## Notes

- USAGE path is unchanged.
