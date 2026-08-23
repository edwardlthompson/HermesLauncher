"""Oracle tests: JWT, PEM, AWS, home paths, and tokens must disappear."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parents[2] / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from privacy_report_fingerprint import fingerprint_crash  # noqa: E402
from privacy_report_markdown import build_report_markdown  # noqa: E402
from privacy_report_sanitize import sanitize_report_text  # noqa: E402

JWT = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxIn0.signaturepart"
PEM = "-----BEGIN RSA PRIVATE KEY-----\nMIIEowIBAAKCAQEA\n-----END RSA PRIVATE KEY-----"
STACK = (
    f"TypeError: boom\n    at C:\\Users\\Ada\\secret.env:1\n"
    f"token=ghp_abcdefghijklmnopqrstuvwxyz012345\n"
    f"{JWT}\nAKIAIOSFODNN7EXAMPLE\n"
)


class SanitizeTests(unittest.TestCase):
    def test_null_becomes_empty(self) -> None:
        self.assertEqual(sanitize_report_text(None), "")

    def test_redacts_secrets_and_home(self) -> None:
        out = sanitize_report_text(STACK, stack=True)
        self.assertNotIn("Ada", out)
        self.assertNotIn("ghp_", out)
        self.assertNotIn("eyJ", out)
        self.assertNotIn("AKIA", out)
        self.assertIn("<redacted-secret>", out)
        self.assertIn("<redacted-home>", out)

    def test_email_ip_unc_url(self) -> None:
        raw = "mail a@b.co ip 10.0.0.1 unc \\\\filer\\share\\x url https://x.test/?token=abc"
        out = sanitize_report_text(raw)
        self.assertNotIn("a@b.co", out)
        self.assertNotIn("10.0.0.1", out)
        self.assertNotIn("filer", out)
        self.assertNotIn("token=abc", out)

    def test_fingerprint_stable_across_usernames(self) -> None:
        a = fingerprint_crash("Error\n    at C:\\Users\\Ada\\app\\main.ts:1")
        b = fingerprint_crash("Error\n    at C:\\Users\\Bob\\app\\main.ts:1")
        self.assertEqual(a, b)
        self.assertEqual(len(a), 12)

    def test_markdown_has_no_token(self) -> None:
        md = build_report_markdown("crash", "user ghp_abcdefghijklmnopqrstuvwxyz012345 leaked")
        self.assertNotIn("ghp_", md)
        self.assertIn("crash", md)


if __name__ == "__main__":
    unittest.main()
