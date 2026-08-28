"""CITATION.cff version + date-released (YYYY-MM-DD)."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from citation_sync import sync_citation  # noqa: E402


class CitationSyncTests(unittest.TestCase):
    def test_normalizes_datetime_released(self) -> None:
        text = "version: 0.24.0\ndate-released: 2026-08-17T00:00:00.000Z\n"
        out = sync_citation(text, "0.25.0", "2026-08-27")
        self.assertIn("version: 0.25.0", out)
        self.assertIn("date-released: 2026-08-27\n", out)
        self.assertNotIn("T00:00:00", out)


if __name__ == "__main__":
    unittest.main()
