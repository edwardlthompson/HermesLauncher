"""Managed BUILD_PLAN template-gaps-sync block."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from sync_template_gaps_build_plan import (  # noqa: E402
    BEGIN,
    EMPTY_NOTE,
    END,
    extract_inner,
    render_inner,
    sync_file,
    sync_text,
)

SAMPLE = f"""# Build Plan

<!-- remaining-tally -->
**Remaining:** AGENT 0 · AUTO 0 · HUMAN 0 · ADB 0 · **0 open**
<!-- /remaining-tally -->

### Template gaps (synced)

{BEGIN}
{EMPTY_NOTE}
{END}

### Waiting

1. 🔲 [ADB] Device smoke
"""


class SyncTemplateGapsTests(unittest.TestCase):
    def test_template_repo_empty(self) -> None:
        inner = render_inner({"current": "1.0.0", "latest": "1.1.0", "files": []}, template_repo=True)
        self.assertEqual(inner, EMPTY_NOTE)
        self.assertNotIn("🔲", inner)

    def test_up_to_date(self) -> None:
        inner = render_inner(
            {"current": "1.3.0", "latest": "1.3.0", "files": [], "features": []},
            template_repo=False,
        )
        self.assertEqual(inner, EMPTY_NOTE)

    def test_behind_no_file_rows_banner_only(self) -> None:
        inner = render_inner(
            {
                "current": "1.2.0",
                "latest": "1.3.0",
                "upstream": "org/tpl",
                "files": [],
                "features": [],
            },
            template_repo=False,
        )
        self.assertIn("Parent `1.2.0` → `1.3.0` (org/tpl)", inner)
        self.assertNotIn("🔲", inner)

    def test_behind_classifies_owners(self) -> None:
        report = {
            "current": "1.2.0",
            "latest": "1.3.0",
            "upstream": "edwardlthompson/agent-project-bootstrap",
            "files": [
                {"path": "scripts/foo.sh", "policy": "canon"},
                {"path": "AGENTS.md", "policy": "sacred"},
                {"path": "docs/spec.md", "policy": "mixed"},
            ],
            "features": [
                {"id": "about", "title": "About panel", "spec": "docs/features/about.md"},
            ],
        }
        inner = render_inner(report, template_repo=False)
        self.assertIn("Parent `1.2.0` → `1.3.0` (edwardlthompson/agent-project-bootstrap)", inner)
        self.assertIn("[AGENT] Canon: scripts/foo.sh", inner)
        self.assertIn("[HUMAN] Sacred: AGENTS.md", inner)
        self.assertIn("[AGENT] Mixed: docs/spec.md", inner)
        self.assertIn("[AGENT] Feature gap: [about — About panel](docs/features/about.md)", inner)
        updated = sync_text(SAMPLE, report, template_repo=False)
        self.assertIn("AGENT 3", updated)
        self.assertIn("HUMAN 1", updated)
        self.assertIn("ADB 1", updated)

    def test_file_cap(self) -> None:
        files = [{"path": f"p/{i}.md", "policy": "canon"} for i in range(45)]
        inner = render_inner(
            {"current": "1.0.0", "latest": "1.1.0", "files": files, "features": []},
            template_repo=False,
        )
        self.assertEqual(inner.count("[AGENT] Canon:"), 40)
        self.assertIn("…and 5 more", inner)

    def test_skip_and_offline(self) -> None:
        self.assertIn(
            "skipped",
            render_inner({"skip": ["missing .template-version"]}, template_repo=False).lower(),
        )
        self.assertIn(
            "skipped",
            render_inner(
                {"current": "", "latest": "", "warning": "gh failed", "files": []},
                template_repo=False,
            ).lower(),
        )
        self.assertNotIn("🔲", render_inner({"skip": ["x"]}, template_repo=False))

    def test_idempotent_apply(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "bootstrap.config.json").write_text(
                json.dumps({"project_name": "demo", "purpose": "app", "stack": "web"}),
                encoding="utf-8",
            )
            path = root / "BUILD_PLAN.md"
            path.write_text(SAMPLE, encoding="utf-8")
            report = {"current": "1.0.0", "latest": "1.0.0", "files": [], "features": []}
            self.assertEqual(sync_file(path, report, apply=True, check=False, root=root), 0)
            first = path.read_text(encoding="utf-8")
            self.assertEqual(sync_file(path, report, apply=True, check=False, root=root), 0)
            self.assertEqual(path.read_text(encoding="utf-8"), first)
            self.assertEqual(extract_inner(first), EMPTY_NOTE)

    def test_check_stale(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root / "bootstrap.config.json").write_text(
                json.dumps({"project_name": "demo", "purpose": "app", "stack": "web"}),
                encoding="utf-8",
            )
            path = root / "BUILD_PLAN.md"
            path.write_text(SAMPLE, encoding="utf-8")
            report = {
                "current": "1.0.0",
                "latest": "1.1.0",
                "files": [{"path": "a.sh", "policy": "canon"}],
                "features": [],
            }
            self.assertEqual(sync_file(path, report, apply=False, check=True, root=root), 1)

    def test_markers_preserved(self) -> None:
        report = {
            "current": "1.0.0",
            "latest": "1.1.0",
            "files": [{"path": "x", "policy": "canon"}],
            "features": [],
        }
        updated = sync_text(SAMPLE, report, template_repo=False)
        self.assertIn("### Waiting", updated)
        self.assertIn(BEGIN, updated)
        self.assertIn(END, updated)


if __name__ == "__main__":
    unittest.main()
