# Feature: home-snap-inbox

> Sprint 54. Home on any workspace page snaps to Inbox. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: Home while already on Hermes (Podcasts, News, or desktop) snaps to Inbox; Home from another app restores the last workspace page; a second Home on Inbox still opens All Apps search
- ✅ Offline/error behavior: missing workspace, a non-MAIN intent, or Home from another app leaves the current page
- ✅ Accessibility: Inbox page description stays `launcher_page_feed`
- ✅ i18n: N/A (no new strings)

## Smoke scenario

1. Given Hermes is Home showing News, press Home — the workspace snaps to Inbox
2. Given they open Chrome from News without pressing Home first, then press Home — News is restored

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

Home while already on Hermes snaps to Inbox. Home from other apps keeps the last page. Inbox Home-again search is unchanged.

## Notes

- Do not restore `MainActivity` as HOME
- Do not send `KEYCODE_HOME` from Inbox
