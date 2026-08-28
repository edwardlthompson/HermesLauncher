"""Filter GitHub Actions runs so /coach ignores closed Release Please branches."""
from __future__ import annotations

import json
import shutil
import subprocess
import sys
from pathlib import Path

SKIP_PREFIXES = ("release-please--branches--",)


def _gh(args: list[str], timeout: int = 20) -> str:
    gh = shutil.which("gh")
    if not gh:
        return ""
    try:
        proc = subprocess.run(
            [gh, *args],
            capture_output=True,
            text=True,
            timeout=timeout,
            check=False,
        )
    except (OSError, subprocess.TimeoutExpired):
        return ""
    return proc.stdout if proc.returncode == 0 else ""


def filter_runs(runs: list[dict], default_branch: str = "main") -> list[dict]:
    kept: list[dict] = []
    for run in runs:
        branch = str(run.get("headBranch") or "")
        if any(branch.startswith(p) for p in SKIP_PREFIXES):
            continue
        kept.append(run)
    if not kept:
        return [r for r in runs if str(r.get("headBranch") or "") == default_branch]
    return kept


def format_run(run: dict) -> str:
    status = run.get("status") or ""
    conclusion = run.get("conclusion") or status
    title = run.get("displayTitle") or run.get("name") or "?"
    name = run.get("name") or ""
    branch = run.get("headBranch") or ""
    return f"{status}\t{conclusion}\t{title}\t{name}\t{branch}"


def print_ci_snapshot(root: Path) -> int:
    raw = _gh(
        [
            "run",
            "list",
            "--limit",
            "20",
            "--json",
            "status,conclusion,name,displayTitle,headBranch,url,databaseId",
        ]
    )
    if not raw.strip():
        print("WARN: gh could not read workflow runs (offline or unauthenticated).")
        return 0
    try:
        runs = json.loads(raw)
    except json.JSONDecodeError:
        print("WARN: gh run list returned non-JSON.")
        return 0
    if not isinstance(runs, list):
        return 0
    shown = filter_runs(runs)[:5]
    if not shown:
        print("No recent workflow runs on non-Release-Please branches.")
        return 0
    for run in shown:
        print(format_run(run))
    return 0


def main(argv: list[str] | None = None) -> int:
    del argv
    return print_ci_snapshot(Path(".").resolve())


if __name__ == "__main__":
    raise SystemExit(main())
