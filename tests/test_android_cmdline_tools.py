"""Devcontainer Android cmdline-tools never auto-accept licenses."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from android_cmdline_tools import check_repo, scan_text  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class AndroidCmdlineToolsTests(unittest.TestCase):
    def test_repo_ok(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_detects_auto_license(self) -> None:
        self.assertTrue(scan_text("yes | sdkmanager --licenses"))

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-android-cmdline-tools.sh", text)


if __name__ == "__main__":
    unittest.main()
