"""Crash payload allowlist tests on every sanitizing stack."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from crash_payload_allowlist import check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class CrashPayloadAllowlistTests(unittest.TestCase):
    def test_repo(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_skips_pruned_stack_dir(self) -> None:
        from tempfile import TemporaryDirectory

        with TemporaryDirectory() as tmp:
            root = Path(tmp)
            schema = ROOT / "schemas" / "golden-path"
            dest = root / "schemas" / "golden-path"
            dest.mkdir(parents=True)
            dest.joinpath("crash-payload-allowlist.json").write_text(
                (schema / "crash-payload-allowlist.json").read_text(encoding="utf-8"),
                encoding="utf-8",
            )
            dest.joinpath("crash-report.schema.json").write_text(
                (schema / "crash-report.schema.json").read_text(encoding="utf-8"),
                encoding="utf-8",
            )
            (root / "examples" / "web").mkdir(parents=True)
            errors = check_repo(root)
            self.assertFalse(any("android" in e or "python" in e for e in errors))

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-crash-payload-allowlist.sh", text)


if __name__ == "__main__":
    unittest.main()
