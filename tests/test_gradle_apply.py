"""Patch-only Gradle pin apply."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from gradle_apply import (  # noqa: E402
    apply_depsonar_pins,
    apply_mapping,
    is_patch_bump,
    kotlin_ok,
    parse_pins,
)


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

    def test_depsonar_apply_blocks_kotlin_ceiling(self) -> None:
        import tempfile

        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            gradle = root / "examples" / "android" / "build.gradle.kts"
            gradle.parent.mkdir(parents=True)
            gradle.write_text(
                'id("org.jetbrains.kotlin.android") version "2.3.21"\n',
                encoding="utf-8",
            )
            notes = apply_depsonar_pins(
                root,
                {"org.jetbrains.kotlin.android": "2.3.30"},
                write=True,
            )
            self.assertTrue(any("blocked" in item for item in notes))
            text = gradle.read_text(encoding="utf-8")
            self.assertIn("2.3.21", text)
            self.assertNotIn("2.3.30", text)
        cmd = (Path(__file__).resolve().parent.parent / ".cursor" / "commands" / "update-deps.md")
        self.assertIn("apply-depsonar-gradle", cmd.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
