"""resolve-tools.sh prepends go and cargo dirs like gh/python."""
from __future__ import annotations

from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parent.parent
SCRIPT = ROOT / "scripts" / "lib" / "resolve-tools.sh"


class ResolveToolsTests(unittest.TestCase):
    def test_mentions_go_and_cargo(self) -> None:
        text = SCRIPT.read_text(encoding="utf-8")
        self.assertIn("/c/Program Files/Go/bin", text)
        self.assertIn("$HOME/.cargo/bin", text)
        self.assertIn("$HOME/go/bin", text)
        self.assertIn("Programs/Go/bin", text)


if __name__ == "__main__":
    unittest.main()
