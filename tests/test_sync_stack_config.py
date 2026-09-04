"""Tests for scripts/sync-stack-config.py donation host parsing."""
from __future__ import annotations

import importlib.util
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MODULE = ROOT / "scripts" / "sync-stack-config.py"
spec = importlib.util.spec_from_file_location("sync_stack_config", MODULE)
mod = importlib.util.module_from_spec(spec)
assert spec and spec.loader
sys.modules["sync_stack_config"] = mod
spec.loader.exec_module(mod)
donation_label = mod.donation_label


def test_venmo_host_label() -> None:
    assert donation_label("https://venmo.com/u/example") == "Donate via Venmo"
    assert donation_label("https://www.venmo.com/u/example") == "Donate via Venmo"


def test_other_host_label() -> None:
    assert donation_label("https://liberapay.com/example") == "Donate"
    assert donation_label("https://evilvenmo.com/phish") == "Donate"
