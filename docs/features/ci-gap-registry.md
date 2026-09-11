# Feature: ci-gap-registry

> Living registry of accepted CI skips, aligned with the `ci-gap` issue label.

## Acceptance criteria

- ✅ `schemas/ci-gaps.json` lists required accepted gaps with id/status/note
- ✅ Issue form still offers category `ci-gap`
- ✅ `ci-ok` must not `needs` nix; PR CI stays `main`-only

## Smoke scenario

1. _Given_ the registry and issue form
2. _When_ `scripts/check-ci-gaps.sh` runs
3. _Then_ required ids and the `ci-gap` label stay aligned

## Container map

| Layer | Path |
|-------|------|
| Logic | `scripts/lib/ci_gaps.py` |
| View | `docs/CI_GAPS.md`, `schemas/ci-gaps.json` |
| Tests | `tests/test_ci_gaps.py` |
| Wiring | `scripts/check-ci-gaps.sh` in `validate-bootstrap.sh` |

## Tests

- Automated: yes — required ids, issue label, ci-ok must not need nix
- Coverage: empty gaps list

## Fallback validation

- Why tests are not feasible: N/A
- Command: `python3 scripts/agent-run.py feature-gate --stack docs`

## Definition of Done

See `docs/FEATURE_MODULES.md`.
