#!/usr/bin/env bash
# Schema-check packaging/winget/manifest.stub.yaml (does not submit).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FILE="${1:-$ROOT/packaging/winget/manifest.stub.yaml}"
if [ ! -f "$FILE" ]; then
  echo "SKIP Winget stub (generated in release.yml before this check)"
  exit 0
fi
ERRORS=0
need() {
  if ! grep -qE "^$1:" "$FILE"; then
    echo "FAIL: missing $1 in $FILE"
    ERRORS=$((ERRORS + 1))
  fi
}
need PackageIdentifier
need PackageVersion
need ManifestVersion
need License
if ! grep -q 'InstallerSha256:' "$FILE"; then
  echo "FAIL: missing InstallerSha256"
  ERRORS=$((ERRORS + 1))
fi
if [ "$ERRORS" -gt 0 ]; then
  exit 1
fi
echo "OK   Winget stub schema ($FILE)"
