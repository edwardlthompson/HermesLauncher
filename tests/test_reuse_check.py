"""REUSE.toml + skip-if-missing reuse lint."""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from reuse_check import check_reuse, require_tools  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class ReuseCheckTests(unittest.TestCase):
    def test_files_exist(self) -> None:
        self.assertTrue((ROOT / "REUSE.toml").is_file())
        self.assertTrue((ROOT / "LICENSES" / "MIT.txt").is_file())
        self.assertTrue((ROOT / "LICENSES" / "Apache-2.0.txt").is_file())

    def test_local_skip_and_ci_require(self) -> None:
        env = {
            k: v
            for k, v in os.environ.items()
            if k not in {"CI", "GITHUB_ACTIONS", "REQUIRE_REUSE"}
        }
        with patch.dict(os.environ, env, clear=True):
            self.assertFalse(require_tools())
            self.assertEqual(check_reuse(ROOT, which=lambda _n: None), 0)
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "true"}):
            self.assertTrue(require_tools())
            self.assertEqual(check_reuse(ROOT, which=lambda _n: None), 1)
        text = (ROOT / "scripts/validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-reuse.sh", text)


if __name__ == "__main__":
    unittest.main()
