"""Gradle --offline after first worktree success."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from gradle_offline import extra_args, mark_success, should_offline  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class GradleOfflineTests(unittest.TestCase):
    def test_offline_only_after_stamp(self) -> None:
        with tempfile.TemporaryDirectory() as raw:
            root = Path(raw)
            self.assertFalse(should_offline(root))
            self.assertEqual(extra_args(root), [])
            mark_success(root)
            self.assertTrue(should_offline(root))
            self.assertEqual(extra_args(root), ["--offline"])

    def test_worktree_and_gate_invoke_helper(self) -> None:
        unix = (ROOT / ".cursor" / "setup-worktree-unix.sh").read_text(encoding="utf-8")
        win = (ROOT / ".cursor" / "setup-worktree-windows.ps1").read_text(encoding="utf-8")
        gate = (ROOT / "scripts" / "feature-gate.sh").read_text(encoding="utf-8")
        self.assertIn("gradle_offline.py", unix)
        self.assertIn("--offline", unix)
        self.assertIn("gradle_offline.py", win)
        self.assertIn("gradle_offline.py", gate)


if __name__ == "__main__":
    unittest.main()
