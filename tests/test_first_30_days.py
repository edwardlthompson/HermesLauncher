"""FIRST_30_DAYS.md stays aligned with docs/first-30-days.json."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from first_30_days import check_repo, playbook_pointer  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class First30DaysTests(unittest.TestCase):
    def test_repo(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_playbook_pointer_names_json(self) -> None:
        self.assertIn("docs/first-30-days.json", playbook_pointer())

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-first-30-days.sh", text)
        self.assertIn("docs/first-30-days.json", text)


if __name__ == "__main__":
    unittest.main()
