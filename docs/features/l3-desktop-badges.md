# Feature: l3-desktop-badges

> Sprint 53. Look badge color tints L3 desktop notification dots. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Look badge color is applied to BubbleTextView notification dots on workspace and dock
- ✅ Offline/error behavior: missing color leaves the theme default; reflection failures are ignored
- ✅ Accessibility: show-dots still hides dots when off
- ✅ i18n: `look_badge_body`, `settings_show_dots_body`

## Smoke scenario

1. Given Look badge color is set and an app has a notification
2. When the user returns to desktop
3. Then the icon dot uses the Look color

## Container map

| Layer | Path |
|-------|------|
| Logic | `l3/L3Badge` |
| View | `L3Chrome` |
| Tests | `L3SettingsLogicTest` hideDot / paintsDesktopIcons |
| Wiring | `L3Live` already collects badgeColor |

## Tests

- Automated: yes — `L3Badge.hideDot` / `paintsDesktopIcons`

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Look badges paint L3 desktop dots. Settings copy says so.

## Notes

- L3 still draws a dot, not a numeral. COUNTS remains Compose UnreadDot.
