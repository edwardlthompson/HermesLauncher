"""Local sprint smoke: Android HOME manifest plus BUILD_PLAN ✅ row map."""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

ROW = re.compile(r"^\s*-\s+✅\s+\[(?P<owner>AGENT|AUTO)\]\s+(?P<task>.+)$")
OPEN = re.compile(r"^\s*-\s+[🔲❌]\s+\[(?P<owner>AGENT|AUTO)\]")
APP = "HermesApplication"
HOME = "HermesLauncherActivity"


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser()
    p.add_argument("--root", type=Path, required=True)
    p.add_argument("--require", action="store_true")
    p.add_argument("--if-complete", action="store_true")
    p.add_argument("--dry-run", action="store_true")
    p.add_argument("--sprint", default="")
    return p.parse_args()


def open_agent_auto(text: str) -> int:
    return sum(1 for line in text.splitlines() if OPEN.match(line))


def done_rows(text: str, sprint: str) -> list[str]:
    rows: list[str] = []
    in_sprint = not sprint
    for line in text.splitlines():
        if line.startswith("### "):
            in_sprint = sprint.lower() in line.lower() if sprint else True
            continue
        if not in_sprint:
            continue
        match = ROW.match(line)
        if match:
            rows.append(match.group("task").strip())
    return rows


def android_probe(root: Path) -> dict:
    path = root / "examples" / "android" / "app" / "src" / "main" / "AndroidManifest.xml"
    text = path.read_text(encoding="utf-8") if path.is_file() else ""
    ok = APP in text and HOME in text and "<application" in text
    return {
        "name": "android_home",
        "ok": ok,
        "application": APP if APP in text else None,
        "activity": HOME if HOME in text else None,
        "path": str(path.relative_to(root)) if path.is_file() else None,
    }


def report(root: Path, payload: dict) -> None:
    out = root / ".cursor" / "sprint-smoke.json"
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(payload, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    args = parse_args()
    plan = (args.root / "BUILD_PLAN.md").read_text(encoding="utf-8")
    if args.if_complete and open_agent_auto(plan):
        print("smoke-sprint skip: AGENT/AUTO rows still open")
        return 0
    probe = android_probe(args.root)
    rows = done_rows(plan, args.sprint)
    mapped = [{"row": task, "ok": probe["ok"]} for task in rows]
    payload = {
        "ok": probe["ok"] and all(item["ok"] for item in mapped) if rows else probe["ok"],
        "dry_run": args.dry_run,
        "sprint": args.sprint or None,
        "probes": [probe],
        "rows": mapped,
    }
    if not args.dry_run:
        report(args.root, payload)
    print(json.dumps({"ok": payload["ok"], "rows": len(mapped), "report": ".cursor/sprint-smoke.json"}))
    if args.require and not payload["ok"]:
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
