# Pre-release gate (expanded — used by `/ship`)

> Docs: @docs/CODEX_REVIEW.md · Skill: `.cursor/skills/update-deps/`

`/ship` runs `/update-deps` first, then this command. Autofix + optional Codex happen here so release stays one simple super command.

## Step 0 — Dependencies and Release Please preview

If `/ship` already ran `/update-deps` this session, **audit-only** (do not apply again):

```bash
python3 scripts/agent-run.py update-deps -- --audit

```

If this command was invoked **standalone** (not after `/update-deps`), execute @.cursor/commands/update-deps.md fully first.

Then preview the next Release Please version (never publishes; skip/WARN if no GitHub token):

```bash
python3 scripts/agent-run.py release-please-dry

```

## Step 1 — Mechanical autofix + gate loop

```bash
python3 scripts/agent-run.py prerelease-autofix

```

On exit `2` (env/3-strike): halt — do not `/push`.
On exit `1`: apply semantic fixes in feature scope, re-run step 1 (max 3 cycles), then continue.

## Step 2 — Optional Codex third-party review

```bash
python3 scripts/agent-run.py run-codex-review

```

- Exit `3` (`SKIP: Codex review (no key/CLI)`): print the skip and **continue** (do not block release).
- Exit `1`: leave prior `CODE_REVIEW.md` untouched; halt until fixed or `[HUMAN]` defers.
- Exit `0`: if `CODE_REVIEW.md` has Critical/High findings, append 🔲 `[AGENT]` rows to @BUILD_PLAN.md, implement them, then:

```bash
python3 scripts/agent-run.py watch-agent-gates --once --autofix --scope full --step none

```

Repeat until Critical/High cleared or 3-strike halt (do not `/push` on halt).

## Step 3 — Hard pre-release gate (local)

```bash
python3 scripts/agent-run.py pre-release-gate -- --local

```

Local: feature-gate, `update-deps --audit`, template version, license. No GitHub CI wait, Dependabot API, or Scorecard here. `/push` and `/regress` still run the full GitHub gate after push. Do not tag until those pass. See @docs/MAINTAINING_THE_TEMPLATE.md Release Checklist for maintainers.

Begin now.
