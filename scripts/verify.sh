#!/usr/bin/env bash
# Unified local verification harness: env + format/lint/bootstrap gates.
# Usage: scripts/verify.sh [--full]
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FULL=false
for arg in "$@"; do
  case "$arg" in
    --full) FULL=true ;;
  esac
done

echo "=== verify: env schema ==="
bash scripts/check-env.sh

echo "=== verify: bootstrap gates ==="
bash scripts/validate-bootstrap.sh --quick

if [ "$FULL" = true ]; then
  STACK="multi"
  if [ -f .cursor/stack-selection.json ]; then
    STACK="$(python3 -c "import json;print(json.load(open('.cursor/stack-selection.json')).get('stack','multi'))" 2>/dev/null || echo multi)"
  fi
  echo "=== verify: feature-gate (stack=$STACK) ==="
  bash scripts/feature-gate.sh --stack "$STACK"
fi

echo "Verification harness passed"
