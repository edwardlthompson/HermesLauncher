# Agent Memory

> Centralized index of tech stack, threat models, persistent context, and retrospectives.
> Update only at session startups, milestone boundaries, or major architectural pivots.

## Tech Stack

| Layer | Technology | Version | Notes |
|-------|-----------|---------|-------|
| Platform | Multi-stack template (Web, Python, Android, Node, optional Lightroom/Rust/Go) | 0.18.2 | Template maintainer repo |
| License | MIT | - | Pure FOSS |
| Distribution | GitHub Releases + GitHub Pages demo | - | F-Droid/Winget stubs for child repos |
## Active Modules

- ✅ Web / PWA (`modules/web/MODULE.md`)
- ✅ Python (`modules/python/MODULE.md`)
- ✅ Android / F-Droid (`modules/android/MODULE.md`)
- ✅ Node API (`modules/node/MODULE.md`)
- ✅ Lightroom Classic (`modules/lightroom/MODULE.md`)
- ✅ Rust (`modules/rust/MODULE.md`)
- ✅ Go (`modules/go/MODULE.md`)

## Threat Model Checklist

- ✅ `docs/THREAT_MODEL.md` drafted (STRIDE, trust boundaries, top abuse cases, OWASP LLM Top 10 walk)
- ✅ No proprietary closed-source SDKs in production path
- ✅ Opt-in only telemetry (GDPR/CCPA compliant); see `docs/PRIVACY.md`
- ✅ Secrets excluded from VCS (Gitleaks pre-commit)
- ✅ Dependency vulnerability scanning enabled (CodeQL + Trivy + Dependabot)
- ✅ Input validation at all data boundaries
- ✅ `SECURITY.md` and private vulnerability reporting enabled

## Persistent Context

### Project Purpose

FOSS Cursor agent bootstrap template: labeled BUILD_PLAN sprints, Golden Path examples, CI guardrails, workspace memory, and design-system cohesion across Web and Android.

### Key Constraints

- Max 300 lines per static data file (UI + i18n), 150 lines per pure logic file
- Trunk-based development with Conventional Commits
- Strict type safety and test coverage budgets

## Session Retrospectives

| Date | Milestone | What worked | What to improve |
|------|-----------|-------------|-----------------|
| 2026-08-15 | M35 HUMAN open items | Job-scoped workflow tokens; dismissed 65 PinnedDependencies; merged Dependabot #58–#61; radar max 6 | Rebase Dependabot before Feature Gate on stale lockfiles; Scorecard VulnerabilitiesID lags patched HEAD |
| 2026-08-15 | v0.18.1 /push | `resolve-python.sh` now sets a single executable path so `"$PY"` works; RP #62 admin-merge after CI green | Do not set `PY="py -3"` (quoted invoke fails); keep Unreleased empty before RP or notes land under `chore` |
| 2026-08-15 | M35 /audit | Shared `resolve-python.sh` skips Store stub; About gate restores from HEAD; slim Unreleased; UTF-8 LF rules | Do not run `python3` on Windows PATH; leave Scorecard + Dependabot PRs to HUMAN |
| 2026-08-15 | v0.18.0 /ship | M34 thin steals + extract-zip High cleared via `@puppeteer/browsers` 3.2.0; lockfile needed `proxy-agent` 8 for `npm ci`; RP #56 admin-merge | Generate lockfile with Node 22 / `npm ci` locally after overrides; Windows Store `python3` hangs autofix |
| 2026-08-14 | M34 prior-art thin steals | Honesty labels + handoff + Sacred upgrade column without vendoring cousin repos | Keep fail-open hooks labeled; do not claim `/push` blocks `--force` |
| 2026-08-12 | v0.17.0 /ship | Branding kit + pitch README generator; RP #55 admin-merge; CI green on feat commit | Trigger Release workflow for SBOM if assets empty after tag |
| 2026-08-10 | v0.16.0 /ship | Codex + multi-stack autofix in `/prerelease`; fixed About-without Biome stubs; undici/ip-address/nanoid overrides cleared High alerts after push; RP #51 admin-merge | Prefer Git Bash via agent-run on Windows (System32 bash = WSL1 breaks npm); push security lockfile before expecting Dependabot zero |
| 2026-08-01 | v0.15.2 /ship | Cleared High Dependabot mid-ship (js-yaml, brace-expansion, postcss); RP #50 admin-merge after auto-merge wait | Re-check Dependabot after each push before merge-release-please |
| 2026-07-22 | v0.15.0 /ship | RP #37 merged; fixed duplicate CHANGELOG Unreleased + Node 25 vitest localStorage before CI green | Confirm single Unreleased before push; watch GH Dependabot banner vs triage script |
| 2026-07-21 | M33 Cursor feature integration | Native worktrees + permissions + 7 skills + plugin pack + CLI example; commercial docs deepened | Keep pack script globs wholesale when adding skills; residual Auto-review classifier drift |
| 2026-07-12 | v0.14.1 release | /push merged RP #36; fixed Dependabot alert API + FOSS mcp.json gate | Prefer AUTOMERGE_TOKEN over admin merge fallback for RP |
| 2026-07-12 | M32 audit | Caught GITHUB_TOKEN automerge skipping push CI; Git Bash preference for Windows agent-run | Completed via HUMAN automation; GitHub MCP enabled locally |
| 2026-06-13 | v0.6.0 design system | Cross-stack tokens + i18n scaffold | Restore optional-stack CI jobs after large merge |
| 2026-06-30 | Autonomous /build + HUMAN automation | Grouped human section keeps board readable; automation router backlogs failures only | Release Please PR #20 for 0.12.0 needs human merge |
## Template Provenance

- **Source template:** `edwardlthompson/agent-project-bootstrap` (self-maintained)
- **Template version:** `0.18.2` (see `.template-version`)
- **Last update check:** See `.template-update.json`
