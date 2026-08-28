"""Winget stub schema gate."""
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
    candidate = Path(os.environ.get("ProgramFiles", r"C:\Program Files")) / "Git" / "bin" / "bash.exe"
    return str(candidate) if candidate.is_file() else shutil.which("bash")


class WingetStubTests(unittest.TestCase):
    def test_valid_stub_passes(self) -> None:
        bash = _bash()
        if not bash:
            self.skipTest("bash not available")
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "manifest.stub.yaml"
            path.write_text(
                "PackageIdentifier: Ex.App\nPackageVersion: 1.0.0\n"
                "ManifestVersion: 1.6.0\nLicense: MIT\nInstallerSha256: abc\n",
                encoding="utf-8",
            )
            proc = subprocess.run(
                [bash, "scripts/validate-winget-stub.sh", str(path)],
                cwd=ROOT,
                capture_output=True,
                text=True,
                check=False,
            )
            self.assertEqual(proc.returncode, 0, proc.stdout + proc.stderr)


if __name__ == "__main__":
    unittest.main()
