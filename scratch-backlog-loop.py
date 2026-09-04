import json
import os
import subprocess
from pathlib import Path

os.environ["PYTHONUTF8"] = "1"
os.environ["PYTHONIOENCODING"] = "utf-8"
root = Path(".").resolve()

while True:
    p = subprocess.run(
        ["python", "scripts/agent-run.py", "build-sprint-status", "--json", "--lane", "auto"],
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    st = json.loads(p.stdout)
    nr = st.get("next_row") or {}
    act = nr.get("action")
    print("NEXT", act, nr.get("sprint"), (nr.get("task") or "")[:90], flush=True)
    if act not in ("automate_adb", "automate_human"):
        Path(".cursor/last-build-status.json").write_text(
            json.dumps(st, indent=2, ensure_ascii=False),
            encoding="utf-8",
        )
        break
    att = subprocess.run(
        [
            "python",
            "scripts/agent-run.py",
            "attempt-build-plan-row",
            "--owner",
            nr["owner"],
            "--task",
            nr["task"],
            "--sprint",
            nr["sprint"],
            "--json",
        ],
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    if att.returncode == 0:
        print("SUCCESS", att.stdout[-300:], flush=True)
        break
    try:
        reason = json.loads(att.stdout).get("reason", "automation failed")
    except Exception:
        reason = (att.stdout or att.stderr or "automation failed")[:200]
    add = subprocess.run(
        [
            "python",
            "scripts/agent-run.py",
            "build-backlog",
            "add",
            "--owner",
            nr["owner"],
            "--task",
            nr["task"],
            "--sprint",
            nr["sprint"],
            "--reason",
            str(reason),
        ],
        capture_output=True,
        text=True,
        encoding="utf-8",
    )
    print("backlog", add.stdout.strip(), flush=True)
    if add.stdout.strip() == "duplicate":
        print("STUCK_DUPLICATE", flush=True)
        break
