# Feature: open-prs-build-plan-sync

> Mirror open Dependabot and Release Please PRs into a managed BUILD_PLAN block.

## Acceptance criteria

- ✅ `<!-- open-prs-sync:begin/end -->` markers exist on `BUILD_PLAN.md` and `BUILD_PLAN_TEMPLATE.md`
- ✅ `sync-open-prs-build-plan` rewrites only the marker block and refreshes the remaining tally
- ✅ Empty state has no `🔲` rows
- ✅ Weekly health and Dependabot/Release Please PR events can apply + commit
- ✅ Cloud feature PRs (`cursor/*`) are **not** written into the block

## Smoke scenario

1. _Given_ open Dependabot PR #N
2. _When_ `python3 scripts/agent-run.py sync-open-prs-build-plan -- --apply` runs
3. _Then_ BUILD_PLAN lists a Dependabot row such as ``- 🔲 [AUTO] Merge Dependabot #N`` with the PR URL

## Container map

| Layer | Path |
|-------|------|
| Logic | `scripts/lib/sync_open_prs_build_plan.py` |
| View | `BUILD_PLAN.md` Open PRs (synced) |
| Tests | `tests/test_sync_open_prs_build_plan.py` |
| Wiring | `scripts/sync-open-prs-build-plan.sh`, weekly-health, `sync-open-prs-build-plan.yml` |

## Tests

- Automated: yes — classify, render, tally, idempotent apply, stale `--check`
- Coverage: empty set; Dependabot before release ordering

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack docs`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
