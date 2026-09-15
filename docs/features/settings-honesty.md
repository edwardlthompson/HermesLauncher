# Feature: settings-honesty

> Sprint 51. Caption Desktop/Dock/Labs/Look knobs so copy matches L3 HOME. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Labs overlap is titled Page peek and says icons cannot share a cell. Dock most-used off says long-press the dock. Look badges say they style Hermes counts on Inbox/News/Podcasts, not desktop app icons.
- ✅ Offline/error behavior: switches still persist; no Settings row is removed
- ✅ Accessibility: switch/dropdown content descriptions keep using the visible titles
- ✅ i18n: `labs_overlap*`, `settings_dock_most_used_body`, `settings_show_dots_body`, `look_badge_body`, `backup_labs_hint`

## Smoke scenario

1. Given Hermes is Home
2. When the user opens Settings → Desktop → Desktop motion, Dock, and Look
3. Then each body describes what actually changes on L3 or reserved-page chrome

## Container map

| Layer | Path |
|-------|------|
| Logic | `l3/L3HomeEffect` |
| View | `DesktopSettings`, `DockSettings`, `LookSettings`, `HomeChromeSettings` |
| Tests | `L3SettingsLogicTest` (`L3HomeEffect`) |
| Wiring | string resources only |

## Tests

- Automated: yes — `L3HomeEffect` flags for peek vs cell overlap, dock CUSTOM, Look desktop icons

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Settings copy does not claim cell overlap, Settings dock pins, or L3 desktop icon badges.

## Notes

- Do not hide working knobs. Do not wire DockStore into L3 Favorites.
