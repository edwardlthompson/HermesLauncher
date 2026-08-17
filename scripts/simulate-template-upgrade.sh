#!/usr/bin/env bash
# Simulate a child-repo upgrade: clone template, apply cherry-pick areas, validate bootstrap.
# See docs/UPGRADING_FROM_TEMPLATE.md for the human/agent merge playbook.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
WORKDIR="$(mktemp -d)"
trap 'rm -rf "$WORKDIR"' EXIT

echo "==> Simulating template upgrade in $WORKDIR"

git clone --quiet "file://$ROOT" "$WORKDIR/child"
cd "$WORKDIR/child"

AREAS=(
  scripts/check-file-encoding.sh
  scripts/check-changelog-unreleased.sh
  scripts/validate-bootstrap.sh
  scripts/validate-template-index.sh
  scripts/check-batch-commands.sh
  docs/CURSOR_MODES.md
  docs/help/BATCH_COMMANDS.md
  .cursor/rules/cursor-modes.mdc
  .cursor/rules/batch-commands.mdc
  .github/workflows/dependency-review.yml
)

for path in "${AREAS[@]}"; do
  if [ ! -e "$path" ]; then
    echo "MISSING in clone: $path"
    exit 1
  fi
done

bash scripts/validate-bootstrap.sh --quick
bash scripts/validate-template-index.sh

echo "==> Non-interactive init smoke (web stack, no prune)"
bash scripts/init-project.sh \
  --non-interactive \
  --stack web \
  --project-name "Upgrade Sim" \
  --purpose "Cherry-pick validation" \
  --no-prune \
  --license MIT

for path in \
  bootstrap.config.json \
  PROJECT_CHECKLIST.md \
  CLAUDE.md \
  .github/copilot-instructions.md \
  .cursor/rules/main.mdc \
  docs/spec.md \
  docs/plan.md \
  docs/BEST_PRACTICES.md \
  docs/FIRST_30_DAYS.md \
  env.schema.json \
  .devcontainer/Dockerfile \
  .agent/memory/decisions.md \
  .agent/memory/pitfalls.md \
  scripts/verify.sh
do
  if [ ! -e "$path" ]; then
    echo "FAIL: missing after init: $path"
    exit 1
  fi
done
python3 -c "import json; json.load(open('bootstrap.config.json', encoding='utf-8'))"
if ! grep -q 'Upgrade Sim' AGENTS.md; then
  echo "FAIL: AGENTS.md was not stamped with project name"
  exit 1
fi

bash scripts/validate-bootstrap.sh --quick

echo "==> Non-interactive init smoke with --prune --prune-optional"
git clone --quiet "file://$ROOT" "$WORKDIR/child-prune"
cd "$WORKDIR/child-prune"

bash scripts/init-project.sh \
  --non-interactive \
  --stack web \
  --project-name "Upgrade Sim Prune" \
  --purpose "Prune optional validation" \
  --prune \
  --prune-optional

for path in examples/rust examples/go examples/lightroom modules/rust modules/go modules/lightroom; do
  if [ -e "$path" ]; then
    echo "FAIL: $path still present after --prune-optional"
    exit 1
  fi
done
for path in examples/python examples/android examples/node modules/python modules/android modules/node; do
  if [ -e "$path" ]; then
    echo "FAIL: $path still present after web-stack prune"
    exit 1
  fi
done
bash scripts/validate-bootstrap.sh --quick
echo "Prune-optional smoke passed"

echo "==> Non-interactive init smoke (PowerShell)"
git clone --quiet "file://$ROOT" "$WORKDIR/child-ps"
cd "$WORKDIR/child-ps"

pwsh -NoProfile -File scripts/init-project.ps1 \
  -NonInteractive \
  -Stack web \
  -ProjectName "Upgrade Sim PS" \
  -ProjectPurpose "PS init smoke" \
  -Prune \
  -PruneOptional

bash scripts/validate-bootstrap.sh --quick
echo "PowerShell init smoke passed"

echo "Upgrade simulation passed"
