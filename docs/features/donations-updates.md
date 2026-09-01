# Feature: donations-updates

> Continuum Calendar method: quiet Venmo donate, one optional note after a version change, and a silent daily GitHub installer check.

## Acceptance criteria

- ✅ Quiet **Donate via Venmo** in About and the app menu; never on the update/install dialog
- ✅ First run records the installed version with no donate popup
- ✅ After a later launch where the installed version changed: one optional note (Donate via Venmo | Not now); either button records “seen this version”
- ✅ Once per 24 hours, fetch `https://api.github.com/repos/OWNER/REPO/releases/latest` (User-Agent + 10s timeout); compare product installer filenames, not git/template tags
- ✅ Newer matching asset and not dismissed: **Install** | **Later**; Install opens the asset URL (fallback: release page); Later silences that version
- ✅ Failed fetch, timeout, empty assets, or same version: stay silent; never block the app
- ✅ Donate prefs and last-check timestamps are device-local (not peer-synced; Android Auto Backup excludes `gp_updates`)
- ✅ No dark patterns: no fake close, no guilt copy, no paywalling updates
- ✅ Offline/error: no network required for donate links or first-run version record
- ✅ Accessibility: dialogs are `role="dialog"` with labelled buttons; donate is a real link/button
- ✅ i18n: `about.donate*`, `about.not_now`, `about.update.install`, `about.update.later`

## Smoke scenario

1. Given a fresh install, the app records the version and does not show a donate note
2. When the installed version changes on a later launch, the ethical reminder appears once
3. Then either button hides it until the next version change; a newer installer asset can show Install | Later separately

## Container map

| Layer | Web | Android |
|-------|-----|---------|
| Logic | `examples/web/src/about/productUpdate.ts` | `examples/android/.../about/ProductUpdate.kt` |
| Fetch/prefs | `githubRelease.ts`, `updatePrefs.ts`, `runAppUpdates.ts` | `GithubRelease.kt`, `UpdateLaunchPrefs.kt` |
| View | `AppShell.ts`, `AboutPanel.ts`, `launchPrompt.ts` | `ui/GoldenPathScreen.kt`, `ui/about/` |
| Tests | `productUpdate.test.ts`, `runAppUpdates.test.ts` | `ProductUpdateTest.kt` |
| Wiring | `appBootstrap.ts` ≤10 lines | `HermesApp.kt` ≤10 lines |
## Tests

- Automated: yes — `productUpdate.test.ts`, `runAppUpdates.test.ts`, `ProductUpdateTest.kt`

## Fallback validation

- Why tests are not feasible: N/A (automated tests exist)
- Command: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

## Definition of Done

See `docs/FEATURE_MODULES.md` per-feature checklist. Fallback validation: `python3 scripts/agent-run.py watch-agent-gates --once --autofix`.

## Notes

- `release_repo` `OWNER/REPO` is a no-op. Child init writes the real repo.
- Desktop asset: `{Prefix}-X.Y.Z-x64-setup.exe`. Android: `{prefix}-X.Y.Z-foss.apk`.
- After each AGENT step: `bash scripts/watch-agent-gates.sh --once --autofix`
