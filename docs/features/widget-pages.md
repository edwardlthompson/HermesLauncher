# Feature: widget-pages

> Sprint 3 vertical slice. AppWidgetHost page model and persist. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: pages 1+ show a widget host surface with empty copy and an Add control; tapping Add opens an in-app list sorted by app name then widget label; bound IDs render as labeled slots. Grid occupancy, drag-to-bind, and trailing pages: [`widget-grid.md`](widget-grid.md).
- Offline/error behavior: persisted bindings reload without network; invalid codec input yields empty pages; bind/configure failures log `HermesWidget` and toast
- Accessibility: Add, picker rows, and slot labels have content descriptions
- i18n: keys under `widget_*` in `res/values/strings.xml`

## Smoke scenario

1. Given default `HomePagerState` with two widget pages
2. When a widget id is allocated onto page 1 and encoded
3. Then decode restores that binding and page 2 stays empty

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/widgets/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/widgets/` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/widgets/` |
| Wiring | `LauncherHome` swaps placeholder for `WidgetPage`; host view bind is later |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `WidgetHostIds.HOST_ID` | `1024` — `AppWidgetHost` id for this package |
| `WidgetBinding` | `appWidgetId` (>0), optional `providerFlattened`, `cellsW/H`, `cellX/Y` — see [`widget-grid.md`](widget-grid.md) |
| `WidgetPageState` | `pageIndex` (≥1), `bindings` |
| `WidgetHostState` | pages 1..n with one trailing empty page; `grid: WidgetGridSpec`; `withBinding` / `withoutWidget` / `relocate` / `withGrid` / `page` |
| `WidgetHostCodec.encode/decode` | `v4` `{cols}x{rows}|id:provider:w:h:x:y`; v1–v3 migrate; corrupt input → default empty pages |
| `WidgetBindPolicy.canRecord` | rejects `appWidgetId <= 0` |
| `WidgetCatalog.sorted` | app label then widget label, case-insensitive |

In-app picker + `ACTION_APPWIDGET_BIND` / `ACTION_APPWIDGET_CONFIGURE` live in `WidgetHostController` (not the pure page model).

## Tests

Automated: yes

- `WidgetHostStateTest`, `WidgetHostCodecTest`, `WidgetCatalogTest` under `examples/android/app/src/test/java/org/hermeslauncher/app/widgets/`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — persist and page mutation are unit-tested without a live host.

## Definition of Done

Codec round-trips bindings. Widget pages replace Sprint 1 placeholders. Horizontal swipe still belongs to the pager. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Live `allocateAppWidgetId` + bind permission is a follow-up adapter
- Resize/drag occupancy lives in [`widget-grid.md`](widget-grid.md)
