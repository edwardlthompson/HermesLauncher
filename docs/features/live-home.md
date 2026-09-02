# Feature: live-home

> Sprint 8. First-run grants, wallpaper chrome, live vault feed, working AppWidgetHost. Checklist markers: open / done / blocked use BUILD_PLAN emoji.

## Acceptance criteria

- User-visible behavior: Home has no app TopAppBar; wallpaper shows; first-run overlay asks for notification access, battery, and Home role; Add widget binds a real host view; dock icons launch apps
- Offline/error behavior: missing wallpaper uses a light card scrim only; cancelled widget pick deletes the allocated id; notifications from all apps are kept
- Accessibility: overlay actions, dock slots, drawer apps, and X dismiss have content descriptions
- i18n: keys under `home_*`, `grant_*`, `widget_*`, `drawer_*` in `res/values/strings.xml`

## Smoke scenario

1. Given Hermes is Home on a device with notification access off
2. When the user opens Home
3. Then an overlay names the missing grants; after grant, a MessagingStyle post with store-text allowed appears as a card; Add widget places a host view on page 1

## Container map

| Layer | Path |
|-------|------|
| Logic | `vault/`, `oem/`, `widgets/`, `icons/` |
| View | `ui/onboarding/`, `ui/launcher/`, `ui/widgets/`, `ui/inbox/` |
| Tests | `src/test/.../vault/`, `oem/`, `widgets/`, `icons/` |
| Wiring | `MainActivity`, `HermesApp`, `HermesScreen` |

## Public API (locked)

| Symbol | Contract |
|--------|----------|
| `PermissionSnapshot` | live listener + battery + home-role + media flags |
| `VaultRepository` | persist-first; `visibleItems` Flow |
| `StatusBarNotificationMapper` | SBN → `PostedNotification` including MessagingStyle parts |
| `WidgetHostController` | allocate, in-app picker, bind, configure, persist, createView |
| `DockLayout.withApp` | long-press assign; DataStore round-trip |

## Tests

Automated: yes — mapper, grant policy, dock codec, host bind-id rejection.

## Fallback validation

`python3 scripts/agent-run.py watch-agent-gates --once --autofix`

Why tests are not feasible: N/A for pure logic. Live picker is OP12 ADB smoke.

## Definition of Done

Overlay, wallpaper home, Room feed, grant sheet, MessagingStyle parts, working Add widget, dock assign. Sideload OP12 only.

## Notes

- Resize/drag is Sprint 11
- SQLCipher is Sprint 12
