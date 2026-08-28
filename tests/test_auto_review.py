"""Auto-review permissions fixtures."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from auto_review import check_repo, classify  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class AutoReviewTests(unittest.TestCase):
    def test_repo_fixtures(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_block_beats_unknown(self) -> None:
        self.assertEqual(classify("git push origin main"), "block")
        self.assertEqual(classify("echo hello"), "unknown")

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-auto-review.sh", text)


if __name__ == "__main__":
    unittest.main()
