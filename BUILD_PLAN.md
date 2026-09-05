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

> **Feedback fixes (audit 2026-09-03)** archived in `COMPLETED_TASKS.md`.

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

### Sprint 9 — Inbox filters

> **Sprint 9** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 10 — Feeds and player

> **Sprint 10** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 11 — Resize and icon packs

> **Sprint 11** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 12 — SQLCipher

> **Sprint 12** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Sprint 13 — Pixel widgets and grouped inbox

> **Sprint 13** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Sprint 14 — User widget grid

> **Sprint 14** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Sprint 15 — Launcher3-look home chrome

> **Sprint 15** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Sprint 16 — Inbox views, dock usage, All Apps

> **Sprint 16** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Sprint 17 — Icons, search, gestures, FOSS wallpapers

> **Sprint 17** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

---

### Nova parity board (Sprints 18–27)

> Specs: `docs/features/workspace-screens.md` through `backup-opml.md`. Launcher3 Apache-2.0 algorithms in Compose; do not vendor Java. Skip Nova #16, web search, Discover overlay.

### Critique

| Issue | Resolution |
|-------|------------|
| Null/empty screen id | `homeScreenId` falls back to first INBOX; codec corrupt → defaults (`workspace-screens.md`) |
| Network timeout | N/A for pager/SAF; contacts IO empty on deny (`local-search.md`) |
| Race | one DataStore host mutex; dock nestedScroll vs workspace pager (`hotseat-dock.md`) |
| Unhandled exceptions | SAF `runCatching`; pinch no `popBack`; remap never writes foreign `appWidgetId` (`backup-opml.md`) |
| `StoreGrant` null-on-deny | blacklist inserts `storeContent=false` (`all-apps-drawer.md`) |
| Line caps | extract `WorkspacePager` before Sprint 18; settings hub not one file |
### Parallelization

- Sequential lock: Sprint 18 `workspace/` types + v5 codec + `homeScreenId`
- `agent_count_target`: 3 after each sprint lock
- Dry-run: `python3 scripts/agent-run.py plan-parallel-dispatch --draft BUILD_PLAN.md --suggest` — three non-overlapping Android prefixes per sprint

### Sprint 18 — Workspace screens

> **Sprint 18** archived in COMPLETED_TASKS.md @ `497f7f5`.

---

### Sprint 19 — CellLayout icons

> **Sprint 19** archived in COMPLETED_TASKS.md (2026-09-02).

### Sprint 20 — PagedView motion

> **Sprint 20** archived in COMPLETED_TASKS.md (2026-09-02).

### Sprint 21 — Hotseat dock

> **Sprint 21** archived in COMPLETED_TASKS.md (2026-09-02).

### Sprint 22 — All Apps drawer + blacklist

> **Sprint 22** archived in COMPLETED_TASKS.md (2026-09-02).

### Sprint 23 — Folders

> **Sprint 23** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 24 — Local search

> **Sprint 24** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 25 — Gestures

> **Sprint 25** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 26 — Look and feel

> **Sprint 26** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 27 — Backup and OPML

> **Sprint 27** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 28 — AOSP Launcher3 homescreen

> **Sprint 28** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 29 — Workspace pages and Nova settings

> **Sprint 29** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 30 — Home search, swipe sensitivity, live settings

> **Sprint 30** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 31 — Lite article reader

> Spec: `docs/features/article-reader.md`. Seed Android Authority; reading-mode viewer; preferred opener is launcher reader or browser.

> **Sprint 31 AGENT+ADB** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 32 — Feed inbox chrome

> Spec: `docs/features/feed-inbox.md`. News uses the inbox top bar; thumbnails; read/star filters; 30-day unstarred purge.

> **Sprint 32** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 33 — Full-res article cache and reader pager

> Spec: `docs/features/feed-inbox.md`. List keeps sampled thumbs; reader caches original images; Unread filter survives the reader; next/previous arrows.

> **Sprint 33** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 34 — Feed refresh and reader settings

> Spec: `docs/features/feed-reader-settings.md`. Inventory from Feeder (GPL ideas only). News refresh; scan interval; mobile data; charging; thumbnails.

> **Sprint 34** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 35 — Reader tray chrome, dates, Nova import

> Spec: `docs/features/feed-reader-layout.md`. Prev/next on the News tray; `YY/MM/DD` dates; Inbox settings icon; Nova `.novabackup` onboarding.

> **Sprint 35** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 36 — Reader modes, thumbs, retention

> Spec: `docs/features/feed-reader-modes.md`. Sort stays at list top; refresh fills article images; Reading/Full/Web chips; settings status-bar inset + hub colors; 30-day keep and 24h read purge; Feeder gap inventory.

> **Sprint 36** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 37 — Reader tools and one-res images

> Spec: `docs/features/feed-reader-tools.md`. Share; TTS/find/scale in overflow; Custom Tabs; original-only thumbs.

> **Sprint 37** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 38 — Feeds bubble drawer

> Spec: `docs/features/feed-list-actions.md`. Feeds bubble + drawer; Filter funnel stays status/sort; second seed feed.

> **Sprint 38** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 39 — JSON Feed, styled extract, prefetch

> Spec: `docs/features/feed-fulltext-json.md`.

> **Sprint 39** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 40 — Subscriptions

> Spec: `docs/features/feed-subs.md`.

> **Sprint 40** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 41 — Feed alerts

> Spec: `docs/features/feed-alerts.md`.

> **Sprint 41** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 42 — Background sync and widget

> **Sprint 42** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 43 — Podcast import and OPML

> **Sprint 43** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 44 — Podcasts workspace page

> **Sprint 44** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 45 — AntennaPod must-close player

> **Sprint 45** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 46 — Permissions onboarding, About, zero-inbox copy

> **Sprint 46** archived in COMPLETED_TASKS.md @ `497f7f5`.

### Sprint 47 — Launcher3 live widgets

> **Sprint 47** archived in COMPLETED_TASKS.md @ `497f7f5`.

---

## Ongoing Maintenance (recurring)

> Child repo weekly: Dependabot alerts + `check-github-ci.sh` after push.

### Weekly

- ✅ [AUTO] `check-security-triage.sh --wait-ci 300` (Dependabot + CI) — green on `b68a43a` / v1.0.0 (2026-09-05)

### Monthly

> License/SBOM check archived 2026-09-04.

### Pre-release (every version)

- ✅ [HUMAN] Approve release tag when product-ready

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
| Sprint 18 — Workspace screens | 2026-09-02 | `COMPLETED_TASKS.md` |
| Sprint 19 — CellLayout icons | 2026-09-02 | `COMPLETED_TASKS.md` |
| Sprint 20 — PagedView motion | 2026-09-02 | `COMPLETED_TASKS.md` |
| Sprint 21 — Hotseat dock | 2026-09-02 | `COMPLETED_TASKS.md` |
| Sprint 22 — All Apps drawer + blacklist | 2026-09-02 | `COMPLETED_TASKS.md` |
| Sprint 27 — Backup and OPML | 2026-09-03 | `COMPLETED_TASKS.md` |
| Sprint 29 — Workspace pages and Nova settings | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 26 — Look and feel | 2026-09-03 | `COMPLETED_TASKS.md` |
| Sprint 25 — Gestures | 2026-09-03 | `COMPLETED_TASKS.md` |
| Sprint 24 — Local search | 2026-09-03 | `COMPLETED_TASKS.md` |
| Sprint 23 — Folders | 2026-09-03 | `COMPLETED_TASKS.md` |
| Sprint 32 — Feed inbox chrome | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 33 — Full-res article cache and reader pager | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 34 — Feed refresh and reader settings | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 35 — Reader tray chrome, dates, Nova import | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 36 — Reader modes, thumbs, retention | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 37 — Reader tools and one-res images | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 38 — Feeds bubble drawer | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 39 — JSON Feed, styled extract, prefetch | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 40 — Subscriptions | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 41 — Feed alerts | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 42 — Background sync and widget | 2026-09-04 | `COMPLETED_TASKS.md` |
| Sprint 43 — Podcast import and OPML | 2026-09-05 | `COMPLETED_TASKS.md` |
| Sprint 44 — Podcasts workspace page | 2026-09-05 | `COMPLETED_TASKS.md` |
| Sprint 45 — AntennaPod must-close player | 2026-09-05 | `COMPLETED_TASKS.md` |
| Sprint 46 — Permissions onboarding, About, zero-inbox copy | 2026-09-05 | `COMPLETED_TASKS.md` |
| Sprint 47 — Launcher3 live widgets | 2026-09-05 | `COMPLETED_TASKS.md` |
Template maintainer history (v1.0.0 and earlier) remains in `COMPLETED_TASKS.md` from the seed clone.
