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

#### Sequential

1. ✅ [AGENT] Clone vanilla agent-project-bootstrap into this workspace and rename `origin` to `template`
2. ✅ [AGENT] Run `scripts/init-project.ps1` (`-Stack android`, MIT, prune unused stacks)
3. ✅ [AGENT] Identity lock: `org.hermeslauncher.app`, `HermesTheme` / `HermesScaffold`, hardcoded script paths
4. ✅ [AGENT] Write `docs/spec.md`, ADRs 0001-0004, `docs/THREAT_MODEL.md`, branding, BUILD_PLAN, AGENTS card
5. ✅ [AUTO] Sprint 0 sign-off: `validate-bootstrap.sh --quick`, encoding, `check-build-plan-parallel.sh`; Android `./gradlew test` green on Windows JDK 17

#### Parallel (safe after Sequential step 4)

<!-- agent_count_target: 3 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Android strings and About assets | AGENT | `examples/android/app/src/main/res/**` |
| Gate script package paths | AGENT | `scripts/lib/**` |
| Fastlane / metadata / Module A checklists | AGENT | `examples/android/metadata/**` |

#### Human & device (after automation)

1. ✅ [HUMAN] Create the GitHub repo `edwardlthompson/HermesLauncher` (agent automated via `gh`)
1a. ✅ [HUMAN] Distribution tier is FOSS (MIT)
2. ✅ [HUMAN] Approve ADRs 0001-0004 (plan approval 2026-09-01)
2a. ✅ [HUMAN] Plan Mode then Agent Mode for approved execution
2b. ✅ [HUMAN] Bookmark `docs/help/BATCH_COMMANDS.md` (Cursor user rule added)
3. ✅ [HUMAN] Enable Dependabot alerts, security updates, private vulnerability reporting (`setup-github-repo.sh`)
4. ✅ [ADB] SDK at `%LOCALAPPDATA%\Android\Sdk`, AVD `Medium_Phone_API_36.1`, two devices attached; `assembleDebug` succeeded

### Sprint 1 — Launcher shell

#### Sequential

1. ✅ [AGENT] Lock `HomePagerState`, `DockState`, `DrawerState`, `SwipePolicy` (`docs/features/launcher-shell.md`)
2. ✅ [HUMAN] Confirm ADR-0001 and ADR-0003 match the locked types
3. ✅ [AGENT] Implement HOME intent, pager (feed + 2 widget placeholders), dock stub, drawer stub, strings, tests

#### Parallel (safe after Sequential step 1)

<!-- agent_count_target: 3 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Pager + swipe-contract unit tests | AGENT | `examples/android/app/src/test/java/org/hermeslauncher/app/launcher/**` |
| Dock/drawer Compose | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/launcher/**` |
| Instrumented HOME smoke | AGENT | `examples/android/app/src/androidTest/**` |

#### Human & device (after automation)

1. ✅ [HUMAN] Fill `release_repo` in `.app-update.json` (`edwardlthompson/HermesLauncher`)
2. 🔲 [ADB] Set Hermes as Home on an emulator or device and swipe pages

### Sprint 2 — Notification vault

#### Sequential

1. 🔲 [AGENT] Lock vault public API (`docs/features/notification-vault.md`)
2. 🔲 [AGENT] Scaffold Room types and mapper boundary only

#### Parallel (safe after Sequential step 2)

<!-- agent_count_target: 2 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Mapper + Room unit tests | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/vault/**` |
| X-dismiss card + i18n | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/inbox/**` |

### Sprint 3 — AppWidgetHost pages

#### Sequential

1. 🔲 [AGENT] Lock widget-host API (`docs/features/widget-pages.md`)

#### Parallel (safe after Sequential step 1)

<!-- agent_count_target: 2 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Host bind/persist logic | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/widgets/**` |
| Widget page Compose + i18n | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/widgets/**` |

### Sprint 4 — RSS and podcasts

#### Sequential

1. 🔲 [AGENT] Lock RSS/OPML/episode API (`docs/features/feeds.md`)

#### Parallel (safe after Sequential step 1)

<!-- agent_count_target: 2 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Feed parse + mix policy | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/feeds/**` |
| Media3 mini-player + i18n | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/player/**` |

### Sprint 5 — Dock, icon packs, customization

#### Sequential

1. 🔲 [AGENT] Lock dock/icon-pack/search API (`docs/features/launcher-chrome.md`)

#### Parallel (safe after Sequential step 1)

<!-- agent_count_target: 2 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Icon pack + search logic | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/icons/**` |
| Customization screens + i18n | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/customize/**` |

### Sprint 6 — OEM onboarding

#### Sequential

1. 🔲 [AGENT] Lock permission-repair API (`docs/features/oem-onboarding.md`)

#### Parallel (safe after Sequential step 1)

<!-- agent_count_target: 2 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| OEM detector + instructions | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/oem/**` |
| Repair banner + i18n | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/ui/onboarding/**` |

### Sprint 7 — F-Droid release polish

#### Sequential

1. 🔲 [AGENT] Lock release checklist (`docs/features/fdroid-release.md`)

#### Parallel (safe after Sequential step 1)

<!-- agent_count_target: 2 -->

| Task | Owner | Isolated scope |
| ---- | ----- | -------------- |
| Fastlane / metadata copy | AGENT | `examples/android/fastlane/**` |
| About donations + update stub verify | AGENT | `examples/android/app/src/main/java/org/hermeslauncher/app/about/**` |

---

## Ongoing Maintenance (recurring)

> Child repo weekly: Dependabot alerts + `check-github-ci.sh` after push.

### Weekly

- 🔲 [AUTO] `check-security-triage.sh --wait-ci 300` (Dependabot + CI)
- 🔲 [AGENT] `/update-deps` locally; triage leftover Dependabot PRs
- 🔲 [AUTO] CI + Repo Hygiene + Feature Gate green on `main`

### Monthly

- 🔲 [AUTO] `check-license-compliance.sh` + SBOM on latest release

### Pre-release (every version)

- 🔲 [AUTO] `pre-release-gate.sh --local` before push
- 🔲 [HUMAN] Approve release tag when product-ready

---

## Archived Sprints

Template maintainer history (v1.0.0 and earlier) remains in `COMPLETED_TASKS.md` from the seed clone.
