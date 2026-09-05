#!/usr/bin/env bash
# Enforce file line limits: 300 for static data (UI + i18n), 150 for pure logic
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATIC_DATA_LIMIT=300
LOGIC_LIMIT=150
ERRORS=0

check_static_data_paths() {
  local label="$1"
  while IFS= read -r -d '' file; do
    lines=$(wc -l < "$file" | tr -d ' ')
    if [ "$lines" -gt "$STATIC_DATA_LIMIT" ]; then
      echo "FAIL [$label] $file: $lines lines (max $STATIC_DATA_LIMIT)"
      ERRORS=$((ERRORS + 1))
    fi
  done
}

echo "Checking static data file limits (max $STATIC_DATA_LIMIT lines)..."
# Stay inside app/web trees so vendored AOSP (third_party) is never walked.
check_static_data_paths "static-data" < <(
  find "$ROOT/examples/android/app/src/main/java" -path "*/ui/*" -name "*.kt" -print0 2>/dev/null
  find "$ROOT/examples/android/app/src/main/res" -name "strings.xml" -print0 2>/dev/null
  if [ -d "$ROOT/examples/web/src" ]; then
    find "$ROOT/examples/web/src" -type f \( -name "*.tsx" -o -name "*.jsx" -o -name "*_view.*" -o -path "*/components/*.ts" \) -print0 2>/dev/null
    find "$ROOT/examples/web" -path "*/locales/*.json" -print0 2>/dev/null
  fi
)

echo "Checking scripts/lib logic file limits (max $LOGIC_LIMIT lines)..."
while IFS= read -r -d '' file; do
  lines=$(wc -l < "$file" | tr -d ' ')
  if [ "$lines" -gt "$LOGIC_LIMIT" ]; then
    echo "FAIL [logic] $file: $lines lines (max $LOGIC_LIMIT)"
    ERRORS=$((ERRORS + 1))
  fi
done < <(find "$ROOT/scripts/lib" -type f -name "*.py" -print0 2>/dev/null)

echo "Checking pure logic file limits (max $LOGIC_LIMIT lines)..."
while IFS= read -r -d '' file; do
  lines=$(wc -l < "$file" | tr -d ' ')
  if [ "$lines" -gt "$LOGIC_LIMIT" ]; then
    echo "FAIL [logic] $file: $lines lines (max $LOGIC_LIMIT)"
    ERRORS=$((ERRORS + 1))
  fi
done < <(
  find "$ROOT/examples/android/app/src/main/java" -type f -name "*.kt" \
    ! -path "*/ui/*" \
    -print0 2>/dev/null
)

if [ "$ERRORS" -gt 0 ]; then
  echo "$ERRORS file(s) exceed line limits"
  exit 1
fi

echo "All file line limits OK"
