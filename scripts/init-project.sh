#!/usr/bin/env bash
# Post-template clone customization helper
# Usage: scripts/init-project.sh [options]
#   --stack web|python|android|node|multi|none
#   --distribution-tier foss|commercial
#   --project-name NAME  --purpose TEXT  --interval INTERVAL
#   --release-repo OWNER/REPO  --donation-url URL  --codeowner USER
#   --prune  --no-prune  --non-interactive  --keep-optional  --prune-optional
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

usage() {
  cat <<'EOF'
Usage: scripts/init-project.sh [options]
  --stack STACK          web|python|android|node|multi|none
  --project-name NAME
  --purpose TEXT
  --interval INTERVAL    off|daily|weekly|monthly|on_session
  --release-repo OWNER/REPO
  --donation-url URL
  --codeowner USER       GitHub username without @
  --prune                Prune unused examples/modules without prompting
  --no-prune             Never prune (overrides --prune)
  --non-interactive      Skip prompts (requires --stack, --project-name, --purpose)
  --keep-optional        When pruning, keep rust/go/lightroom examples and modules (default)
  --prune-optional       When pruning, also remove optional stacks (rust/go/lightroom)
  --distribution-tier T  foss|commercial (default foss)
  -h, --help
EOF
}

STACK=""
PROJECT_NAME=""
PROJECT_PURPOSE=""
INTERVAL=""
RELEASE_REPO=""
DONATION_URL=""
CODEOWNER=""
PRUNE_FLAG=""
NONINTERACTIVE=false
KEEP_OPTIONAL=true
DISTRIBUTION_TIER="foss"
while [ $# -gt 0 ]; do
  case "$1" in
    --stack) STACK="${2:-}"; shift 2 ;;
    --distribution-tier) DISTRIBUTION_TIER="${2:-foss}"; shift 2 ;;
    --project-name) PROJECT_NAME="${2:-}"; shift 2 ;;
    --purpose) PROJECT_PURPOSE="${2:-}"; shift 2 ;;
    --interval) INTERVAL="${2:-}"; shift 2 ;;
    --release-repo) RELEASE_REPO="${2:-}"; shift 2 ;;
    --donation-url) DONATION_URL="${2:-}"; shift 2 ;;
    --codeowner) CODEOWNER="${2:-}"; shift 2 ;;
    --prune) PRUNE_FLAG="yes"; shift ;;
    --no-prune) PRUNE_FLAG="no"; shift ;;
    --non-interactive) NONINTERACTIVE=true; shift ;;
    --keep-optional) KEEP_OPTIONAL=true; shift ;;
    --prune-optional) KEEP_OPTIONAL=false; shift ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
  esac
done

case "$DISTRIBUTION_TIER" in
  foss|commercial) ;;
  *)
    echo "Invalid distribution tier '$DISTRIBUTION_TIER'; defaulting to foss."
    DISTRIBUTION_TIER="foss"
    ;;
esac
export BUILD_DISTRIBUTION_TIER="$DISTRIBUTION_TIER"

prune_optional_stacks() {
  if [ "$KEEP_OPTIONAL" = true ]; then
    return 0
  fi
  rm -rf examples/rust examples/go examples/lightroom modules/rust modules/go modules/lightroom 2>/dev/null || true
}

prune_primary_stack() {
  local stack="$1"
  case "$stack" in
    web) rm -rf examples/python examples/android examples/node modules/python modules/android modules/node 2>/dev/null || true ;;
    python) rm -rf examples/web examples/android examples/node modules/web modules/android modules/node 2>/dev/null || true ;;
    android) rm -rf examples/web examples/python examples/node modules/web modules/python modules/node 2>/dev/null || true ;;
    node) rm -rf examples/web examples/python examples/android modules/web modules/python modules/android 2>/dev/null || true ;;
  esac
  prune_optional_stacks
}

if [ "$NONINTERACTIVE" = true ]; then
  if [ -z "$STACK" ] || [ -z "$PROJECT_NAME" ] || [ -z "$PROJECT_PURPOSE" ]; then
    echo "ERROR: --non-interactive requires --stack, --project-name, and --purpose" >&2
    exit 1
  fi
fi

echo "=== agent-project-bootstrap init ==="
echo ""

if [ -z "$PROJECT_NAME" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "Project name: " PROJECT_NAME
fi
if [ -z "$PROJECT_PURPOSE" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "One-line purpose: " PROJECT_PURPOSE
fi
if [ -z "$STACK" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "Primary stack (web/python/android/node/multi/none): " STACK
fi
STACK="${STACK:-none}"
case "$STACK" in
  web|python|android|node|multi|none) ;;
  *)
    echo "Invalid stack '$STACK'; defaulting to none (keep all examples)."
    STACK=none
    ;;
esac
if [ -z "$INTERVAL" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "Template update check interval (off/daily/weekly/monthly/on_session) [weekly]: " INTERVAL
fi
INTERVAL="${INTERVAL:-weekly}"
if [ "$NONINTERACTIVE" != true ]; then
  echo "Distribution tier:"
  echo "  1) FOSS (default) — MIT, no proprietary SDKs"
  echo "  2) Commercial — proprietary SDKs, full Cursor Cloud stack"
  read -rp "Choose [1/2]: " TIER_CHOICE
  case "$TIER_CHOICE" in
    2|commercial|Commercial) DISTRIBUTION_TIER="commercial" ;;
    *) DISTRIBUTION_TIER="foss" ;;
  esac
  export BUILD_DISTRIBUTION_TIER="$DISTRIBUTION_TIER"
fi

# Replace placeholders (Python handles special characters in names)
if [ -n "$STACK" ] && [ -n "$PROJECT_PURPOSE" ]; then
  python3 - "$STACK" "$PROJECT_PURPOSE" "$ROOT" << 'PY'
import sys
from pathlib import Path

stack, purpose, root = sys.argv[1], sys.argv[2], Path(sys.argv[3])
replacements = [
    ("[INSERT PLATFORM / TECH STACK HERE]", stack),
    ("[INSERT DETAILED APP DESCRIPTION AND GOALS HERE]", purpose),
]
for rel in ("docs/INITIALIZATION_PROMPT.md", "AGENT_MEMORY.md"):
    path = root / rel
    if not path.is_file():
        continue
    text = path.read_text(encoding="utf-8")
    for old, new in replacements:
        text = text.replace(old, new)
    path.write_text(text, encoding="utf-8")
PY
fi

# Update check config
python3 - "$INTERVAL" "$ROOT/.template-update.json" << 'PY'
import json, sys
interval, path = sys.argv[1], sys.argv[2]
with open(path, encoding="utf-8") as f:
    d = json.load(f)
d["check_interval"] = interval
with open(path, "w", encoding="utf-8") as f:
    json.dump(d, f, indent=2)
    f.write("\n")
PY


if [ -z "$RELEASE_REPO" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "GitHub owner/repo for app release checks (OWNER/REPO) [skip]: " RELEASE_REPO
fi
if [ -z "$DONATION_URL" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "Donation URL [skip]: " DONATION_URL
fi

python3 - "$ROOT" "$RELEASE_REPO" "$DONATION_URL" << 'PY'
import json, shutil, sys
from pathlib import Path
root, repo, url = sys.argv[1], sys.argv[2], sys.argv[3]
root = Path(root)
src_app = root / ".app-update.json.example"
dst_app = root / ".app-update.json"
if src_app.exists() and not dst_app.exists():
    shutil.copy(src_app, dst_app)
if repo.strip():
    data = json.loads(dst_app.read_text(encoding="utf-8"))
    data["release_repo"] = repo.strip()
    dst_app.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
src_don = root / "donations.json.example"
dst_don = root / "donations.json"
if src_don.exists() and not dst_don.exists():
    shutil.copy(src_don, dst_don)
if url.strip() and dst_don.exists():
    data = json.loads(dst_don.read_text(encoding="utf-8"))
    data["links"] = [{"label": "Donate", "url": url.strip()}]
    dst_don.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")
PY

python3 scripts/sync-stack-config.py "$ROOT" "$RELEASE_REPO" "$DONATION_URL"

if [ -z "$CODEOWNER" ] && [ "$NONINTERACTIVE" != true ]; then
  read -rp "GitHub username for CODEOWNERS (without @): " CODEOWNER
fi
if [ -n "$CODEOWNER" ]; then
  sed -i "s/@\[PROJECT_OWNER\]/@$CODEOWNER/g" .github/CODEOWNERS 2>/dev/null || \
    sed -i '' "s/@\[PROJECT_OWNER\]/@$CODEOWNER/g" .github/CODEOWNERS
fi

ABOUT="$PROJECT_NAME - $PROJECT_PURPOSE. Built with agent-project-bootstrap. FOSS MIT."
python3 - "$ABOUT" "$ROOT/docs/GITHUB_ABOUT.md" << 'PY'
import sys
from pathlib import Path
about, path = sys.argv[1], Path(sys.argv[2])
path.write_text(
    f"""# GitHub About Block

## Draft Description (edit to <=350 chars)

{about}

## Topics

Add topics relevant to your project and stack.
""",
    encoding="utf-8",
)
PY

# Prune unused examples/modules
PRUNED=false
if [ "$STACK" = "none" ]; then
  echo "Stack 'none': keeping all examples and modules."
elif [ "$STACK" = "multi" ]; then
  if [ "$PRUNE_FLAG" = "yes" ]; then
    echo "Keeping all examples (multi-stack)."
  elif [ "$PRUNE_FLAG" = "no" ] || [ "$NONINTERACTIVE" = true ]; then
    echo "Skipping prune (--no-prune or --non-interactive)."
  else
    read -rp "Prune unused examples/modules? (y/N): " PRUNE
    if [ "$PRUNE" = "y" ] || [ "$PRUNE" = "Y" ]; then
      echo "Keeping all examples (multi-stack)."
    fi
  fi
else
  if [ "$PRUNE_FLAG" = "yes" ]; then
    PRUNED=true
    prune_primary_stack "$STACK"
  elif [ "$PRUNE_FLAG" = "no" ] || [ "$NONINTERACTIVE" = true ]; then
    echo "Skipping prune (--no-prune or --non-interactive)."
  else
    read -rp "Prune unused examples/modules? (y/N): " PRUNE
    if [ "$PRUNE" = "y" ] || [ "$PRUNE" = "Y" ]; then
      PRUNED=true
      prune_primary_stack "$STACK"
    fi
  fi
fi

python3 scripts/init-stack-sync.py "$STACK" "$ROOT" "$PRUNED"
COPY_COMM=""
if [ "$DISTRIBUTION_TIER" = "commercial" ]; then
  COPY_COMM="--copy-commercial"
fi
python3 scripts/sync-cursor-features.py --root "$ROOT" --tier "$DISTRIBUTION_TIER" --patch-init $COPY_COMM
python3 scripts/sync-design-tokens.py || true
python3 scripts/generate-project-readme.py || true
echo "Wrote .cursor/stack-selection.json (tier=$DISTRIBUTION_TIER) and synced AGENT_MEMORY active modules."

echo ""
echo "=== Workflow validation ==="
if command -v gh >/dev/null 2>&1; then
  if bash scripts/validate-workflow-actions.sh; then
    echo "Workflow action refs validated via GitHub API."
  else
    echo "WARN: validate-workflow-actions.sh failed. Fix refs before first push."
  fi
else
  echo "WARN: gh CLI not found. Install GitHub CLI and run:"
  echo "  bash scripts/validate-workflow-actions.sh"
  echo "See README.md and docs/SECURITY_TRIAGE.md for setup."
fi

echo ""
echo "=== Done ==="
echo ""
echo "Next steps:"
echo "  1. Review SECURITY.md, CODEOWNERS, playbooks, and .env.example"
echo "  2. Run scripts/setup-github-repo.sh (or .ps1) for Dependabot alerts, private reporting, branch protection"
echo "     See docs/SECURITY_TRIAGE.md if the script prints a manual checklist (API 422)"
echo "  3. Open Cursor and paste:"
echo ""
echo "  Read @docs/START_HERE.md, @docs/CURSOR_MODES.md, and @docs/INITIALIZATION_PROMPT.md."
echo "  Pick Cursor mode per CURSOR_MODES.md. Follow Section 8 Startup Sequence."
echo "  Use BUILD_PLAN.md Sequential lane first; respect AGENT/HUMAN/ADB/AUTO labels."
echo ""
echo "  4. After first push to main, poll required workflows:"
echo "     bash scripts/check-github-ci.sh --wait 300"
echo ""
echo "  5. Install pre-commit hooks and preview ephemeral purge:"
echo "     pip install pre-commit && pre-commit install"
echo "     bash scripts/purge-ephemeral.sh"
echo ""
echo "GitHub About draft: docs/GITHUB_ABOUT.md"
echo "Stack selection: .cursor/stack-selection.json"
echo "Agent shortcuts: docs/help/BATCH_COMMANDS.md (type / in Agent chat)"
