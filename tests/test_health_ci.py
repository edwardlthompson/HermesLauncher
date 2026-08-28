"""Health CI snapshot skips Release Please branches."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from health_ci import filter_runs, format_run  # noqa: E402


class FilterRunsTests(unittest.TestCase):
    def test_drops_release_please_branch(self) -> None:
        runs = [
            {"headBranch": "release-please--branches--main", "name": "Dependency Review"},
            {"headBranch": "main", "name": "CI", "status": "completed", "conclusion": "success"},
        ]
        kept = filter_runs(runs)
        self.assertEqual(len(kept), 1)
        self.assertEqual(kept[0]["name"], "CI")

    def test_format_line(self) -> None:
        line = format_run(
            {
                "status": "completed",
                "conclusion": "success",
                "displayTitle": "feat: x",
                "name": "CI",
                "headBranch": "main",
            }
        )
        self.assertIn("main", line)
        self.assertIn("CI", line)


if __name__ == "__main__":
    unittest.main()
