# Implementation Plan

> Active work lives in `BUILD_PLAN.md`. This file is the milestone map.
> Status: use BUILD_PLAN emoji markers.

## Milestone — Sprint 0 seed

| Task | Owner | Tests / fallback |
|------|-------|------------------|
| Clone template, init android/MIT, prune unused stacks | AGENT | `validate-bootstrap.sh --quick` |
| Identity lock `org.hermeslauncher.app` | AGENT | package + script path grep |
| Spec, ADRs 0001-0004, threat model, BUILD_PLAN | AGENT | `check-build-plan-parallel.sh` |
| GitHub repo + Dependabot + branch protection | AGENT automating HUMAN | `setup-github-repo.sh` |

## Milestone — Sprint 1 launcher shell

| Task | Owner | Tests / fallback |
|------|-------|------------------|
| Lock `HomePagerState` / `DockState` / `DrawerState` | AGENT | unit tests in `launcher/` |
| HOME intent + pager + dock stub + drawer stub | AGENT | swipe-contract unit test + `MainActivitySmokeTest` |

## Next feature

1. Copy `docs/features/_template.md` → `docs/features/{name}.md`
2. Lock the public API (Sequential)
3. Add unit tests before or with the implementation
4. Run `python3 scripts/agent-run.py watch-agent-gates --once --autofix`

If automated tests are not feasible, write the justification and fallback command in the feature spec before marking the BUILD_PLAN row done.
