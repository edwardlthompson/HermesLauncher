"""Managed BUILD_PLAN open-prs-sync block."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from sync_open_prs_build_plan import (  # noqa: E402
    BEGIN,
    EMPTY_NOTE,
    END,
    classify_pr,
    extract_inner,
    render_inner,
    sync_file,
    sync_text,
)

SAMPLE = f"""# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · AUTO 0 · HUMAN 0 · ADB 0 · **0 open**
<!-- /remaining-tally -->

### Open PRs (synced)

{BEGIN}
{EMPTY_NOTE}
{END}

### Waiting

1. 🔲 [ADB] Device smoke
"""


class SyncOpenPrsTests(unittest.TestCase):
    def test_classify(self) -> None:
        self.assertEqual(
            classify_pr({"author": {"login": "dependabot[bot]"}, "title": "chore(deps): bump"}),
            "dependabot",
        )
        self.assertEqual(
            classify_pr({"labels": [{"name": "dependencies"}], "author": {"login": "x"}}),
            "dependabot",
        )
        self.assertEqual(
            classify_pr({"headRefName": "release-please--branches--main", "title": "chore(main): release 1.2.0"}),
            "release",
        )
        self.assertEqual(
            classify_pr({"title": "chore(main): release 1.2.0", "headRefName": "other"}),
            "release",
        )
        self.assertIsNone(classify_pr({"title": "feat: cloud", "headRefName": "cursor/foo", "author": {"login": "bot"}}))

    def test_render_and_tally(self) -> None:
        prs = [
            {
                "number": 102,
                "title": "chore(main): release 1.2.0",
                "url": "https://example.com/102",
                "headRefName": "release-please--branches--main",
                "author": {"login": "github-actions[bot]"},
                "labels": [],
            },
            {
                "number": 103,
                "title": "chore(deps): bump actions",
                "url": "https://example.com/103",
                "headRefName": "dependabot/github_actions",
                "author": {"login": "dependabot[bot]"},
                "labels": [{"name": "dependencies"}],
            },
        ]
        inner = render_inner(prs)
        self.assertIn("[AUTO] Merge Dependabot [#103]", inner)
        self.assertIn("[AGENT] Merge release [#102]", inner)
        self.assertLess(inner.index("[AUTO]"), inner.index("[AGENT]"))
        updated = sync_text(SAMPLE, prs)
        self.assertIn("AUTO 1", updated)
        self.assertIn("AGENT 1", updated)
        self.assertIn("**3 open**", updated)  # + ADB
        self.assertEqual(extract_inner(updated), inner)

    def test_empty_no_checkbox(self) -> None:
        self.assertEqual(render_inner([]), EMPTY_NOTE)
        self.assertNotIn("🔲", EMPTY_NOTE)
        updated = sync_text(SAMPLE, [])
        self.assertIn(EMPTY_NOTE, updated)
        self.assertIn("ADB 1", updated)
        self.assertIn("**1 open**", updated)

    def test_idempotent_apply(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            path = root / "BUILD_PLAN.md"
            path.write_text(SAMPLE, encoding="utf-8")
            fixture = root / "prs.json"
            fixture.write_text("[]", encoding="utf-8")
            self.assertEqual(sync_file(path, [], apply=True, check=False, root=root), 0)
            first = path.read_text(encoding="utf-8")
            self.assertEqual(sync_file(path, [], apply=True, check=False, root=root), 0)
            self.assertEqual(path.read_text(encoding="utf-8"), first)

    def test_markers_only(self) -> None:
        prs = [
            {
                "number": 1,
                "title": "chore(deps): x",
                "url": "https://example.com/1",
                "author": {"login": "dependabot[bot]"},
                "labels": [],
                "headRefName": "dependabot/x",
            }
        ]
        updated = sync_text(SAMPLE, prs)
        self.assertIn("### Waiting", updated)
        self.assertIn("1. 🔲 [ADB] Device smoke", updated)
        self.assertIn(BEGIN, updated)
        self.assertIn(END, updated)
        self.assertNotIn(EMPTY_NOTE, updated)

    def test_check_stale(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            path = root / "BUILD_PLAN.md"
            path.write_text(SAMPLE, encoding="utf-8")
            prs = [
                {
                    "number": 9,
                    "title": "chore(deps): y",
                    "url": "https://example.com/9",
                    "author": {"login": "dependabot[bot]"},
                    "labels": [],
                    "headRefName": "dependabot/y",
                }
            ]
            self.assertEqual(sync_file(path, prs, apply=False, check=True, root=root), 1)


if __name__ == "__main__":
    unittest.main()
