# Optional DX. CI does not require `just`.
# https://github.com/casey/just
# health is offline-first except optional `gh run list`.

verify:
    bash scripts/verify.sh

gates:
    python3 scripts/agent-run.py validate-bootstrap --quick

health:
    bash scripts/project-health.sh

update-deps-dry:
    python3 scripts/agent-run.py update-deps -- --dry-run

update-deps:
    python3 scripts/agent-run.py update-deps -- --apply

audit-deps:
    python3 scripts/agent-run.py update-deps -- --audit

check-template:
    python3 scripts/agent-run.py check-template-updates -- --verbose

release-please-dry:
    python3 scripts/agent-run.py release-please-dry

local-compute:
    python3 scripts/agent-run.py check-local-compute

android-instrumented:
    python3 scripts/agent-run.py run-android-emulator-local
