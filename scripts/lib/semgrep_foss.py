"""FOSS Semgrep config gate. Run the scanner when installed; never require SaaS."""
from __future__ import annotations

import os
import shutil
import subprocess
from pathlib import Path

CONFIG = Path(".semgrep.yml")
FORBIDDEN = ("SEMGREP_APP_TOKEN", "semgrep.dev/login", "returntocorp/semgrep-action")


def require_scan() -> bool:
    raw = os.environ.get("REQUIRE_SEMGREP", "").strip().lower()
    return raw in {"1", "true", "yes"}


def check_config(root: Path) -> list[str]:
    path = root / CONFIG
    if not path.is_file():
        return [f"MISSING: {CONFIG.as_posix()}"]
    text = path.read_text(encoding="utf-8")
    errors: list[str] = []
    if "FOSS-only" not in text:
        errors.append(".semgrep.yml must say FOSS-only")
    if "SEMGREP_APP_TOKEN" in text and "Do not set SEMGREP_APP_TOKEN" not in text:
        errors.append(".semgrep.yml must not require SEMGREP_APP_TOKEN")
    security = (root / ".github" / "workflows" / "security.yml").read_text(encoding="utf-8")
    for needle in FORBIDDEN:
        if needle in security and needle != "SEMGREP_APP_TOKEN":
            errors.append(f"security.yml must not use {needle}")
    if "semgrep --config .semgrep.yml" not in security:
        errors.append("security.yml must run FOSS semgrep --config .semgrep.yml")
    if "--metrics=off" not in security:
        errors.append("security.yml must pass --metrics=off")
    return errors


def run_scan(root: Path, *, which=shutil.which) -> int:
    exe = which("semgrep")
    if not exe:
        if require_scan():
            print("FAIL semgrep missing on PATH")
            return 1
        print("SKIP semgrep scan (not on PATH; config still required)")
        return 0
    try:
        proc = subprocess.run(
            [exe, "--config", str(root / CONFIG), "--metrics=off", "--error", "--quiet"],
            cwd=root,
            capture_output=True,
            text=True,
            timeout=180,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired) as exc:
        print(f"FAIL semgrep: {exc}")
        return 1
    out = (proc.stdout or "") + (proc.stderr or "")
    if out.strip():
        print(out.rstrip())
    if proc.returncode != 0:
        print("FAIL: semgrep")
        return 1
    print("OK: semgrep FOSS")
    return 0


def main() -> int:
    root = Path.cwd()
    errors = check_config(root)
    if errors:
        print("Semgrep FOSS check failed:")
        for item in errors:
            print(f"  {item}")
        return 1
    return run_scan(root)


if __name__ == "__main__":
    raise SystemExit(main())
