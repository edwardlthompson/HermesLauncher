"""OP12 UIAutomator smoke handlers for BUILD_PLAN ADB rows."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
import time
from pathlib import Path

from human_task_core import AttemptResult, run_cmd


def adb_serial_authorized(_root: Path, serial: str) -> bool:
    adb = os.environ.get("ADB", "adb")
    if os.name == "nt" and not shutil.which(adb):
        win = os.environ.get("LOCALAPPDATA", "")
        if win:
            candidate = Path(win) / "Android/Sdk/platform-tools/adb.exe"
            if candidate.is_file():
                adb = str(candidate)
    try:
        out = subprocess.run(
            [adb, "devices"],
            capture_output=True,
            text=True,
            check=False,
        )
    except FileNotFoundError:
        return False
    if out.returncode != 0:
        return False
    for line in out.stdout.splitlines()[1:]:
        parts = line.split()
        if len(parts) >= 2 and parts[0] == serial and parts[1] == "device":
            return True
    return False


def run_op12_smoke(root: Path, extra: list[str], *, allow_cache: bool) -> AttemptResult:
    script = root / "scripts/op12-device-smoke.py"
    if not script.is_file():
        return AttemptResult(1, "op12-smoke", "scripts/op12-device-smoke.py missing", True)
    serial = os.environ.get("HERMES_ADB_SERIAL", "b5214fc6")
    if not adb_serial_authorized(root, serial):
        return AttemptResult(1, "op12-smoke", f"serial {serial} not authorized", True)
    stamp = root / ".cursor/last-op12-smoke-ok"
    if allow_cache and stamp.is_file():
        age = time.time() - stamp.stat().st_mtime
        if age < 7200:
            return AttemptResult(
                0,
                "op12-smoke-cached",
                f"Reused OP12 smoke ok from {int(age)}s ago on {serial}",
                False,
            )
    cmd = [sys.executable, str(script), "--root", str(root), "--serial", serial, *extra]
    code, tail = run_cmd(root, cmd)
    if code == 0:
        stamp.parent.mkdir(parents=True, exist_ok=True)
        stamp.write_text(f"ok {serial}\n", encoding="utf-8")
        return AttemptResult(0, "op12-smoke", f"OP12 smoke passed on {serial}", False)
    return AttemptResult(1, "op12-smoke", tail or f"exit {code}", True)


def automate_op12_device_smoke(root: Path, _cfg: dict) -> AttemptResult:
    """Sideload + UIAutomator settings smoke on OP12 serial b5214fc6 only."""
    return run_op12_smoke(root, [], allow_cache=True)


def automate_op12_widget_dnd(root: Path, _cfg: dict) -> AttemptResult:
    """OP12 smoke plus widget tray long-press-drag onto the desktop grid."""
    return run_op12_smoke(root, ["--require-widget-dnd"], allow_cache=False)
