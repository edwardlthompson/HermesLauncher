"""shellcheck wrapper skip vs CI require."""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from shellcheck_scripts import check_shellcheck, require_tools, script_files  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class ShellcheckScriptsTests(unittest.TestCase):
    def test_lists_top_level_scripts(self) -> None:
        files = script_files(ROOT)
        self.assertTrue(any(p.name == "verify.sh" for p in files))
        self.assertFalse(any(p.parent.name == "lib" for p in files))

    def test_local_skip_when_missing(self) -> None:
        env = {
            k: v
            for k, v in os.environ.items()
            if k not in {"CI", "GITHUB_ACTIONS", "REQUIRE_SHELLCHECK"}
        }
        with patch.dict(os.environ, env, clear=True):
            self.assertFalse(require_tools())
            self.assertEqual(check_shellcheck(ROOT, which=lambda _n: None), 0)

    def test_ci_requires_and_wired(self) -> None:
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "true"}):
            self.assertTrue(require_tools())
            self.assertEqual(check_shellcheck(ROOT, which=lambda _n: None), 1)
        text = (ROOT / "scripts/validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-shellcheck.sh", text)


if __name__ == "__main__":
    unittest.main()
