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

> **Template maintainer:** No active AGENT sprint — **maintenance + human open items** below. **Child repos:** copy the playbook.

---

## Template Maintainer — Active Board

> **v0.15.2** archived in `COMPLETED_TASKS.md` @ `634d06d`. **v0.15.0** archived in `COMPLETED_TASKS.md` @ `2e010ae`. **M33** archived in `COMPLETED_TASKS.md` @ `5d2d129`. **v0.14.1** archived in `COMPLETED_TASKS.md` @ `a6c6be1`. **M32** archived in `COMPLETED_TASKS.md` @ `e532c20`. **M31** archived in `COMPLETED_TASKS.md` @ `cd21e5a`. **v0.14.0** @ `4b94298`. **v0.13.2** @ `ff8e4e6`. **M19–M30** archived in `COMPLETED_TASKS.md`. **M18** @ `d6b92a2`. **M30** @ `508a541`.

### Sequential — Branding kit (template)

1. ✅ [AGENT] Ship `branding/` kit (assets, product.json, voice, BRANDING.md) + `official-colors.css` via sync
2. ✅ [AGENT] Extend `scripts/sync-design-tokens.py` + `scripts/generate-project-readme.py` (mode-gated pitch README)
3. ✅ [AGENT] Wire cohesion / validate-bootstrap / readme-health + DESIGN_GUIDE / init / MODULE docs

> Shared `branding/` and `design-tokens/` remain **Sequential-only** (see `docs/PARALLEL_AGENT_SCOPES.md`).

### Open (human judgment only)

- 🔲 [HUMAN] Quarterly review of `CURSOR_RADAR_REPORT.md` / backlog (top items → BUILD_PLAN)
- 🔲 [HUMAN] Visual review of `branding/generated/README.preview.md` and logo suite on GitHub

*Recurring maintenance: see **Ongoing Maintenance** below.*

---

## Child Repo Playbook (copy after Use this template)

> Init scripts, feature docs (`docs/features/_template.md`), and About + Settings exemplars ship with the template. Mirror the Sequential + Parallel lane structure from Sprint M9 when customizing.

### Sprint 0 — Template Customization

#### Sequential

1. 🔲 [AGENT] Run `scripts/init-project.sh` or `scripts/init-project.ps1` (`--stack <name>`; `--non-interactive` with `--project-name` + `--purpose` for scripted init)
1b. 🔲 [AGENT] Fill `branding/product.json` (set `mode: product`), replace logos if needed, run `sync-design-tokens.py` + `generate-project-readme.py`
2. 🔲 [AGENT] Run `scripts/setup-github-repo.sh` (requires `gh` auth with admin)
3. 🔲 [AUTO] Sprint 0 sign-off (all green on `main`):
  - `validate-bootstrap.sh --quick`
  - `feature-gate.sh --stack <active>`
  - `check-github-ci.sh --wait 300`
  - `check-license-compliance.sh` (after `npm ci` / `uv sync`)

#### Parallel (safe after Sequential step 5)

<!-- parallel_exception: Sprint 0 — stack not selected; Parallel rows added after init -->

| Task                                  | Owner | Isolated scope |
| ------------------------------------- | ----- | -------------- |
| *None — see parallel_exception above* | —     | —              |
#### Human & device (after automation)

> Address after `/build` completes AGENT/AUTO work above. `/build` attempts each row via automation; failures land in `HUMAN_BACKLOG.md`.

1. 🔲 [HUMAN] Click **Use this template** on GitHub to create your project repo

1a. 🔲 [HUMAN] Choose **distribution tier** (FOSS default vs Commercial) via `init-project.sh --distribution-tier`
2. 🔲 [HUMAN] Fill placeholders in `docs/INITIALIZATION_PROMPT.md` (platform, purpose)
2a. 🔲 [HUMAN] Pick Cursor mode per `[docs/CURSOR_MODES.md](docs/CURSOR_MODES.md)` (Ask to explore, Plan for architecture)
2b. 🔲 [HUMAN] Bookmark `[docs/help/BATCH_COMMANDS.md](docs/help/BATCH_COMMANDS.md)` — type `/` in Agent chat (`/bootstrap` for Sprint 0)

### Sprint 1 — Golden Path Foundation

#### Sequential

1. 🔲 [AGENT] Lock shared Golden Path schema/types/API for active stack (About + navigation surface only)

#### Parallel (safe after Sequential step 1)

| Task                 | Owner | Isolated scope               |
| -------------------- | ----- | ---------------------------- |
| About screen verify  | AGENT | `examples/{stack}/**/about/` |
| Stack public assets  | AGENT | `examples/{stack}/public/`   |
| Module + design docs | AGENT | `modules/{stack}/`           |
#### Human & device (after automation)

> Address after `/build` completes AGENT/AUTO and Parallel work above.

1. 🔲 [HUMAN] Fill stack-local config: web `examples/web/public/app-update.json` + `donations.json`; Android `assets/` mirrors; or root `.app-update.json` / `donations.json` (init runs `scripts/sync-stack-config.py`)
2. 🔲 [HUMAN] Approve ADR-0001 and BUILD_PLAN Sprint 1 for your stack

### Sprint 2+ — Incremental Features

> One vertical slice at a time. See `docs/FEATURE_MODULES.md`. Reference exemplars: `docs/features/settings.md` (Sprint 2), About (Sprint 1).

**Agent rule:** After every `[AGENT]` step → `bash scripts/watch-agent-gates.sh --once --autofix --step <scaffold|tests|wire>`.

#### Per-feature Sequential (steps 1–2: lock API)

1. 🔲 [AGENT] Copy `docs/features/_template.md` → `docs/features/{name}.md`; refine acceptance criteria
2. 🔲 [AGENT] Scaffold feature container (public API boundary only)

#### Per-feature Parallel (safe after Sequential step 2)

| Task                      | Owner | Isolated scope                                                                    |
| ------------------------- | ----- | --------------------------------------------------------------------------------- |
| Logic + unit tests        | AGENT | `examples/{stack}/src/{feature}/` or stack equivalent                             |
| View + i18n               | AGENT | `examples/{stack}/src/components/` or `ui/{feature}/`, `locales/` / `strings.xml` |
| Feature spec + acceptance | AGENT | `docs/features/{feature}.md`                                                      |
| E2e / instrumented smoke  | AGENT | `examples/{stack}/e2e/` or `examples/{stack}/**/androidTest/`                     |
#### Per-feature Sequential (steps 3–4: after Parallel merge)

1. 🔲 [AGENT] Unit tests for feature pure logic (skip if Parallel agent completed)
2. 🔲 [AGENT] Wire view/adapter; composition root (`appBootstrap.ts` / `GoldenPathApp.kt`) ≤10 lines

#### Human & device (after automation)

> Optional product judgment after gates pass.

1. 🔲 [HUMAN] Optional product smoke after `[AUTO]` gate pass

> Gates (`watch-agent-gates.sh`) run Sequential-side after each AGENT step — not in Parallel.

---

## Ongoing Maintenance (recurring)

> **Template maintainer:** `bash scripts/run-maintainer-gates.sh` weekly (omit `--quick` for full CI wait).

### Weekly

- 🔲 [AUTO] `cursor-feature-radar.sh` (non-blocking; artifact in weekly-health-check)
- 🔲 [AUTO] `check-security-triage.sh --wait-ci 300` (Dependabot + CI + Scorecard)
- 🔲 [AGENT] Apply Dependabot bumps; triage Scorecard SARIF findings
- 🔲 [AUTO] CI matrix + Repo Hygiene + Feature Gate green on `main`

### Monthly

- 🔲 [AUTO] `simulate-template-upgrade.sh` (also in `weekly-health-check.yml`)
- 🔲 [AUTO] `check-license-compliance.sh` + SBOM on latest release
- 🔲 [AGENT] Review Dependabot auto-merge PRs (KB-007)

### Pre-release (every version)

- 🔲 [AUTO] `pre-release-gate.sh` + `run-maintainer-gates.sh` (includes `verify-branch-protection.sh`)
- 🔲 [AUTO] Release Please PR merged; CHANGELOG + manifest bumped

### Human (after automation)

> Product approvals after automated pre-release gates pass.

- 🔲 [HUMAN] Approve release tag when product-ready
- 🔲 [HUMAN] Quarterly Cursor feature radar backlog review (see Sprint M30)

---

## Archived Sprints

| Sprint                                                            | Status   | Archive                          |
| ----------------------------------------------------------------- | -------- | -------------------------------- |
| v0.15.2 release                                                   | Complete | `COMPLETED_TASKS.md` @ `634d06d` |
| v0.15.0 release                                                   | Complete | `COMPLETED_TASKS.md` @ `2e010ae` |
| M33 — Cursor 3.9–3.11 + local-first compute                       | Complete | `COMPLETED_TASKS.md` @ `5d2d129` |
| v0.14.1 release                                                   | Complete | `COMPLETED_TASKS.md` @ `a6c6be1` |
| M32 — Audit 2026-07-12                                              | Complete | `COMPLETED_TASKS.md` @ `e532c20` |
| v0.14.0 release                                                   | Complete | `COMPLETED_TASKS.md` @ `4b94298` |
| v0.13.2 release                                                   | Complete | `COMPLETED_TASKS.md` @ `ff8e4e6` |
| M31 — Audit 2026-07-01                                            | Complete | `COMPLETED_TASKS.md`             |
| M30 — Cursor FOSS integration + feature radar                     | Complete | `COMPLETED_TASKS.md` @ `508a541` |
| M19–M29 — Cursor modes, batch commands, maintain, v0.11.0 release | Complete | `COMPLETED_TASKS.md`             |
| v0.10.0 release (`36a02e4`)                                       | Complete | `COMPLETED_TASKS.md`             |
| M5–M18 maintainer sprints (seq + P2)                              | Complete | `COMPLETED_TASKS.md` @ `d6b92a2` |
| Child Sprint 2 starter scaffold                                   | Complete | `COMPLETED_TASKS.md`             |
| v0.9.0 release (`fd699bc`)                                        | Complete | `COMPLETED_TASKS.md`             |
