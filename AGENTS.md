# Agent Router

1. **First read:** `docs/START_HERE.md`
2. **Cursor modes:** `docs/CURSOR_MODES.md` (Ask / Plan / Agent / Debug routing)
3. **Bootstrap mode:** `docs/INITIALIZATION_PROMPT.md`
4. **Reference mode:** `docs/FOR_AGENTS.md` + `TEMPLATE_INDEX.json`
5. **Task board:** `BUILD_PLAN.md` (Sequential before Parallel) — status: 🔲 open · ✅ done · ❌ blocked
6. **Parallel dispatch:** parallel-first BUILD_PLAN; `/build` automates HUMAN/ADB first, backlogs failures to `HUMAN_BACKLOG.md`, never halts on human labels — `scripts/build-sprint-status.sh --lane child`
7. **Living memory:** update `AGENT_MEMORY.md` only at milestone boundaries

> Legacy `.cursorrules` is deprecated. Use `.cursor/rules/*.mdc` and this file instead.

## Architecture Constraints

- Pure FOSS under MIT license; no proprietary closed-source SDKs in production path
- Max 300 lines per static data file (UI + i18n), 150 lines per pure logic file
- Strict type safety and runtime validation at all data boundaries
- Core business logic decoupled from layout framework (MVVM / Clean / Hexagonal)
- Opt-in only telemetry; GDPR/CCPA compliant

## Coding Style

- Conventional Commits for all changes
- Small, modular functions; keep files within token-optimal size
- Read-before-write: inspect types/interfaces via `@filename` before editing
- Cursor mode routing per `docs/CURSOR_MODES.md`; Plan for non-trivial tasks with resolved `### Critique` (Issue→Resolution baked into the plan body)

## Session Protocol

- On session start: read `START_HERE.md`, pick mode via `docs/CURSOR_MODES.md`, then `BUILD_PLAN.md` Sequential lane
- On milestone end: update `AGENT_MEMORY.md`, append to `DECISION_LOG.md` or `docs/adr/`
- On 3-strike failure: halt and escalate to human
- On context bloat: write `.cursor-session-state`, ask human to clear chat
- Sprint 2+ features: after each AGENT step run `scripts/watch-agent-gates.sh --once --autofix` (see `docs/FEATURE_MODULES.md`)
- Destructive operations require `[HUMAN]` approval (see `.cursor/rules/destructive-ops.mdc`)
- Repo hygiene: track source only; run `scripts/check-repo-hygiene.sh` before push (see `docs/REPO_HYGIENE.md`)
- Log significant agent actions in `DECISION_LOG.md` at milestone boundaries

## Module Activation

Activate only the modules matching your stack. See `modules/*/MODULE.md`.

## Cursor FOSS integrations

Shipped in template (see `docs/CURSOR_INTEGRATIONS.md`):

- **Hooks** — `.cursor/hooks.json` enforces destructive-ops + UTF-8 (fail-open; `/push` session override)
- **Skills (7)** — `.cursor/skills/` progressive-load companions for `/gates`, `/scope`, `/fix`, hygiene, Sprint 0, features, canvas status
- **Subagents (3)** — `.cursor/agents/` verifier, gate-fixer, explorer
- **Local compute first** — `.cursor/rules/local-compute.mdc`: This Computer + parallel Task/worktrees/`/best-of-n` before Cloud; multi-core bootstrap checks
- **Worktrees** — `.cursor/worktrees.json` + fail-soft OS setup (`/worktree`, `/best-of-n`)
- **Auto-review** — `.cursor/permissions.json` dual layer with hooks
- **Plugin pack** — `.cursor-plugin/plugin.json` + `scripts/pack-cursor-plugin.*` → `dist/cursor-plugin/`
- **CLI (opt-in)** — `docs/CURSOR_CLI.md` + `.github/workflow-examples/cursor-agent.yml`
- **Optional MCP** — copy `.cursor/mcp.foss.example` → gitignored `.cursor/mcp.json`

Validate: `python3 scripts/agent-run.py check-cursor-hooks -- --smoke`, `python3 scripts/agent-run.py check-cursor-integrations -- --tier foss`

## Cursor Commercial integrations

Hidden on FOSS bootstrap (`distribution_tier: foss` in `.cursor/stack-selection.json`). When `--distribution-tier commercial`:

- `commercial-compliance.mdc` replaces active FOSS compliance rule
- Activate Cloud/Bugbot/MCP via `docs/CURSOR_COMMERCIAL_ACTIVATION.md`
- Android proprietary patterns: `modules/android/COMMERCIAL.md`

Router reads `distribution_tier` from `.cursor/stack-selection.json` (set by `init-project.sh`).

## Ecosystem-Specific Rules

- **Android:** FOSS only; reproducible builds with `SOURCE_DATE_EPOCH`
- **Web/PWA:** Offline-first service workers; Lighthouse budget gates
- **Python:** Strict typing (mypy), ruff lint/format, locked dependencies
- **Lightroom:** Adobe SDK Lua API only (`Lr*` namespaces)
