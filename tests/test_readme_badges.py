"""README badge accuracy gate."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from readme_badges import check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class ReadmeBadgesTests(unittest.TestCase):
    def test_readme_matches_repo(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_detects_version_drift(self) -> None:
        import tempfile

        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            (root / ".template-version").write_text("9.9.9\n", encoding="utf-8")
            (root / "README.md").write_text("# empty\n", encoding="utf-8")
            errors = check_repo(root)
            self.assertTrue(any("template-9.9.9" in item for item in errors))

    def test_validate_bootstrap_runs_check(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-readme-badges.sh", text)

    def test_sync_rewrites_html_shields_badge(self) -> None:
        import re

        sync = (ROOT / "scripts" / "sync-template-version.sh").read_text(encoding="utf-8")
        self.assertIn("img.shields.io/badge/template-", sync)
        self.assertIn("branding/generated/README.preview.md", sync)
        html = (
            '<img src="https://img.shields.io/badge/template-1.0.0-656d76'
            '?style=flat-square" alt="template-1.0.0" />'
        )
        rewritten = re.sub(
            r"(https://img\.shields\.io/badge/template-)[\d.]+",
            r"\g<1>1.1.0",
            html,
        )
        rewritten = re.sub(
            r'(alt="template-)[\d.]+(")',
            r"\g<1>1.1.0\2",
            rewritten,
        )
        self.assertIn("template-1.1.0", rewritten)
        self.assertNotIn("template-1.0.0", rewritten)


if __name__ == "__main__":
    unittest.main()
