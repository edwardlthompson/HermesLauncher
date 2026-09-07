#!/usr/bin/env bash
# Write gitignored release keystore files from GitHub Actions secrets.
# Never prints secret values. Fail closed if any required secret is empty.
# Usage: scripts/ci-write-android-keystore.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"
ANDROID="$ROOT/examples/android"
JKS="$ANDROID/upload-keystore.jks"
PROPS="$ANDROID/keystore.properties"

missing=()
[ -n "${ANDROID_KEYSTORE_BASE64:-}" ] || missing+=(ANDROID_KEYSTORE_BASE64)
[ -n "${ANDROID_KEYSTORE_PASSWORD:-}" ] || missing+=(ANDROID_KEYSTORE_PASSWORD)
[ -n "${ANDROID_KEY_ALIAS:-}" ] || missing+=(ANDROID_KEY_ALIAS)
if [ "${#missing[@]}" -gt 0 ]; then
  echo "FAIL: missing GitHub secrets: ${missing[*]}" >&2
  echo "Set them with: bash scripts/set-android-signing-secrets.sh" >&2
  exit 1
fi

umask 077
mkdir -p "$ANDROID"
printf '%s' "$ANDROID_KEYSTORE_BASE64" | base64 -d > "$JKS"
export HL_STORE_FILE="$JKS"
export HL_KEY_PASSWORD="${ANDROID_KEY_PASSWORD:-$ANDROID_KEYSTORE_PASSWORD}"
"$PY" - "$PROPS" <<'PY'
import os
import sys
from pathlib import Path

out = Path(sys.argv[1])
out.write_text(
    "\n".join(
        [
            f"storeFile={os.environ['HL_STORE_FILE']}",
            f"storePassword={os.environ['ANDROID_KEYSTORE_PASSWORD']}",
            f"keyAlias={os.environ['ANDROID_KEY_ALIAS']}",
            f"keyPassword={os.environ['HL_KEY_PASSWORD']}",
        ]
    )
    + "\n",
    encoding="utf-8",
)
PY
echo "OK   wrote release keystore for assembleRelease"
