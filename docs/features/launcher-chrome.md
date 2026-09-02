# Feature: launcher-chrome

> Sprint 5 vertical slice. Dock slots, icon-pack keys, app search. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: customization copy explains dock slots and the active icon pack; dock ranks last-7-day usage (`lastTimeUsed`, then foreground time) unless Custom; All Apps is a 5-column A–Z grid with a letter rail and dismissible search; long-press shows `LauncherApps` shortcuts when Hermes is default Home; search filters launchable labels; long-press empty home opens Wallpaper / Widgets / Settings; Settings is grouped Home / Appearance / Inbox
- Offline/error behavior: empty query returns the full list; unknown pack falls back to the system key; no PackageManager in unit tests; missing wallpaper picker toasts
- Accessibility: search field and pack name are labeled
- i18n: keys under `chrome_*` in `res/values/strings.xml`

## Smoke scenario

1. Given three launchable apps and query `mail`
2. When `AppSearch.filter` runs
3. Then only labels containing `mail` (case-insensitive) remain, and `IconPackResolver.componentKey` is stable

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/icons/` |
| View | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/customize/` |
| Tests | `examples/android/app/src/test/java/org/hermeslauncher/app/icons/` |
| Wiring | Customize screen is standalone this slice; Settings hook is later |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `LaunchableApp` | packageName, activityName, label |
| `IconPackId` | `packageName` null = system icons |
| `DockLayout` | slotCount (1..8), `mode` usage\|custom, `slot(index)` empty until assigned |
| `AppSearch.filter(apps, query)` | case-insensitive substring on label |
| `IconPackResolver.componentKey(pack, app)` | `{packOrSystem}/{package}/{activity}` |
| `IconPackResources.drawable` | pack APK `Resources`; null falls back to system icons |

## Tests

Automated: yes

- `AppSearchTest`, `IconPackResolverTest`, `DockLayoutTest`

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A — search and keys are fixture-tested.

## Definition of Done

Search and pack keys are unit-tested. Customize screen uses `chrome_*` strings. `watch-agent-gates.sh --once --autofix` after the slice.

## Notes

- Live icon-pack `Resources` load: shipped (`IconPackResources`, fallback to system)
- Dock long-press: shortcuts when filled; Custom empty slots assign from All Apps
- All Apps Compose restyle after Launcher3 look; no Launcher3 Java
- Icons decode off the UI thread (`IconBitmapLoader` LruCache ~64)
- Notification dots are visible vault unread counts per package (Settings toggle)
- Home-again opens a unified search overlay ranked by usage recency then inbox time
- Double-tap empty chrome: Off / Lock (device admin) / Flashlight
- Wallpaper picker never pins a Google package; prefer AOSP/Lineage if present; in-tree Gradient and Clock live wallpapers
