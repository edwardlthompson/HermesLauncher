"""Discoverable wrapper for privacy-report oracle tests."""
from __future__ import annotations

import sys
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
PKG = Path(__file__).resolve().parent / "privacy_report"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))
if str(PKG) not in sys.path:
    sys.path.insert(0, str(PKG))

from test_sanitize import SanitizeTests  # noqa: E402, F401
