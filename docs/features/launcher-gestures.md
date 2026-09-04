# Feature: launcher-gestures

> Sprint 25. Nova #37–39. Unify with DockBar swipe-up. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: swipe up/down/left/right, two-finger, pinch map to drawer, search, lock, torch, shade, or launch app; defaults swipe down = search, swipe up = All Apps; empty-space action picker; Home-again and double-tap stay
- 🔲 Offline/error behavior: unknown action enum → no-op; lock without admin toasts (existing)
- 🔲 Accessibility: picker actions labeled
- 🔲 i18n: `gesture_*`

## Smoke scenario

1. Given default map
2. When swipe-up fires on empty workspace (not dock)
3. Then All Apps opens once (`DrawerState` shared with DockBar)

## Container map

| Layer | Path |
|-------|------|
| Logic | `launcher/LauncherAction.kt` + DataStore |
| View | `ui/settings/GestureSettings.kt` + empty-space detector |
| Tests | action decode + default map |
| Wiring | `HomeActions` + DockBar one drawer |

## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `AllAppsTransitionController` | swipe-up drawer |
| `OptionsPopupView` | empty-space picker |
| `Workspace` swipe | extra directions |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | default map; test decode |
| Network timeout | N/A |
| Race | one `DrawerState` so dock + gesture do not double-open |
| Unhandled exceptions | unknown action no-op |

## Tests

- Automated: yes — codec + defaults

## Fallback validation

- Why tests are not feasible: N/A for map. Live swipe is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Gesture map persisted. Dock swipe-up and workspace swipe-up share one controller.

## Notes

- Do not add a second All Apps path
