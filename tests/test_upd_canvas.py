"""Update-deps dry-run canvas."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from upd_canvas import default_rows, markdown_report, parse_upd_lines, write_status  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class UpdCanvasTests(unittest.TestCase):
    def test_parse_and_write(self) -> None:
        rows = parse_upd_lines("ruff 0.1.0 -> 0.1.1\n# skip\nGradle: Dependabot backup\n")
        self.assertEqual(rows[0][0], "ruff")
        self.assertEqual(rows[1][0], "gradle")
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / ".cursor").mkdir()
            dest = write_status(root, rows)
            text = dest.read_text(encoding="utf-8")
            self.assertIn("ruff", text)
            self.assertIn("Update-deps dry-run", markdown_report(root, rows))

    def test_default_rows_include_ecosystems(self) -> None:
        rows = default_rows(ROOT)
        names = [name for name, _ in rows]
        self.assertIn("ecosystems", names)

    def test_update_deps_renders_canvas(self) -> None:
        cmd = (ROOT / ".cursor" / "commands" / "update-deps.md").read_text(encoding="utf-8")
        self.assertIn("render-upd-status", cmd)
        ignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn(".cursor/upd-status.md", ignore)


if __name__ == "__main__":
    unittest.main()
