"""Glossary jargon in onboarding docs must link to GLOSSARY.md."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from glossary_links import check_files, glossary_terms, unlinked_jargon  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class GlossaryLinksTests(unittest.TestCase):
    def test_parse_and_unlinked(self) -> None:
        terms = glossary_terms("| **Sacred** | law |\n| **Bootstrap vs Reference** | modes |\n")
        self.assertIn("Sacred", terms)
        self.assertIn("Bootstrap", terms)
        self.assertIn("Reference", terms)
        self.assertEqual(unlinked_jargon("See **Sacred** here.", terms), ["Sacred"])
        self.assertEqual(
            unlinked_jargon("See [**Sacred**](help/GLOSSARY.md) here.", terms),
            [],
        )

    def test_repo_onboarding_passes(self) -> None:
        self.assertEqual(check_files(ROOT), [])

    def test_fixture_reports_missing(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            help_dir = root / "docs" / "help"
            help_dir.mkdir(parents=True)
            (help_dir / "GLOSSARY.md").write_text(
                "| **Sacred** | x |\n| **Canon** | x |\n| **AGENT** | x |\n"
                "| **HUMAN** | x |\n| **ADB** | x |\n| **AUTO** | x |\n",
                encoding="utf-8",
            )
            (root / "docs" / "START_HERE.md").write_text("# hi\n", encoding="utf-8")
            errors = check_files(root)
            self.assertTrue(any("START_HERE" in e for e in errors))


if __name__ == "__main__":
    unittest.main()
