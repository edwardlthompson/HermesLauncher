#!/usr/bin/env bash
# Fail if any tracked file exceeds size budget (matches pre-commit 500KB gate)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

MAX_KB=500
MAX_BYTES=$((MAX_KB * 1024))
ERRORS=0
MAX_REPORT=20
reported=0

list_files() {
  if [ "$#" -gt 0 ]; then
    printf '%s\0' "$@"
    return
  fi
  local lock
  if [ "${PRE_COMMIT:-}" = "1" ]; then
    echo "SKIP git diff/ls-files (PRE_COMMIT=1; avoid Windows index lock)" >&2
    return 0
  fi
  git diff --cached --name-only -z 2>/dev/null || git ls-files -z
}

while IFS= read -r -d '' file; do
  [ -z "$file" ] && continue
  [ -f "$file" ] || continue
  case "$file" in
    examples/android/third_party/*) continue ;;
  esac
  size=$(wc -c < "$file" | tr -d ' ')
  if [ "$size" -gt "$MAX_BYTES" ]; then
    kb=$((size / 1024))
    echo "LARGE TRACKED FILE: $file (${kb} KB > ${MAX_KB} KB)"
    ERRORS=$((ERRORS + 1))
    reported=$((reported + 1))
    if [ "$reported" -ge "$MAX_REPORT" ]; then
      echo "... truncated (max $MAX_REPORT)"
      break
    fi
  fi
done < <(list_files "$@")

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS tracked file(s) exceed ${MAX_KB} KB"
  exit 1
fi

echo "Large tracked file check passed"
