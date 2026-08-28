"""Print cheat sheet stays in sync with the batch command registry."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
ROOT = Path(__file__).resolve().parent.parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from batch_commands_print import check, load_commands, render  # noqa: E402


class BatchCommandsPrintTests(unittest.TestCase):
    def test_committed_html_matches_render(self) -> None:
        check_script = (ROOT / "scripts/check-batch-commands.sh").read_text(encoding="utf-8")
        self.assertIn("batch_commands_print.py", check_script)
        cmds = load_commands(ROOT)
        html = render(cmds)
        for name in cmds:
            self.assertIn(f"/{name}", html)
        self.assertIn("/push", html)
        self.assertIn("/ship", html)
        self.assertNotIn("atomic", html.lower())
        errors = check(ROOT, set(cmds))
        self.assertEqual(errors, [])


if __name__ == "__main__":
    unittest.main()
