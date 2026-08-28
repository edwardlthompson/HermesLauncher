"""CONTRIBUTING.md keeps the coding-agent edition."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from contributing_agent import check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class ContributingAgentTests(unittest.TestCase):
    def test_repo(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-contributing-agent.sh", text)


if __name__ == "__main__":
    unittest.main()
