# Agent shortcuts (cheat sheet)

Shortcut commands for Cursor Agent — type `/` in Agent chat to pick a recipe.

**Any other IDE** (Windsurf, Antigravity, Claude Code, Copilot, Aider, Cline): paste `Read docs/help/TOUR.md and walk me through it.` Slash commands are Cursor-only; the same recipes live in this `docs/help/` folder. See [`docs/AGENT_PORTABILITY.md`](../AGENT_PORTABILITY.md).

## 30-second start

1. Open **Agent** chat in Cursor.
2. Type **`/`** to open the command menu.
3. Pick a command (e.g. `/tour`, `/bootstrap`, `/verify`, `/build`).
4. The agent runs the workflow step by step.

Bookmark this page for when you come back after a break. **Print every command:** open [`batch-commands-print.html`](batch-commands-print.html) in a browser and use Print.

## Try these first (super commands)

| Command | When to use |
|---------|-------------|
| `/bootstrap` | Brand-new project — Sprint 0 setup, then a Welcome Tour (`/tour`) |
| `/tour` | 10-minute first-run walk (any IDE: `docs/help/TOUR.md`) |
| `/coach` | What to do next and why (health snapshot + 30-day playbook). Other IDEs: [`COACH.md`](COACH.md) |
| `/ideas` | Ranked backlog of in-scope next features (does not implement) |
| `/allideas` | Complete in-scope dump to fill BUILD_PLAN (does not implement until you say `board`) |
| `/verify` | After your changes, before opening a pull request |
| `/build` | Run BUILD_PLAN end-to-end — per-row gates only dirty stacks; wrap-up `/gates` is full |
| `/ship` | Publish a release to GitHub (runs checks, push, post-release) |
| `/maintain` | Weekly health pass — security, dependencies, full review |
**Worked example — new project:** clone your repo → open your agent → in Cursor type `/bootstrap` (elsewhere: ask it to follow `docs/help/TOUR.md` after init). The agent walks through init, stack setup, GitHub settings, validation gates, and `/tour`. Type `/coach` later for the next recommended action.

## When you need one step

Grouped by life moment (not every command — use `/` menu for the full list).

**Getting started:** `/tour` · `/init` · `/setup` · `/prune` · `/gates` · `/coach` · `/ideas` · `/allideas`

**Building:** `/plan` · `/adr` (next `docs/adr/` record) · `/feature` · `/fix` (gates failed after `/build`) · `/cleanup` (archive finished BUILD_PLAN rows) · `/scope` (parallel manifest + auto Task dispatch)

**Docs & checks:** `/docs` · `/ci` (CI poll only) · `/gates` (full local validation, including compute probe; always render the canvas status overview). **bootstrap-doctor** is an alias for the same maintainer/validate gates: `python3 scripts/agent-run.py validate-bootstrap --quick` or `python3 scripts/agent-run.py run-maintainer-gates`.

**Publishing:** `/update-deps` (local bumps) · `/prerelease` (autofix + optional Codex + `--local` gate) · `/push` (commit + push + release) · `/regress` (after release) · `/codex-review` (third-party review alone)

**Local hardware:** `/best-of-n` (worktree model race) · `/emulator` (optional AOSP GPU tests; skips without SDK)

**Maintenance:** `/triage` · `/update-deps` · `/dependabot` (GitHub leftover) · `/audit` (full repo review) · `/upgrade` (child: template gap plan only; this template: upgrade sim)

`/ship` runs `/update-deps` then `/prerelease` — one command for local bumps, autofix, optional Codex, local gates, push, and regress.

**Long sessions:** `/compact` (save checkpoint before clearing chat) · `/restore` (load checkpoint)

**Print this list:** open [`docs/help/batch-commands-print.html`](batch-commands-print.html) in a browser and use Print (Ctrl+P). Novice wording, every command, no PDF required.

## Before you publish

`/push` and `/ship` **push code to GitHub**. Only run them when you intend to publish. `/ship` is the full path (local dep update → pre-release checks → push → post-release verification). Use `/prerelease` alone if you want checks without pushing yet.

## Coming back after a break?

Same menu: type **`/`** in Agent chat. Supers like `/verify` or `/bootstrap` are a good refresher. Keep this file bookmarked.

## Bare words (optional)

You can type a single word like `audit` instead of `/audit`. Slash commands are more reliable — use them if a bare word is ignored.

---

Advanced registry (maintainers): [docs/BATCH_COMMANDS.md](../BATCH_COMMANDS.md)
