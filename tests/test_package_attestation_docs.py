"""npm/uv attestation docs stay next to release provenance."""
from __future__ import annotations

import sys
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from package_attestation_docs import check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class PackageAttestationDocsTests(unittest.TestCase):
    def test_repo_doc_complete(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_security_docs_link(self) -> None:
        triage = (ROOT / "docs" / "SECURITY_TRIAGE.md").read_text(encoding="utf-8")
        security = (ROOT / "SECURITY.md").read_text(encoding="utf-8")
        self.assertIn("PACKAGE_ATTESTATION.md", triage)
        self.assertIn("PACKAGE_ATTESTATION.md", security)


if __name__ == "__main__":
    unittest.main()
