# Feature: hotseat-dock

> Sprint 21. Nova #13–15, #17. Skip #16 drawer handle. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- 🔲 User-visible behavior: dock can have extra Hotseat pages swiped **inside the dock**; background, height, radius, draw behind nav bar; dock icon size independent of desktop; hide-until-swipe peeks from the bottom; Apps icon + swipe-up All Apps stay
- 🔲 Offline/error behavior: one Hotseat page if codec empty; nested dock swipe does not change workspace `currentScreenId`
- 🔲 Accessibility: hide-until-swipe still has an Apps control when shown
- 🔲 i18n: `dock_*` chrome keys

## Smoke scenario

1. Given two dock pages
2. When the user swipes horizontally on the dock
3. Then workspace page stays; dock page changes

## Container map

| Layer | Path |
|-------|------|
| Logic | `icons/DockLayout` + Hotseat pages |
| View | `ui/launcher/DockBar.kt` nested pager |
| Tests | nested-scroll policy (pure) |
| Wiring | Dock settings section |

## Launcher3 class map

| AOSP | Hermes |
|------|--------|
| `Hotseat` + `CellLayout` | dock container, not a workspace screen |
| `DeviceProfile.hotseatIconSize` | dock icon size pref |
| `WindowInsets` | draw behind nav |
| `AllAppsTransitionController` | existing DockBar swipe-up |

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty | default 5 slots one page |
| Network timeout | N/A |
| Race | `nestedScroll` so workspace pager does not steal |
| Unhandled exceptions | coerce dock page index |
| Nova #16 | skipped — no third handle |

## Tests

- Automated: yes — dock page index + workspace index independence helper

## Fallback validation

- Why tests are not feasible: N/A for index helper. Live nested swipe is OP12 ADB.
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Hotseat is not a workspace screen. #16 is not implemented.

## Notes

- Skip Nova dock “drawer button” style
