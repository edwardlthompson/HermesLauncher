# Feature: workspace-pages

> Sprint 29. News and Inbox are reserved Launcher3 Workspace pages; Home snaps to Inbox; long-press Settings opens the Nova hub. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Workspace order is News, Inbox, desktop pages; Home / `moveToDefaultScreen` lands on Inbox; the Launcher3 dock stays visible; long-press empty News/Inbox/desktop opens Wallpaper, Widgets, live wallpaper, add icon, Settings
- ✅ Offline/error behavior: empty News and Inbox copy stay in `FeedsPage` / `FeedPage`; `HermesPages.ensure` no-ops when reserved screens already exist
- ✅ Accessibility: News/Inbox pages use `launcher_page_news` and `launcher_page_feed`; they are Workspace children (no overlay in the a11y tree)
- ✅ i18n: existing `launcher_page_news` / `launcher_page_feed`; L3 pref labels come from library strings

## Smoke scenario

1. Given Hermes is Home on OP12 (`b5214fc6`)
2. When the user swipes News ↔ Inbox ↔ desktop, presses Home, then long-presses empty desktop and taps Settings
3. Then the dock stays visible, Home lands on Inbox, and Settings opens `HermesSettingsActivity` with no `AndroidRuntime` fatal

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/workspace/HermesScreens.kt`, `HermesPageHost` |
| View | `HermesWorkspace`, `HermesPages`, `ui/launcher/HermesWorkspacePages.kt`, `ui/settings/` |
| Tests | `src/test/.../workspace/HermesScreensTest.kt`, `HermesSettingsActivityTest.kt` |
| Wiring | `launcher.xml` `@id/workspace` is `HermesWorkspace`; `HermesLauncherActivity` bind + options; manifest `APPLICATION_PREFERENCES` |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `HermesScreens.NEWS` | `-301` |
| `HermesScreens.INBOX` | `-302` |
| `HermesScreens.homePageIndex` | Inbox child index `1` when `pageCount > 1` |
| `HermesScreens.canDrop` | false on reserved IDs |
| `HermesPages.ensure` | insert News at 0 and Inbox at 1 if missing; idempotent |
| `HermesSettingsActivity` | `ACTION_APPLICATION_PREFERENCES` |
## Tests

- Automated: yes — `HermesScreensTest`, `HermesSettingsActivityTest`
- Coverage: reserved IDs, home index, drop deny, preferences intent resolve

## Fallback validation

- Why tests are not feasible: N/A for IDs/intent. Live swipe is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

News/Inbox are Workspace children sharing `PagedView` and the hotseat. Overlay is not attached. Long-press Settings opens the hub. Desktop/Dock/Drawer/Labs DataStore knobs do not restyle L3 `DeviceProfile` in this slice.

## Notes

- Supersedes `docs/features/minus-one-overlay.md`
- Do not restore `MainActivity` as HOME
- Follow-up: map Compose desktop knobs onto InvariantDeviceProfile / LauncherPrefs
