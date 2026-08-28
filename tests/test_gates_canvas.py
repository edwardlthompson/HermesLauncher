"""Gates status markdown writer."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from gates_canvas import fix_banner, load_gate_rows, markdown_report, write_status  # noqa: E402


class GatesCanvasTests(unittest.TestCase):
    def test_markdown_includes_stack(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cursor = root / ".cursor"
            cursor.mkdir()
            (cursor / "stack-selection.json").write_text(
                '{"stack":"web","distribution_tier":"foss"}', encoding="utf-8"
            )
            (root / "BUILD_PLAN.md").write_text("1. 🔲 [HUMAN] smoke\n", encoding="utf-8")
            md = markdown_report(root, [("encoding", "Pass")])
            self.assertIn("`web`", md)
            self.assertIn("encoding", md)
            dest = write_status(root, [("encoding", "Pass")])
            self.assertTrue(dest.is_file())

    def test_load_gate_rows_from_json(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cursor = root / ".cursor"
            cursor.mkdir()
            (cursor / "last-feature-gate.json").write_text(
                '{"ok":false,"gates_passed":["encoding"],"failed_stage":"web-lint"}',
                encoding="utf-8",
            )
            rows = load_gate_rows(root)
            self.assertEqual(rows, [("encoding", "Pass"), ("web-lint", "Fail")])

    def test_fix_banner_prints_strikes_and_stage(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cursor = root / ".cursor"
            cursor.mkdir()
            (cursor / "agent-progress.json").write_text('{"strikes": 2}', encoding="utf-8")
            (cursor / "last-feature-gate.json").write_text(
                '{"failed_stage":"web-lint"}', encoding="utf-8"
            )
            text = fix_banner(root)
            self.assertIn("strikes=2", text)
            self.assertIn("failed_stage=web-lint", text)


if __name__ == "__main__":
    unittest.main()
