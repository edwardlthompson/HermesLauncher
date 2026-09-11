# Feature: resume-handoff

> One command for Cloud → PC handoff without a second memory tree.

## Acceptance criteria

- ✅ `/resume` runs `resume-handoff` (fetch + open-PR sync + digest)
- ✅ Digest names next `🔲 [AGENT]` row, dirty Unreleased flag, CI-red one-liner (failed required checks), Dependabot/release PRs, and open `cursor/*` PRs
- ✅ Never resets a dirty tree; never relies on gitignored `/compact` state across machines
- ✅ `sessionStart` fail-open nudge mentions `/resume` after Cloud

## Smoke scenario

1. _Given_ Cloud left an open `cursor/*` PR and BUILD_PLAN has a next AGENT row
2. _When_ the human types `/resume` on This Computer
3. _Then_ the digest lists the Cloud PR and the next AGENT line

## Container map

| Layer | Path |
|-------|------|
| Logic | `scripts/lib/resume_handoff.py` |
| View | `.cursor/commands/resume.md` |
| Tests | `tests/test_resume_handoff.py` |
| Wiring | `scripts/resume-handoff.sh`, `session_start_context.py`, batch-commands registry |
## Tests

- Automated: yes — cursor PR filter + digest contents + mocked resume
- Coverage: Unreleased yes/no; gh unavailable path via format_digest

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack docs`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
