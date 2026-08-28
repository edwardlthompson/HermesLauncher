"""Worktree setup skips stack installs on the primary checkout."""
from __future__ import annotations

import os
import shutil
import subprocess
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def _bash() -> str | None:
    if os.name != "nt":
        return shutil.which("bash")
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


class WorktreeSetupTests(unittest.TestCase):
    def test_skips_install_when_root_unset(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        env = {**os.environ}
        env.pop("ROOT_WORKTREE_PATH", None)
        proc = subprocess.run(
            [bash, ".cursor/setup-worktree-unix.sh"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            env=env,
        )
        self.assertEqual(proc.returncode, 0, proc.stdout + proc.stderr)
        combined = proc.stdout + proc.stderr
        self.assertIn("SKIP stack install", combined)
        self.assertNotIn("OK npm ci", combined)


if __name__ == "__main__":
    unittest.main()
