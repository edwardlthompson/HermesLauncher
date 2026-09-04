# Feature: paged-view-motion

> Sprint 20. Nova #4–5, #7–8, #10–11. `PagedView` physics. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: scroll adjacent / continuous / inverse; QSB top/bottom/none; pinch → overview **or** All Apps (one default, picker in Desktop); infinite wrap and widget overlap **off** until Labs on
- 🔲 Offline/error behavior: wrap off if `screenIds.size < 2`; overlap off keeps `canPlace` false; pinch never finishes the activity
- 🔲 Accessibility: QSB placement announced; Labs toggles labeled
- 🔲 i18n: `paged_*`, `labs_*`

## Smoke scenario

1. Given Labs wrap off and three screens
2. When the user flings past the last desktop
3. Then the pager stops (no wrap)

## Container map

| Layer | Path |
|-------|------|
| Logic | `workspace/PagedPolicy.kt` + Labs flags |
| View | `WorkspacePager` fling / `reverseLayout`; QSB bar |
| Tests | wrap modulo + overlap `canPlace` |
| Wiring | Desktop settings + Labs |

## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `PagedView` snap | adjacent vs continuous |
| inverse scroll | `HorizontalPager(reverseLayout)` |
| `WorkspaceState.OVERVIEW` | pinch overview |
| `AllAppsTransitionController` | pinch-to-drawer option |
| `SearchContainerView` | QSB |
| occupancy overlap | Labs; default deny |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | wrap no-op on empty ids |
| Network timeout | N/A |
| Race | Labs DataStore sequential |
| Unhandled exceptions | pinch `coerceIn`; no `popBack` |
| Infinite wrap vs HOME | HOME still animates to `homeScreenId` |

## Tests

- Automated: yes — wrap/overlap policy

## Fallback validation

- Why tests are not feasible: N/A for policy. Live fling is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Defaults match Launcher3 (no wrap, no overlap). Nova extras are Labs or explicit Desktop prefs.

## Notes

- Launcher3 does not infinite-wrap; Nova does — Labs off by default
