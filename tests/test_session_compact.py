"""compact-session-state merges Unreleased + HUMAN rows."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from session_compact import merge_compact, open_human_adb_rows, unreleased_excerpt  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class SessionCompactTests(unittest.TestCase):
    def test_excerpt_and_human_rows(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "CHANGELOG.md").write_text(
                "## [Unreleased]\n\n* one\n* two\n\n## [0.1.0]\n",
                encoding="utf-8",
            )
            (root / "BUILD_PLAN.md").write_text(
                "1. 🔲 [HUMAN] Scorecard\n2. ✅ [AGENT] done\n3. 🔲 [ADB] emulator\n",
                encoding="utf-8",
            )
            self.assertEqual(unreleased_excerpt(root), ["* one", "* two"])
            human = open_human_adb_rows(root)
            self.assertEqual(len(human), 2)
            data = merge_compact(root)
            self.assertTrue(data["unreleased_has_entries"])
            state = json.loads((root / ".cursor-session-state.json").read_text(encoding="utf-8"))
            self.assertEqual(state["open_human_adb_rows"], human)

    def test_compact_command_runs_script(self) -> None:
        compact = (ROOT / ".cursor/commands/compact.md").read_text(encoding="utf-8")
        self.assertIn("compact-session-state", compact)
        example = (ROOT / ".cursor-session-state.example.json").read_text(encoding="utf-8")
        self.assertIn("unreleased_excerpt", example)
        self.assertIn("open_human_adb_rows", example)


if __name__ == "__main__":
    unittest.main()
