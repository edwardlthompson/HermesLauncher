"""markdownlint + yamllint non-blocking wrapper."""
from __future__ import annotations

import os
import sys
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from md_yaml_lint import check_md_yaml, require_tools  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class MdYamlLintTests(unittest.TestCase):
    def test_local_skip_when_missing(self) -> None:
        env = {
            k: v
            for k, v in os.environ.items()
            if k not in {"CI", "GITHUB_ACTIONS", "REQUIRE_MDLINT", "MDLINT_HARD"}
        }
        with patch.dict(os.environ, env, clear=True):
            self.assertFalse(require_tools())
            self.assertEqual(check_md_yaml(ROOT, which=lambda _n: None), 0)

    def test_ci_requires_and_wired(self) -> None:
        with patch.dict(os.environ, {"GITHUB_ACTIONS": "true"}):
            self.assertTrue(require_tools())
            self.assertEqual(check_md_yaml(ROOT, which=lambda _n: None), 1)
        text = (ROOT / "scripts/validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-md-yaml-lint.sh", text)
        self.assertTrue((ROOT / ".markdownlint.yaml").is_file())
        self.assertTrue((ROOT / ".yamllint.yaml").is_file())


if __name__ == "__main__":
    unittest.main()
