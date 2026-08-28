"""Local commit-msg hook gate skips in CI and fails when missing."""
from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
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


class PreCommitHookTests(unittest.TestCase):
    def test_skips_in_ci(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        proc = subprocess.run(
            [bash, "scripts/check-pre-commit-hooks.sh"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            env={**os.environ, "CI": "true"},
        )
        self.assertEqual(proc.returncode, 0, proc.stdout + proc.stderr)
        self.assertIn("skipped in CI", proc.stdout + proc.stderr)

    def test_fails_without_hook(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        with tempfile.TemporaryDirectory() as tmp:
            repo = Path(tmp)
            subprocess.run(["git", "init"], cwd=repo, check=True, capture_output=True)
            script = repo / "scripts" / "check-pre-commit-hooks.sh"
            script.parent.mkdir()
            script.write_text(
                (ROOT / "scripts" / "check-pre-commit-hooks.sh").read_text(encoding="utf-8"),
                encoding="utf-8",
            )
            env = {**os.environ}
            env.pop("CI", None)
            env.pop("GITHUB_ACTIONS", None)
            empty = repo / "empty.gitconfig"
            empty.write_text("", encoding="utf-8")
            env["GIT_CONFIG_GLOBAL"] = str(empty)
            env["GIT_CONFIG_SYSTEM"] = str(empty)
            env["GIT_CONFIG_NOSYSTEM"] = "1"
            proc = subprocess.run(
                [bash, str(script.as_posix())],
                cwd=repo,
                capture_output=True,
                text=True,
                check=False,
                env=env,
            )
            self.assertEqual(proc.returncode, 1, proc.stdout + proc.stderr)
            self.assertIn("commit-msg hook missing", proc.stdout + proc.stderr)


if __name__ == "__main__":
    unittest.main()
