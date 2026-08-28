"""Merge queue stays optional and points at settings.yml."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from merge_queue_docs import check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class MergeQueueDocsTests(unittest.TestCase):
    def test_doc(self) -> None:
        self.assertEqual(check_repo(ROOT), [])


if __name__ == "__main__":
    unittest.main()
