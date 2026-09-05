# Feature: home-gestures-settings

> Sprint 30. Home on Inbox opens All Apps search; swipe sensitivity; Settings hub knobs drive Launcher3. Checklist markers: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: tapping Home while Inbox is showing opens All Apps with the search field focused and the keyboard up; Inbox/News vertical scrolls do not open the drawer (dock swipe still can); Gestures → Swipe sensitivity Low/Medium/High changes how far a desktop/dock swipe must travel; Desktop/Dock/Drawer/Folders/Search/Look/Gestures/Inbox/Feeds/Backup options persist and change live launcher behavior
- ✅ Offline/error behavior: missing icon pack drawables fall back to system icons; usage dock skips pin when Usage Access or All Apps data is empty
- ✅ Accessibility: All Apps search `ExtendedEditText` receives focus; Inbox/News still use `launcher_page_feed` / `launcher_page_news`
- ✅ i18n: `gesture_sensitivity*` in `strings_gestures.xml`

## Smoke scenario

1. Given Hermes is Home on OP12 (`b5214fc6`) showing Inbox
2. When the user taps Home, then scrolls Inbox, then opens Settings and changes swipe sensitivity, grid, labels, drawer hide, and theme
3. Then search+IME appear, Inbox does not open the drawer, and each Settings control changes the homescreen (no placeholder panes)

## Container map

| Layer | Path |
|-------|------|
| Logic | `examples/android/app/src/main/java/org/hermeslauncher/app/l3/`, `launcher/SwipeSensitivity.kt` |
| View | `HermesLauncherActivity`, `HermesSwipeController`, `ui/settings/HomeChromeSettings.kt` |
| Tests | `l3/L3SettingsLogicTest.kt`, `launcher/SwipeSensitivityTest.kt`, `ui/launcher/EmptySpaceSwipeTest.kt` |
| Wiring | `HermesLauncherActivity.onCreate` → `L3Live.attach`; `createTouchControllers` |
## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `HomeAgainSearch.shouldOpen` | second Home on Inbox + NORMAL + no floating view + ACTION_MAIN |
| `SwipeSensitivity` | LOW 4.5× slop, MEDIUM 2.5× (default), HIGH 1× |
| `HermesSwipeController` | no intercept on reserved pages except hotseat |
## Tests

- Automated: yes — `HomeAgainSearchTest`, `SwipeSensitivityTest`, `L3GridTest`, `L3NightModeTest`, `L3DockTest`, `EmptySpaceSwipeTest`, `FolderLid.previewCap`
- Coverage: home-again predicate, slop ordering, grid pick, night delegate, dock icon mapping, empty-space threshold

## Fallback validation

- Why tests are not feasible: N/A for predicates. Live Home/IME/scroll is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Home-again search, adjustable swipe slop, Inbox scroll isolation, and Settings hub options applied to Launcher3 (grid, labels, wrap/overlap/inverse, drawer columns/hide/rail, dock size/usage, folders, search cap, look, gestures).

## Notes

- Do not restore `MainActivity` as HOME
- OP12 ADB serial `b5214fc6` only
