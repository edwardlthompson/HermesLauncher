"""PSScriptAnalyzer wrapper skip vs CI require."""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from psscriptanalyzer_check import check_psscriptanalyzer, require_tools  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class PssaTests(unittest.TestCase):
    def test_local_skip_without_shell(self) -> None:
        env = {
            k: v
            for k, v in os.environ.items()
            if k not in {"CI", "GITHUB_ACTIONS", "REQUIRE_PSSA"}
        }
        with patch.dict(os.environ, env, clear=True):
            self.assertFalse(require_tools())
            self.assertEqual(check_psscriptanalyzer(ROOT, shell=""), 0)

    def test_ci_requires_and_wired(self) -> None:
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "true"}):
            self.assertTrue(require_tools())
            self.assertEqual(check_psscriptanalyzer(ROOT, shell=""), 1)
        text = (ROOT / "scripts/validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-psscriptanalyzer.sh", text)
        ci = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
        self.assertIn("PSScriptAnalyzer", ci)


if __name__ == "__main__":
    unittest.main()
