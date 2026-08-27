#!/usr/bin/env python3
"""INFO probe for local CPU/RAM/Ollama/emulator. Exit 0 unless misconfig."""
from __future__ import annotations

import json
import os
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from local_resources import (  # noqa: E402
    InvalidJobs,
    cpu_count,
    env_jobs,
    ollama_up,
    ram_gb_or_none,
    recommended_check_jobs,
    recommended_stack_slots,
)


def _adb() -> str | None:
    return shutil.which("adb")


def _sdk() -> str | None:
    for key in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        raw = os.environ.get(key, "").strip()
        if raw and Path(raw).is_dir():
            return raw
    return None


def _kvm() -> str:
    if sys.platform.startswith("linux"):
        return "yes" if Path("/dev/kvm").exists() else "no"
    if sys.platform == "darwin":
        return "hv"
    if sys.platform == "win32":
        return "unknown"
    return "unknown"


def _jobs_misconfig() -> str | None:
    for name in ("BOOTSTRAP_CHECK_JOBS", "FEATURE_GATE_JOBS"):
        try:
            env_jobs(name)
        except InvalidJobs as exc:
            return str(exc)
    return None


def main() -> int:
    bad = _jobs_misconfig()
    if bad:
        print(f"FAIL: {bad}", file=sys.stderr)
        return 2
    sel = ROOT / ".cursor" / "stack-selection.json"
    if sel.is_file():
        try:
            json.loads(sel.read_text(encoding="utf-8"))
        except json.JSONDecodeError as exc:
            print(f"FAIL: corrupt stack-selection.json: {exc}", file=sys.stderr)
            return 2
    cpu = cpu_count()
    ram = ram_gb_or_none()
    try:
        jobs = recommended_check_jobs()
        slots = recommended_stack_slots()
    except InvalidJobs as exc:
        print(f"FAIL: {exc}", file=sys.stderr)
        return 2
    ollama = "up" if ollama_up() else "down"
    sdk = _sdk() or "none"
    adb = "yes" if _adb() else "no"
    print(f"cpus={cpu} ram_gb={ram if ram is not None else 'unknown'} jobs={jobs} slots={slots}")
    print(f"ollama={ollama} emulator_gpu=unknown sdk={sdk} adb={adb} kvm={_kvm()}")
    mcp = ROOT / ".cursor" / "mcp.json"
    print(f"mcp.json={'yes' if mcp.is_file() else 'no (copy mcp.foss.example optional)'}")
    if ram is not None and ram < 16 and ollama == "up":
        print("WARN: RAM < 16G with Ollama up; skip /emulator or FEATURE_GATE_JOBS=1", file=sys.stderr)
    print("GPU: no CUDA in template; Ollama/emulator use host GPU when present")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
