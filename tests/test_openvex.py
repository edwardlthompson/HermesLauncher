"""OpenVEX example next to the release SBOM."""
from __future__ import annotations

import json
import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from openvex_check import check_repo, validate_openvex  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class OpenVexTests(unittest.TestCase):
    def test_example_valid(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_rejects_empty(self) -> None:
        self.assertTrue(validate_openvex({}))

    def test_release_and_wait_mention_vex(self) -> None:
        release = (ROOT / ".github/workflows/release.yml").read_text(encoding="utf-8")
        wait = (ROOT / "scripts/wait-release-sbom.sh").read_text(encoding="utf-8")
        self.assertIn("openvex.json", release)
        self.assertIn("openvex.json", wait)
        ignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn("openvex.json", ignore)


if __name__ == "__main__":
    unittest.main()
