#!/usr/bin/env bash
# Validate all paths in TEMPLATE_INDEX.json exist
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
INDEX="$ROOT/TEMPLATE_INDEX.json"

if ! command -v jq &>/dev/null; then
  echo "jq not found; using python fallback"
  python3 - "$INDEX" "$ROOT" << 'PY'
import glob, json, sys, os
index_path, root = sys.argv[1], sys.argv[2]
with open(index_path) as f:
    data = json.load(f)
errors = []
for ep in data.get("entry_points", {}).values():
    if not os.path.exists(os.path.join(root, ep)):
        errors.append(ep)
for item in data.get("files", []):
    if not os.path.exists(os.path.join(root, item["path"])):
        errors.append(item["path"])
for mod in data.get("modules", {}).values():
    if mod.get("guide") and not os.path.exists(os.path.join(root, mod["guide"])):
        errors.append(mod["guide"])
    if mod.get("example") and not os.path.exists(os.path.join(root, mod["example"])):
        errors.append(mod["example"])
if errors:
    print("Missing paths:", *errors, sep="\n  ")
    sys.exit(1)
indexed = set()
for item in data.get("files", []):
    indexed.add(item["path"])
unindexed = []
for sh in sorted(glob.glob(os.path.join(root, "scripts", "*.sh"))):
    rel = os.path.relpath(sh, root).replace("\\", "/")
    if rel not in indexed and not rel.endswith(".ps1"):
        unindexed.append(rel)
for wf in sorted(glob.glob(os.path.join(root, ".github", "workflows", "*.yml"))):
    rel = os.path.relpath(wf, root).replace("\\", "/")
    if rel not in indexed:
        unindexed.append(rel)
if unindexed:
    print("Unindexed paths (add to TEMPLATE_INDEX.json):", *unindexed, sep="\n  ")
    sys.exit(1)
print("TEMPLATE_INDEX.json validation passed")
PY
  exit $?
fi

ERRORS=0
check_path() {
  # jq.exe on Windows Git Bash emits CRLF; a trailing CR makes -e fail for every path.
  local rel="${1//$'\r'/}"
  if [ ! -e "$ROOT/$rel" ]; then
    echo "MISSING: $rel"
    ERRORS=$((ERRORS + 1))
  fi
}

for path in $(jq -r '.entry_points | .[]' "$INDEX"); do
  check_path "$path"
done

for path in $(jq -r '.files[].path' "$INDEX"); do
  check_path "$path"
done

for path in $(jq -r '.modules[].guide // empty' "$INDEX"); do
  check_path "$path"
done

for path in $(jq -r '.modules[].example // empty' "$INDEX"); do
  check_path "$path"
done

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS path(s) missing"
  exit 1
fi

python3 - "$INDEX" "$ROOT" << 'PY' || exit 1
import glob, json, os, sys
index_path, root = sys.argv[1], sys.argv[2]
with open(index_path) as f:
    data = json.load(f)
indexed = {item["path"] for item in data.get("files", [])}
unindexed = []
for sh in sorted(glob.glob(os.path.join(root, "scripts", "*.sh"))):
    rel = os.path.relpath(sh, root).replace("\\", "/")
    if rel not in indexed:
        unindexed.append(rel)
for wf in sorted(glob.glob(os.path.join(root, ".github", "workflows", "*.yml"))):
    rel = os.path.relpath(wf, root).replace("\\", "/")
    if rel not in indexed:
        unindexed.append(rel)
if unindexed:
    print("Unindexed paths (add to TEMPLATE_INDEX.json):")
    for p in unindexed:
        print(f"  {p}")
    sys.exit(1)
PY

echo "TEMPLATE_INDEX.json validation passed"
