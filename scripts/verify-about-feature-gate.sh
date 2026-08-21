#!/usr/bin/env bash
# Verify lego removal: feature-gate passes with About present and after simulated removal.
# Usage: scripts/verify-about-feature-gate.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

# shellcheck source=lib/resolve-python.sh
. "$(cd "$(dirname "$0")" && pwd)/lib/resolve-python.sh"

WEB_SRC="$ROOT/examples/web/src"
WEB_E2E="$ROOT/examples/web/e2e"
BACKUP="$(mktemp -d)"

ABOUT_TRACKED=(
  examples/web/src/about
  examples/web/src/main.ts
  examples/web/src/appBootstrap.ts
  examples/web/src/appBootstrap.test.ts
  examples/web/src/AppShell.ts
  examples/web/src/components/AboutPanel.ts
  examples/web/src/settings/preferences.ts
  examples/web/e2e/app.spec.ts
  examples/web/vitest.config.ts
)

restore() {
  if [ -d "$BACKUP/about" ]; then
    rm -rf "$WEB_SRC/about"
    cp -a "$BACKUP/about" "$WEB_SRC/about"
    for rel in main.ts appBootstrap.ts appBootstrap.test.ts AppShell.ts; do
      if [ -f "$BACKUP/$rel" ]; then
        cp -a "$BACKUP/$rel" "$WEB_SRC/$rel"
      fi
    done
    if [ -f "$BACKUP/components/AboutPanel.ts" ]; then
      cp -a "$BACKUP/components/AboutPanel.ts" "$WEB_SRC/components/AboutPanel.ts"
    fi
    if [ -f "$BACKUP/settings/preferences.ts" ]; then
      cp -a "$BACKUP/settings/preferences.ts" "$WEB_SRC/settings/preferences.ts"
    fi
    if [ -f "$BACKUP/app.spec.ts" ]; then
      cp -a "$BACKUP/app.spec.ts" "$WEB_E2E/app.spec.ts"
    fi
    if [ -f "$BACKUP/vitest.config.ts" ]; then
      cp -a "$BACKUP/vitest.config.ts" "$ROOT/examples/web/vitest.config.ts"
    fi
  else
    echo "WARN: About backup missing; restoring tracked slice from HEAD"
    git checkout HEAD -- "${ABOUT_TRACKED[@]}" || true
  fi
  rm -rf "$BACKUP"
}
trap restore EXIT

echo "=== About feature gate verification ==="

echo "1/2 Gate with About feature present..."
bash scripts/feature-gate.sh --stack web --step about-with

if [ ! -d "$WEB_SRC/about" ]; then
  echo "WARN: About slice missing before backup; restoring from HEAD"
  git checkout HEAD -- "${ABOUT_TRACKED[@]}"
fi
mkdir -p "$BACKUP/components" "$BACKUP/settings"
cp -a "$WEB_SRC/about" "$BACKUP/about"
cp -a "$WEB_SRC/main.ts" "$BACKUP/main.ts"
cp -a "$WEB_SRC/appBootstrap.ts" "$BACKUP/appBootstrap.ts"
cp -a "$WEB_SRC/appBootstrap.test.ts" "$BACKUP/appBootstrap.test.ts"
cp -a "$WEB_SRC/AppShell.ts" "$BACKUP/AppShell.ts"
cp -a "$WEB_SRC/components/AboutPanel.ts" "$BACKUP/components/AboutPanel.ts"
cp -a "$WEB_SRC/settings/preferences.ts" "$BACKUP/settings/preferences.ts"
cp -a "$WEB_E2E/app.spec.ts" "$BACKUP/app.spec.ts"
cp -a "$ROOT/examples/web/vitest.config.ts" "$BACKUP/vitest.config.ts"

$PY << 'PY'
from pathlib import Path
import os
import re
import shutil
import time

web = Path("examples/web/src")
e2e = Path("examples/web/e2e")

def write_lf(path: Path, text: str) -> None:
    # Biome format:check fails on CRLF stubs on Windows — always write LF.
    # Write via sibling tmp + replace; retry if the target is briefly locked.
    data = text.replace("\r\n", "\n").encode("utf-8")
    tmp = path.with_name(path.name + ".about-stub.tmp")
    last: OSError | None = None
    for attempt in range(8):
        try:
            tmp.write_bytes(data)
            os.replace(tmp, path)
            return
        except OSError as exc:
            last = exc
            time.sleep(0.25 * (attempt + 1))
    raise last if last else OSError("write_lf failed")


write_lf(
    web.joinpath("main.ts"),
    """import "./style.css";
import { createThemeToggle } from "./components/ThemeToggle";
import { isOnline } from "./greet";
import { t } from "./i18n";
import { initTheme } from "./theme";

const app = document.querySelector<HTMLDivElement>("#app");
if (!app) throw new Error("App root element not found");
const root = app;

function render(): void {
  const online = isOnline();
  const statusKey = online ? "app.status.online" : "app.status.offline";
  root.innerHTML = `
    <main>
      <div class="gp-header">
        <h1 class="gp-title">${t("app.title")}</h1>
        <div class="gp-header-actions"></div>
      </div>
      <p class="gp-headline">${t("app.greeting")}</p>
      <p class="gp-body" data-testid="status">${t(statusKey)}</p>
    </main>
  `;
  const actions = root.querySelector<HTMLDivElement>(".gp-header-actions");
  if (actions) actions.insertBefore(createThemeToggle(), actions.firstChild);
}

initTheme();
render();
window.addEventListener("online", render);
window.addEventListener("offline", render);
""",
)

# Settings is theme-only and does not import About — leave preferences.ts in place.

for path in (
    web / "about",
    web / "appBootstrap.ts",
    web / "appBootstrap.test.ts",
    web / "AppShell.ts",
    web / "components" / "AboutPanel.ts",
):
    if path.is_dir():
        shutil.rmtree(path, ignore_errors=True)
    elif path.exists():
        path.unlink()

vitest = Path("examples/web/vitest.config.ts")
if vitest.is_file():
    text = vitest.read_text(encoding="utf-8").replace("\r\n", "\n")
    patched, n = re.subn(
        r"include:\s*\[.*?\]",
        'include: [\n        "src/settings/preferences.ts",\n        "src/greet.ts",\n      ]',
        text,
        count=1,
        flags=re.S,
    )
    if n != 1:
        raise SystemExit("could not rewrite vitest coverage include for about-without")
    write_lf(vitest, patched)

write_lf(
    e2e.joinpath("app.spec.ts"),
    """import { expect, test } from "@playwright/test";

test("renders golden path heading without About slice", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Golden Path PWA" })).toBeVisible();
  await expect(page.getByTestId("status")).toBeVisible();
});
""",
)
PY

# Normalize stub formatting for Biome format:check (import order, etc.)
if command -v npm >/dev/null 2>&1 && [ -f examples/web/package.json ]; then
  (cd examples/web && npm run format >/dev/null 2>&1) || true
fi

echo "2/2 Gate after About removal (in-place, restored on exit)..."
set +e
ABOUT_WITHOUT_JSON="$(bash scripts/feature-gate.sh --stack web --step about-without --json)"
ABOUT_WITHOUT_EXIT=$?
set -e
if [ "$ABOUT_WITHOUT_EXIT" -ne 0 ]; then
  echo "$ABOUT_WITHOUT_JSON"
  echo "FAIL: about-without feature-gate (exit $ABOUT_WITHOUT_EXIT)"
  exit "$ABOUT_WITHOUT_EXIT"
fi

echo "About add/remove verification passed"
