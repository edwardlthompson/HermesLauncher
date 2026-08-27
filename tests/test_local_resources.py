"""Tests for local CPU/RAM caps, CI slot limit, and Ollama probe timeout."""
from __future__ import annotations

import os
import sys
import time
import unittest
from pathlib import Path
from unittest.mock import patch

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from local_resources import (  # noqa: E402
    GIB,
    InvalidJobs,
    cpu_count,
    env_jobs,
    in_ci,
    ollama_up,
    recommended_check_jobs,
    recommended_stack_slots,
    schedule_waves,
    stack_weight,
)


class EnvJobsTests(unittest.TestCase):
    def test_unset_is_none(self) -> None:
        with patch.dict(os.environ, {}, clear=False):
            os.environ.pop("BOOTSTRAP_CHECK_JOBS", None)
            self.assertIsNone(env_jobs("BOOTSTRAP_CHECK_JOBS"))

    def test_zero_invalid(self) -> None:
        with patch.dict(os.environ, {"BOOTSTRAP_CHECK_JOBS": "0"}):
            with self.assertRaises(InvalidJobs):
                env_jobs("BOOTSTRAP_CHECK_JOBS")

    def test_garbage_invalid(self) -> None:
        with patch.dict(os.environ, {"FEATURE_GATE_JOBS": "abc"}):
            with self.assertRaises(InvalidJobs):
                env_jobs("FEATURE_GATE_JOBS")


class SlotTests(unittest.TestCase):
    def test_check_jobs_never_exceed_cpu(self) -> None:
        cpu = cpu_count()
        with patch.dict(os.environ, {}, clear=False):
            os.environ.pop("BOOTSTRAP_CHECK_JOBS", None)
            with patch("local_resources.ram_gb_or_none", return_value=64):
                self.assertLessEqual(recommended_check_jobs(), cpu)

    def test_three_gb_one_stack_slot(self) -> None:
        with patch.dict(os.environ, {}, clear=False):
            os.environ.pop("FEATURE_GATE_JOBS", None)
            os.environ.pop("CI", None)
            os.environ.pop("GITHUB_ACTIONS", None)
            with patch("local_resources.ram_gb_or_none", return_value=3):
                with patch("local_resources.cpu_count", return_value=8):
                    self.assertEqual(recommended_stack_slots(), 1)

    def test_android_weight_fills_two_slots(self) -> None:
        self.assertEqual(stack_weight("android", 2), 2)
        self.assertEqual(stack_weight("web", 2), 1)
        waves = schedule_waves(["web", "python", "android", "node"], 2)
        for wave in waves:
            if "android" in wave:
                self.assertEqual(wave, ["android"])

    def test_ci_caps_at_two(self) -> None:
        env = {"CI": "true"}
        with patch.dict(os.environ, env, clear=False):
            os.environ.pop("FEATURE_GATE_JOBS", None)
            os.environ.pop("GITHUB_ACTIONS", None)
            with patch("local_resources.ram_gb_or_none", return_value=64):
                with patch("local_resources.cpu_count", return_value=16):
                    self.assertTrue(in_ci())
                    self.assertEqual(recommended_stack_slots(), 2)

    def test_feature_gate_jobs_wins(self) -> None:
        with patch.dict(os.environ, {"FEATURE_GATE_JOBS": "1", "CI": "true"}):
            self.assertEqual(recommended_stack_slots(), 1)


class OllamaTests(unittest.TestCase):
    def test_probe_timeout_fast(self) -> None:
        started = time.monotonic()
        with patch("local_resources.urllib.request.build_opener") as opener:
            opener.side_effect = TimeoutError("slow")
            self.assertFalse(ollama_up(timeout=1.0))
        self.assertLess(time.monotonic() - started, 1.2)


class RamLinuxTests(unittest.TestCase):
    def test_memavailable(self) -> None:
        from local_resources import _ram_linux

        text = "MemTotal:       8000000 kB\nMemAvailable:   3145728 kB\n"
        self.assertEqual(_ram_linux(text), 3145728 * 1024)
        self.assertEqual(3145728 * 1024 // GIB, 3)


if __name__ == "__main__":
    unittest.main()
