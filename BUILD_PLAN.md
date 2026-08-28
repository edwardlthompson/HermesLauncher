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

> **Template maintainer:** active AGENT sprint **M46** (`/allideas` backlog). Last ship **v0.25.0**. **Child repos:** copy the playbook.

---

## Template Maintainer — Active Board

> **v0.25.0** archived in COMPLETED_TASKS.md @ `7670444`. **M43/M42** AGENT rows archived; HUMAN/ADB leftovers remain. **v0.24.0** archived in COMPLETED_TASKS.md @ `c0f0dee`. **M41** AGENT/AUTO archived in COMPLETED_TASKS.md. **v0.23.0** @ `b85cd74`. **M40** archived in `COMPLETED_TASKS.md`. **v0.22.0** @ `9a18276`. **M39** archived in `COMPLETED_TASKS.md`. **v0.21.0** @ `1525cd6`. **M38** archived in `COMPLETED_TASKS.md`. **Coach / M37 / M36** archived in `COMPLETED_TASKS.md`. **v0.20.0** @ `b570f07`. **v0.19.0** archived in `COMPLETED_TASKS.md` @ `2bef8ac`. **v0.18.3** archived in `COMPLETED_TASKS.md` @ `013e688`. **v0.18.2** archived in `COMPLETED_TASKS.md` @ `7d46e68`. **M35 HUMAN** (Scorecard + Dependabot + radar) archived in `COMPLETED_TASKS.md`. **v0.18.1** archived in `COMPLETED_TASKS.md` @ `fe80fea`. **M35** AGENT rows archived in `COMPLETED_TASKS.md`. **v0.18.0** archived in `COMPLETED_TASKS.md` @ `3f0b5a3`. **M34** (prior-art thin steals) archived in `COMPLETED_TASKS.md`. **v0.17.0** archived in `COMPLETED_TASKS.md` @ `701cd24`. **v0.16.0** @ `90ce3db`. **v0.15.2** archived in `COMPLETED_TASKS.md` @ `634d06d`. **v0.15.0** archived in `COMPLETED_TASKS.md` @ `2e010ae`. **M33** archived in `COMPLETED_TASKS.md` @ `5d2d129`. **v0.14.1** archived in `COMPLETED_TASKS.md` @ `a6c6be1`. **M32** archived in `COMPLETED_TASKS.md` @ `e532c20`. **M31** archived in `COMPLETED_TASKS.md` @ `cd21e5a`. **v0.14.0** @ `4b94298`. **v0.13.2** @ `ff8e4e6`. **M19–M30** archived in `COMPLETED_TASKS.md`. **M18** @ `d6b92a2`. **M30** @ `508a541`.

> **M46** `/allideas` backlog open. **M45** `/ideas` round 2 done. **M44** `/ideas` ship hygiene done. **M43/M42** AGENT archived in COMPLETED_TASKS.md @ `7670444`. **M41** archived in COMPLETED_TASKS.md @ `c0f0dee`.

### M46 — /allideas command

1. ✅ [AGENT] Ship `/allideas` (uncapped idea dump) + `docs/help/ALLIDEAS.md` + batch registry; `/build --lane auto` reads this board

### M46 — /allideas template backlog

> Source: 2026-08-27 uncapped dump. One Sequential `[AGENT]` row per `/build`/`/feature` cycle. Crash-proxy DPIA stays on M43 (do not duplicate). After P0 (rows 1–5), Golden Path rows may `/scope` by non-overlapping `examples/{stack}/` paths.

#### Sequential — P0 ship honesty

1. ✅ [AGENT] P0: Deny `git push --force` even when the session only approved `git push` (hooks + tests)
2. ✅ [AGENT] P0: `resolve-tools.sh` adds `go` and `cargo` the same way as `gh` / `python`
3. ✅ [AGENT] P0: `agent-run.py` never selects System32/`bash` (WSL1)
4. ✅ [AGENT] P0: Upgrade simulation asserts Sacred files are never overwritten (child `AGENTS.md`)
5. ✅ [AGENT] P0: Plugin pack `version` tracks `.template-version`

#### Sequential — Golden Path parity

6. ✅ [AGENT] P1: Shared About/crash/donate JSON schema + contract tests
7. ✅ [AGENT] P1: Node Golden Path About + crash sanitize
8. ✅ [AGENT] P1: Python Golden Path About + crash sanitize
9. ✅ [AGENT] P1: Android vs web sanitizer fixture parity
10. ✅ [AGENT] P1: `verify-about-feature-gate` for rust/go/node/python
11. ✅ [AGENT] P1: Feature `_template.md` schema + fallback validation command
12. ✅ [AGENT] P1: i18n key parity web ↔ Android
13. ✅ [AGENT] P1: RTL + reduced-motion Playwright coverage
14. ✅ [AGENT] P1: WCAG contrast check on design tokens
15. ✅ [AGENT] P1: Keyboard-only e2e for About / Settings / Feedback
16. ✅ [AGENT] P1: CSP + Referrer-Policy + Permissions-Policy on web
17. ✅ [AGENT] P2: PWA share-target
18. ✅ [AGENT] P2: UnifiedPush Android FOSS
19. ✅ [AGENT] P2: Settings export/import as a file
20. ✅ [AGENT] P2: Lightroom one real `Lr*` export

#### Sequential — Agent UX

21. ✅ [AGENT] P1: `/ideas` waits for confirmation (no silent `do all`)
22. ✅ [AGENT] P1: `/build` refuses a second feature until gates pass
23. ✅ [AGENT] P1: Gate JSON → `render-gates-status` after `watch-agent-gates`
24. ✅ [AGENT] P1: Debug recipe uses `last-feature-gate.json` + 3-strike
25. ✅ [AGENT] P1: `/fix` prints strike/stage first
26. ✅ [AGENT] P1: Portability gate for command ↔ `docs/help` twins
27. ✅ [AGENT] P1: `/coach` dirty-Unreleased vs empty-board
28. ✅ [AGENT] P1: Session-start dirty Unreleased + next AGENT row
29. ✅ [AGENT] P1: Copilot/Cline adapter byte-budget
30. ✅ [AGENT] P1: `alwaysApply` rule audit
31. ✅ [AGENT] P2: `/compact` writes Unreleased + HUMAN rows into session state
32. ✅ [AGENT] P2: Parallel-lock GC
33. ✅ [AGENT] P2: Worktree GC
34. ✅ [AGENT] P2: `/tour` runs verify and interprets the first failure
35. ✅ [AGENT] P2: Glossary term linter

#### Sequential — Ship / CI

36. ✅ [AGENT] P1: actionlint + zizmor in `validate-bootstrap --quick`
37. ✅ [AGENT] P1: shellcheck all `scripts/*.sh`
38. ✅ [AGENT] P1: PSScriptAnalyzer on `*.ps1`
39. ✅ [AGENT] P2: hadolint
40. ✅ [AGENT] P2: markdownlint + yamllint
41. 🔲 [HUMAN] P2: Scorecard badge; keep `/ship --local` non-blocking on the live score
42. ✅ [AGENT] P2: REUSE / SPDX
43. ✅ [AGENT] P2: VEX with SBOM
44. ✅ [AGENT] P2: npm/uv attestation docs
45. ✅ [AGENT] P2: GitHub `settings.yml`
46. ✅ [AGENT] P2: Merge queue docs
47. ✅ [AGENT] P1: `/regress` fails if Pages has analytics
48. ✅ [AGENT] P1: README badge accuracy gate
49. ✅ [AGENT] P1: Release Please extra-files includes plugin.json version
50. ✅ [AGENT] P0: `/push` always runs Unreleased `--require-empty` before `git push`

#### Sequential — Local compute

51. ✅ [AGENT] P1: Gradle `--offline` after first worktree success
52. ✅ [AGENT] P1: Playwright cache-hash skip
53. ✅ [AGENT] P1: depsonar Gradle apply with Kotlin cap
54. ✅ [AGENT] P2: upd dry-run canvas
55. ✅ [AGENT] P1: Per-stack feature-gate timeout
56. ✅ [AGENT] P2: Check-name manifest instead of content-hash cache
57. 🔲 [AGENT] P2: Devcontainer Android cmdline-tools (no auto-license)
58. 🔲 [AGENT] P2: Optional Nix flake wrapping existing scripts only (not a second generator CLI)

#### Sequential — Security

59. 🔲 [AGENT] P1: Prompt-injection fixtures for crash/feedback text
60. 🔲 [AGENT] P1: Denylist unit tests for every line
61. 🔲 [AGENT] P2: Auto-review fixtures
62. 🔲 [AGENT] P2: Gitleaks baseline
63. 🔲 [AGENT] P2: Semgrep FOSS
64. 🔲 [AGENT] P1: `beforeMCPExecution` allowlist servers
65. 🔲 [AGENT] P1: Crash payload allowlist tests on every sanitizing stack

#### Sequential — Docs / community

66. 🔲 [AGENT] P2: FIRST_30_DAYS from health JSON
67. 🔲 [AGENT] P1: CONTRIBUTING agent edition
68. 🔲 [AGENT] P1: Issue form for template-upgrade Sacred/Canon/Mixed
69. 🔲 [AGENT] P2: Discussion template for `/ideas`
70. 🔲 [AGENT] P2: `/adr` command
71. 🔲 [AGENT] P2: Mermaid in generated README
72. 🔲 [AGENT] P2: OG / social preview from tokens
73. 🔲 [AGENT] P2: F-Droid screenshot dummy fail
74. 🔲 [AGENT] P2: Winget publish runbook
75. 🔲 [HUMAN] P2: CII Best Practices checklist (login + public badge)

### M45 — /ideas round 2

1. ✅ [AGENT] Health CI ignores Release Please branches; init installs commit-msg hook; `--quick` runs action-ref format; JSON writes stay LF
2. ✅ [AGENT] `/gates` status script; worktree `resolve-python`; Gradle patch apply; SBOM wait; plugin-pack CI
3. ✅ [AGENT] Rust/Go About + crash sanitize; F-Droid in Android feature-gate; Lightroom in stack waves
4. ✅ [AGENT] Winget stub validate; crash-proxy docs (DPIA HUMAN); encoding strict opt-in; radar AGENT stub; sandbox copy; `--force` denylist test; citation date-only

### M44 — /ideas ship hygiene

1. ✅ [AGENT] Stop Release Please patch bumps for `docs`/`chore`; keep `[Unreleased]` first on the RP PR
2. ✅ [AGENT] Skip worktree stack installs unless `ROOT_WORKTREE_PATH` is a different checkout
3. ✅ [AGENT] Ignore `.cursor-session-state.json`; Gradle pins on `update-deps` dry-run; Dependency Review check on RP PRs
4. ✅ [AGENT] `/gates` always reports via canvas skill; local `commit-msg` hook check (skip in CI)

### M43 leftovers (human/device)

1. 🔲 [HUMAN] Optional: install Ollama and point Cursor Models at `http://127.0.0.1:11434/v1` (`docs/LOCAL_MODELS.md`)
2. 🔲 [HUMAN] Crash-proxy GitHub App: DPIA before enable (`docs/CRASH_PROXY.md`)
3. 🔲 [ADB] Optional: Android SDK licenses + first AVD (`/emulator` or `just android-instrumented`)

### M42 leftovers (human only)

1. 🔲 [HUMAN] Optional: copy `.cursor/mcp.foss.example` → `.cursor/mcp.json` and restart Cursor
2. 🔲 [HUMAN] Optional: reduce Dependabot interval or disable automerge

### M41 leftovers (human only)

1. 🔲 [HUMAN] Watch repo Issues + add CODEOWNERS as collaborator; optional About smoke

### Open (human judgment only)

*🔲 [HUMAN] Optional product smoke: donate link + first-run (no popup) + version-change note (M40 leftover). Recurring maintenance: see **Ongoing Maintenance** below.*

---

## Child Repo Playbook (copy after Use this template)

> Init scripts, feature docs (`docs/features/_template.md`), and About + Settings exemplars ship with the template. Mirror the Sequential + Parallel lane structure from Sprint M9 when customizing.

### CRITICAL NOTES (phase transitions)

When **Sprint 0** ends: stop re-reading `docs/INITIALIZATION_PROMPT.md` as the daily driver. `/feature` expects a copied `docs/features/{name}.md` from `_template.md`, a locked public API, then Parallel logic/view slices. Copy `scratchpad.md.example` → `scratchpad.md` (gitignored) and **reset** it on sprint/phase change — do not replace `AGENT_MEMORY.md`.

### Sprint 0 — Template Customization

#### Sequential

1. 🔲 [AGENT] Run `scripts/init-project.sh` or `scripts/init-project.ps1` (`--stack <name>`; `--non-interactive` with `--project-name` + `--purpose` for scripted init)
1b. 🔲 [AGENT] Fill `branding/product.json` (set `mode: product`), replace logos if needed, run `sync-design-tokens.py` + `generate-project-readme.py`
2. 🔲 [AGENT] Run `scripts/setup-github-repo.sh` (requires `gh` auth with admin)
3. 🔲 [AUTO] Sprint 0 sign-off (all green on `main`):
  - `validate-bootstrap.sh --quick`
  - `feature-gate.sh --stack <active>`
  - `check-github-ci.sh --wait 300` (required: **CI**, **Security Scan**, **CodeQL**; **CI** must include **Template Upgrade Simulation (Windows)**, **Repo Hygiene**, **Feature Gate**)
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
- 🔲 [AGENT] `/update-deps` locally; triage leftover Dependabot PRs and Scorecard SARIF
- 🔲 [AUTO] CI matrix + Repo Hygiene + Feature Gate green on `main`

### Monthly

- 🔲 [AUTO] `simulate-template-upgrade.sh` (also in `weekly-health-check.yml`)
- 🔲 [AUTO] `check-license-compliance.sh` + SBOM on latest release
- 🔲 [AGENT] Review Dependabot auto-merge PRs (KB-007)

### Pre-release (every version)

- 🔲 [AUTO] `pre-release-gate.sh --local` before push; full `pre-release-gate.sh` + `run-maintainer-gates.sh` after (`verify-branch-protection.sh`)
- 🔲 [AUTO] Release Please PR merged; CHANGELOG + manifest bumped

### Human (after automation)

> Product approvals after automated pre-release gates pass.

- 🔲 [HUMAN] Approve release tag when product-ready
- 🔲 [HUMAN] Quarterly Cursor feature radar backlog review (next due 2026-11-15; last pass 2026-08-15)

---

## Archived Sprints

| Sprint                                                            | Status   | Archive                          |
| ----------------------------------------------------------------- | -------- | -------------------------------- |
| v0.25.0 Local-first deps and resource packing                     | Complete | `COMPLETED_TASKS.md` @ `7670444` |
| M43 — Local resource packing                                      | Complete | `COMPLETED_TASKS.md`             |
| M42 — Local-first dependency updater                              | Complete | `COMPLETED_TASKS.md`             |
| v0.24.0 Privacy-first GitHub feedback                             | Complete | `COMPLETED_TASKS.md` @ `c0f0dee` |
| M41 — Privacy-first GitHub crash and feedback                     | Complete | `COMPLETED_TASKS.md`             |
| v0.23.0 Continuum donations and updates                           | Complete | `COMPLETED_TASKS.md` @ `b85cd74` |
| M40 — Donations and updates (Continuum method)                    | Complete | `COMPLETED_TASKS.md`             |
| v0.22.0 Android same-resolution high-refresh                      | Complete | `COMPLETED_TASKS.md` @ `9a18276` |
| M39 /ideas Windows PATH + ship hygiene                            | Complete | `COMPLETED_TASKS.md`             |
| v0.21.0 Windows PATH + Unreleased fold                            | Complete | `COMPLETED_TASKS.md` @ `1525cd6` |
| M38 /ideas ship-hardening                                         | Complete | `COMPLETED_TASKS.md`             |
| Coach / M37 / M36 (stale ✅ active-board rows)                     | Complete | `COMPLETED_TASKS.md`             |
| v0.20.0 first-run backlog + Windows upgrade-sim                   | Complete | `COMPLETED_TASKS.md` @ `b570f07` |
| v0.19.0 portable first-run release                                | Complete | `COMPLETED_TASKS.md` @ `2bef8ac` |
| v0.18.3 Compose BOM release                                       | Complete | `COMPLETED_TASKS.md` @ `013e688` |
| v0.18.2 Scorecard + Dependabot release                            | Complete | `COMPLETED_TASKS.md` @ `7d46e68` |
| M35 HUMAN — Scorecard + Dependabot + radar                        | Complete | `COMPLETED_TASKS.md`             |
| v0.18.1 Windows Python resolver release                           | Complete | `COMPLETED_TASKS.md` @ `fe80fea` |
| M35 — Audit 2026-08-15                                            | Complete | `COMPLETED_TASKS.md`             |
| v0.18.0 prior-art thin steals release                             | Complete | `COMPLETED_TASKS.md` @ `3f0b5a3` |
| M34 — Prior-art thin steals                                       | Complete | `COMPLETED_TASKS.md`             |
| v0.17.0 branding kit release                                      | Complete | `COMPLETED_TASKS.md` @ `701cd24` |
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
