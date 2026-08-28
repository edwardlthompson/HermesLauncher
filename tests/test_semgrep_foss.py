"""FOSS Semgrep config (no SaaS token)."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from semgrep_foss import check_config  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class SemgrepFossTests(unittest.TestCase):
    def test_config(self) -> None:
        self.assertEqual(check_config(ROOT), [])

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-semgrep.sh", text)
        security = (ROOT / ".github" / "workflows" / "security.yml").read_text(encoding="utf-8")
        self.assertIn("semgrep --config .semgrep.yml", security)
        self.assertNotIn("python3 -m semgrep", security)


if __name__ == "__main__":
    unittest.main()
