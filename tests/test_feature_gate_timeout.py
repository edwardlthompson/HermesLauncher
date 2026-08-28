"""Per-stack feature-gate timeouts."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from feature_gate_timeout import stack_for_stage, timeout_seconds  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class FeatureGateTimeoutTests(unittest.TestCase):
    def test_stage_maps_and_overrides(self) -> None:
        self.assertEqual(stack_for_stage("android-test"), "android")
        self.assertEqual(stack_for_stage("web-lint"), "web")
        self.assertEqual(stack_for_stage("hygiene"), "docs")
        self.assertEqual(timeout_seconds("android", {}), 600)
        self.assertEqual(timeout_seconds("web", {"FEATURE_GATE_TIMEOUT": "10"}), 10)
        self.assertEqual(
            timeout_seconds("python", {"FEATURE_GATE_TIMEOUT_PYTHON": "33"}), 33
        )

    def test_feature_gate_wraps_timeout(self) -> None:
        text = (ROOT / "scripts" / "feature-gate.sh").read_text(encoding="utf-8")
        self.assertIn("feature_gate_timeout.py", text)
        self.assertIn("timeout", text)


if __name__ == "__main__":
    unittest.main()
