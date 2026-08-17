#!/usr/bin/env bash
# Project health snapshot for /coach (offline-first).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== Project health ==="
if [ -f .cursor/stack-selection.json ]; then
  python3 -c "import json;d=json.load(open('.cursor/stack-selection.json',encoding='utf-8'));print(f\"Stack: {d.get('stack','?')}  tier: {d.get('distribution_tier','?')}\")"
else
  echo "Stack: (no .cursor/stack-selection.json)"
fi

echo ""
echo "--- BUILD_PLAN ---"
bash scripts/build-sprint-status.sh --lane auto || true

echo ""
echo "--- CI (best effort) ---"
if command -v gh >/dev/null 2>&1; then
  if ! gh run list --limit 1 2>/dev/null; then
    echo "WARN: gh could not read workflow runs (offline or unauthenticated)."
  fi
else
  echo "WARN: gh not installed; skip remote CI. After push: bash scripts/check-github-ci.sh --wait 300"
fi

echo ""
echo "--- Next human action ---"
echo "If the next row is HUMAN/ADB, do that. Otherwise run /coach or bash scripts/verify.sh."
echo "Playbook: docs/FIRST_30_DAYS.md   Why: docs/BEST_PRACTICES.md"
