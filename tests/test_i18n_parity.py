"""Web ↔ Android i18n key parity."""

from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
ROOT = Path(__file__).resolve().parent.parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from i18n_parity import check_files, check_repo  # noqa: E402


class I18nParityTests(unittest.TestCase):
    def test_repo_locales_align(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_skips_when_android_pruned(self) -> None:
        from tempfile import TemporaryDirectory

        with TemporaryDirectory() as tmp:
            self.assertEqual(check_repo(Path(tmp)), [])

    def test_reports_missing_android(self) -> None:
        errors = check_files({"about.close": "Close about"}, set(), {"android_only": []})
        self.assertTrue(any("about_close" in e for e in errors))

    def test_alias_and_allowlist(self) -> None:
        errors = check_files(
            {"about.update.install": "Install"},
            {"about_install", "app_name"},
            {"aliases": {"about.update.install": "about_install"}, "android_only": ["app_name"]},
        )
        self.assertEqual(errors, [])


if __name__ == "__main__":
    unittest.main()
