"""Stale parallel-scope-lock GC."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from datetime import datetime, timedelta, timezone
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from parallel_lock_gc import gc_parallel_lock  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class ParallelLockGcTests(unittest.TestCase):
    def test_deletes_stale_and_empty(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cursor = root / ".cursor"
            cursor.mkdir()
            lock = cursor / "parallel-scope-lock.json"
            lock.write_text("not-json", encoding="utf-8")
            self.assertEqual(gc_parallel_lock(root), "deleted-invalid")
            lock.write_text('{"created_at":"2026-01-01T00:00:00Z","agents":[]}', encoding="utf-8")
            self.assertEqual(gc_parallel_lock(root), "deleted-empty")
            old = (datetime.now(timezone.utc) - timedelta(hours=48)).isoformat()
            lock.write_text(
                json.dumps({"created_at": old, "agents": [{"id": "a", "scope": "examples/web"}]}),
                encoding="utf-8",
            )
            self.assertEqual(gc_parallel_lock(root, dirty_paths=[]), "deleted-stale")
            lock.write_text(
                json.dumps({"created_at": old, "agents": [{"id": "a", "scope": "examples/web"}]}),
                encoding="utf-8",
            )
            self.assertEqual(
                gc_parallel_lock(root, dirty_paths=["examples/web/src/app.ts"]),
                "kept-active",
            )
            fresh = datetime.now(timezone.utc).isoformat()
            lock.write_text(
                json.dumps({"created_at": fresh, "agents": [{"id": "a"}]}),
                encoding="utf-8",
            )
            self.assertEqual(gc_parallel_lock(root), "kept")

    def test_scope_and_cleanup_mention_gc(self) -> None:
        scope = (ROOT / ".cursor/commands/scope.md").read_text(encoding="utf-8")
        cleanup = (ROOT / ".cursor/commands/cleanup.md").read_text(encoding="utf-8")
        self.assertIn("gc-parallel-lock", scope)
        self.assertIn("gc-parallel-lock", cleanup)


if __name__ == "__main__":
    unittest.main()
