"""Release Please changelog types and RP workflow hygiene."""
from __future__ import annotations

import json
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


class ReleasePleaseHygieneTests(unittest.TestCase):
    def test_docs_and_chore_do_not_bump(self) -> None:
        cfg = json.loads((ROOT / "release-please-config.json").read_text(encoding="utf-8"))
        types = {item["type"] for item in cfg["changelog-sections"]}
        self.assertNotIn("docs", types)
        self.assertNotIn("chore", types)
        self.assertIn("feat", types)
        self.assertIn("fix", types)

    def test_release_please_runs_dependency_review(self) -> None:
        text = (ROOT / ".github/workflows/release-please.yml").read_text(encoding="utf-8")
        self.assertIn("actions/dependency-review-action@v5", text)
        self.assertIn("checks: write", text)
        self.assertIn("name='Dependency Review'", text)

    def test_session_state_json_gitignored(self) -> None:
        text = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn(".cursor-session-state.json", text)
        hygiene = (ROOT / "scripts/check-repo-hygiene.sh").read_text(encoding="utf-8")
        self.assertIn(".cursor-session-state.json", hygiene)


if __name__ == "__main__":
    unittest.main()
