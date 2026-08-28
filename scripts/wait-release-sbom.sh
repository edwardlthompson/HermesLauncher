#!/usr/bin/env bash
# Poll GitHub Release assets until sbom.cyclonedx.json and openvex.json appear (or timeout).
# Usage: scripts/wait-release-sbom.sh [tag] [--wait SEC]
set -euo pipefail
WAIT=300
TAG=""
while [ $# -gt 0 ]; do
  case "$1" in
    --wait) WAIT="${2:-300}"; shift 2 ;;
    --wait=*) WAIT="${1#*=}"; shift ;;
    *) TAG="$1"; shift ;;
  esac
done

if ! command -v gh >/dev/null 2>&1; then
  echo "WARN: gh not installed; skip SBOM wait"
  exit 0
fi

if [ -z "$TAG" ]; then
  TAG="$(gh release view --json tagName -q .tagName 2>/dev/null || true)"
fi
if [ -z "$TAG" ]; then
  echo "WARN: no release tag; skip SBOM wait"
  exit 0
fi

deadline=$((SECONDS + WAIT))
while [ "$SECONDS" -lt "$deadline" ]; do
  names="$(gh release view "$TAG" --json assets -q '.assets[].name' 2>/dev/null || true)"
  if echo "$names" | grep -qx 'sbom.cyclonedx.json' && echo "$names" | grep -qx 'openvex.json'; then
    echo "OK   SBOM + OpenVEX assets on $TAG"
    exit 0
  fi
  sleep 15
done
echo "FAIL: timed out after ${WAIT}s waiting for sbom.cyclonedx.json and openvex.json on $TAG"
exit 1
