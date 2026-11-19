#!/usr/bin/env bash
# Validate F-Droid/Fastlane metadata scaffold (AGENT gate; APK hashes remain ADB).
# Usage: scripts/verify-fdroid-metadata.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

FASTLANE="$ROOT/fastlane/metadata/android/en-US"
META="$ROOT/examples/android/metadata/en-US"
VERSION_JSON="$ROOT/schemas/golden-path/app-version.json"
GRADLE="$ROOT/examples/android/app/build.gradle.kts"
ERRORS=0

fail() {
  echo "FAIL: $1"
  ERRORS=$((ERRORS + 1))
}

ok() {
  echo "OK   $1"
}

if [ ! -d "$FASTLANE" ]; then
  fail "missing $FASTLANE (F-Droid scrapes repo-root fastlane/)"
else
  ok "root fastlane metadata directory present"
fi

for f in title.txt short_description.txt full_description.txt; do
  if [ ! -s "$FASTLANE/$f" ]; then
    fail "missing or empty $FASTLANE/$f"
  else
    ok "fastlane $f present"
  fi
done

if [ ! -s "$FASTLANE/images/icon.png" ]; then
  fail "missing $FASTLANE/images/icon.png"
else
  ok "fastlane icon.png present"
fi

if [ ! -d "$FASTLANE/images/phoneScreenshots" ]; then
  fail "missing $FASTLANE/images/phoneScreenshots/"
else
  ok "fastlane phoneScreenshots directory present"
fi

if [ ! -d "$META" ]; then
  fail "missing $META"
else
  ok "metadata directory present"
fi

for f in title.txt short_description.txt full_description.txt; do
  if [ ! -s "$META/$f" ]; then
    fail "missing or empty $META/$f"
  else
    ok "$f present"
  fi
done

if [ ! -f "$VERSION_JSON" ]; then
  fail "missing $VERSION_JSON"
else
  VERSION_CODE="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["versionCode"])' "$VERSION_JSON")"
  VERSION_NAME="$(python3 -c 'import json,sys; print(json.load(open(sys.argv[1]))["versionName"])' "$VERSION_JSON")"
  if [ -z "${VERSION_CODE:-}" ] || [ -z "${VERSION_NAME:-}" ]; then
    fail "could not parse version from app-version.json"
  elif [ ! -s "$FASTLANE/changelogs/${VERSION_CODE}.txt" ]; then
    fail "missing changelog $FASTLANE/changelogs/${VERSION_CODE}.txt"
  else
    ok "changelog for versionCode ${VERSION_CODE} (${VERSION_NAME})"
  fi
  if [ -f "$GRADLE" ] && ! grep -q 'loadAppVersion' "$GRADLE"; then
    fail "app/build.gradle.kts must read schemas/golden-path/app-version.json"
  fi
fi

if [ ! -d "$META/images" ]; then
  fail "missing $META/images/ (add README + assets before submit)"
else
  ok "images directory present"
fi

if [ -f "$ROOT/LICENSE" ]; then
  ok "root LICENSE present (MIT for template)"
else
  fail "missing root LICENSE"
fi

if [ -d "$ROOT/examples/android/fastlane/metadata/android/en-US" ]; then
  ok "fastlane metadata mirror present"
fi

echo ""
echo "SKIP [ADB] reproducible APK hash verification — run on device/emulator per modules/android/MODULE.md"

# Dummy/placeholder screenshots fail even when metadata text is present.
if ! bash "$ROOT/scripts/check-fdroid-screenshots.sh"; then
  fail "dummy F-Droid screenshots are not allowed"
fi

if [ "$ERRORS" -gt 0 ]; then
  echo "${ERRORS} F-Droid metadata check(s) failed"
  exit 1
fi

echo "F-Droid metadata scaffold verified"
