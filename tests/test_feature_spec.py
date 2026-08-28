"""Feature spec heading contract."""

from __future__ import annotations

import json
import sys
import tempfile
import unittest
from pathlib import Path

LIB = Path(__file__).resolve().parent.parent / "scripts" / "lib"
ROOT = Path(__file__).resolve().parent.parent
if str(LIB) not in sys.path:
    sys.path.insert(0, str(LIB))

from feature_spec import check_repo, check_text  # noqa: E402


class FeatureSpecTests(unittest.TestCase):
    def test_template_and_features_pass(self) -> None:
        self.assertEqual(check_repo(ROOT), [])

    def test_contract_matches_schema_required_keys(self) -> None:
        schema = json.loads(
            (ROOT / "schemas/features/feature-spec.schema.json").read_text(encoding="utf-8")
        )
        contract = json.loads(
            (ROOT / "schemas/features/feature-spec.contract.json").read_text(encoding="utf-8")
        )
        for key in schema["required"]:
            self.assertIn(key, contract)
        self.assertIn("Tests", contract["required_h2"])
        self.assertIn("Fallback validation", contract["required_h2"])

    def test_automated_no_rejects_na_why(self) -> None:
        contract = json.loads(
            (ROOT / "schemas/features/feature-spec.contract.json").read_text(encoding="utf-8")
        )
        text = (
            "## Acceptance criteria\n## Smoke scenario\n## Container map\n"
            "## Tests\n- Automated: no\n"
            "## Fallback validation\n- Why tests are not feasible: N/A\n"
            "- Command: `bash scripts/feature-gate.sh`\n"
        )
        errors = check_text(text, "x.md", contract)
        self.assertTrue(any("why tests are not feasible" in e for e in errors))

    def test_skip_handoff(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            dest = root / "docs" / "features"
            dest.mkdir(parents=True)
            schema_dir = root / "schemas" / "features"
            schema_dir.mkdir(parents=True)
            (schema_dir / "feature-spec.contract.json").write_text(
                (ROOT / "schemas/features/feature-spec.contract.json").read_text(encoding="utf-8"),
                encoding="utf-8",
            )
            (dest / "_handoff.md").write_text("# skip\n", encoding="utf-8")
            (dest / "ok.md").write_text(
                "## Acceptance criteria\n## Smoke scenario\n## Container map\n"
                "## Tests\n- Automated: yes — unit\n"
                "## Fallback validation\n- Why tests are not feasible: N/A (automated tests exist)\n"
                "- Command: `python3 scripts/agent-run.py feature-gate --stack web`\n",
                encoding="utf-8",
            )
            self.assertEqual(check_repo(root), [])


if __name__ == "__main__":
    unittest.main()
