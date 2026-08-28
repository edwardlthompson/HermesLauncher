"""beforeMCPExecution FOSS server allowlist."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from mcp_allowlist import check_repo, decide, load_allowed  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class McpAllowlistTests(unittest.TestCase):
    def test_repo(self) -> None:
        self.assertEqual(check_repo(ROOT), [])
        self.assertIn("github", load_allowed(ROOT))

    def test_allow_and_deny(self) -> None:
        self.assertEqual(decide({"server": "github"}, ROOT)["permission"], "allow")
        self.assertEqual(decide({"server": "evil-exfil"}, ROOT)["permission"], "deny")
        self.assertEqual(decide({}, ROOT)["permission"], "allow")

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-mcp-allowlist.sh", text)


if __name__ == "__main__":
    unittest.main()
