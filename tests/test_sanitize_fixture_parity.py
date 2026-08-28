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
        if ANDROID.is_file():
            self.assertEqual(canon, ANDROID.read_bytes())
        if WEB.is_file():
            self.assertEqual(canon, WEB.read_bytes())
        if not ANDROID.is_file() and not WEB.is_file():
            self.skipTest("stack sanitizer copies pruned")
        text = CANON.read_text(encoding="utf-8")
        self.assertIn("<redacted-injection>", text)
        self.assertIn("Ignore previous", text)


if __name__ == "__main__":
    unittest.main()
