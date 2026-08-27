#!/usr/bin/env python3
"""sessionStart: one-line stack / distribution context. Fail-open."""
from __future__ import annotations

import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent.parent


def main() -> None:
    parts: list[str] = []
    sel = ROOT / ".cursor/stack-selection.json"
    if sel.is_file():
        try:
            data = json.loads(sel.read_text(encoding="utf-8"))
            stack = data.get("stack", "?")
            tier = data.get("distribution_tier", "foss")
            parts.append(f"stack={stack} tier={tier}")
        except json.JSONDecodeError:
            pass
    cpus = os.cpu_count() or 1
    ram = "?"
    jobs = "?"
    ollama = "down"
    try:
        sys.path.insert(0, str(ROOT / "scripts" / "lib"))
        from local_resources import ollama_up, ram_gb_or_none, recommended_check_jobs

        gb = ram_gb_or_none()
        ram = str(gb) if gb is not None else "?"
        jobs = str(recommended_check_jobs())
        ollama = "up" if ollama_up() else "down"
    except Exception:
        pass
    parts.append(
        f"local-first cpus={cpus} ram={ram} jobs={jobs} ollama={ollama}: "
        f"prefer This Computer + parallel Task/worktrees/"
        f"/best-of-n over Cloud; BOOTSTRAP_CHECK_JOBS overrides gate parallelism"
    )
    if parts:
        print(json.dumps({"user_message": "Session context: " + ", ".join(parts)}))


if __name__ == "__main__":
    main()
