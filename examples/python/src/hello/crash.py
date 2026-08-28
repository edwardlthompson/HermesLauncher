"""Crash text sanitizer: redact email, home paths, and token assignments."""

from __future__ import annotations

import re

MAX_STACK_LINES = 200

_EMAIL = re.compile(r"[A-Za-z0-9._%+\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}")
_WIN_HOME = re.compile(r"C:\\Users\\[^\\]+\\", re.I)
_UNIX_HOME = re.compile(r"/(?:home|Users)/[^/\s]+/")
_TOKEN = re.compile(r"(?:api[_-]?key|token)\s*[:=]\s*\S+", re.I)


def sanitize_crash_text(text: str) -> str:
    """Redact PII-like tokens from crash text."""
    out = _EMAIL.sub("<redacted-email>", text)
    out = _WIN_HOME.sub("<redacted-home>", out)
    out = _UNIX_HOME.sub("<redacted-home>/", out)
    out = _TOKEN.sub("<redacted-secret>", out)
    return "\n".join(out.splitlines()[:MAX_STACK_LINES])


def sanitize_crash_payload(raw: dict[str, str]) -> dict[str, str]:
    """Sanitize message/stack fields for the shared crash-report schema."""
    return {
        "message": sanitize_crash_text(raw.get("message", "")),
        "stack": sanitize_crash_text(raw.get("stack", "")),
    }
