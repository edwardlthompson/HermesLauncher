"""Stale .cursor/worktrees GC planner."""
from __future__ import annotations

import sys
import unittest
from datetime import datetime, timedelta, timezone
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from worktree_gc import parse_worktree_paths, plan_actions  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class WorktreeGcTests(unittest.TestCase):
    def test_parse_and_plan(self) -> None:
        porcelain = "worktree /repo/.cursor/worktrees/a\nHEAD abc\n\nworktree /repo\n"
        parsed = parse_worktree_paths(porcelain)
        self.assertTrue(any(p.name == "a" for p in parsed))
        now = datetime(2026, 8, 27, tzinfo=timezone.utc)
        stale = now - timedelta(hours=48)
        fresh = now - timedelta(hours=1)
        a = Path("/repo/.cursor/worktrees/a")
        b = Path("/repo/.cursor/worktrees/b")
        c = Path("/repo/.cursor/worktrees/c")
        planned = {
            path: action
            for action, path in plan_actions(
                children=[(a, stale), (b, stale), (c, fresh)],
                registered={a.resolve()},
                protected={b.resolve()},
                cwd=Path("/repo"),
                now=now,
            )
        }
        self.assertEqual(planned[a.resolve()], "remove-stale")
        self.assertEqual(planned[b.resolve()], "keep")
        self.assertEqual(planned[c.resolve()], "keep")

    def test_commands_mention_gc(self) -> None:
        cleanup = (ROOT / ".cursor/commands/cleanup.md").read_text(encoding="utf-8")
        best = (ROOT / ".cursor/commands/best-of-n.md").read_text(encoding="utf-8")
        self.assertIn("gc-worktrees", cleanup)
        self.assertIn("gc-worktrees", best)


if __name__ == "__main__":
    unittest.main()
