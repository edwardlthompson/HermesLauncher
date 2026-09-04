# Feature: desktop-cell-layout

> Sprint 19. Nova #1–3, #9, #12 schema. Port `CellLayout` occupancy, not the Java view. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: Settings Desktop picks independent icon vs widget grid (default both 4×5 so current tiles stay); icon size % and label show/hide/wrap; page insets; optional card behind icons; folders exist as `FolderInfo` ids (window UI is Sprint 23)
- ✅ User-visible behavior: All Apps long-press-drag drops a 1×1 shortcut at the finger cell (`DesktopPin.drop`)
- 🔲 Offline/error behavior: shrinking a grid `fitToSpec`s like widgets today; `firstFit` miss does not place; unknown folder id ignored
- 🔲 Accessibility: grid steppers and label toggles labeled
- 🔲 i18n: `desktop_*` in `res/values/strings.xml`

## Smoke scenario

1. Given a 4×5 occupancy with one 2×2 widget
2. When icon grid stays 4×5 and widget grid stays 4×5
3. Then the widget origin is unchanged after round-trip

## Container map

| Layer | Path |
|-------|------|
| Logic | `workspace/` + extend `widgets/WidgetGrid.kt` |
| View | `ui/workspace/CellLayoutGrid.kt` + `ui/settings/DesktopSettings.kt` |
| Tests | `WidgetGrid` span map + occupancy |
| Wiring | Desktop page uses `CellLayoutGrid`; do not grow `WidgetPage.kt` |

## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `CellLayout` + `markCells` | `WidgetGrid.canPlace` / `firstFit` for icons 1×1 and folders |
| `ItemInfo.spanX/spanY` | widget span = ceil(widgetCells × iconCols/widgetCols) |
| `DeviceProfile.iconSizePx` | icon size percent pref |
| `WorkspaceItemInfo` / `FolderInfo` | sealed `DesktopItem` |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | missing item id skipped; test |
| Network timeout | N/A |
| Race | same host DataStore mutex |
| Unhandled exceptions | place no-op on `canPlace` false |
| `WidgetGrid` 150-line cap | split span mapper if needed |

## Tests

- Automated: yes — occupancy + span mapping

## Fallback validation

- Why tests are not feasible: N/A for math. Live icon drop is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Two-grid spec persisted. Folder ids in the model. CellLayout composable hosts widgets and 1×1 icons.

## Notes

- Default both grids 4×5 so Sprint 18 tiles do not jump
