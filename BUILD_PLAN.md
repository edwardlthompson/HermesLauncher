# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · AUTO 0 · HUMAN 0 · ADB 0 · **0 open**
<!-- /remaining-tally -->

Live board for **Hermes Launcher**. Finished work: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).

**Who:** `AGENT` code · `HUMAN` person · `ADB` device · `AUTO` CI/scripts
**State:** 🔲 open · ✅ done · ❌ blocked — reason

Format: `🔲 [AGENT] Short task`. Sequential `[AGENT]` first. Parallel scopes: [`docs/PARALLEL_AGENT_SCOPES.md`](docs/PARALLEL_AGENT_SCOPES.md). `/build` tries HUMAN/ADB after automation; failures go to `HUMAN_BACKLOG.md`.

## Smoke gate (hard stop)

After every `[AGENT]` row: `python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope auto`

After the **last** `[AGENT]`/`[AUTO]` row in a sprint is ✅, do **not** start the next sprint until this exits 0:

```bash
python3 scripts/agent-run.py smoke-sprint --require

```

That command re-smokes **every** ✅ row: no errors or crashes, plus startup time and load order. Details: [`docs/SPRINT_SMOKE.md`](docs/SPRINT_SMOKE.md). Fail → leave the last row open or ❌; fix; re-run. `/gates` wrap-up includes the same check.

---

## Product

**Now:** AGENT/AUTO board empty. Next product work is a new sprint.

> **Sprint 54** archived in COMPLETED_TASKS.md @ `37225b4`.
> **Sprint 53** archived in COMPLETED_TASKS.md @ `ccd382d`.
> **Sprint 52** archived in COMPLETED_TASKS.md @ `ccd382d`.
> **Sprint 51** archived in COMPLETED_TASKS.md @ `ccd382d`.
> **Sprint 50** archived in COMPLETED_TASKS.md @ `5482f20`.
> **Sprint 49** archived in COMPLETED_TASKS.md @ `5482f20`.
> **Sprint 48** archived in COMPLETED_TASKS.md @ `5482f20`.

### Open PRs (synced)

> Auto-managed. Do not hand-edit rows inside the markers.

<!-- open-prs-sync:begin -->
_No open Dependabot or Release Please PRs._
<!-- open-prs-sync:end -->

### Template gaps (synced)

> Auto-managed Monday cron + `sync-template-gaps-build-plan`. Do not hand-edit inside markers. Plan-only — run `/upgrade` then name item numbers.

<!-- template-gaps-sync:begin -->
_No template gaps; .template-version matches upstream (or template maintainer N/A)._
<!-- template-gaps-sync:end -->

Done on this board: **Sprints 0–50** · **51** inbox feel · **52** inbox calm · **53** inbox-calm leftovers · **54** Home snap. Archive: `COMPLETED_TASKS.md`.

---

## Ongoing Maintenance

Not a checklist. GitHub Monday cron (`.github/workflows/weekly-health-check.yml`) already runs CI wait, security triage, parent template-gap BUILD_PLAN sync (this child board), open-PR BUILD_PLAN sync, radar, `update-deps` dry-run, Dependabot leftover list, and latest-release SBOM. Upgrade-sim stays on the template maintainer repo. `/ship` owns pre-release and the release tag.

Open Dependabot / Release Please PRs are mirrored into **Open PRs (synced)** above; child catch-up rows land in **Template gaps (synced)** — allowed board automation, not standing chore rows.

If Monday cron is red: Cursor Automation `weekly-maintain`. Do not put those chores back on this board. [`docs/CURSOR_AUTOMATIONS.commercial.md`](docs/CURSOR_AUTOMATIONS.commercial.md)

---

## Archive

Older sprints: [`COMPLETED_TASKS.md`](COMPLETED_TASKS.md).
### Local agent (This Computer)

Standing queue for This Computer. Rows: `🔲 [AGENT][LOCAL] … — scope: path`.

<!-- local-agent-lane:begin -->
- 🔲 [AGENT][LOCAL] Bump Media3 1.11.0 to 1.11.1 — scope: examples/android/ (news P2, 2026-11-13) https://developer.android.com/jetpack/androidx/releases/media3
- 🔲 [AGENT][LOCAL] Cut/update GitHub Release with signed Android APK/AAB — scope: examples/android/
<!-- local-agent-lane:end -->

### Cloud agent (Cursor Cloud)

Standing queue for Cursor Cloud. Rows: `🔲 [AGENT][CLOUD] … — scope: path`.

<!-- cloud-agent-lane:begin -->
_No cloud agent items._
<!-- cloud-agent-lane:end -->

<!-- argus-portfolio-chores:start -->
## Argus portfolio chores — Android release

Synced from argus-runner. **Agent must ship binaries**, not only docs.

| Status | Venue | Task |
|--------|-------|------|
| 🔲 | `[AGENT][LOCAL]` — scope: `examples/android/` | **Cut/update GitHub Release with signed Android APK/AAB** |

### Agent instructions (paste / follow)

```text
Repo: edwardlthompson/HermesLauncher
Goal: Latest GitHub Release must include a real Android binary (.apk and/or .aab).

Why: Latest tag may already have an older-named APK — rebuild from current main and attach assets that match the release version.

Steps:
1) Read AGENT.md + docs for the Android ship path. Prefer gradle under `examples/android/`.
2) Ensure signing is available (local keystore / CI secrets already used by this repo). Do not invent a new Play upload.
3) From `examples/android` (or repo root if that is the documented path):
     ./gradlew assembleRelease
     # and if the project ships bundles:
     ./gradlew bundleRelease
4) Locate outputs under app/build/outputs/apk/ and app/build/outputs/bundle/.
5) Create a new tag/release OR update the latest release:
     gh release list -R edwardlthompson/HermesLauncher --limit 3
     # Prefer the repo's normal /ship or Release Please flow if documented; otherwise:
     gh release create <TAG> --generate-notes -R edwardlthompson/HermesLauncher
     gh release upload <TAG> <path-to.apk> [<path-to.aab>] -R edwardlthompson/HermesLauncher --clobber
6) Expected artifact names: hermes-launcher-*-foss.apk (and .aab if assembleRelease produces one)
7) Verify:
     gh release view <TAG> -R edwardlthompson/HermesLauncher --json assets --jq ".assets[].name"
   Gate: at least one asset ends with .apk or .aab.
8) Mark this BUILD_PLAN row ✅ and note the tag + asset names in the commit/PR body.

Do not force-push main. Do not delete the repo. Prefer PR + release assets over committing binaries to git.
```

Releases page: https://github.com/edwardlthompson/HermesLauncher/releases
<!-- argus-portfolio-chores:end -->
