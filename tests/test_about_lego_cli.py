"""CLI About lego strip leaves greet/crash slices testable."""

from __future__ import annotations

import shutil
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
ROOT = Path(__file__).resolve().parent.parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from about_lego_cli import TRACKED, backup, restore, strip  # noqa: E402


class AboutLegoCliTests(unittest.TestCase):
    def test_strip_removes_about_and_restore_roundtrips(self) -> None:
        missing = [rel for rel in TRACKED if not (ROOT / rel).is_file()]
        if missing:
            self.skipTest(f"about lego sources pruned: {missing[0]}")
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            for rel in TRACKED:
                src = ROOT / rel
                dest = root / rel
                dest.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(src, dest)
            bak = root / "backup"
            backup(bak, root)
            strip(root)
            self.assertFalse((root / "examples/rust/src/about.rs").exists())
            self.assertFalse((root / "examples/go/about.go").exists())
            self.assertFalse((root / "examples/node/src/about.ts").exists())
            self.assertFalse((root / "examples/python/src/hello/about.py").exists())
            lib = (root / "examples/rust/src/lib.rs").read_text(encoding="utf-8")
            self.assertNotIn("pub mod about", lib)
            self.assertIn("pub mod crash", lib)
            go_main = (root / "examples/go/main.go").read_text(encoding="utf-8")
            self.assertNotIn("AboutSummary", go_main)
            node_app = (root / "examples/node/src/app.ts").read_text(encoding="utf-8")
            self.assertNotIn("/about", node_app)
            py_cli = (root / "examples/python/src/hello/cli.py").read_text(encoding="utf-8")
            self.assertNotIn("about_summary", py_cli)
            restore(bak, root)
            self.assertTrue((root / "examples/rust/src/about.rs").is_file())
            self.assertIn(
                "AboutSummary",
                (root / "examples/go/main.go").read_text(encoding="utf-8"),
            )


if __name__ == "__main__":
    unittest.main()
