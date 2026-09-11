"""Resume handoff digest for Cloud → PC."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from resume_handoff import cursor_prs, format_digest, resume  # noqa: E402
from sync_open_prs_build_plan import BEGIN, EMPTY_NOTE, END  # noqa: E402

PLAN = f"""# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 1 · AUTO 0 · HUMAN 0 · ADB 0 · **1 open**
<!-- /remaining-tally -->

### Open PRs (synced)

{BEGIN}
{EMPTY_NOTE}
{END}

1. 🔲 [AGENT] Do the next thing
"""


class ResumeHandoffTests(unittest.TestCase):
    def test_cursor_prs_filters(self) -> None:
        prs = [
            {"number": 1, "headRefName": "cursor/foo", "title": "feat", "author": {"login": "x"}, "labels": []},
            {
                "number": 2,
                "headRefName": "dependabot/x",
                "title": "chore(deps): x",
                "author": {"login": "dependabot[bot]"},
                "labels": [],
            },
            {"number": 3, "headRefName": "feature/bar", "title": "feat", "author": {"login": "x"}, "labels": []},
        ]
        cloud = cursor_prs(prs)
        self.assertEqual([p["number"] for p in cloud], [1])

    def test_digest_includes_next_agent(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "BUILD_PLAN.md").write_text(PLAN, encoding="utf-8")
            (root / "CHANGELOG.md").write_text("## [Unreleased]\n\n- note\n", encoding="utf-8")
            digest = format_digest(
                root,
                sync_inner="- 🔲 [AUTO] Merge Dependabot [#9](https://example.com/9) (x)",
                synced_prs=[
                    {
                        "number": 9,
                        "title": "chore(deps): x",
                        "url": "https://example.com/9",
                        "author": {"login": "dependabot[bot]"},
                        "labels": [],
                        "headRefName": "dependabot/x",
                    }
                ],
                cloud_prs=[
                    {
                        "number": 8,
                        "title": "feat: cloud",
                        "url": "https://example.com/8",
                        "headRefName": "cursor/cloud",
                    }
                ],
                gh_error=None,
                fetch_note="ok",
                branch_notes=[],
                ci_line="CI red: failed required checks: CI",
            )
            self.assertIn("Next BUILD_PLAN row:", digest)
            self.assertIn("[AGENT] Do the next thing", digest)
            self.assertIn("CHANGELOG [Unreleased] has entries: yes", digest)
            self.assertIn("CI red: failed required checks: CI", digest)
            self.assertIn("Handoff: dirty Unreleased", digest)
            self.assertIn("#8", digest)
            self.assertIn("cursor/cloud", digest)

    def test_resume_with_mocks(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "BUILD_PLAN.md").write_text(PLAN, encoding="utf-8")
            (root / "CHANGELOG.md").write_text("## [Unreleased]\n\n", encoding="utf-8")
            prs = [
                {
                    "number": 8,
                    "title": "feat: cloud",
                    "url": "https://example.com/8",
                    "headRefName": "cursor/cloud",
                    "author": {"login": "bot"},
                    "labels": [],
                }
            ]
            with (
                mock.patch("resume_handoff.git_fetch", return_value="ok"),
                mock.patch("resume_handoff.branch_status", return_value=[]),
                mock.patch("resume_handoff.fetch_open_prs", return_value=prs),
            ):
                code, digest = resume(root, apply_sync=True)
            self.assertEqual(code, 0)
            self.assertIn("cursor/cloud", digest)
            self.assertIn("[AGENT] Do the next thing", digest)


if __name__ == "__main__":
    unittest.main()
