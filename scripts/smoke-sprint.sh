#!/usr/bin/env bash
# Prove BUILD_PLAN ✅ rows still load: Android HOME manifest, optional web/cli budgets.
# Usage: smoke-sprint.sh [--require] [--if-complete] [--dry-run] [--sprint NAME]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"
exec "$PY" "$ROOT/scripts/lib/sprint_smoke.py" --root "$ROOT" "$@"
