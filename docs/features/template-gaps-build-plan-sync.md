# Feature: template-gaps-build-plan-sync

> Monday cron (and local CLI) mirrors parent template gaps into a managed BUILD_PLAN block.

## Acceptance criteria

- ✅ `<!-- template-gaps-sync:begin/end -->` markers exist on `BUILD_PLAN.md` and `BUILD_PLAN_TEMPLATE.md`
- ✅ `sync-template-gaps-build-plan` rewrites only the marker block and refreshes the remaining tally
- ✅ Empty / up-to-date / template-maintainer state has no `🔲` rows
- ✅ Canon/Mixed → `[AGENT]`; Sacred → `[HUMAN]` (never blind-overwrite); features listed when missing
- ✅ File rows capped at 40 with a “N more” pointer to `check-template-gaps`
- ✅ Weekly health: child applies + commits gap sync; template keeps upgrade-sim

## Smoke scenario

1. _Given_ a child with `.template-version` behind upstream
2. _When_ `python3 scripts/agent-run.py sync-template-gaps-build-plan -- --apply` runs
3. _Then_ BUILD_PLAN **Template gaps (synced)** lists Canon/Mixed/Sacred/Feature rows for `/upgrade`

## Container map

| Layer | Path |
|-------|------|
| Logic | `scripts/lib/sync_template_gaps_render.py`, `scripts/lib/sync_template_gaps_build_plan.py` |
| View | `BUILD_PLAN.md` / `BUILD_PLAN_TEMPLATE.md` Template gaps (synced) |
| Tests | `tests/test_sync_template_gaps_build_plan.py` |
| Wiring | `scripts/sync-template-gaps-build-plan.sh`, `weekly-health-check.yml` |

## Tests

- Automated: yes — empty, behind, cap, Sacred HUMAN, idempotent apply, stale `--check`
- Coverage: template N/A; skip/offline notes without checkboxes

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack docs`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
