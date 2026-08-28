"""Upgrade sim never lists Sacred child files in cherry-pick AREAS."""
from __future__ import annotations

import re
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parent.parent
SCRIPT = ROOT / "scripts" / "simulate-template-upgrade.sh"
SACRED = (
    "AGENTS.md",
    "docs/spec.md",
    "docs/plan.md",
    "docs/INITIALIZATION_PROMPT.md",
)


class UpgradeSimSacredTests(unittest.TestCase):
    def test_areas_skip_sacred_and_assert_marker(self) -> None:
        text = SCRIPT.read_text(encoding="utf-8")
        match = re.search(r"AREAS=\((.*?)\)", text, re.S)
        self.assertIsNotNone(match)
        areas = match.group(1)
        for path in SACRED:
            self.assertNotIn(path, areas)
        self.assertIn("upgrade-sim-sacred-agents-md", text)
        self.assertIn("Sacred AGENTS.md was overwritten", text)


if __name__ == "__main__":
    unittest.main()
