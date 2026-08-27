"""Tests for feature-gate parallel stack dispatcher contracts."""
from __future__ import annotations

import os
import shutil
import subprocess
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

ROOT = Path(__file__).resolve().parent.parent
LIB = ROOT / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from local_resources import schedule_waves  # noqa: E402
from run_feature_stacks import main as stacks_main  # noqa: E402


def _bash() -> str | None:
    if os.name == "nt":
        for base in (
            os.environ.get("ProgramFiles", r"C:\Program Files"),
            os.environ.get("ProgramFiles(x86)", r"C:\Program Files (x86)"),
        ):
            candidate = Path(base) / "Git" / "bin" / "bash.exe"
            if candidate.is_file():
                return str(candidate)
        which = shutil.which("bash")
        if which and "System32" not in which.replace("/", "\\"):
            return which
        return None
    return shutil.which("bash")


class WaveTests(unittest.TestCase):
    def test_ci_four_stacks_android_alone(self) -> None:
        waves = schedule_waves(["web", "python", "android", "node"], 2)
        for wave in waves:
            if "android" in wave:
                self.assertEqual(wave, ["android"])


class DispatcherTests(unittest.TestCase):
    def test_strips_json_flag(self) -> None:
        seen: list[list[str]] = []

        def fake_child(stack: str, extra: list[str], env: dict[str, str]) -> tuple[str, int, str]:
            seen.append(extra)
            return stack, 0, "ok\n"

        with patch("run_feature_stacks.discover_stacks", return_value=["web"]):
            with patch("run_feature_stacks.recommended_stack_slots", return_value=1):
                with patch("run_feature_stacks.run_child", side_effect=fake_child):
                    self.assertEqual(stacks_main(["--json", "--strict"]), 0)
        self.assertEqual(seen, [[]])

    def test_invalid_jobs_exit_2(self) -> None:
        with patch.dict(os.environ, {"FEATURE_GATE_JOBS": "abc"}):
            self.assertEqual(stacks_main([]), 2)


class ShellContractTests(unittest.TestCase):
    def test_skip_preamble_multi_exits_2(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        proc = subprocess.run(
            [bash, "scripts/feature-gate.sh", "--skip-preamble", "--stack", "multi"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
        )
        self.assertEqual(proc.returncode, 2)
        self.assertIn("skip-preamble", (proc.stdout + proc.stderr).lower())

    def test_child_json_not_on_stdout(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        proc = subprocess.run(
            [
                bash,
                "scripts/feature-gate.sh",
                "--skip-preamble",
                "--stack",
                "lightroom",
                "--json",
            ],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            env={**os.environ, "FEATURE_GATE_CHILD": "1"},
        )
        stripped = (proc.stdout or "").strip()
        self.assertFalse(stripped.startswith("{"), msg=stripped[:200])

    def test_child_missing_go_skips(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        if shutil.which("go"):
            self.skipTest("go is installed")
        proc = subprocess.run(
            [bash, "scripts/feature-gate.sh", "--skip-preamble", "--stack", "go"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            env={**os.environ, "FEATURE_GATE_CHILD": "1"},
        )
        self.assertEqual(proc.returncode, 0, proc.stdout + proc.stderr)
        self.assertIn("Skipping go gate", proc.stdout + proc.stderr)

    def test_garbage_jobs_exit_2(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        proc = subprocess.run(
            [bash, "scripts/feature-gate.sh", "--stack", "web"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            env={**os.environ, "FEATURE_GATE_JOBS": "abc"},
        )
        self.assertEqual(proc.returncode, 2)


if __name__ == "__main__":
    unittest.main()
