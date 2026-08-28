"""Tests for hello.crash sanitizer."""

from hello.crash import sanitize_crash_payload, sanitize_crash_text


def test_redacts_email_home_and_token() -> None:
    got = sanitize_crash_text(r"user@example.com C:\Users\ada\secret token=abc /home/ada/.env")
    assert "user@example.com" not in got
    assert r"Users\ada" not in got
    assert "<redacted-email>" in got
    assert "<redacted-home>" in got
    assert "<redacted-secret>" in got


def test_payload_keeps_schema_keys() -> None:
    got = sanitize_crash_payload(
        {"message": "boom user@example.com", "stack": r"at C:\Users\ada\x.py"}
    )
    assert set(got) == {"message", "stack"}
    assert "<redacted-email>" in got["message"]
    assert "<redacted-home>" in got["stack"]
