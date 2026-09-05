# Feature: all-apps-drawer

> Sprint 22. Nova #18–27 + notification blacklist with X. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: drawer columns 4–6; list vs grid; All/Predicted tabs (predicted row stays); hide apps from drawer; surface scrim; letter rail hide
- ✅ User-visible behavior: long-press-drag an All Apps icon onto a desktop cell (Launcher3); drawer stays composed at alpha 0 until drop; tap still launches; dock/desktop assign mode does not start a home drag
- ✅ User-visible behavior: App drawer settings lists ignored notification packages; **X** removes the row (whitelist); add from installed-app search
- ✅ Offline/error behavior: hide-apps empty = show all; blacklist blank package ignored; `READ` of policy miss = store (current mapper)
- ✅ Accessibility: X is `inbox`/`settings` content description “Stop ignoring {app}”
- ✅ i18n: `drawer_*`, `blacklist_*`

## Smoke scenario

1. Given `insertPolicy(AppStorePolicy("eu.faircode.email", storeContent=false))`
2. When a FairEmail notification posts
3. Then `VaultMapper.decide` skips persist; X on the row deletes the policy and later posts store

## Container map

| Layer | Path |
|-------|------|
| Logic | `icons/AllAppsIndex` prefs; `vault` `deletePolicy` + `policiesFlow` |
| View | `ui/launcher/AppDrawer.kt` + `HomeDrawerHost` / `HomeIconDrag` + `ui/settings/DrawerSettings.kt` |
| Tests | `StoreGrant` not used for deny; DAO delete; mapper skip; `DesktopPin.drop` |
| Wiring | Settings App drawer section; Inbox links here |
## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `AllAppsContainerView` | `AppDrawer` |
| `AlphabeticalAppsList` | existing A–Z + `LetterRail` |
| `PredictionRowView` | predicted row (shipped) |
| `ItemLongClickListener` + `DragView` | `detectDragGesturesAfterLongPress` + `HomeIconDrag` (drawer stays composed, alpha 0) |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty package | ignore add; test |
| Network timeout | N/A |
| Race | `insertPolicy` REPLACE vs `deletePolicy` DAO test |
| Unhandled exceptions | blur `RenderEffect` fail → solid scrim |
| Uncompose drawer kills pointer | Keep All Apps composed with `graphicsLayer` alpha 0 until drop (`WidgetPicker`) |
| Drop on inbox/feeds | `ensureDesktopPage` + `DesktopPin.drop` page 0 → `place()`; test `dropOnInboxPagePlacesOnFirstDesktop` |
| Occupied cell | `DesktopPin.drop` firstFit then `place()`; test `dropFallsBackWhenCellOccupied` |
| Blank package | `sanitize` returns null; test `dropIgnoresBlankPackage` |
## Tests

- Automated: yes — policy skip + delete; drawer column pref; `DesktopPin.drop` occupancy

## Fallback validation

- Why tests are not feasible: N/A for policy. Live hide-app is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Hide-apps ≠ blacklist. Blacklist X restores storage. Predicted row remains.

## Notes

- Do not use `StoreGrant.policy` for the ignore list
- Drawer-to-home drag copies `WidgetPicker`: keep All Apps composed with `graphicsLayer` alpha 0 until drop; long-press does **not** open shortcuts (dock still does); `DesktopPin.drop` uses the finger cell, then `firstFit`, then `place()`
