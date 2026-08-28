"""Pages analytics gate used by /regress."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from pages_analytics import check_repo, scan_text  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class PagesAnalyticsTests(unittest.TestCase):
    def test_repo_clean(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_detects_gtag(self) -> None:
        self.assertIn("gtag(", scan_text('<script>gtag("config","G-X")</script>'))

    def test_regress_invokes_check(self) -> None:
        text = (ROOT / ".cursor" / "commands" / "regress.md").read_text(encoding="utf-8")
        self.assertIn("check-pages-analytics", text)


if __name__ == "__main__":
    unittest.main()
