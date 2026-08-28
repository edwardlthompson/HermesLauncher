"""Android Golden Path must not pull Play Services or Firebase."""

from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ANDROID = ROOT / "examples" / "android"
BANNED = ("firebase", "play-services", "com.google.android.gms")


class AndroidFossPushTests(unittest.TestCase):
    def test_gradle_has_no_proprietary_push(self) -> None:
        files = [
            ANDROID / "build.gradle.kts",
            ANDROID / "app" / "build.gradle.kts",
            ANDROID / "settings.gradle.kts",
        ]
        existing = [path for path in files if path.is_file()]
        if not existing:
            self.skipTest("android example pruned")
        for path in existing:
            text = path.read_text(encoding="utf-8").lower()
            for needle in BANNED:
                self.assertNotIn(needle, text, msg=str(path))


if __name__ == "__main__":
    unittest.main()
