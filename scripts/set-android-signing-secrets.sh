#!/usr/bin/env bash
# Upload local gitignored keystore files to GitHub Actions repository secrets.
# Does not print passwords or keystore bytes. Requires gh auth with repo admin.
# Usage: scripts/set-android-signing-secrets.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
# shellcheck source=lib/resolve-python.sh
. "$ROOT/scripts/lib/resolve-python.sh"

PROPS="$ROOT/examples/android/keystore.properties"
if [ ! -f "$PROPS" ]; then
  echo "FAIL: $PROPS missing. Copy it from the Windows HermesLauncher tree." >&2
  exit 1
fi
if ! command -v gh >/dev/null 2>&1; then
  echo "FAIL: gh CLI not on PATH" >&2
  exit 1
fi

export PYTHONPATH="$ROOT/scripts/lib${PYTHONPATH:+:$PYTHONPATH}"
exec "$PY" "$ROOT/scripts/lib/set_android_signing_secrets.py" "$PROPS"
