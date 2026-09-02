# Feature: widget-grid

> Sprint 13 occupancy + Sprint 14 user-chosen grid size + Sprint 15 Launcher3-look chrome. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: Settings picks 4×5, 6×6, 8×8, or any 3–12 axes; long-press empty home opens Wallpaper / Widgets / Settings; Widgets opens a preview-card sheet with search by app or widget label; drop places a 2×2 tile; long-press a placed widget shows a four-handle resize frame (left/top may move origin) down to 1×1; drag to the top Remove well deletes; a trailing empty widget page appears until `MAX_WIDGET_PAGES`
- Offline/error behavior: `firstFit` miss keeps the sheet and allocates nothing; bind deny / configure cancel deletes the id; corrupt codec yields default empty pages; v1–v3 migrate onto `WidgetGridSpec.DEFAULT`; shrinking the grid `fitToSpec`s overflow instead of dropping ids when a 1×1 hole exists; missing wallpaper picker toasts and does not crash
- Accessibility: empty-page Add, Remove well, four resize handles, and grid stepper icons have content descriptions; resize announces `widget_resized`
- i18n: keys under `widget_*`, `home_option_*`, `settings_widget_grid*`, `settings_wallpaper*` in `res/values/strings.xml`

## Smoke scenario

1. Given Settings set to 6×6
2. When the user long-presses empty home, picks Widgets, and drops two preview cards onto free cells
3. Then both bind at 2×2 without overlapping; logcat `HermesWidget` has no fail/AndroidRuntime

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/widgets/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/widgets/` + `ui/launcher/HomeOptionsPopup.kt` + `ui/settings/WidgetGridSettings.kt` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/widgets/` |
| Wiring | `LauncherHome` pager + `WidgetHostController` drop-to-bind; Settings collects `widgetStore` |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `WidgetBinding` | `appWidgetId`, `providerFlattened`, `cellsW/H`, `cellX/Y`; `PLACE_CELLS` = 2×2 |
| `WidgetGridSpec` | `columns`/`rows` clamped 3..12; presets 4×5, 5×5, 6×6, 8×8; default 4×5 |
| `WidgetHostState.grid` | persisted with pages |
| `WidgetHostState.applySpan` | persist origin+size when `canPlace`; else no-op |
| `WidgetResize.spansForDelta` | left/top may change `cellX`/`cellY`; clamp to spec |
| `WidgetPreview.kind` | previewImage != 0 → IMAGE; else icon or NONE |
| `WidgetGrid.MAX_WIDGET_PAGES` | `20` |
| `WidgetGrid.canPlace` | false on overlap or out of bounds for the given spec |
| `WidgetGrid.firstFit` | origin or `null` when full |
| `WidgetGrid.dropCandidate` | 2×2 at snapped origin |
| `WidgetGrid.applyGrid` | `fitToSpec` every page then trailing empty |
| `WidgetHostCodec` | `v4` `colsxrows` then `id:provider:w:h:x:y`; v1–v3 migrate |
| `HomePagerState.pageCountFor` | `1 + widgetPages.coerceIn(1, MAX)` |
| `WallpaperIntents.picker` | `ACTION_SET_WALLPAPER`, no package |
| `WidgetCatalog.filter` | case-insensitive app/widget label; empty query is identity |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty at boundary | `WidgetGridSpec.parse` and codec corrupt → `WidgetGridSpec.DEFAULT` + empty pages; tested in `WidgetHostCodecTest`; empty preview → icon card |
| Network timeout | N/A — no network I/O |
| Race | single DataStore key `v4` encodes grid with pages; Settings `save(withGrid)` is sequential; resize persist on pointer up |
| Unhandled exceptions | bind/configure still `fail` + delete id; HostView size `runCatching`; wallpaper `resolveActivity` + toast |
| Provider minWidth fills the page | drop uses `PLACE_CELLS` 2×2; resize floor is 1×1, not `minWidth` |
| Google wallpaper package | `WallpaperIntents` never `setPackage` |
| GPL / Launcher3 Java | Compose restyle only; no vendored launcher sources |

## Tests

Automated: yes — `WidgetGridTest`, `WidgetHostCodecTest`, `WidgetHostStateTest`, `WidgetResizeTest`, `WidgetPreviewTest`, `WallpaperIntentsTest`, `HomePagerStateTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A for occupancy/codec/resize math. Live drag/bind is OP12 ADB.

## Definition of Done

Occupancy uses `WidgetGridSpec`. Codec v4 round-trips grid. New drops are 2×2. Home chrome matches Launcher3 interaction (Compose). `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Look is modeled on AOSP Launcher3 (Apache-2.0); do not vendor GPL launcher sources or copy Launcher3 Java
- `widget-pages.md` remains the host id / catalog contract; this spec owns the grid and home chrome
