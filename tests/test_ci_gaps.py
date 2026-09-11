"""Living ci-gap registry stays aligned with the issue form."""
from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from ci_gaps import REQUIRED_IDS, check_repo  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent


class CiGapsTests(unittest.TestCase):
    def test_repo(self) -> None:
        self.assertEqual(check_repo(ROOT), [])
        data = json.loads((ROOT / "schemas/ci-gaps.json").read_text(encoding="utf-8"))
        ids = {row["id"] for row in data["gaps"]}
        self.assertTrue(set(REQUIRED_IDS).issubset(ids))

    def test_wired(self) -> None:
        text = (ROOT / "scripts" / "validate-bootstrap.sh").read_text(encoding="utf-8")
        self.assertIn("check-ci-gaps.sh", text)

    def test_rejects_missing_required_id(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            schema = root / "schemas"
            schema.mkdir()
            schema.joinpath("ci-gaps.json").write_text(
                json.dumps({"issue_label": "ci-gap", "gaps": []}), encoding="utf-8"
            )
            dest = root / ".github" / "ISSUE_TEMPLATE"
            dest.mkdir(parents=True)
            dest.joinpath("template_improvement.yml").write_text(
                "- ci-gap\n", encoding="utf-8"
            )
            (root / "docs").mkdir()
            (root / "docs/CI_GAPS.md").write_text(
                "schemas/ci-gaps.json ci-gap\n", encoding="utf-8"
            )
            wf = root / ".github" / "workflows"
            wf.mkdir(parents=True)
            wf.joinpath("ci.yml").write_text(
                "on:\n  pull_request:\n    branches: [main]\nci-ok:\n  needs:\n    - feature-gate\n  steps:\n",
                encoding="utf-8",
            )
            errors = check_repo(root)
            self.assertTrue(any("missing required ids" in e for e in errors))


if __name__ == "__main__":
    unittest.main()
