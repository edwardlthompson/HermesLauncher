# Build Plan

> Prioritized task board with owner labels. **Completed sprints:** `COMPLETED_TASKS.md`.

## Owner Label Legend

| Label   | Owner           | When to use                                                |
| ------- | --------------- | ---------------------------------------------------------- |
| `AGENT` | Cursor Agent    | Code, docs, scaffolding, tests, CI config                  |
| `HUMAN` | Human developer | Approvals, credentials, GitHub settings, product decisions |
| `ADB`   | Human (Android) | Android SDK, emulator/device testing, F-Droid submission   |
| `AUTO`  | CI/scripts/bots | GitHub Actions, Dependabot, pre-commit, update checker     |
## Status markers

Use **emoji markers** (not `- [ ]` GitHub checkboxes) so task state reads clearly in Markdown source and Preview. **Applies repo-wide** — `BUILD_PLAN.md`, module checklists, PR template, feature specs, and security triage.

| Marker | State   | Agent action                                                          |
| ------ | ------- | --------------------------------------------------------------------- |
| 🔲     | Open    | Default for new tasks; work or leave queued                           |
| ✅      | Done    | Replace 🔲 when complete; archive sprint rows to `COMPLETED_TASKS.md` |
| ❌      | Blocked | Replace 🔲 when blocked; add brief reason after the description       |
**Task format:** `🔲 [OWNER] Description` · done: `✅ [OWNER] Description` · blocked: `❌ [OWNER] Description — reason`

```bash
grep '\[AGENT\]' BUILD_PLAN.md
grep '\[HUMAN\]' BUILD_PLAN.md
grep '\[ADB\]' BUILD_PLAN.md
grep '\[AUTO\]' BUILD_PLAN.md

```

**Agent rule:** Execute all `[AGENT]` **Sequential** items first, then dispatch **Parallel** agents with isolated file scopes (`docs/PARALLEL_AGENT_SCOPES.md`). Shared schema/types are Sequential-only.

### Parallel dispatch protocol (orchestrator)

| Step | Action                                                                                                                                                                     |
| ---- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1    | Finish all `[AGENT]` **Sequential** items for the active sprint/feature (shared schema/types locked)                                                                       |
| 2    | **Discover** parallelizable work using the decomposition checklist below; add Parallel table rows with non-overlapping ``path/**`` scopes                                  |
| 3    | Run `bash scripts/plan-parallel-dispatch.sh` → read **agent_count**                                                                                                        |
| 4    | If `agent_count >= 2`, run `/scope` (auto Task dispatch); if `1`, execute inline; if `0`, run `--suggest` and expand the Parallel table (or document `parallel_exception`) |
| 5    | Sequential owner merges results, runs `watch-agent-gates.sh`, updates BUILD_PLAN (Parallel agents never edit BUILD_PLAN)                                                   |
**Decomposition checklist** (apply before finalizing Sequential items):

| Heuristic                     | Split into Parallel agents                                                                  |
| ----------------------------- | ------------------------------------------------------------------------------------------- |
| Multi-stack repo              | One agent per active module (`examples/{stack}/`**)                                         |
| Feature container (Sprint 2+) | Agent A: pure logic + unit tests; Agent B: view/Composable + i18n                           |
| Tests vs production code      | Separate `**/*.test.*`, `e2e/**`, `androidTest/**` when paths do not overlap implementation |
| Docs vs code                  | Agent A: `examples/**`; Agent B: `docs/**`, `modules/**`, `.cursor/rules/**`                |
| CI/gates vs app code          | Agent A: `scripts/**`, `.github/workflows/**`; Agent B: stack example tree                  |
**Default rule:** If a Sequential `[AGENT]` item touches two or more non-overlapping directory prefixes, **split it** — leave only schema-lock work Sequential.

**Planning (Plan Mode):** Every BUILD_PLAN proposal must include `### Parallelization` with `agent_count_target`, decomposition table, and dry-run from `plan-parallel-dispatch.sh`. Run `check-build-plan-parallel.sh` before human approval.

**Autonomous `/build`:** Runs all `[AGENT]`/`[AUTO]` and Parallel work first, then attempts the grouped **Human & device (after automation)** section via `scripts/attempt-build-plan-row.sh`. Success marks ✅; failure appends `HUMAN_BACKLOG.md` and continues — never halts on human labels. Humans review the grouped section (and backlog) after automation finishes. Status: `bash scripts/build-sprint-status.sh --json`.

> **Child repo:** Hermes Launcher. Upstream template history is archived below. Use this playbook.

---

## Child Repo Playbook

### CRITICAL NOTES (phase transitions)

When **Sprint 0** ends: stop re-reading `docs/INITIALIZATION_PROMPT.md` as the daily driver. `/feature` expects a copied `docs/features/{name}.md` from `_template.md`, a locked public API, then Parallel logic/view slices. Copy `scratchpad.md.example` → `scratchpad.md` (gitignored) and **reset** it on sprint/phase change — do not replace `AGENT_MEMORY.md`.

### Sprint 0 — Hermes seed

> **Sprint 0** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 1 — Launcher shell

> **Sprint 1** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 2 — Notification vault

> **Sprint 2** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 3 — AppWidgetHost pages

> **Sprint 3** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 4 — RSS and podcasts

> **Sprint 4** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 5 — Dock, icon packs, customization

> **Sprint 5** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 6 — OEM onboarding

> **Sprint 6** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 7 — F-Droid release polish

> **Sprint 7** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 8 — Live home

> **Sprint 8** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] Sideload OP12 (`b5214fc6` only); confirm overlay, wallpaper home, Add widget, dock assign

### Sprint 9 — Inbox filters

> **Sprint 9** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 10 — Feeds and player

> **Sprint 10** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 11 — Resize and icon packs

> **Sprint 11** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 12 — SQLCipher

> **Sprint 12** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] Confirm granted vault rows survive migration or show rebuild copy

---

### Sprint 13 — Pixel widgets and grouped inbox

> **Sprint 13** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] OP12 `b5214fc6` only: bind-deny, configure-cancel (no ghost), drag onto new page, visible width resize, group expand + per-item X + group X; logcat `HermesWidget` / `HermesVault`

---

### Sprint 14 — User widget grid

> **Sprint 14** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Sprint 15 — Launcher3-look home chrome

> **Sprint 15** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] OP12 `b5214fc6` only: empty long-press → Wallpaper / Widgets / Settings; wallpaper chooser; preview-card bind 2×2; four-handle resize; drop on Remove well; logcat `HermesWidget`

---

### Sprint 16 — Inbox views, dock usage, All Apps

> **Sprint 16** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] OP12 `b5214fc6` only: unread badge; search close; dismissed history; category/time; usage dock after notification-open; All Apps rail; shortcuts if Home; widget search

---

### Sprint 17 — Icons, search, gestures, FOSS wallpapers

> **Sprint 17** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] OP12 `b5214fc6` only: live widgets tick; All Apps fling; usage banner grant; prune not per-persist; unread dots; Home-again search; double-tap torch/lock; AOSP wallpaper chooser; Hermes Gradient/Clock live wallpaper

---

## Ongoing Maintenance (recurring)

> Child repo weekly: Dependabot alerts + `check-github-ci.sh` after push.

### Weekly

- ❌ [AUTO] `check-security-triage.sh --wait-ci 300` (Dependabot + CI) — origin CI red on `c2bb2c4` (Feature Gate / Node `setup-node` npm cache); local workaround unpushed
- ✅ [AGENT] `/update-deps` locally; triage leftover Dependabot PRs
- ❌ [AUTO] CI + Repo Hygiene + Feature Gate green on `main` — same origin CI failure; needs `[HUMAN]` push

### Monthly

- ✅ [AUTO] `check-license-compliance.sh` + SBOM on latest release

### Pre-release (every version)

- ✅ [AUTO] `pre-release-gate.sh --local` before push
- 🔲 [HUMAN] Approve release tag when product-ready

---

## Archived Sprints

| Sprint | Complete | Archive |
| ------ | -------- | ------- |
| Sprint 0 — Hermes seed | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 1 — Launcher shell | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 2 — Notification vault | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 3 — AppWidgetHost pages | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 4 — RSS and podcasts | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 5 — Dock, icon packs, customization | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 6 — OEM onboarding | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 7 — F-Droid release polish | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 8 — Live home | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 9 — Inbox filters | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 10 — Feeds and player | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 11 — Resize and icon packs | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 12 — SQLCipher | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 13 — Pixel widgets and grouped inbox | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 14 — User widget grid | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 15 — Launcher3-look home chrome | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 16 — Inbox views, dock usage, All Apps | 2026-09-01 | `COMPLETED_TASKS.md` |
| Sprint 17 — Icons, search, gestures, FOSS wallpapers | 2026-09-01 | `COMPLETED_TASKS.md` |
Template maintainer history (v1.0.0 and earlier) remains in `COMPLETED_TASKS.md` from the seed clone.
