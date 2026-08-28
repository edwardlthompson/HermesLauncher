""" /ideas must wait for numbered confirmation; /build must gate between features. """

from __future__ import annotations

import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


class IdeasConfirmTests(unittest.TestCase):
    def test_ideas_refuses_silent_do_all(self) -> None:
        cmd = (ROOT / ".cursor/commands/ideas.md").read_text(encoding="utf-8")
        help_twin = (ROOT / "docs/help/IDEAS.md").read_text(encoding="utf-8")
        for text in (cmd, help_twin):
            self.assertIn("do all", text)
            self.assertIn("wait", text.lower())

    def test_debug_reads_last_feature_gate(self) -> None:
        debug = (ROOT / ".cursor/commands/debug.md").read_text(encoding="utf-8")
        playbook = (ROOT / "docs/FOR_AGENTS.md").read_text(encoding="utf-8")
        seven_b = (ROOT / "docs/INITIALIZATION_PROMPT.md").read_text(encoding="utf-8")
        for text in (debug, playbook, seven_b):
            self.assertIn("last-feature-gate.json", text)
            self.assertIn("strikes", text)
        self.assertIn("halt", debug.lower())
        self.assertIn(">= 3", debug)

    def test_watch_agent_gates_renders_status(self) -> None:
        watch = (ROOT / "scripts/watch-agent-gates.sh").read_text(encoding="utf-8")
        self.assertIn("render-gates-status.sh", watch)
        self.assertIn("last-feature-gate.json", watch)
        build = (ROOT / ".cursor/commands/build.md").read_text(encoding="utf-8")
        self.assertIn("Gate lock", build)
        self.assertIn("watch-agent-gates", build)
        self.assertIn("Never skip 1c", build)
        self.assertIn("--scope auto", build)

    def test_fix_prints_strike_stage_first(self) -> None:
        fix = (ROOT / ".cursor/commands/fix.md").read_text(encoding="utf-8")
        banner_at = fix.find("--fix-banner")
        gate_at = fix.find("watch-agent-gates")
        self.assertNotEqual(banner_at, -1)
        self.assertNotEqual(gate_at, -1)
        self.assertLess(banner_at, gate_at)
        self.assertIn("strikes", fix)
        self.assertIn("failed_stage", fix)

    def test_coach_unreleased_vs_empty_board(self) -> None:
        for rel in (".cursor/commands/coach.md", "docs/help/COACH.md"):
            text = (ROOT / rel).read_text(encoding="utf-8")
            self.assertIn("Unreleased", text)
            self.assertIn("/build", text)
            self.assertIn("/ship", text)
            self.assertIn("/ideas", text)

    def test_portable_help_twins(self) -> None:
        check = (ROOT / "scripts/check-batch-commands.sh").read_text(encoding="utf-8")
        self.assertIn("PORTABLE=", check)
        for name in ("tour", "coach", "ideas", "allideas", "debug", "upgrade"):
            twin = ROOT / "docs" / "help" / f"{name.upper()}.md"
            cmd = ROOT / ".cursor" / "commands" / f"{name}.md"
            self.assertTrue(cmd.is_file(), cmd)
            self.assertTrue(twin.is_file(), twin)
            self.assertIn(f"docs/help/{name.upper()}.md", cmd.read_text(encoding="utf-8"))

    def test_session_start_unreleased_and_next_agent(self) -> None:
        agents = (ROOT / "AGENTS.md").read_text(encoding="utf-8")
        start = (ROOT / "docs/START_HERE.md").read_text(encoding="utf-8")
        for text in (agents, start):
            self.assertIn("[Unreleased]", text)
            self.assertIn("[AGENT]", text)


if __name__ == "__main__":
    unittest.main()
