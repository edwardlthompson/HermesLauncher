# Feature: folders

> Sprint 23. Nova #28–32. Depends on Sprint 19 `FolderInfo`. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: folder window vs fullscreen; background, radius, shadow; lid preview stack/grid/fan; folder icon size + labels; unread dots/counts on lids
- ✅ Offline/error behavior: empty folder shows empty lid; missing child activity dropped from preview
- ✅ Accessibility: folder name and badge counts announced
- ✅ i18n: `folder_*`

## Smoke scenario

1. Given a folder with three apps and unread on one package
2. When the lid is shown
3. Then preview layout matches the pref and a badge appears

## Container map

| Layer | Path |
|-------|------|
| Logic | `workspace/FolderInfo` + `unreadByPackage` |
| View | `ui/workspace/FolderWindow.kt` + `FolderIcon.kt` |
| Tests | preview pick + badge |
| Wiring | Settings Folders section |
## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `Folder` / `STATE_FULL_SCREEN` | window vs fullscreen |
| `FolderIcon` + `PreviewItemManager` | lid preview |
| `AbstractFloatingView` | folder overlay |
| `DotInfo` | lid badges |
### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | empty preview list; test |
| Network timeout | N/A |
| Race | folder contents via workspace store mutex |
| Unhandled exceptions | missing icon → Apps vector |
## Tests

- Automated: yes — preview slice + badge count

## Fallback validation

- Why tests are not feasible: N/A for preview math. Live open is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Folders are first-class desktop containers with lid chrome.

## Notes

- Schema locked in Sprint 19; this sprint is UI
