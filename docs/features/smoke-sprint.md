# Feature: smoke-sprint

> Sprint 53. Add the missing `smoke-sprint --require` script. Checklist: 🔲 open · ✅ done · ❌ blocked.

## Acceptance criteria

- ✅ User-visible behavior: `python3 scripts/agent-run.py smoke-sprint --require` reads the Android HOME manifest and maps ✅ AGENT/AUTO rows
- ✅ Offline/error behavior: `--if-complete` skips while AGENT/AUTO rows are open; missing manifest fails `--require`
- ✅ Accessibility: N/A
- ✅ i18n: N/A

## Smoke scenario

1. Given BUILD_PLAN has ✅ rows
2. When smoke-sprint --require runs
3. Then it exits 0 if HermesApplication and HermesLauncherActivity are in the manifest

## Container map

| Layer | Path |
|-------|------|
| Logic | `scripts/lib/sprint_smoke.py` |
| View | N/A |
| Tests | dry-run command |
| Wiring | `scripts/smoke-sprint.sh` |

## Tests

- Automated: no

## Fallback validation

- Why tests are not feasible: the script is the validation command
- Command: `python3 scripts/agent-run.py smoke-sprint --require`

## Definition of Done

BUILD_PLAN wrap-up can call smoke-sprint. Report at `.cursor/sprint-smoke.json`.

## Notes

- Device TalkBack stays `[ADB]`.
