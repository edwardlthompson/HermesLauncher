"""Patch-only Gradle pin apply."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from gradle_apply import apply_mapping, is_patch_bump, kotlin_ok, parse_pins  # noqa: E402


class GradleApplyTests(unittest.TestCase):
    def test_patch_only(self) -> None:
        self.assertTrue(is_patch_bump("8.13.0", "8.13.1"))
        self.assertFalse(is_patch_bump("8.13.0", "8.14.0"))

    def test_kotlin_ceiling(self) -> None:
        self.assertTrue(kotlin_ok("org.jetbrains.kotlin.android", "2.3.21"))
        self.assertFalse(kotlin_ok("org.jetbrains.kotlin.android", "2.3.30"))

    def test_apply_and_skip_major(self) -> None:
        text = 'id("com.android.application") version "8.13.0"\n'
        out, changed = apply_mapping(text, {"com.android.application": "8.13.1"})
        self.assertIn("8.13.1", out)
        self.assertTrue(any("8.13.1" in c for c in changed))
        same, skipped = apply_mapping(text, {"com.android.application": "9.0.0"})
        self.assertEqual(same, text)
        self.assertTrue(any("not patch" in c for c in skipped))

    def test_parse_pins(self) -> None:
        self.assertEqual(parse_pins("a=1.2.3, b=4.5.6"), {"a": "1.2.3", "b": "4.5.6"})


if __name__ == "__main__":
    unittest.main()
