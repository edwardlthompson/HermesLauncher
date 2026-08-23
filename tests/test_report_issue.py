"""Dry-run report-issue must not leak process env secrets."""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from report_issue import compose  # noqa: E402


class ReportIssueTests(unittest.TestCase):
    def test_print_strips_token_and_skips_env(self) -> None:
        os.environ["UNIT_TEST_FAKE_SECRET"] = "should-not-appear"
        title, body = compose(
            "crash",
            "boom ghp_abcdefghijklmnopqrstuvwxyz012345",
            "at C:\\Users\\Ada\\app.ts",
            "1.2.3",
        )
        self.assertNotIn("ghp_", body)
        self.assertNotIn("Ada", body)
        self.assertNotIn("should-not-appear", body)
        self.assertNotIn("UNIT_TEST_FAKE_SECRET", body)
        self.assertTrue(title.startswith("[crash]"))


if __name__ == "__main__":
    unittest.main()
