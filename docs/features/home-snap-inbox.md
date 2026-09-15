# Feature: home-snap-inbox

> Sprint 54. Home on any workspace page snaps to Inbox. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Home from Podcasts, News, or a desktop page snaps to Inbox; a second Home on Inbox still opens All Apps search
- ✅ Offline/error behavior: missing workspace or a non-MAIN intent leaves the current page
- ✅ Accessibility: Inbox page description stays `launcher_page_feed`
- ✅ i18n: N/A (no new strings)

## Smoke scenario

1. Given Hermes is Home showing News, Podcasts, or a widget page
2. When the user presses Home
3. Then the workspace snaps to Inbox without opening All Apps

## Container map

| Layer | Path |
|-------|------|
| Logic | `l3/HomeAgainSearch.kt` `shouldSnap` |
| View | `HermesWorkspace.moveToDefaultScreen`, `HermesLauncherActivity.onNewIntent` |
| Tests | `l3/L3SettingsLogicTest.kt` `HomeAgainSearchTest` |
| Wiring | `HermesLauncherActivity.onNewIntent` |

## Tests

- Automated: yes — `HomeAgainSearch.shouldSnap`

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

## Definition of Done

Home from any workspace page lands on Inbox. Inbox Home-again search is unchanged.

## Notes

- Do not restore `MainActivity` as HOME
- Do not send `KEYCODE_HOME` from Inbox
