"""First-failure interpreter for /tour verify."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from verify_first_failure import extract_first_failure, format_report  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class VerifyFirstFailureTests(unittest.TestCase):
    def test_hint_block_wins(self) -> None:
        log = (
            "=== verify: env schema ===\n"
            "MISSING: .env.example\n"
            "What failed: verify-env\n"
            "What that means: Env schema mismatch.\n"
            "What to run: bash scripts/check-env.sh\n"
            "Why: Local gates match CI.\n"
            "\n"
            "later noise FAIL: ignored\n"
        )
        first = extract_first_failure(log)
        self.assertIn("What failed: verify-env", first)
        report = format_report(1, log)
        self.assertIn("first failure", report)
        self.assertIn("/fix", report)

    def test_pass_and_commands(self) -> None:
        self.assertIn("passed", format_report(0, "ok"))
        tour = (ROOT / ".cursor/commands/tour.md").read_text(encoding="utf-8")
        help_tour = (ROOT / "docs/help/TOUR.md").read_text(encoding="utf-8")
        self.assertIn("tour-verify", tour)
        self.assertIn("tour-verify", help_tour)
        verify = (ROOT / "scripts/verify.sh").read_text(encoding="utf-8")
        self.assertIn("--quick", verify)


if __name__ == "__main__":
    unittest.main()
