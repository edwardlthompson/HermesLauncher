"""Dummy F-Droid screenshots fail the listing gate."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from fdroid_screenshots import check_tree  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
TINY_PNG = (
    b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01"
    b"\x08\x02\x00\x00\x00\x90wS\xde\x00\x00\x00\x0cIDATx\x9cc\xf8\x0f"
    b"\x00\x00\x01\x01\x00\x05\x18\xd8N\x00\x00\x00\x00IEND\xaeB`\x82"
)


class FdroidScreenshotsTests(unittest.TestCase):
    def test_repo_has_no_dummies(self) -> None:
        self.assertEqual(check_tree(ROOT), [])

    def test_dummy_name_fails(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            folder = root / "examples/android/metadata/en-US/images/phoneScreenshots"
            folder.mkdir(parents=True)
            (folder / "dummy.png").write_bytes(TINY_PNG)
            errors = check_tree(root)
            self.assertTrue(any("dummy" in item for item in errors), errors)

    def test_tiny_png_fails(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            folder = root / "examples/android/metadata/en-US/images/phoneScreenshots"
            folder.mkdir(parents=True)
            (folder / "home.png").write_bytes(TINY_PNG)
            errors = check_tree(root)
            self.assertTrue(any("1x1" in item for item in errors), errors)

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-fdroid-screenshots.sh", text)
        verify = (ROOT / "scripts" / "verify-fdroid-metadata.sh").read_text(encoding="utf-8")
        self.assertIn("check-fdroid-screenshots.sh", verify)


if __name__ == "__main__":
    unittest.main()
