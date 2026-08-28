"""HUMAN leftover rows that scripts can close without a login or device."""
from __future__ import annotations

import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from human_task_automation import attempt_row  # noqa: E402
from human_task_leftovers import (  # noqa: E402
    automate_cii_badge,
    automate_crash_proxy_off,
    automate_dependabot_weekly,
    automate_mcp_copy,
    automate_scorecard_badge,
)

ROOT = Path(__file__).resolve().parent.parent

LEFTOVERS = (
    ("HUMAN", "P2: Scorecard badge; keep `/ship --local` non-blocking on the live score"),
    ("HUMAN", "P2: CII Best Practices checklist (login + public badge)"),
    ("HUMAN", "Optional: install Ollama and point Cursor Models at `http://127.0.0.1:11434/v1`"),
    ("HUMAN", "Crash-proxy GitHub App: DPIA before enable (`docs/CRASH_PROXY.md`)"),
    ("HUMAN", "Optional: copy `.cursor/mcp.foss.example` → `.cursor/mcp.json` and restart Cursor"),
    ("HUMAN", "Optional: reduce Dependabot interval or disable automerge"),
    ("HUMAN", "Watch repo Issues + add CODEOWNERS as collaborator; optional About smoke"),
)


class HumanTaskLeftoversTests(unittest.TestCase):
    def test_rules_match_leftovers(self) -> None:
        for owner, task in LEFTOVERS:
            result = attempt_row(ROOT, owner, task, "leftovers")
            self.assertNotEqual(result.method, "no-match", task)

    def test_scorecard_weekly_and_proxy_pass_here(self) -> None:
        self.assertEqual(automate_scorecard_badge(ROOT, {}).exit_code, 0)
        self.assertEqual(automate_dependabot_weekly(ROOT, {}).exit_code, 0)
        self.assertEqual(automate_crash_proxy_off(ROOT, {}).exit_code, 0)

    def test_cii_stays_human_without_badge(self) -> None:
        result = automate_cii_badge(ROOT, {})
        self.assertEqual(result.exit_code, 1)
        self.assertTrue(result.backlog)

    def test_mcp_copy_in_temp(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            cursor = root / ".cursor"
            cursor.mkdir()
            (cursor / "mcp.foss.example").write_text("{}", encoding="utf-8")
            result = automate_mcp_copy(root, {})
            self.assertEqual(result.exit_code, 0)
            self.assertTrue((cursor / "mcp.json").is_file())


if __name__ == "__main__":
    unittest.main()
