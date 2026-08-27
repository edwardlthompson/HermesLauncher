"""Skip-contract tests for the local Android emulator wrapper."""
from __future__ import annotations

import os
import shutil
import subprocess
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SCRIPT = ROOT / "scripts" / "run-android-emulator-local.sh"


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


class EmulatorSkipTests(unittest.TestCase):
    def _run(self, env: dict[str, str], extra: list[str] | None = None) -> subprocess.CompletedProcess[str]:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        return subprocess.run(
            [bash, str(SCRIPT.relative_to(ROOT).as_posix()), *(extra or [])],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=False,
            env=env,
        )

    def test_help_keep_emulator(self) -> None:
        proc = self._run(os.environ.copy(), ["--help"])
        self.assertEqual(proc.returncode, 0)
        self.assertIn("--keep-emulator", proc.stdout)

    def test_android_home_missing_skips(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            missing = str(Path(tmp) / "no-sdk")
            env = {**os.environ, "ANDROID_HOME": missing, "ANDROID_SDK_ROOT": missing}
            env.pop("ANDROID_EMULATOR_LOCAL", None)
            proc = self._run(env)
        self.assertEqual(proc.returncode, 0)
        self.assertIn("SKIP", proc.stdout)

    def test_force_off_skips(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            fake = Path(tmp) / "sdk"
            fake.mkdir()
            env = {
                **os.environ,
                "ANDROID_HOME": str(fake),
                "ANDROID_SDK_ROOT": str(fake),
                "ANDROID_EMULATOR_LOCAL": "0",
            }
            proc = self._run(env)
        self.assertEqual(proc.returncode, 0)
        self.assertIn("ANDROID_EMULATOR_LOCAL=0", proc.stdout)


if __name__ == "__main__":
    unittest.main()
