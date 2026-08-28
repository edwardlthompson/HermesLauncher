"""Web and Android sanitizer fixtures must stay identical."""
from __future__ import annotations

from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parent.parent
CANON = ROOT / "schemas" / "golden-path" / "sanitize-fixtures.json"
ANDROID = (
    ROOT
    / "examples"
    / "android"
    / "app"
    / "src"
    / "test"
    / "resources"
    / "sanitize-fixtures.json"
)
WEB = (
    ROOT
    / "examples"
    / "web"
    / "src"
    / "privacy-report"
    / "sanitize-fixtures.json"
)


class SanitizeFixtureParityTests(unittest.TestCase):
    def test_copies_match_canonical(self) -> None:
        canon = CANON.read_bytes()
        self.assertEqual(canon, ANDROID.read_bytes())
        self.assertEqual(canon, WEB.read_bytes())


if __name__ == "__main__":
    unittest.main()
