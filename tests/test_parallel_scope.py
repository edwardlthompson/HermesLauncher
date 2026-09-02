"""Archived BUILD_PLAN sprints do not require Parallel tables."""

from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from parallel_scope import check_build_plan_parallel  # noqa: E402

ARCHIVED = """# Build Plan

## Child Repo Playbook

### Sprint 0 — Hermes seed

> **Sprint 0** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

### Sprint 8 — Live home

> **Sprint 8** archived in COMPLETED_TASKS.md @ `c2bb2c4`.

#### Human & device (after automation)

1. 🔲 [ADB] Sideload OP12
"""


class ArchivedSprintSkipTests(unittest.TestCase):
    def test_archived_one_liners_skip_parallel_table(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            path = Path(tmp) / "BUILD_PLAN.md"
            path.write_text(ARCHIVED, encoding="utf-8")
            ok, errors = check_build_plan_parallel(path, root=Path(tmp))
            self.assertTrue(ok, errors)
            self.assertEqual(errors, [])


if __name__ == "__main__":
    unittest.main()
